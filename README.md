# Stack Overflow Top 20

An Android client for the Stack Exchange API. Lists the top users by reputation, with follow / unfollow toggles, local persistence, pull-to-refresh, and pagination.

## Features

- Top users from Stack Exchange, ordered by reputation
- Follow / unfollow toggle, persisted locally
- Pull-to-refresh on the list
- Scroll-triggered pagination using the `has_more` flag from the API
- Loading, error, and empty list states
- Unit-tested business and data logic

## Modules

The project is split across three Gradle modules:

**`:domain`** — Pure Kotlin/JVM. Holds the business types, repository contracts, and the state-machine reducer. No Android dependencies, so the tests run directly on the JVM.

**`:data`** — Network and persistence. Retrofit and kotlinx-serialization handle the Stack Exchange API; DataStore handles follow-state persistence. The repository owns its in-memory cache as a `MutableStateFlow<TopUsersState>`, exposed read-only to consumers.

**`:app`** — Compose UI, ViewModel, and dependency-injection wiring. The ViewModel composes the flows from each repository, runs them through the reducer, and exposes a `StateFlow<UsersViewState>` for the screen.

## Running

Open the project in Android Studio and run the `:app` configuration. From the command line:

```
./gradlew :app:installDebug
```

## Tests

```
./gradlew test
```

Each module has its own test suite. The reducer and mappers are tested as pure functions in `:domain`. The data-layer state machine (refresh, loadMore, retry, cancellation) is tested in `:data`. The ViewModel pipeline is tested in `:app` with mockk.

## Architectural decisions

### Three modules

The split adds upfront boilerplate but pays back as the app grows. `:domain` stays JVM-only, so its tests do not require an Android device. `:data` can swap its remote or local source without touching the UI. The dependency graph enforces the boundary: `:app` does not depend on Retrofit, so an HTTP type cannot leak into the ViewModel.

### MVI with a pure reducer

The reducer is a top-level pure function: `reduceUsersState(TopUsersState, Set<Long>) -> UsersUiState`. It holds no state and requires no mocks to test. The ViewModel composes the repository flows, applies the reducer, and exposes the result.

Unidirectional state flow keeps the UI logic deterministic and the test surface small. Compose's snapshot model fits this approach naturally, and a plain reducer avoids the overhead of a full Redux-style framework.

### Sealed states throughout the pipeline

`TopUsersState` (data) → `UsersUiState` (domain) → `UsersViewState` (UI). Each layer carries the concerns relevant to that layer: the data layer carries a `Throwable`, the domain layer carries a user-facing message, and the UI layer carries an `ImmutableList<UserRowUiModel>` for Compose stability. The sealed hierarchy guarantees every UI state is explicitly handled in the screen's `when` expression.

### Manual pagination over Paging 3

Paging 3 is well-suited to an open-ended feed: infinite scroll, retry per direction, placeholders, and page-level diffing. For this app's dataset and within the existing sealed-state pipeline, a manual approach was simpler to integrate: a `nextPage: Int?` on the `Loaded` state, a `loadMore()` method on the repository, and a scroll-threshold trigger in the UI. Migrating to Paging 3 later would be a focused change inside the data layer.

### DataStore over Room

The persisted state is a small `Set<Long>`. Room would require an entity, a DAO, a database, and migrations for a single key-value entry. DataStore preferences cover this directly. The choice is contained behind the `FollowRepository` interface, so swapping the underlying store would not affect the rest of the codebase.

## Error handling

Errors surface through the sealed state pipeline rather than being thrown:

- Network or deserialisation failures during `refresh()` produce a `TopUsersState.Failed(cause)`, which the reducer maps to a user-facing message.
- Pagination failures during `loadMore()` preserve the current cached list and clear the `isAppending` flag.
- DataStore read failures are caught and replaced with `emptyPreferences()`, so a corrupt prefs file degrades gracefully rather than terminating the upstream `StateFlow`.
- `CancellationException` is rethrown explicitly in repository catch blocks to preserve coroutine cancellation semantics.
