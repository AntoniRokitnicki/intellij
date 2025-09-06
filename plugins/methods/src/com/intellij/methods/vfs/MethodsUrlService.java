package com.intellij.methods.vfs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for constructing and resolving methods:// URLs.
 */
public final class MethodsUrlService {
  private MethodsUrlService() {}

  public static @NotNull String byFile(@NotNull Project project, @NotNull VirtualFile file) {
    String base = project.getBasePath();
    String path = file.getPath();
    if (base != null && path.startsWith(base)) {
      String rel = path.substring(base.length());
      if (rel.startsWith("/")) rel = rel.substring(1);
      return MethodsVirtualFileSystem.PROTOCOL + "://by-file/" + project.getName() + "/" + rel;
    }
    return MethodsVirtualFileSystem.PROTOCOL + "://by-file/abs/" + path;
  }

  public static @NotNull String byScope(@NotNull Project project) {
    return MethodsVirtualFileSystem.PROTOCOL + "://by-scope/" + project.getName();
  }

  public static @Nullable VirtualFile resolve(@NotNull String url) {
    return VirtualFileManager.getInstance().findFileByUrl(url);
  }
}
