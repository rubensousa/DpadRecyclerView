# Drag and Drop Recipe

Sometimes, users need to arrange the order of some collection. 
The APIs mentioned here should assist you in developing such a feature.

## Make your adapter mutable

`DpadDragHelper` requires a `DpadDragHelper.DragAdapter<T>` that exposes the mutable collection backing the adapter contents.
This allows `DpadDragHelper` to change the order of the elements for you automatically.

You just need to implement `DpadDragHelper.DragAdapter<T>` for this step:


```kotlin  linenums="1" hl_lines="8"
class ExampleAdapter(
    private val adapterConfig: AdapterConfig
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), 
    DpadDragHelper.DragAdapter<Int> {
    
    private val items = ArrayList<Int>()
    
    override fun getMutableItems(): MutableList<Int> = items

```

## Create a `DpadDragHelper`

Now that you have a `DragAdapter` setup, just create a `DpadDragHelper` like so:

```kotlin linenums="1"
private val adapter = ExampleAdapter()
private val dragHelper = DpadDragHelper(
    adapter = dragAdapter,
    callback = object : DpadDragHelper.DragCallback {
        override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
            // ViewHolder is now being dragged
        }
        override fun onDragStopped(fromUser: Boolean) {
            // Dragging was cancelled either by user or programmatically
        }
    }
)

```

Then attach it to your `DpadRecyclerView`:

```kotlin
dragHelper.attachToRecyclerView(dpadRecyclerView)
``` 

!!! note
    This only supports drag and drop for linear and grid layouts with the same number of spans.


## Start and stop dragging

Now that `DpadDragHelper` is setup, you can start dragging by using:

```kotlin
dragHelper.startDrag(position = 0)
```

If the position passed in the method above is not currently selected, a selection will be triggered.

To cancel dragging for any reason, use:

```kotlin
dragHelper.stopDrag()
```

!!! note
    Users can also stop dragging by pressing the following keys: `KeyEvent.KEYCODE_DPAD_CENTER`,
    `KeyEvent.KEYCODE_ENTER`. These are customizable in the constructor of `DpadDragHelper`
