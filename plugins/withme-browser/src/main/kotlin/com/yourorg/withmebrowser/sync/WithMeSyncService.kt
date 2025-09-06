package com.yourorg.withmebrowser.sync

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Warstwa synchronizacji. Działa w trzech trybach:
 *  - AVAILABLE: jeśli Code With Me API jest obecne, używa kanału współdzielonego (odbiór/nadawanie)
 *  - EMULATED_LOCAL: jeśli brak CWM, nadal działa lokalny EventBus (bez sieci)
 *  - UNAVAILABLE: gdy coś poszło nie tak
 *
 * Implementacja CWM bazuje na refleksji, aby nie mieć twardej zależności.
 * Jako nośnik JSON: minimalny format "type|payload".
 */
@Service
class WithMeSyncService {
  private val log = Logger.getInstance(WithMeSyncService::class.java)
  private val listeners = CopyOnWriteArrayList<(BrowserEvent) -> Unit>()
  @Volatile private var transport: Transport = detectTransport()

  enum class Role { HOST, GUEST, NONE }
  enum class TransportState { AVAILABLE, EMULATED_LOCAL, UNAVAILABLE }
  enum class EventType { NAVIGATE, BACK, FORWARD, RELOAD }

  data class BrowserEvent(val type: EventType, val payload: String)

  fun state(): TransportState = when (transport) {
    is CwmTransport -> TransportState.AVAILABLE
    is LocalTransport -> TransportState.EMULATED_LOCAL
    else -> TransportState.UNAVAILABLE
  }

  fun role(): Role = transport.role()

  fun publish(ev: BrowserEvent) {
    transport.send(ev)
  }

  fun subscribe(l: (BrowserEvent) -> Unit) {
    listeners += l
    transport.setReceiver { evt -> l(evt) }
  }

  private fun detectTransport(): Transport {
    return try {
      CwmTransport(this)
    } catch (t: Throwable) {
      log.info("CWM transport not available, falling back to local bus: ${t.message}")
      LocalTransport(this)
    }
  }

  // wywoływane przez transport po dekodowaniu
  internal fun dispatch(ev: BrowserEvent) {
    listeners.forEach { it(ev) }
  }

  interface Transport {
    fun role(): Role
    fun setReceiver(r: (BrowserEvent) -> Unit)
    fun send(ev: BrowserEvent)
  }

  /**
   * Fallback: lokalny EventBus w wątku puli. Nie udostępnia nic gościom.
   */
  private class LocalTransport(private val svc: WithMeSyncService) : Transport {
    private var receiver: ((BrowserEvent) -> Unit)? = null
    override fun role() = Role.NONE
    override fun setReceiver(r: (BrowserEvent) -> Unit) { receiver = r }
    override fun send(ev: BrowserEvent) {
      AppExecutorUtil.getAppExecutorService().execute { receiver?.invoke(ev) }
    }
  }

  /**
   * CwmTransport – refleksyjny adapter do Code With Me:
   * Założenia:
   *  - Istnieje "com.jetbrains.codeWithMe.api.SessionApi" z metodami:
   *      getCurrentRole(): "HOST"/"GUEST"
   *      sendCustomMessage(String channel, String data)
   *      onCustomMessage(String channel, Consumer<String>)
   *  - Kanał: "withme.embeddedBrowser"
   *
   * Jeśli realne API różni się nazwami – podmień literówki w jednym miejscu
   */
  private class CwmTransport(private val svc: WithMeSyncService) : Transport {
    private val channel = "withme.embeddedBrowser"
    private val api: Any
    private val roleMethod: java.lang.reflect.Method
    private val sendMethod: java.lang.reflect.Method
    private var receiver: ((WithMeSyncService.BrowserEvent) -> Unit)? = null

    init {
      val apiClass = Class.forName("com.jetbrains.codeWithMe.api.SessionApi")
      val getInstance = apiClass.getMethod("getInstance")
      api = getInstance.invoke(null)
      roleMethod = apiClass.getMethod("getCurrentRole") // returns enum or string
      sendMethod = apiClass.getMethod("sendCustomMessage", String::class.java, String::class.java)
      val onMethod = apiClass.getMethod("onCustomMessage", String::class.java, java.util.function.Consumer::class.java)
      // zarejestruj odbiorcę
      onMethod.invoke(api, channel, java.util.function.Consumer<String> { data ->
        parse(data)?.let { svc.dispatch(it) }
      })
    }

    override fun role(): Role {
      val r = roleMethod.invoke(api)?.toString()?.uppercase()
      return when (r) {
        "HOST" -> Role.HOST
        "GUEST" -> Role.GUEST
        else -> Role.NONE
      }
    }

    override fun setReceiver(r: (WithMeSyncService.BrowserEvent) -> Unit) { receiver = r }

    override fun send(ev: WithMeSyncService.BrowserEvent) {
      val payload = "${ev.type.name}|${ev.payload}"
      sendMethod.invoke(api, channel, payload)
    }

    private fun parse(s: String): WithMeSyncService.BrowserEvent? {
      val idx = s.indexOf('|')
      if (idx <= 0) return null
      val t = s.substring(0, idx)
      val p = s.substring(idx + 1)
      val type = runCatching { EventType.valueOf(t) }.getOrNull() ?: return null
      return WithMeSyncService.BrowserEvent(type, p)
    }
  }

  companion object {
    fun getInstance(): WithMeSyncService = service()
  }
}
