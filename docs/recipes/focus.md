# Focus Recipes

## Disabling focus changes

You might want to temporarily disable focus changes and prevent other views from being selected
by the user:

```kotlin
recyclerView.setFocusSearchDisabled(true)
```

This will block focus requests coming from DPAD events.

## Grid circular focus

![Circular focus](../img/circular_focus.png)

Dispatches focus back to the opposite span when it is currently at an edge.

```kotlin
recyclerView.setFocusableDirection(FocusableDirection.CIRCULAR)
```


## Grid continuous focus

![Continuous focus](../img/continuous_focus.png)

Dispatches focus to the next or previous positions.

```kotlin
recyclerView.setFocusableDirection(FocusableDirection.CONTINUOUS)
```


## Preventing focus losses

You might want your `DpadRecyclerView` to keep focus if the user presses a DPAD event that would trigger a focus change
to an outside view. This typically happens when the selection is at the first or last item.

### Main direction

To prevent focus leaving from the main direction of scrolling, use `setFocusOutAllowed`:

```kotlin
recyclerView.setFocusOutAllowed(throughFront = true, throughBack = false)
```

Let's assume this RecyclerView has vertical orientation.
The example above would allow focusing out when the first item is selected and `KEYCODE_DPAD_UP` is pressed, 
but it would prevent focus from leaving when the last item is selected and `KEYCODE_DPAD_DOWN` is pressed.


### Secondary direction

To prevent focus leaving from the secondary direction of scrolling, use `setFocusOutSideAllowed`:

```kotlin
recyclerView.setFocusOutSideAllowed(throughFront = true, throughBack = false)
```

Let's again assume this RecyclerView has vertical orientation.
The example above would allow focusing out when focus is at the first span and `KEYCODE_DPAD_LEFT` is pressed,
but it would prevent focus from leaving when the last span is selected and `KEYCODE_DPAD_RIGHT` is pressed.
