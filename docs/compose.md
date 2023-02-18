# Compose interoperability

The `dpadrecyclerview-module` contains a `DpadComposeViewHolder`
that you can extend to easily render composables in your `RecyclerView`.

Example: `ItemComposable` that should render a text and different colors based on the focus state

```kotlin linenums="1"
@Composable
fun ItemComposable(
    item: Int, 
    isFocused: Boolean,
    modifier: Modifier = Modifier, 
) {
    val backgroundColor = if (isFocused) {
        Color.White
    } else {
        Color.Black
    }
    val textColor = if (isFocused) {
        Color.Black
    } else {
        Color.White
    }
    Box(
        modifier = modifier.background(backgroundColor),
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

To render `ItemComposable` in a `RecyclerView.Adapter`, just use `DpadComposeViewHolder`:


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
            composable = { item, isFocused, _ ->
                ItemComposable(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(9 / 16f),
                    item = item,
                    isFocused = isFocused
                )
            },
            onClick = onItemClick
        )
    }

    override fun onBindViewHolder(holder: DpadComposeViewHolder<Int>, position: Int) {
        holder.setItemState(getItem(position))
    }
    
}

```

New compositions will be triggered whenever the following happens:

- New item is bound in `onBindViewHolder`
- Focus state changes
- Selection state changes

Check the sample on [Github](https://github.com/rubensousa/DpadRecyclerView/) for more examples that include simple animations.