# IntelliJ VCS and Git Listener Interfaces

This document focuses on listener interfaces related to version control. For each listener you will find a short description, the key methods with their inputs and outputs, and three common scenarios where the listener is useful.

## VcsListener
**Description:** Notified when per-directory VCS mappings change.

**Methods and data:**
- `directoryMappingChanged()` – no parameters and returns `void`. The mapping is already updated; use this callback to react to the new configuration.

**Top 3 use cases:**
1. Adjust plugin configuration after repository roots are added or removed.
2. Trigger reindexing or rescan affected modules.
3. Update UI components that depend on VCS settings.

## ChangeListListener
**Description:** Tracks VCS changelist events from `ChangeListManager`.

**Methods and data:**
- `changeListAdded(ChangeList list)`, `changeListRemoved(ChangeList list)` – receive affected `ChangeList` objects; return type `void`.
- `changesAdded(Collection<Change> changes, ChangeList toList)` – supplies modified `Change` entries and target list; returns `void`.
- Other callbacks such as `changeListRenamed` and `defaultListChanged` provide old and new values. Implementations return `void`; to modify changelists call `ChangeListManager` APIs.

**Top 3 use cases:**
1. React to files being added or removed from changelists.
2. Enforce coding standards before changes are committed.
3. Synchronize changelist state with external tools.

## BranchChangeListener
**Description:** Receives notifications when the active VCS branch changes.

**Methods and data:**
- `branchWillChange(String branchName)` – called before the checkout begins.
- `branchHasChanged(String branchName)` – invoked after the branch switch completes.
Both methods receive the target branch name and return `void`.

**Top 3 use cases:**
1. Refresh project configuration when switching branches.
2. Validate work-in-progress against target branch policies.
3. Update status bars or badges reflecting current branch.

## VcsAnnotationRefresher
**Description:** Signals that file annotations, such as blame info, need refreshing.

**Methods and data:**
- `dirtyUnder(VirtualFile file)` – mark all annotations under a directory as stale.
- `dirty(BaseRevision currentRevision)` / `dirty(String path)` – request a refresh for a specific revision or path.
- `configurationChanged(VcsKey key)` – called when annotation-related VCS settings change.
All methods return `void` and supply context objects needed to re-fetch annotations.

**Top 3 use cases:**
1. Invalidate cached annotations after commit operations.
2. Update gutter displays when authorship data changes.
3. Refresh external annotation views following VCS actions.

## GitPushListener
**Description:** Informed when a Git push operation completes.

**Methods and data:**
- `onCompleted(GitRepository repository, GitPushRepoResult pushResult)` – provides the repository and the push result.
- `onCompleted(GitRepository repository, GitPushRepoResult pushResult, Map<String, VcsPushOptionValue> params)` – experimental overload supplying push parameters.
Methods return `void`; use the supplied `GitPushRepoResult` to access commits pushed and remote messages.

**Top 3 use cases:**
1. Notify issue trackers or CI systems after pushing.
2. Display results of push in custom tool windows.
3. Trigger post-push code analysis.

## GitRepositoryChangeListener
**Description:** Fired when Git repository state updates, excluding index changes.

**Methods and data:**
- `repositoryChanged(GitRepository repository)` – supplies the updated repository; return type `void`.

**Top 3 use cases:**
1. Refresh branch lists when remote refs change.
2. Update plugin caches of repository metadata.
3. Monitor repository for external modifications.

## GitRepositoryStateChangeListener
**Description:** Provides detailed notifications about repository state transitions, e.g., during rebase.

**Methods and data:**
- `repositoryCreated(GitRepository repository, GitRepoInfo info)` – optional notification when tracking begins.
- `repositoryChanged(GitRepository repository, GitRepoInfo previousInfo, GitRepoInfo info)` – gives old and new repository information to compare states. Methods return `void`.

**Top 3 use cases:**
1. Track ongoing operations like merges or rebases.
2. Show warnings when repository enters a detached HEAD state.
3. Automate cleanup tasks after operations finish.

## GitConfigListener
**Description:** Notified when the `.git/config` file is modified.

**Methods and data:**
- `notifyConfigChanged(GitRepository repository)` – receives the repository whose configuration changed; returns `void`.

**Top 3 use cases:**
1. Reapply plugin-specific Git settings when configuration changes.
2. Validate repository settings for required options.
3. Alert users to potential misconfigurations.

