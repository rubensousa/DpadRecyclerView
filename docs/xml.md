# XML Attributes

## Alignment

```xml linenums="1"
<attr name="dpadRecyclerViewChildAlignmentOffset" format="dimension" />
<attr name="dpadRecyclerViewChildAlignmentFraction" format="float" />
<attr name="dpadRecyclerViewChildAlignmentFractionEnabled" format="boolean" />
<attr name="dpadRecyclerViewParentAlignmentEdge" format="enum">
    <enum name="none" value="0" />
    <enum name="min" value="1" />
    <enum name="max" value="2" />
    <enum name="min_max" value="3" />
</attr>
<attr name="dpadRecyclerViewParentAlignmentOffset" format="dimension" />
<attr name="dpadRecyclerViewParentAlignmentFraction" format="float" />
<attr name="dpadRecyclerViewParentAlignmentFractionEnabled" format="boolean" />
<attr name="dpadRecyclerViewParentAlignmentPreferKeylineOverEdge" format="boolean" />
```


## Focus

```xml linenums="1"
<attr name="dpadRecyclerViewFocusOutFront" format="boolean" />
<attr name="dpadRecyclerViewFocusOutBack" format="boolean" />
<attr name="dpadRecyclerViewFocusOutSideFront" format="boolean" />
<attr name="dpadRecyclerViewFocusOutSideBack" format="boolean" />
<attr name="dpadRecyclerViewSmoothFocusChangesEnabled" format="boolean" />
<attr name="dpadRecyclerViewFocusableDirection" format="enum">
    <enum name="standard" value="0" />
    <enum name="circular" value="1" />
    <enum name="continuous" value="2" />
</attr>
```

## Layout

These default attributes from other ViewGroups are supported:

```xml linenums="1"
<attr name="spanCount" />
<attr name="reverseLayout" />
<attr name="android:gravity" />
<attr name="android:orientation" />
```

## Styling

You can apply a default style in your theme:

```xml linenums="1"
<style name="Theme.App" parent="Theme.MaterialComponents.NoActionBar">
    <item name="dpadRecyclerViewStyle">@style/DpadRecyclerViewStyle</item>
</style>

<style name="DpadRecyclerViewStyle">
   <!-- Attributes here -->
</style>
```

Or create individual styles:

```xml linenums="1"
<style name="HorizontalStartDpadRecyclerView">
    <item name="android:orientation">horizontal</item>
    <item name="dpadRecyclerViewChildAlignmentOffset">@dimen/list_margin_start</item>
    <item name="dpadRecyclerViewChildAlignmentFraction">0.0f</item>
    <item name="dpadRecyclerViewParentAlignmentEdge">min_max</item>
    <item name="dpadRecyclerViewParentAlignmentFraction">0.0f</item>
</style>

<style name="VerticalCenterDpadRecyclerView">
    <item name="android:orientation">vertical</item>
    <item name="dpadRecyclerViewChildAlignmentFraction">0.5f</item>
    <item name="dpadRecyclerViewParentAlignmentEdge">min_max</item>
    <item name="dpadRecyclerViewParentAlignmentFraction">0.5f</item>
</style>
```