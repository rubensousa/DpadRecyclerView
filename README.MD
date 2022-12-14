# DpadRecyclerView

A RecyclerView built for Android TV as a replacement for Leanback's BaseGridView.

## Install

```groovy
 implementation 'com.rubensousa.dpadrecyclerview:dpadrecyclerview:1.0.0-alpha02'

// Optional Espresso test extensions
androidTestImplementation 'com.rubensousa.dpadrecyclerview:dpadrecyclerview-testing:1.0.0-alpha02'
```

## Feature comparison with Leanback's BaseGridView

| Feature                           | BaseGridView | DpadRecyclerView |
|-----------------------------------|--------------|------------------|
| Customize View alignment          | ✅            | ✅                |
| Sub position focus                | ✅            | ✅                |
| Smooth view alignment changes     | ❌            | ✅                |
| Multiple span sizes               | ❌            | ✅                |
| Change orientation at runtime     | ❌            | ✅                |
| Disabling smooth scrolling        | ❌            | ✅                |
| Circular grid focus               | ❌            | ✅                |
| Continuous grid focus             | ❌            | ✅                |
| XML attributes for configuration  | ❌            | ✅                |
| Touch event scrolling             | ✅            | ❌                |
| Item spacing                      | ✅            | ❌                |
| Save children state automatically | ✅            | ❌                |
| setLayoutEnabled                  | ✅            | ❌                |
| setScrollEnabled                  | ✅            | ❌                |
| Scroll in secondary direction     | ✅            | ❌                |


Item spacing wasn't supported on purpose since you can achieve the same with a plain `RecyclerView.ItemDecoration`. Check this library for some standard use cases: https://github.com/rubensousa/Decorator/

Features still planned:

- setLayoutEnabled
- setScrollEnabled
- setSelectedSubPosition with `ViewHolderTask`
- Custom sub position parent alignment with `ViewHolderAlignment`
- State saving for nested lists

## How to use

- Migrating from Leanback's BaseGridView: https://github.com/rubensousa/DpadRecyclerView/wiki/Migrating-from-Leanback's-BaseGridView

I tried to keep the public API of `DpadRecyclerView` really similar to the one of `BaseGridView`. The migration should be pretty straightforward.

- XML Attributes: https://github.com/rubensousa/DpadRecyclerView/wiki/XML-Attributes

- View Alignment: https://github.com/rubensousa/DpadRecyclerView/wiki/Customize-View-Alignment

- Compose Interoperability: https://github.com/rubensousa/DpadRecyclerView/wiki/Compose-Interoperability

## Testing

See this for more details: https://github.com/rubensousa/DpadRecyclerView/wiki/Testing

## Sample app

Check the sample app for a complete example of integration of this library.

Nested lists:

[nested_lists.webm](https://user-images.githubusercontent.com/10662096/200195777-e6020eb8-5a85-4254-82e2-f2f0cc503406.webm)


Grid with different span sizes and sub focus positions:


[grid_span_different.webm](https://user-images.githubusercontent.com/10662096/200195709-203b78ec-873c-4e21-bab4-7cd8dede808e.webm)



## License

    Copyright 2022 Rúben Sousa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

