# Spacing

Unlike `BaseGridView`, `DpadRecyclerView` does not have a `setItemSpacing` method.
However, you can achieve the same effect with one of the implementations of `ItemDecoration` provided by this library:

- `DpadLinearSpacingDecoration`: for columns and rows
- `DpadGridSpacingDecoration`: for grids

Both of them support vertical and horizontal orientations. Please check the examples below.


## Linear spacings

### Column

<img width="800" alt="image" src="https://user-images.githubusercontent.com/10662096/216611718-1771e4ed-c7da-4532-90c2-65300ce0a744.png">

```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadLinearSpacingDecoration.create(
            itemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.vertical_item_spacing
            ),
            edgeSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.vertical_edge_spacing
            ),
            perpendicularEdgeSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.horizontal_edge_spacing
            )
        )
    )
}
```

!!! note

    - If you don't specify a `edgeSpacing`, by default it will be the same as `itemSpacing`.
    - If you don't specify a `perpendicularEdgeSpacing`, by default it will be set to 0.

If you just need even spacings across all items in the layout direction, you can do the following:

```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadLinearSpacingDecoration.create(
            itemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.vertical_item_spacing
            )
        )
    )
}
```

### Row

<img width="800" alt="image" src="https://user-images.githubusercontent.com/10662096/216611767-ab2903f9-b122-4599-ae26-3828edec0c92.png">

```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadLinearSpacingDecoration.create(
            itemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.horizontal_item_spacing
            ),
            edgeSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.horizontal_edge_spacing
            ),
            perpendicularEdgeSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.vertical_edge_spacing
            )
        )
    )
}
```

!!! note

    - If you don't specify a `edgeSpacing`, by default it will be the same as `itemSpacing`.
    - If you don't specify a `perpendicularEdgeSpacing`, by default it will be set to 0.
    

If you just need even spacings across all items in the layout direction, you can do the following:


```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadLinearSpacingDecoration.create(
            itemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.vertical_item_spacing
            )
        )
    )
}
```

## Grid spacings


### Vertical

<img width="800" alt="image" src="https://user-images.githubusercontent.com/10662096/216621920-e6a9cd5e-c5d7-43e9-9814-3c7e1b08b400.png">

```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadGridSpacingDecoration.createVertical(
            itemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.grid_horizontal_item_spacing
            ),
            perpendicularItemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.grid_vertical_item_spacing
            ),
            edgeSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.grid_vertical_edge_spacing
            )
        )
    )
}
```

!!! note

    - If you don't specify a `edgeSpacing` or `perpendicularItemSpacing`, by default both will be the same as `itemSpacing`.


If you just want to apply the same spacing to all sides, you can do the following:

```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadGridSpacingDecoration.create(
            itemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.grid_item_spacing
            )
        )
    )
}
```

### Horizontal

<img width="800" alt="image" src="https://user-images.githubusercontent.com/10662096/216621977-6cc8ec2d-9e8d-49c7-87a9-d48a49369728.png">


```kotlin linenums="1"
fun setupSpacing(recyclerView: DpadRecyclerView) {
    recyclerView.addItemDecoration(
        DpadGridSpacingDecoration.createVertical(
            horizontalItemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.grid_horizontal_item_spacing
            ),
            verticalItemSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.grid_horizontal_item_spacing
            ),
            verticalEdgeSpacing = recyclerView.resources.getDimensionPixelOffset(
                R.dimen.horizontal_edge_spacing
            )
        )
    )
}
```
