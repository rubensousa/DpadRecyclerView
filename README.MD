# DpadRecyclerView

A RecyclerView built for Android TV as a replacement for Leanback's BaseGridView.

See the project website for more information: https://rubensousa.github.io/DpadRecyclerView

Motivation for this library: https://rubensousa.com/2022/11/08/dpadrecyclerview/

## Getting started

Add the following dependency to your app's `build.gradle`:

```groovy
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview:$latestVersion"

// Optional: If you want to use Compose together with DpadRecyclerView
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-compose:$latestVersion"

// Optional: Espresso test helpers for your instrumented tests:
androidTestImplementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-testing:$latestVersion"
```

Check the official website for more information and recipes: https://rubensousa.github.io/DpadRecyclerView

## New Features compared to Leanback's `BaseGridView`

### Layout

- Supports grids with different span sizes
- Supports reverse layout
- XML attributes for easier configuration

### Scrolling and focus

- Supports changing the alignment configuration smoothly
- Supports limiting the number of pending alignments
- Supports non smooth scroll changes
- Supports continuous and circular grid focus

## Features missing from Leanback's `BaseGridView`

- Scrolling in secondary direction
- Disabling recycling of children
- Saving and restoring children states: clients can save and restore children on the appropriate RecyclerView.Adapter callbacks
- `setChildrenVisibility`: clients can do this by iterating over the children

## Sample app

Check the sample app for a complete example of integration of this library.

Nested lists:

[nested_lists.webm](https://user-images.githubusercontent.com/10662096/200195777-e6020eb8-5a85-4254-82e2-f2f0cc503406.webm)


Grid with different span sizes and sub focus positions:

[grid_span_different.webm](https://user-images.githubusercontent.com/10662096/200195709-203b78ec-873c-4e21-bab4-7cd8dede808e.webm)

## License

    Copyright 2023 Rúben Sousa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

