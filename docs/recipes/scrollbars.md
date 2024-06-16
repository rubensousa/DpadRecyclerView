# Scrollbar Recipe

Scrollbars are composed of two parts:

1. Thumb: the main indicator for the current scroll position
2. Track: the background of the scrollbar


!!! note
    Unfortunately, scrollbars can only be defined in XML. You can create a style for re-usability.

## Defining scrollbar appearance

Thumb example:

```xml linenums="1"
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <corners android:radius="4dp" />
    <solid android:color="@color/white" />

</shape>
```

Track example:

```xml linenums="1"
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <solid android:color="@color/black" />

</shape>

```

## `DpadRecyclerView` scrollbar configuration

To apply the above scrollbar appearance, place this in your `DpadRecyclerView` XML definition

```xml linenums="1" hl_lines="6-8"
<com.rubensousa.dpadrecyclerview.DpadRecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbarSize="6dp"
    android:scrollbarThumbVertical="@drawable/scrollbar_thumb"
    android:scrollbarTrackVertical="@drawable/scrollbar_track"
    android:scrollbars="vertical" />
```

!!! note
    Replace "Vertical" with "Horizontal" for horizontal scrollbars


By default, the scrollbar is only shown after scrolling and then disappears. 
You can force it to always appear by using this attribute:

```xml linenums="1"
<com.rubensousa.dpadrecyclerview.DpadRecyclerView
    android:scrollbarAlwaysDrawVerticalTrack="true" />
```

