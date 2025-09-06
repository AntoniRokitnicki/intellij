package com.yourorg.zenecjava

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiFile

class ZenecToJavaRenderer {

  fun render(jsonPsi: PsiFile?): String {
    val root = jsonPsi as? JsonFile ?: return defaultSkeleton()
    val obj = root.topLevelValue as? JsonObject ?: return defaultSkeleton()

    val className = obj.findProperty("className")?.value?.text?.trim('"') ?: "Zenec"
    return """
      public class $className {

        public static void main(String[] args) {
          System.out.println("$className from zenec.json preview");
        }
      }
    """.trimIndent()
  }

  companion object {
    fun defaultSkeleton(): String = """
      public class Zenec {

        public static void main(String[] args) {
          System.out.println("Zenec from zenec.json preview");
        }
      }
    """.trimIndent()
  }
}
