package com.intellij.methods.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight virtual file storing generated content in-memory.
 */
public final class MethodsVirtualFile extends VirtualFile {
  private final String myPath;
  private final String myName;
  private volatile byte[] myContent = new byte[0];
  private volatile long myStamp = 0L;

  MethodsVirtualFile(@NotNull String path) {
    myPath = path;
    int idx = path.lastIndexOf('/');
    myName = idx >= 0 ? path.substring(idx + 1) : path;
  }

  void updateContent(byte[] data, long stamp) {
    myContent = data;
    myStamp = stamp;
  }

  private String fullPath() {
    return MethodsVirtualFileSystem.PROTOCOL + "://" + myPath;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @NotNull VirtualFileSystem getFileSystem() {
    return MethodsVirtualFileSystem.getInstance();
  }

  @Override
  public @NotNull String getPath() {
    return fullPath();
  }

  @Override
  public boolean isWritable() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public VirtualFile getParent() {
    return null;
  }

  @Override
  public VirtualFile[] getChildren() {
    return EMPTY_ARRAY;
  }

  @Override
  public @NotNull InputStream getInputStream() {
    byte[] data = contentsToByteArray();
    return new ByteArrayInputStream(data);
  }

  @Override
  public byte @NotNull [] contentsToByteArray() {
    byte[] data = myContent;
    if (data.length == 0) {
      data = MethodsVirtualFileSystem.buildContent(myPath);
      updateContent(data, System.currentTimeMillis());
    }
    return data;
  }

  @Override
  public long getTimeStamp() {
    return myStamp;
  }

  @Override
  public long getLength() {
    return contentsToByteArray().length;
  }

  @Override
  public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
  }

  @Override
  public @NotNull OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
    throw new IOException("Read only");
  }

  @Override
  public long getModificationStamp() {
    return myStamp;
  }

  @Override
  public @NotNull byte[] getOutputStreamContent() {
    return myContent;
  }
}
