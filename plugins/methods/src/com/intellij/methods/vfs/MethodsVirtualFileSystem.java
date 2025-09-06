package com.intellij.methods.vfs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.KeyedLazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Virtual file system exposing generated method listings.
 */
public final class MethodsVirtualFileSystem extends VirtualFileSystem {
  public static final String PROTOCOL = "methods";
  private static final int MAX_SCOPE_ITEMS = 500;
  private static volatile MethodsVirtualFileSystem ourInstance;
  private static final AtomicBoolean registered = new AtomicBoolean(false);

  private MethodsVirtualFileSystem() { }

  public static MethodsVirtualFileSystem getInstance() {
    ensureRegistered();
    return ourInstance;
  }

  public static void ensureRegistered() {
    if (registered.compareAndSet(false, true)) {
      ourInstance = new MethodsVirtualFileSystem();
      VirtualFileSystem.EP_NAME.getPoint().registerExtension(new KeyedLazyInstance<>() {
        @Override
        public @NotNull String getKey() {
          return PROTOCOL;
        }

        @Override
        public @NotNull VirtualFileSystem getInstance() {
          return ourInstance;
        }
      }, ApplicationManager.getApplication());
    }
  }

  @Override
  public @NotNull String getProtocol() {
    return PROTOCOL;
  }

  @Override
  public @Nullable VirtualFile findFileByPath(@NotNull String path) {
    return MethodsFileRegistry.getOrCreate(path);
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
  protected void fireBeforeFileDeletion(@NotNull VirtualFile file, @NotNull Object requestor) {
  }

  @Override
  protected void fireBeforeFileChange(@NotNull VirtualFile file, @NotNull Object requestor) {
  }

  @Override
  public void deleteFile(Object requestor, @NotNull VirtualFile vFile) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public @NotNull VirtualFile createChildFile(Object requestor, @NotNull VirtualFile parent, @NotNull String file) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public @NotNull VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile parent, @NotNull String dir) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public @NotNull VirtualFile copyFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent, @NotNull String copyName) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  // --- content builders ---
  static byte[] buildContent(String path) {
    if (path.startsWith("by-file/")) {
      return buildFileContent(path.substring("by-file/".length()));
    }
    if (path.startsWith("by-scope/")) {
      return buildScopeContent(path.substring("by-scope/".length()));
    }
    return new byte[0];
  }

  private static byte[] buildFileContent(String spec) {
    String text = ReadAction.compute(() -> {
      Project project = null;
      VirtualFile real = null;
      if (spec.startsWith("abs/")) {
        String abs = spec.substring("abs/".length());
        real = StandardFileSystems.local().findFileByPath(abs);
        if (real != null) {
          project = ProjectLocator.getInstance().guessProjectForFile(real);
        }
      }
      else {
        int idx = spec.indexOf('/');
        if (idx > 0) {
          String projectName = spec.substring(0, idx);
          String rel = spec.substring(idx + 1);
          for (Project p : ProjectManager.getInstance().getOpenProjects()) {
            if (p.getName().equals(projectName)) {
              project = p;
              String base = p.getBasePath();
              if (base != null) {
                real = StandardFileSystems.local().findFileByPath(base + "/" + rel);
              }
              break;
            }
          }
        }
      }
      if (project == null || real == null) return "";
      DumbService dumb = DumbService.getInstance(project);
      if (dumb.isDumb()) {
        dumb.runWhenSmart(() -> MethodsFileRegistry.update("by-file/" + spec, buildFileContent(spec), System.currentTimeMillis()));
        return "Indexing in progress";
      }
      PsiFile psi = PsiManager.getInstance(project).findFile(real);
      if (psi == null) return "";
      List<String> names = new ArrayList<>();
      psi.accept(new JavaRecursiveElementVisitor() {
        @Override
        public void visitMethod(@NotNull PsiMethod method) {
          names.add(method.getName());
        }
      });
      return String.join("\n", names);
    });
    return text.getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] buildScopeContent(String projectId) {
    String text = ReadAction.compute(() -> {
      Project project = null;
      for (Project p : ProjectManager.getInstance().getOpenProjects()) {
        if (p.getName().equals(projectId)) {
          project = p;
          break;
        }
      }
      if (project == null) return "";
      DumbService dumb = DumbService.getInstance(project);
      if (dumb.isDumb()) {
        dumb.runWhenSmart(() -> MethodsFileRegistry.update("by-scope/" + projectId, buildScopeContent(projectId), System.currentTimeMillis()));
        return "Indexing in progress";
      }
      PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
      GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
      String[] names = cache.getAllMethodNames();
      List<String> lines = new ArrayList<>();
      outer: for (String name : names) {
        PsiMethod[] methods = cache.getMethodsByNameIfNotMoreThan(name, scope, MAX_SCOPE_ITEMS);
        for (PsiMethod method : methods) {
          PsiClass cls = method.getContainingClass();
          String owner = cls != null ? cls.getQualifiedName() : "";
          lines.add(name + (owner == null || owner.isEmpty() ? "" : " " + owner));
          if (lines.size() >= MAX_SCOPE_ITEMS) break outer;
        }
      }
      return String.join("\n", lines);
    });
    return text.getBytes(StandardCharsets.UTF_8);
  }
}
