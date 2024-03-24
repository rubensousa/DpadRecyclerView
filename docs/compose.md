# Compose interoperability

The `dpadrecyclerview-compose` module contains the following:

- `DpadComposeFocusViewHolder`: ViewHolder that exposes a function to render a Composable and sends the focus directly to Composables.
- `DpadComposeViewHolder`:  ViewHolder that exposes a function to render a Composable but keeps the focus state in the View system
- `RecyclerViewCompositionStrategy.DisposeOnRecycled`: a custom `ViewCompositionStrategy` that only disposes compositions when ViewHolders are recycled

## Compose ViewHolder

### Receive focus inside Composables

Use `DpadComposeFocusViewHolder` to let your Composables receive the focus state.

```kotlin linenums="1"
class ComposeItemAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<Int, DpadComposeFocusViewHolder<Int>>(Item.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeFocusViewHolder<Int> {
        return DpadComposeFocusViewHolder(parent) { item ->
            ItemComposable(
                item = item,
                onClick = {
                    onItemClick(item)
                }
            )
        }
    }

    override fun onBindViewHolder(
        holder: DpadComposeFocusViewHolder<Int>, 
        position: Int
    ) {
        holder.setItemState(getItem(position))
    }
    
}
```

Then use the standard focus APIs to react to focus changes:

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

### Keep focus inside the view system

If you want to keep the focus inside the View system, use `DpadComposeViewHolder` instead:

```kotlin linenums="1"
class ComposeItemAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<Int, DpadComposeViewHolder<Int>>(Item.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeViewHolder<Int> {
        return DpadComposeViewHolder(
            parent,
            onClick = onItemClick
        ) { item, isFocused ->
            ItemComposable(item, isFocused)
        }
    }

    override fun onBindViewHolder(
        holder: DpadComposeViewHolder<Int>, 
        position: Int
    ) {
        holder.setItemState(getItem(position))
    }
    
}
```

In this case, you receive the focus state as an input that you can pass to your Composables:

```kotlin linenums="1"
@Composable
fun ItemComposable(item: Int, isFocused: Boolean) {
    val backgroundColor = if (isFocused) Color.White else Color.Black
    val textColor = if (isFocused) Color.Black else Color.White
    Box(
        modifier = Modifier.background(backgroundColor),
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

## Handle clicks with sound

Use `Modifier.dpadClickable` instead of `Modifier.clickable` because of this issue: 
[/b/268268856](https://issuetracker.google.com/issues/268268856)

## Performance optimizations

- If you plan to use compose animations, check the performance during fast scrolling and consider throttling key events using the APIs explained [here](recipes/scrolling.md#limiting-number-of-pending-alignments)
- Consider using `dpadRecyclerView.setLayoutWhileScrollingEnabled(false)` to discard layout requests during scroll events.
This will skip unnecessary layout requests triggered by some compose animations.


Check the sample on [Github](https://github.com/rubensousa/DpadRecyclerView/) for more examples that include simple animations.