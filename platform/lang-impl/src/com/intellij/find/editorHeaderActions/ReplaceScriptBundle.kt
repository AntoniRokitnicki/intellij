package com.intellij.find.editorHeaderActions

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val BUNDLE: @NonNls String = "messages.ReplaceScriptBundle"

object ReplaceScriptBundle : DynamicBundle(BUNDLE) {
  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String =
    getMessage(key, *params)

  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> =
    getLazyMessage(key, *params)
}
