package com.intellij.methods.vfs;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory registry mapping path strings to {@link MethodsVirtualFile} instances.
 */
public final class MethodsFileRegistry {
  private static final ConcurrentMap<String, MethodsVirtualFile> FILES = new ConcurrentHashMap<>();

  private MethodsFileRegistry() {}

  public static @NotNull MethodsVirtualFile getOrCreate(@NotNull String path) {
    return FILES.computeIfAbsent(path, MethodsVirtualFile::new);
  }

  public static void update(@NotNull String path, byte @NotNull [] bytes, long stamp) {
    getOrCreate(path).updateContent(bytes, stamp);
  }
}
