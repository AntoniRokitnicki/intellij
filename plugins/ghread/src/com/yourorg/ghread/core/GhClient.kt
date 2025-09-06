package com.yourorg.ghread.core

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class GhClient(private val project: Project) {
  private val log = Logger.getInstance(GhClient::class.java)
  private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  data class CmdResult(val ok: Boolean, val out: String, val err: String, val exit: Int)

  fun available(): Boolean = runCmd(listOf("gh", "--version"), 5_000).ok

  fun authStatus(): CmdResult = runCmd(listOf("gh", "auth", "status", "--show-token"), 10_000)

  fun apiJson(endpoint: String, params: List<String> = emptyList(), paginate: Boolean = false, timeoutMs: Long = 30_000): CmdResult {
    val base = mutableListOf("gh", "api", endpoint)
    if (paginate) base += "--paginate"
    base += listOf("-H", "Accept: application/vnd.github+json")
    base += params
    return runCmd(base, timeoutMs)
  }

  fun <T> parseJson(json: String, clazz: Class<T>): T = mapper.readValue(json, clazz)
  inline fun <reified T> parseJson(json: String): T = mapper.readValue(json)

  private fun runCmd(args: List<String>, timeoutMs: Long): CmdResult {
    val indicator = ProgressManager.getInstance().progressIndicator
    try {
      val pb = ProcessBuilder(args).redirectErrorStream(false)
      val p = pb.start()
      val out = p.inputStream.readBytes()
      val err = p.errorStream.readBytes()
      var waited = 0L
      while (true) {
        if (indicator?.isCanceled == true) {
          p.destroyForcibly()
          return CmdResult(false, "", "Canceled", -1)
        }
        if (p.waitFor(50, TimeUnit.MILLISECONDS)) break
        waited += 50
        if (waited > timeoutMs) {
          p.destroyForcibly()
          return CmdResult(false, "", "Timeout", -1)
        }
      }
      val exit = p.exitValue()
      return CmdResult(exit == 0, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8), exit)
    } catch (t: Throwable) {
      log.warn("gh exec failed: ${t.message}", t)
      return CmdResult(false, "", t.message ?: "error", -1)
    }
  }
}
