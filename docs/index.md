# DpadRecyclerView

A RecyclerView built for Android TV as a replacement for [Leanback's](https://developer.android.com/jetpack/androidx/releases/leanback) BaseGridView.

Proceed to [Getting started](getting_started.md) to start adding `DpadRecyclerView`
to your application.

Motivation for this library is available in my [blog](https://rubensousa.com/2022/11/08/dpadrecyclerview/) in case you're interested.

## New Features compared to Leanback's `BaseGridView`

### Layout

- Grids with different span sizes
- Reverse layout
- XML attributes for easier configuration

### Scrolling and focus

- Changing the alignment configuration smoothly
- Limiting the number of pending alignments
- Non smooth scroll changes
- Continuous and circular grid focus


### Upcoming features

- [Looping adapter contents](https://github.com/rubensousa/DpadRecyclerView/issues/20)
- [State saving/restoring helper](https://github.com/rubensousa/DpadRecyclerView/issues/45)
- [Drag and drop helper](https://github.com/rubensousa/DpadRecyclerView/issues/12)


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
