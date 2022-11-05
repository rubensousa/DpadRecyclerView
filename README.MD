# DpadRecyclerView

A RecyclerView built for Android TV as a replacement for Leanback's BaseGridView.

## Install

```groovy
 implementation 'com.rubensousa.dpadrecyclerview:dpadrecyclerview:1.0.0-alpha01'

// Optional Espresso test extensions
androidTestImplementation 'com.rubensousa.dpadrecyclerview:dpadrecyclerview-testing:1.0.0-alpha01'
```

## Feature comparison with Leanback's BaseGridView

| Feature                       | Leanback's BaseGridView | DpadRecyclerView |
|-------------------------------|-------------------------|------------------|
| View alignment                | Supported               | Supported        |
| Smooth view alignment changes | Not Supported           | Supported        |
| Multiple span sizes           | Not Supported           | Supported        |
| Change orientation at runtime | Not Supported           | Supported        |
| Disabling smooth scrolling    | Not Supported           | Supported        |
| Circular focus                | Not supported           | Supported        |
| Continuous focus              | Not supported           | Supported        |
| setLayoutEnabled              | Supported               | Not Supported    |
| setScrollEnabled              | Supported               | Not Supported    |
| Scroll in secondary direction | Supported               | Not Supported    |

Features still planned:

- setLayoutEnabled
- setScrollEnabled
- setSelectedSubPosition with `ViewHolderTask`
- Custom sub position parent alignment with `ViewHolderAlignment`

## Migrating from Leanback

See: https://github.com/rubensousa/DpadRecyclerView/wiki/Migrating-from-Leanback

## XML Attributes

See: https://github.com/rubensousa/DpadRecyclerView/wiki/XML-Attributes

## View Alignment

See: https://github.com/rubensousa/DpadRecyclerView/wiki/Customize-View-Alignment

## Compose Interoperability

See: https://github.com/rubensousa/DpadRecyclerView/wiki/Compose-Interoperability

## Testing

See: https://github.com/rubensousa/DpadRecyclerView/wiki/Testing

## Sample app

Check the sample app for a complete example of integration of the library.

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

