package com.yourorg.paged3way

data class PagedSettings(
  val branches: List<String>,
  var overlap: Int = 2,
  var pinMiddle: String? = null
) {
  companion object {
    fun defaultFive(): PagedSettings =
      PagedSettings(listOf("branchA", "branchB", "branchC", "branchD", "branchE"))
  }
}

