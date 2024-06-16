# DpadRecyclerView

A RecyclerView built for Android TV as a replacement for [Leanback's](https://developer.android.com/jetpack/androidx/releases/leanback) BaseGridView.

See the project website for more information: https://rubensousa.github.io/DpadRecyclerView

Why should you use this library?

1. Leanback hasn't received any significant update for years
2. Compose support for TV is still in its early stages
3. RecyclerView is stable and works well with Compose
4. You need to maintain an existing TV app and wish to introduce Compose in an incremental way
5. Contains useful Espresso testing helpers for your TV UI tests
6. More feature complete:

| Feature                          | DpadRecyclerView | Leanback | Compose TV |
|----------------------------------|------------------|----------|------------|
| Custom scrolling speeds          | ✅                | ✅        | ❌          |
| Edge alignment preference        | ✅                | ✅        | ❌          |
| Sub position selections          | ✅                | ✅        | ❌          |
| Fading edges                     | ✅                | ✅        | ❌          |
| Alignment listener               | ✅                | ✅        | ❌          |
| Grids with uneven span sizes     | ✅                | ❌        | ✅          |
| Extra layout space               | ✅                | ❌        | ✅          |
| Prefetching upcoming items       | ✅                | ❌        | ✅          |
| Reverse layout                   | ✅                | ❌        | ✅          |
| Testing library                  | ✅                | ❌        | ✅          |
| Scrollbars                       | ✅                | ❌        | ❌          |
| Drag and Drop                    | ✅                | ❌        | ❌          |
| Infinite layout with loop        | ✅                | ❌        | ❌          |
| Smooth alignment changes         | ✅                | ❌        | ❌          |
| Child focus observer             | ✅                | ❌        | ❌          |
| Circular and continuous focus    | ✅                | ❌        | ❌          |
| Throttling scroll events         | ✅                | ❌        | ❌          |
| Scrolling without animation      | ✅                | ❌        | ❌          |
| Scrolling in secondary directory | ❌                | ✅        | ❌          |


Background story for this library is available in my [blog](https://rubensousa.com/2022/11/08/dpadrecyclerview/) in case you're interested.

Check the sample app for a complete example of integration of this library


![sample](https://github.com/rubensousa/DpadRecyclerView/blob/master/assets/sample_cover.png?raw=true)

## Getting started

Add the following dependency to your app's `build.gradle`:

```groovy
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview:$latestVersion"

// Recommended: To use Compose together with DpadRecyclerView
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-compose:$latestVersion"

// Optional: Espresso test helpers for your instrumented tests:
androidTestImplementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-testing:$latestVersion"
```

Check the official website for more information and recipes: https://rubensousa.github.io/DpadRecyclerView

## Easier Compose Integration

Documentation: https://rubensousa.github.io/DpadRecyclerView/compose/

```kotlin
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

## Sample app

Nested lists:

![nested_lists](https://github.com/rubensousa/DpadRecyclerView/blob/master/assets/sample_nested_lists.png?raw=true)

[focus_overlay.webm](https://user-images.githubusercontent.com/10662096/219993088-c292de23-0c65-48fb-bde0-ea5ddf9b3bd4.webm)

Grid with different span sizes:

![grid](https://github.com/rubensousa/DpadRecyclerView/blob/master/assets/sample_grid.png?raw=true)


## License

    Copyright 2024 Rúben Sousa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

