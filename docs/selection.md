# Focus and selection

It's important to distinguish between a selection event and a focus event.

You can change the selection state of a `DpadRecyclerView` while focus is in a completely separate view.

However, when focus changes to different ViewHolders inside `DpadRecyclerView` (due to DPAD navigation for example), this will automatically fire a selection event.

So, in short:

1. Selection changes can happen without focus requests
2. Focusing another view triggers a selection event

## Focus changes

To enable focus changes in your `RecyclerView.ViewHolder`, you need to set a child view as focusable. Otherwise, `DpadRecyclerView` won't find it
when the user presses the DPAD.

Do this either in XML or after `onCreateViewHolder`:

```kotlin
view.isFocusable = true
view.isFocusableInTouchMode = true
```

To observe focus changes, simply use a `OnFocusChangeListener`:

```kotlin linenums="1"
view.setOnFocusChangeListener { _, hasFocus ->
    if (hasFocus) {
        // React to focus gain
    } else {
        // React to focus loss
    }
}
```

You can also observe focus changes inside `DpadRecyclerView` with the following:

```kotlin linenums="1"
recyclerView.addOnViewFocusedListener(object : OnViewFocusedListener {
    override fun onViewFocused(parent: RecyclerView.ViewHolder, child: View) {
        // Child has focus
    }
})
```

## Selection changes

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

If your `ViewHolder` implements the interface `DpadViewHolder` you can also get these events automatically from these callbacks:

```kotlin linenums="1"
class ExampleViewHolder(
    view: View
) : RecyclerView.ViewHolder(view), DpadViewHolder {

    override fun onViewHolderSelected() {}

    override fun onViewHolderDeselected() {}

}
```
