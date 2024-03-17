# Getting started

Add the following dependency to your app's `build.gradle`:

```groovy
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview:{{ dpadrecyclerview.version }}"

// Recommended: To use Compose together with DpadRecyclerView
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-compose:{{ dpadrecyclerview.version }}"

// Optional: Espresso test helpers for your instrumented tests:
androidTestImplementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-testing:{{ dpadrecyclerview.version }}"
```

## Basic setup

Since `DpadRecyclerView` is a custom view that extends from `RecyclerView`, you just need to add it to your XML layout as any other view:

```xml linenums="1"
<com.rubensousa.dpadrecyclerview.DpadRecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical" />
```

!!! warning 
    Don't set a `LayoutManager` because `DpadRecyclerView` already assigns one internally.

Follow the [official RecyclerView guides](https://developer.android.com/develop/ui/views/layout/recyclerview) to render Views on the screen
or use any RecyclerView library as you would for mobile apps. 

You can also render Composables inside using the `dpadrecyclerview-compose` library.


## Observe selection changes

You can observe selection changes using the following:

```kotlin linenums="1"
recyclerView.addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {}

    override fun onViewHolderSelectedAndAligned(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {}
})
```

## Observe focus changes

To react to focus changes, use this:

```kotlin linenums="1"
recyclerView.addOnViewFocusedListener(object : OnViewFocusedListener {
    override fun onViewFocused(
        parent: RecyclerView.ViewHolder,
        child: View,
    ) {
        // Child is now focused
    }
})
```

## How to use with Compose

Check [this](compose.md) page to see more some examples with Compose

```kotlin linenums="1", hl_lines="13-16"
@Composable
fun ItemComposable(
    item: Int, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor = if (isFocused) Color.White else Color.Black
    val textColor = if (isFocused) Color.Black else Color.White
    Box(
        modifier = modifier
            .background(backgroundColor)
            .onFocusChanged { focusState ->
                isFocused = focusState.hasFocus
            }
            .focusTarget()
            .dpadClickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.toString(),
            color = textColor,
            fontSize = 35.sp
        )
    }
}
```

## More customizations

Check the following recipes:

1. [Layout](recipes/layout.md): for defining the type of layout (linear or grid) or to enable infinite carousels
2. [Spacing](recipes/spacing.md): add spacing between items
3. [Alignment](recipes/alignment.md): align items to different regions of the screen
4. [Focus](recipes/focus.md): configure how focus is handled
5. [Scrolling](recipes/scrolling.md): configure the scrolling speed

## Sample

The sample on [Github](https://github.com/rubensousa/DpadRecyclerView) contains a complete example of 
how to use this library. 