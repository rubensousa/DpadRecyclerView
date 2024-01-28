# Changelog

## Version 1.2.0

### 1.2.0-beta02

2024-01-28

#### New Features

- Added new `RecyclerViewCompositionStrategy.DisposeOnRecycled` for compose interop 
to re-use compositions when views are detached and attached from the window again.

#### Bug fixes

- Fixed views not being laid out correctly sometimes when `setRecycleChildrenOnDetach` is used.

### 1.2.0-beta01

2024-01-17

#### New Features

- Added new `setSelectedSubPosition` that allows passing a callback for the target alignment ([#43](https://github.com/rubensousa/DpadRecyclerView/issues/43))

#### Bug fixes

- Fixed navigation sound not being played when searching for pivot ([#167](https://github.com/rubensousa/DpadRecyclerView/issues/167))
- Fixed sub selection not being dispatched when used multiple times in a row

### 1.2.0-alpha03

2024-01-10

#### Bug fixes

- Fixed crash when requesting layout after a selection to an index out of bounds ([#178](https://github.com/rubensousa/DpadRecyclerView/issues/178))

### 1.2.0-alpha02

2023-12-27

#### Bug fixes

- Fixed layout jumping to the top when the user scrolls with touch events ([#171](https://github.com/rubensousa/DpadRecyclerView/issues/171))
- Fixed alignment configuration not being respected for small lists ([#172](https://github.com/rubensousa/DpadRecyclerView/issues/172))

### 1.2.0-alpha01

2023-11-25

#### New Features

- Added support for scrollbars
- Added `DpadScroller` for scrolling without any alignment. Typical use case is for long text displays (terms & conditions and consent pages).

## Version 1.1.0

### 1.1.0

2023-11-12

- No changes since 1.1.0-rc01

### 1.1.0-rc01

2023-10-18

#### Dependency updates

- Updated `androidx.recyclerview` to version `1.3.2` to fix sporadic crashes during animations: ([/9e69afd](https://android.googlesource.com/platform/frameworks/support/+/9e69afd7854894754df62143d0b3c2f8be9ddaa0))
- Updated `androidx.collection` to stable version `1.3.0`

### 1.1.0-beta02

2023-09-15

#### Bug fixes

- Fixed wrong scrolling behavior when the `app:spanCount` attribute is used for grids ([#162](https://github.com/rubensousa/DpadRecyclerView/issues/162))

### 1.1.0-beta01

2023-09-10

#### Bug fixes

- Fixed wrong scrolling behavior when the layout isn't completely filled and `Edge.MAX` alignment is used: ([#160](https://github.com/rubensousa/DpadRecyclerView/issues/160))
- Fixed XML attribute `app:dpadRecyclerViewParentAlignmentPreferKeylineOverEdge` not being applied correctly

### 1.1.0-alpha03

2023-08-04

#### Bug fixes

- Fixed grid layout not placing views in the correct spans when scrolling in opposite direction: ([#156](https://github.com/rubensousa/DpadRecyclerView/issues/156))

### 1.1.0-alpha02

2023-06-23

#### Bug fixes

- Fixed crash when updating adapter contents too frequently: ([#153](https://github.com/rubensousa/DpadRecyclerView/issues/153))

### 1.1.0-alpha01

2023-06-08

#### New Features

- Added support for endless scrolling with looping adapter contents: ([#20](https://github.com/rubensousa/DpadRecyclerView/issues/20)).
  Check [this](recipes/layout.md#looping-adapter-contents) for more information.


## Version 1.0.0

### 1.0.0

2023-05-16

#### New Features

- Added support for API 19 ([#146](https://github.com/rubensousa/DpadRecyclerView/issues/146))
- Added missing XML attribute for parent alignment `app:dpadRecyclerViewParentAlignmentPreferKeylineOverEdge` ([#145](https://github.com/rubensousa/DpadRecyclerView/issues/145))

#### Bug fixes

- Fixed crash when R8 is applied ([#122](https://github.com/rubensousa/DpadRecyclerView/issues/122))

### 1.0.0-rc01

2023-05-07

#### Dependency updates

- Removed consumer proguard rules
- Exposed recyclerview dependency to clients

### 1.0.0-beta03

2023-05-03

#### Dependency updates

- Updated compose-ui to version `1.4.2`

#### New Features 

- Added `getSpanSizeLookup()` to `DpadRecyclerView`
- Added `onViewHolderSelectedAndAligned` to `DpadViewHolder`

#### Compose

- Added `DpadAbstractComposeViewHolder` to allow subclasses to get access to focus changes. Check [Compose interoperability](compose.md) for more information.


#### Testing

See the documentation [here](testing.md)

- Added `KeyEvents.back()` to easily press back key events
- Added `DpadRecyclerViewActions.scrollTo` and `DpadRecyclerViewActions.scrollToHolder` to scroll to specific ViewHolders using KeyEvents.
- Added `DpadViewAssertions` for asserting focus states:
    - `DpadViewAssertions.hasFocus()`
    - `DpadViewAssertions.doesNotHaveFocus()`
    - `DpadViewAssertions.isFocused()`
    - `DpadViewAssertions.isNotFocused()`


### 1.0.0-beta02

2023-04-18

#### Dependency updates

- Updated RecyclerView to stable version `1.3.0`

#### New Features

- Added support for fading edges. Check [this](migrating_leanback.md#fading-edges) for more information. ([#18](https://github.com/rubensousa/DpadRecyclerView/issues/18)).

#### Bug fixes

- Fixed `DpadRecyclerView` not measuring itself correctly when `wrap_content` is used. ([#123](https://github.com/rubensousa/DpadRecyclerView/issues/123))
- Fixed max edge alignment not working correctly when scrolling back. ([#124](https://github.com/rubensousa/DpadRecyclerView/issues/124))

### 1.0.0-beta01

2023-04-02

#### Bug fixes

- Fixed crash in grid layout when pivot is updated during pagination. ([#114](https://github.com/rubensousa/DpadRecyclerView/issues/114))

### 1.0.0-alpha05

2023-02-21

#### New Features

- New `dpadrecyclerview-compose` module that contains `DpadComposeViewHolder` for Compose interoperability. Check [Compose interoperability](compose.md) for more information.
- Allow setting 0 max pending moves with `setSmoothScrollMaxPendingMoves(0)` to fully prevent unwanted scroll events.

#### API changes

- `offsetRatio` in `ParentAlignment`, `ChildAlignment` and `SubPositionAlignment`  was renamed to `fraction`.

#### Bug fixes

- Fixed alignment issue when the first item has decoration ([#91](https://github.com/rubensousa/DpadRecyclerView/issues/91))
- Fixed alignment issue when `Edge.MIN` is used and there's a small number of adapter items ([#93](https://github.com/rubensousa/DpadRecyclerView/issues/93))
- Fixed focus changing to the incorrect span in a grid ([#96](https://github.com/rubensousa/DpadRecyclerView/issues/96))
- Fixed focus being lost when scrolling a grid and triggering layout passes at the same time ([#102](https://github.com/rubensousa/DpadRecyclerView/issues/102))
- Fixed `DpadRecyclerView` scrolling automatically to the current selected position when there's a touch event ([#104](https://github.com/rubensousa/DpadRecyclerView/issues/104))
- Fixed pending alignments in opposite direction not being ignored ([#106](https://github.com/rubensousa/DpadRecyclerView/issues/106))

#### Testing

- New `KeyEvents.click()` to easily dispatch click events in UI tests

### 1.0.0-alpha04

2023-02-13

#### New Features

- Added `DpadLinearSpacingItemDecoration` and `DpadGridSpacingItemDecoration` to easily set spacing between items
- Added `setScrollEnabled` to enable or disable scroll events
- Added `setLayoutEnabled` to enable or disable the layout of children
- Added support for reverse layout via `setReverseLayout`
- Restored support for basic touch event scrolling without triggering selection or alignment changes
- Added support for item prefetching for nested RecyclerViews
- `setSmoothScrollMaxPendingAlignments` allows limiting the number of scroll changes still not applied
- `setSmoothScrollMaxPendingMoves` allows remembering DPAD events not yet applied
- Improved fast smooth scrolling in grids with different span sizes
- Added `setOnChildLaidOutListener` to observe when each view is laid out
- `setFocusSearchEnabledDuringAnimations` allows enabling or disabling focus changes during item animations.

#### API Changes

- `ViewHolderAlignment` was renamed  to `SubPositionAlignment`. Now `DpadViewHolder` returns these alignments in `getSubPositionAlignments`.

#### Bug fixes

- Fixed `DpadRecyclerView` not searching for focus if it's currently retaining focus due to a pivot removal. This prevented accumulating pending moves during smooth scrolling.
- A nested `DpadRecyclerView` now won't search for focus if its parent `RecyclerView` is still smooth scrolling ([#50](https://github.com/rubensousa/DpadRecyclerView/issues/50))
- Alignment not being restored correctly for small lists ([#71](https://github.com/rubensousa/DpadRecyclerView/issues/71))
- Unnecessary `ItemAnimator` animations were running when scrolling during item changes ([#47](https://github.com/rubensousa/DpadRecyclerView/issues/47))
- a11y scroll actions were not triggering scroll events ([#66](https://github.com/rubensousa/DpadRecyclerView/issues/66))
- Circular focus not working correctly for the first row ([#37](https://github.com/rubensousa/DpadRecyclerView/issues/37))

#### Testing

- `DisableIdleTimeoutRule` will now wait for idle input after the test is over to avoid dispatching key events to other tests

### 1.0.0-alpha03

2023-01-26

#### New Features

- Added support for findFirstVisibleItemPosition and findLastVisibleItemPosition ([#23](https://github.com/rubensousa/DpadRecyclerView/issues/23))
- Added support for recycling children on detach ([#17](https://github.com/rubensousa/DpadRecyclerView/issues/17))

#### API Changes
- Replaced `DpadLayoutManager` with new `PivotLayoutManager` for proper customization of layout logic ([#10](https://github.com/rubensousa/DpadRecyclerView/issues/10)), ([#16](https://github.com/rubensousa/DpadRecyclerView/issues/16))


### 1.0.0-alpha02

2022-12-10

#### API Changes
- Allow extending from `DpadRecyclerView`
- Removed `RecyclerView.canScrollHorizontally` and `RecyclerView.canScrollVertically` since they're not used and clients can create them themselves

### 1.0.0-alpha01 

2022-11-06

- Initial alpha release