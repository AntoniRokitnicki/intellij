# CheckboxTree

## Overview
CheckboxTree is a UI component based on `JTree` where each node displays its own tri-state checkbox. It is built on top of `CheckboxTreeBase` and `CheckboxTreeHelper`, which manage rendering, selection propagation, and event dispatching.

## Features
- **Tri-state checkboxes** – nodes can be selected, unselected, or in a partial state when children differ.
- **Configurable check policy** – checking or unchecking a node automatically updates parents and children according to the supplied `CheckPolicy` and notifies listeners of changes.
- **Keyboard and mouse support** – space bar toggles the current selection and mouse clicks detect whether a checkbox was hit.
- **Speed search** – a search field is installed by default to filter nodes quickly.
- **Retrieving selections** – utility methods return all checked nodes of a given type.

## Basic API Example
```kotlin
val root = CheckedTreeNode("Root").apply {
    add(CheckedTreeNode("Child 1"))
    add(CheckedTreeNode("Child 2"))
}

val tree = CheckboxTree(object : CheckboxTree.CheckboxTreeCellRenderer() {
    override fun customizeRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean,
                                   leaf: Boolean, row: Int, hasFocus: Boolean) {
        textRenderer.append(value.toString())
    }
}, root, CheckPolicy(checkChildrenWithCheckedParent = true,
                     uncheckChildrenWithUncheckedParent = true,
                     checkParentWithCheckedChild = true,
                     uncheckParentWithUncheckedChild = false))

// listen for changes
 tree.addCheckboxTreeListener(object : CheckboxTreeListener {
    override fun nodeStateChanged(node: CheckedTreeNode) {
        println("Node changed: ${'$'}{node.userObject}")
    }
})

// collect checked items
val checked: Array<String> = tree.getCheckedNodes(String::class.java, null)
```

## Extended Components
### CheckboxTreeTable
Integrates a `CheckboxTree` into a table with additional columns. Create it with a root node, renderer, and column info:
```java
ColumnInfo[] columns = { /* column definitions */ };
CheckboxTreeTable table = new CheckboxTreeTable(rootNode, renderer, columns);
```

### BreakpointsCheckboxTree
Used in the debugger to react to breakpoint toggles. It exposes a delegate to observe state transitions and custom speed search text.
```java
BreakpointsCheckboxTree tree = new BreakpointsCheckboxTree(project, controller);
tree.setDelegate(new BreakpointsCheckboxTree.Delegate() {
    public void nodeStateDidChange(CheckedTreeNode node) { /* ... */ }
    public void nodeStateWillChange(CheckedTreeNode node) { /* ... */ }
});
```

### FrameworksTree
Appears in project wizards to enable framework support. It uses a custom `CheckPolicy`, special mouse handling to detect checkbox hits, and installs speed search over framework names.
```java
FrameworksTree tree = new FrameworksTree(model);
// set roots and react to checkbox-only mouse events
```

### DetectedFrameworksTree
Shows frameworks detected in a project. Its `onNodeStateChanged` implementation automatically unchecks incompatible frameworks.
```java
DetectedFrameworksTree tree = new DetectedFrameworksTree(context, groupByOption);
```

### IntentionSettingsTree
Wraps a `CheckboxTree` with filtering and toolbar actions for configuring inspection intentions.
```java
IntentionSettingsTree settings = new IntentionSettingsTree() {
    protected void selectionChanged(Object selected) { /* ... */ }
    protected Collection<IntentionActionMetaData> filterModel(String filter, boolean force) { /* ... */ return List.of(); }
};
JComponent component = settings.getComponent();
```

