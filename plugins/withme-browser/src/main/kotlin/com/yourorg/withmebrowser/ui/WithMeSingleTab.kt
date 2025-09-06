package com.yourorg.withmebrowser.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.yourorg.withmebrowser.sync.WithMeSyncService
import com.yourorg.withmebrowser.sync.WithMeSyncService.BrowserEvent
import com.yourorg.withmebrowser.sync.WithMeSyncService.EventType
import com.yourorg.withmebrowser.sync.WithMeSyncService.Role
import com.yourorg.withmebrowser.sync.WithMeSyncService.TransportState
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class WithMeSingleTab(
  private val project: Project,
  startUrl: String,
  private val onOpenDevTools: (java.awt.Component?) -> Unit
) : Disposable {

  private val sync = WithMeSyncService.getInstance()
  private val client = JBCefApp.getInstance().createClient()
  private val browser = JBCefBrowser(client, startUrl)
  private val address = JBTextField(startUrl)
  private val panel = JPanel(BorderLayout())
  private val toolbar = JToolBar().apply { isFloatable = false }

  private var suppressLocal = false // by uniknąć pętli: odbiór→nawigacja→wysyłka

  init {
    // pasek narzędzi
    val back = JButton(AllIcons.Actions.Back).apply { addActionListener { goBack() } }
    val fwd  = JButton(AllIcons.Actions.Forward).apply { addActionListener { goForward() } }
    val reload = JButton(AllIcons.Actions.Refresh).apply { addActionListener { reload() } }
    val stop = JButton(AllIcons.Actions.Suspend).apply { addActionListener { browser.cefBrowser.stopLoad() } }
    val shareUrl = JButton(AllIcons.Ide.ExternalLinkArrow).apply { addActionListener { copyUrl() } }
    val devt = JButton(AllIcons.Debugger.ShowCurrentFrame).apply { addActionListener { openDevTools() } }

    address.addActionListener { navigateFromUI(address.text) }
    address.addKeyListener(object : KeyAdapter() {
      override fun keyReleased(e: KeyEvent) {
        if (e.keyCode == KeyEvent.VK_ESCAPE) address.text = browser.cefBrowser.url
      }
    })

    listOf(back, fwd, reload, stop, devt, shareUrl).forEach(toolbar::add)
    toolbar.add(address)

    // JCEF eventy – aktualizacja paska adresu i broadcast
    client.addDisplayHandler(object : org.cef.handler.CefDisplayHandlerAdapter() {
      override fun onAddressChange(browser: org.cef.browser.CefBrowser?, frame: org.cef.browser.CefFrame?, url: String?) {
        if (url != null) SwingUtilities.invokeLater { address.text = url }
      }
      override fun onTitleChange(browser: org.cef.browser.CefBrowser?, title: String?) {
        // można ustawiać label zakładki
      }
    }, browser.cefBrowser)

    client.addLoadHandler(object : org.cef.handler.CefLoadHandlerAdapter() {
      override fun onLoadingStateChange(b: org.cef.browser.CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
        // po zakończeniu ładowania host może rozesłać bieżący URL
        if (!isLoading && sync.role() == Role.HOST && sync.state() == TransportState.AVAILABLE) {
          broadcast(EventType.NAVIGATE, browser.cefBrowser.url ?: "")
        }
      }
    }, browser.cefBrowser)

    // subskrypcja sync
    sync.subscribe { ev -> SwingUtilities.invokeLater { handleRemote(ev) } }

    panel.add(toolbar, BorderLayout.NORTH)
    panel.add(browser.component, BorderLayout.CENTER)
  }

  fun component(): JComponent = panel
  fun title(): String = "Web"
  fun icon(): Icon = AllIcons.General.Web
  fun tooltip(): String = "WithMe Embedded Browser"

  private fun navigateFromUI(url: String) {
    val finalUrl = if (url.matches(Regex("^[a-zA-Z]+://.*"))) url else "https://$url"
    browser.loadURL(finalUrl)
    if (sync.role() == Role.HOST && sync.state() == TransportState.AVAILABLE) {
      broadcast(EventType.NAVIGATE, finalUrl)
    }
  }

  private fun goBack() {
    browser.cefBrowser.goBack()
    if (sync.role() == Role.HOST) broadcast(EventType.BACK, "")
  }

  private fun goForward() {
    browser.cefBrowser.goForward()
    if (sync.role() == Role.HOST) broadcast(EventType.FORWARD, "")
  }

  private fun reload() {
    browser.cefBrowser.reloadIgnoreCache()
    if (sync.role() == Role.HOST) broadcast(EventType.RELOAD, "")
  }

  private fun copyUrl() {
    val u = browser.cefBrowser.url ?: return
    Toolkit.getDefaultToolkit().systemClipboard.setContents(java.awt.datatransfer.StringSelection(u), null)
  }

  private fun openDevTools() {
    val dev = browser.createDevtoolsBrowser()
    onOpenDevTools(dev.component)
  }

  private fun broadcast(type: EventType, payload: String) {
    sync.publish(BrowserEvent(type, payload))
  }

  private fun handleRemote(ev: BrowserEvent) {
    // Gość podąża za hostem. Host ignoruje własne eventy echo.
    if (sync.role() != Role.GUEST) return
    suppressLocal = true
    try {
      when (ev.type) {
        EventType.NAVIGATE -> if (ev.payload.isNotBlank()) browser.loadURL(ev.payload)
        EventType.BACK -> browser.cefBrowser.goBack()
        EventType.FORWARD -> browser.cefBrowser.goForward()
        EventType.RELOAD -> browser.cefBrowser.reloadIgnoreCache()
      }
    } finally {
      suppressLocal = false
    }
  }

  override fun dispose() {
    browser.dispose()
  }
}
