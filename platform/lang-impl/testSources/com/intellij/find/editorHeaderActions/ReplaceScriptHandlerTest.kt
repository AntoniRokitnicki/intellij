package com.intellij.find.editorHeaderActions

import com.intellij.find.AbstractFindInEditorTest

class ReplaceScriptHandlerTest : AbstractFindInEditorTest() {
  fun testReplaceScriptTransformsSelection() {
    init("""<selection>foo\nbar</selection>\nbaz""")
    initReplace()
    val script = "fun process(line: String): String = line.uppercase()"
    ReplaceScriptHandler.execute(myFixture.project, editor, script)
    checkResultByText("""FOO\nBAR\nbaz""")
  }
}
