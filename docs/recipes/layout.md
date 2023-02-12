# Layout Recipes

## Rows

A Row is simply a `DpadRecyclerView` with `RecyclerView.HORIZONTAL` as its orientation.
Do the following either in XML or Kotlin:

=== "XML"
    ```xml linenums="1" hl_lines="5"
    <com.rubensousa.dpadrecyclerview.DpadRecyclerView 
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal" />
    ```

=== "Kotlin"

    ```kotlin
    recyclerView.setOrientation(RecyclerView.HORIZONTAL)
    ```

!!! note
    To center views vertically inside a horizontal `DpadRecyclerView`, 
    you can use the `gravity` attribute like so:

    ```xml linenums="1" hl_lines="6"
    <com.rubensousa.dpadrecyclerview.DpadRecyclerView 
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="260dp"
        android:orientation="horizontal"
        android:gravity="center" />
    ```


## Columns

A Column is simply a `DpadRecyclerView` with `RecyclerView.VERTICAL` as its orientation.
Do the following either in XML or Kotlin:

=== "XML"
    ```xml linenums="1" hl_lines="5"
    <com.rubensousa.dpadrecyclerview.DpadRecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical" />
    ```

=== "Kotlin"

    ```kotlin
    recyclerView.setOrientation(RecyclerView.VERTICAL)
    ```

## Grids

The API is similar to the one of `GridLayoutManager` from androidx.recyclerview:

=== "XML"
    ```xml linenums="1" hl_lines="5"
    <com.rubensousa.dpadrecyclerview.DpadRecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:spanCount="5" />
    ```

=== "Kotlin"

    ```kotlin
    recyclerView.setSpanCount(5)
    ```


### Different span sizes

To customise the size of each span, use `DpadSpanSizeLookup`.

This example would create a full size header for the item at the first position:

```kotlin linenums="1"
recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        return if (position == 0) {
            recyclerView.getSpanCount()
        } else {
            1
        }
    }
})
```

## Extra layout space

`DpadRecyclerView` won't layout any extra space by default, however, 
you might want to create extra views in case you're aligning items to an edge.

The example below would create half a page of extra items at the start of the layout:

```kotlin linenums="1"
recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
    override fun calculateStartExtraLayoutSpace(state: RecyclerView.State): Int {
        return recyclerView.width / 2
    }
})
```

## 