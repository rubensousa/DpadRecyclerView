# Migrating from Leanback

## Interopability

`DpadRecyclerView` is also compatible with Leanback and can be used together in the same view hierarchy.

You can combine `DpadRecyclerView` with `BaseGridView` as follows:

1. `DpadRecyclerView` as the parent RecyclerView and `BaseGridView` for the nested lists

2. `BaseGridView` as the parent RecyclerView and `DpadRecyclerView` for the nested lists


## Themes

`DpadRecyclerView` does not require any theme like `Theme.Leanback`, so feel free to use any.

## VerticalGridView and HorizontalGridView

Instead of using `VerticalGridView` or `HorizontalGridView`, use `DpadRecyclerView` and set the orientation either programmatically
with `setOrientation` or with plain XML:

```xml linenums="1" hl_lines="6"
<!-- This is the same as VerticalGridView -->
<com.rubensousa.dpadrecyclerview.DpadRecyclerView 
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"/>
```

```xml linenums="1" hl_lines="6"
<!-- This is the same as HorizontalGridView -->
<com.rubensousa.dpadrecyclerview.DpadRecyclerView 
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"/>
```

## Window Alignment

The window alignment APIs from `BaseGridView` can now be found in the `ParentAlignment` class.

=== "BaseGridView"

    ```kotlin linenums="1"
    recyclerView.windowAlignment = BaseGridView.WINDOW_ALIGN_NO_EDGE
    recyclerView.windowAlignmentOffset = 0
    recyclerView.windowAlignmentOffsetPercent = 100f
    recyclerView.windowAlignmentPreferKeyLineOverLowEdge = false
    recyclerView.windowAlignmentPreferKeyLineOverHighEdge = false
    ```

=== "DpadRecyclerView"

    ```kotlin linenums="1"
    recyclerView.setParentAlignment(
        ParentAlignment(
            edge = ParentAlignment.Edge.NONE,
            offset = 0,
            fraction = 1f,
            preferKeylineOverEdge = false
        )
    )
    ```


Edge alignment mapping:

| BaseGridView                          | DpadRecyclerView               |
|---------------------------------------|--------------------------------|
| `BaseGridView.WINDOW_ALIGN_NO_EDGE`   | `ParentAlignment.Edge.NONE`    |
| `BaseGridView.WINDOW_ALIGN_LOW_EDGE`  | `ParentAlignment.Edge.MIN`     |
| `BaseGridView.WINDOW_ALIGN_MAX_EDGE`  | `ParentAlignment.Edge.MAX`     |
| `BaseGridView.WINDOW_ALIGN_BOTH_EDGE` | `ParentAlignment.Edge.MIN_MAX` |

## Item Alignment


The child alignment APIs from `BaseGridView` can now be found in the `ChildAlignment` class.

=== "BaseGridView"

    ```kotlin linenums="1"
    recyclerView.itemAlignmentOffset = 0
    recyclerView.itemAlignmentOffsetPercent = 100f
    recyclerView.isItemAlignmentOffsetWithPadding = true
    ```

=== "DpadRecyclerView"

    ```kotlin linenums="1"
    recyclerView.setChildAlignment(
        ChildAlignment(
            offset = 0,
            fraction = 1f,
            includePadding = true
        )
    )
    ```

## Selection changes

=== "BaseGridView"

    ```kotlin linenums="1"
    recyclerView.addOnChildViewHolderSelectedListener(object : OnChildViewHolderSelectedListener() {
        override fun onChildViewHolderSelected(
            parent: RecyclerView,
            child: RecyclerView.ViewHolder?,
            position: Int,
            subposition: Int
        ) {}
    
        override fun onChildViewHolderSelectedAndPositioned(
            parent: RecyclerView,
            child: RecyclerView.ViewHolder?,
            position: Int,
            subposition: Int
        ) {}
    })
    ```

=== "DpadRecyclerView"

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

## ItemAlignmentFacet or FacetProviderAdapter

This was typically used for configuring sub position alignment. Check this [page](recipes/alignment.md) for more information about sub position alignment.

If you were using `FacetProviderAdapter` for anything else, just write your own logic in the `RecyclerView.Adapter` and expose those events to the `ViewHolders` directly

## Item spacing

`DpadRecyclerView` ships with `DpadLinearSpacingDecoration` and `DpadGridSpacingDecoration` to achieve this, together with `setItemSpacing` and similar APIs.

Please check the examples in the recipes at [Spacing](recipes/spacing.md).

## Fading edges

Support for fading edges is enabled with the default XML attribute `fadingEdgeLength`.

```xml linenums="1" hl_lines="5"
<com.rubensousa.dpadrecyclerview.DpadRecyclerView 
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fadingEdgeLength="120dp"
    android:orientation="vertical|horizontal" 
```

Alternatively, you can customise the fading edge with the following methods:

- `setMinEdgeFadingLength`
- `setMinEdgeFadingOffset`
- `setMaxEdgeFadingLength`
- `setMaxEdgeFadingOffset`
- `setFadingEdgeLength`

## Presenters

These will still work out of the box with `DpadRecyclerView` since they're just a wrapper over `RecyclerView.Adapter`,
 but feel free to use any other RecyclerView framework in case you want to get rid completely of the Leanback library.