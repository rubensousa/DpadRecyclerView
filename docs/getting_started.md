# Getting started

Add the following dependency to your app's `build.gradle`:

```groovy
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview:{{ dpadrecyclerview.version }}"

// Optional: If you want to use Compose together with DpadRecyclerView
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

Follow the [official RecyclerView guides](https://developer.android.com/develop/ui/views/layout/recyclerview) to render Views on the screen or use any RecyclerView library as you would for mobile apps.

## Recipes

Take a look at the sections inside "Recipes" on this website to customise `DpadRecyclerView` according to your needs.

## Sample

The sample on [Github](https://github.com/rubensousa/DpadRecyclerView) contains a complete example of 
how to use this library. 