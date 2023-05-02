# Compose interoperability

The `dpadrecyclerview-compose` module contains the following:

- `DpadAbstractComposeViewHolder`: ViewHolder that exposes a `Content` function to render a Composable
- `DpadComposeViewHolder`: simple implementation of `DpadAbstractComposeViewHolder` that just forwards a lambda to the `Content` function

You can use these to easily render composables in your `RecyclerView`.

The focus is kept in the `itemView` and not actually sent to the Composables inside due to these issues:

1. Focus is not sent correctly from Views to Composables: [b/268248352](https://issuetracker.google.com/issues/268248352)
2. Clicking on a focused Composable does not trigger the standard audio feedback: [b/268268856](https://issuetracker.google.com/issues/268268856)

!!! note
    If you plan to use compose animations, check the performance during fast scrolling and consider
    throttling key events using the APIs explained [here](recipes/scrolling.md#limiting-number-of-pending-alignments)


## DpadComposeViewHolder
Example: `ItemComposable` that should render a text and different colors based on the focus state

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
        ) { item, isFocused, isSelected ->
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

New compositions will be triggered whenever the following happens:

- New item is bound in `onBindViewHolder`
- Focus state changes
- Selection state changes

## Dpad AbstractComposeViewHolder

Extending from this class directly gives you more flexibility for customizations:

```kotlin linenums="1"
class ComposeItemAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<Int, ComposeItemViewHolder>(Item.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ComposeItemViewHolder {
        return ComposeItemViewHolder(parent, onItemClick)
    }

    override fun onBindViewHolder(
        holder: ComposeItemViewHolder, 
        position: Int
    ) {
        holder.setItemState(getItem(position))
    }
    
    override fun onViewRecycled(holder: ComposeItemViewHolder) {
        holder.onRecycled()
    }
}
```

```kotlin linenums="1"
class ComposeItemViewHolder(
    parent: ViewGroup,
    onClick: (Int) -> Unit
) : DpadAbstractComposeViewHolder<Int>(parent) {

    private val itemAnimator = ItemAnimator(itemView)

    init {
        itemView.setOnClickListener {
            getItem()?.let(onItemClick)
        }
    }

    @Composable
    override fun Content(item: Int, isFocused: Boolean, isSelected: Boolean) {
        ItemComposable(item, isFocused)
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            itemAnimator.startFocusGainAnimation()
        } else {
            itemAnimator.startFocusLossAnimation()
        }
    }
    
    fun onRecycled() {
        itemAnimator.cancel()
    }

}
```

Check the sample on [Github](https://github.com/rubensousa/DpadRecyclerView/) for more examples that include simple animations.