package com.yourorg.methodsfs.vfs

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame

class MethodsVfsRegistrar : ApplicationActivationListener {
  override fun applicationActivated(ideFrame: IdeFrame) {
    MethodsVirtualFileSystem.instance()
  }
}
