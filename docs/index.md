# DpadRecyclerView

A RecyclerView built for Android TV as a replacement
for [Leanback's](https://developer.android.com/jetpack/androidx/releases/leanback) BaseGridView.

Proceed to [Getting started](getting_started.md) to start adding `DpadRecyclerView`
to your application.

Why should you use this library?

1. Leanback hasn't received any significant update for years
2. Compose support for TV is still in its early stages
3. RecyclerView is stable and works well with Compose
4. You need to maintain an existing TV app and wish to introduce Compose in an incremental way
5. Contains useful Espresso testing helpers for your TV UI tests
6. More feature complete:

| Feature                           | DpadRecyclerView | Leanback | Compose TV |
|-----------------------------------|------------------|----------|------------|
| Custom scrolling speeds           | ✅                | ✅        | ❌          |
| Edge alignment preference         | ✅                | ✅        | ❌          |
| Sub position selections           | ✅                | ✅        | ❌          |
| Fading edges                      | ✅                | ✅        | ❌          |
| Alignment listener                | ✅                | ✅        | ❌          |
| Grids with uneven span sizes      | ✅                | ❌        | ✅          |
| Extra layout space                | ✅                | ❌        | ✅          |
| Prefetching upcoming items        | ✅                | ❌        | ✅          |
| Reverse layout                    | ✅                | ❌        | ✅          |
| Testing library                   | ✅                | ❌        | ✅          |
| Scrollbars                        | ✅                | ❌        | ❌          |
| Drag and Drop                     | ✅                | ❌        | ❌          |
| Infinite layout with loop         | ✅                | ❌        | ❌          |
| Smooth alignment changes          | ✅                | ❌        | ❌          |
| Discrete scrolling for text pages | ✅                | ❌        | ❌          |
| Child focus observer              | ✅                | ❌        | ❌          |
| Circular and continuous focus     | ✅                | ❌        | ❌          |
| Throttling scroll events          | ✅                | ❌        | ❌          |
| Scrolling without animation       | ✅                | ❌        | ❌          |
| Scrolling in secondary directory  | ❌                | ✅        | ❌          |


Background story for this library is available in my [blog](https://rubensousa.com/2022/11/08/dpadrecyclerview/) in case you're interested.

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
