package com.intellij.methods.vfs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.DumbAware;
import org.jetbrains.annotations.NotNull;

/**
 * Ensures the methods:// file system is registered early.
 */
public final class MethodsVfsInitializer implements StartupActivity, DumbAware {
  @Override
  public void runActivity(@NotNull Project project) {
    MethodsVirtualFileSystem.ensureRegistered();
  }
}
