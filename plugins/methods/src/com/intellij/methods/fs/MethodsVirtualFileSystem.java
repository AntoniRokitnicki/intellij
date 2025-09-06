package com.intellij.methods.fs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MethodsVirtualFileSystem extends VirtualFileSystem {
  public static final String PROTOCOL = "methods";

  private final Map<String, MethodsVirtualFile> myFiles = new ConcurrentHashMap<>();

  public static MethodsVirtualFileSystem getInstance() {
    return (MethodsVirtualFileSystem)VirtualFileManager.getInstance().getFileSystem(PROTOCOL);
  }

  @Override
  public @NotNull String getProtocol() {
    return PROTOCOL;
  }

  public @NotNull MethodsVirtualFile createFile(@NotNull String path, @NotNull String content) {
    MethodsVirtualFile file = new MethodsVirtualFile(path, content);
    myFiles.put(path, file);
    return file;
  }

  @Override
  public @Nullable MethodsVirtualFile findFileByPath(@NotNull String path) {
    return myFiles.get(path);
  }

  @Override
  public void refresh(boolean asynchronous) {
  }

  @Override
  public @Nullable VirtualFile refreshAndFindFileByPath(@NotNull String path) {
    return findFileByPath(path);
  }

  @Override
  public void addVirtualFileListener(@NotNull VirtualFileListener listener) {
  }

  @Override
  public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {
  }

  @Override
  protected void deleteFile(Object requestor, @NotNull VirtualFile vFile) {
    throw new IncorrectOperationException();
  }

  @Override
  protected void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) {
    throw new IncorrectOperationException();
  }

  @Override
  protected void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) {
    throw new IncorrectOperationException();
  }

  @Override
  protected @NotNull VirtualFile createChildFile(Object requestor, @NotNull VirtualFile vDir, @NotNull String fileName) {
    throw new IncorrectOperationException();
  }

  @Override
  protected @NotNull VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile vDir, @NotNull String dirName) {
    throw new IncorrectOperationException();
  }

  @Override
  protected @NotNull VirtualFile copyFile(Object requestor, @NotNull VirtualFile virtualFile, @NotNull VirtualFile newParent, @NotNull String copyName) {
    throw new IncorrectOperationException();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
}
