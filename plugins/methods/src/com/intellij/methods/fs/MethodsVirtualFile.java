package com.intellij.methods.fs;

import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class MethodsVirtualFile extends LightVirtualFile {
  private final String myPath;

  public MethodsVirtualFile(@NotNull String path, @NotNull String content) {
    super(new File(path).getName(), content);
    myPath = path;
    setWritable(false);
  }

  @Override
  public @NotNull String getPath() {
    return myPath;
  }

  @Override
  public @NotNull MethodsVirtualFileSystem getFileSystem() {
    return MethodsVirtualFileSystem.getInstance();
  }
}
