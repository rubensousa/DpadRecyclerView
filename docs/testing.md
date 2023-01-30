# Testing

The module `dpadrecyclerview-testing` includes useful Espresso helpers for UI tests that require DPAD navigation.

Check the [Espresso training guide](https://developer.android.com/training/testing/espresso/basics) if you're not familiar with this testing framework. 

## Dispatching key events

`KeyEvents` provides some utility methods for easily pressing keys a certain amount of times.

```kotlin
KeyEvents.pressKey(KeyEvent.KEYCODE_DPAD_CENTER)
KeyEvents.pressDown(times = 5)
// 50 ms between each key press
KeyEvents.pressUp(times = 5, delay = 50)
```

## Speeding up rate of key events

`DisableIdleTimeoutRule` is a rule useful for removing any artificial delays produced automatically by UiAutomator when injecting key events into the test application.

```kotlin linenums="1"
class UiTest() {

    @get:Rule
    val disableIdleTimeoutRule = DisableIdleTimeoutRule()

}
``` 

## ViewActions


`DpadViewActions` contains the following:

* `getViewBounds`: returns the bounds of a view in the coordinate-space of the root view of the window
* `clearFocus`: clears the focus of a view if something else can take focus in its place
* `requestFocus`: requests focus of a view

`DpadRecyclerViewActions` contains the following:

* `selectLastPosition `: selects the last position of the adapter
* `selectPosition `: selects a given position or position-subPosition pair
* `selectSubPosition `: selects a given subPosition for the current selected position
* `getItemViewBounds`: returns the bounds of the itemView of a ViewHolder at a given position in the coordinate-space of the root view of the window
* `waitForAdapterUpdate`: loops the main thread until there's a given amount of adapter updates
* `waitForIdleScroll`: loops the main thread until the `DpadRecyclerView` scroll state is not idle
* `execute`: perform a generic action on the `DpadRecyclerView`

Example:

```kotlin
Espresso.onView(withId(R.id.recyclerView)).perform(DpadRecyclerViewActions.selectPosition(5))
``` 

## ViewAssertions

`DpadRecyclerViewAssertions` contains the following:

* `isNotFocused`: checks if the `DpadRecyclerView` is not focused
* `isFocused`: checks if a ViewHolder at a given position is focused
* `isSelected`: checks if a ViewHolder at a given position or position-subPosition pair is selected

Example:

```kotlin
Espresso.onView(withId(R.id.recyclerView)).assert(DpadRecyclerViewAssertions.isFocused(position = 5))
``` 