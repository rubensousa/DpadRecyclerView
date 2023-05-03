# Testing

The module `dpadrecyclerview-testing` includes useful Espresso helpers for UI tests that require DPAD navigation.

Check the [Espresso training guide](https://developer.android.com/training/testing/espresso/basics) if you're not familiar with this testing framework. 

The official project sample over at Github also contains example UI tests [here](https://github.com/rubensousa/DpadRecyclerView/blob/master/sample/src/androidTest/java/com/rubensousa/dpadrecyclerview/sample/test/SampleTests.kt)

## Dispatching key events

`KeyEvents` provides some utility methods for easily pressing keys a certain amount of times.

```kotlin
KeyEvents.click()
KeyEvents.back()
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
* `getRelativeViewBounds`: returns the bounds of a view in the coordinate-space of the parent view
* `clearFocus`: clears the focus of a view if something else can take focus in its place
* `requestFocus`: requests focus of a view

`DpadRecyclerViewActions` contains the following:

* `scrollTo`: scrolls to a specific itemView using KeyEvents
* `scrollToHolder`: scrolls to a specific ViewHolder using KeyEvents
* `selectLastPosition `: selects the last position of the adapter
* `selectPosition `: selects a given position or position-subPosition pair
* `selectSubPosition `: selects a given subPosition for the current selected position
* `getItemViewBounds`: returns the bounds of the itemView of a ViewHolder at a given position in the coordinate-space of the root view of the window
* `waitForAdapterUpdate`: loops the main thread until there's a given amount of adapter updates
* `waitForIdleScroll`: loops the main thread until the `DpadRecyclerView` scroll state is not idle
* `execute`: perform a generic action on the `DpadRecyclerView`

Example:

```kotlin
Espresso.onView(withId(R.id.recyclerView))
    .perform(DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
        hasDescendant(withText("Some title"))
    ))
``` 

## ViewAssertions

`DpadRecyclerViewAssertions`:

* `isFocused`: checks if a ViewHolder at a given position is focused
* `isSelected`: checks if a ViewHolder at a given position or position-subPosition pair is selected

`DpadViewAssertions`:

* `isFocused` and `isNotFocused` : checks if a View is focused
* `hasFocus` and `doesNotHaveFocus`: checks if a View or one of its descendants has focus

Example:

```kotlin
Espresso.onView(withId(R.id.recyclerView))
    .assert(DpadRecyclerViewAssertions.isFocused(position = 5))
``` 