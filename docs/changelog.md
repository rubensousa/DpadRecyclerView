# Changelog

## Version 1.5.0

### 1.5.0-alpha01

2025-06-28

#### Bug fixes

- Fixed scrolling issue when extra layout space is used ([#309](https://github.com/rubensousa/DpadRecyclerView/pull/309))

#### New features

- The new `setFocusSearchDebounceMs` allows throttling focus searches based on the time elapsed between different key events ([#308](https://github.com/rubensousa/DpadRecyclerView/pull/308))

#### API changes

- Deprecated `setSmoothScrollMaxPendingAlignments` in favor of the new `setFocusSearchDebounceMs` 
since there was an issue with controlling the scroll speed near the edges of the list.
- Deprecated `DpadComposeViewHolder` in favor of `DpadComposeFocusViewHolder` to always hold focus inside Compose.
- Removed `RecyclerViewCompositionStrategy` since it wasn't needed since a few releases ago due to changes in `RecyclerView`.

## Version 1.4.3

2025-05-07

#### Bug fixes

- Fixed grids not aligning to min edge: ([#302](https://github.com/rubensousa/DpadRecyclerView/pull/302))

## Version 1.4.2

2025-03-30

#### Bug fixes

- Fixed -1 being sent to `DpadSpanSizeLookup.getSpanSize` when views are removed from the adapter: ([#298](https://github.com/rubensousa/DpadRecyclerView/pull/298))

## Version 1.4.1

2025-02-11

#### Bug fixes

- Fixed layout inconsistency when scrolling if `Edge.Min` is used for parent alignment. ([#291](https://github.com/rubensousa/DpadRecyclerView/pull/291))

#### API changes

- Breaking change: Now DpadRecyclerView is passed in `OnViewHolderSelectedListener` and `OnChildLaidOutListener` instead of plain `RecyclerView`.

## Version 1.4.0

### 1.4.0

2024-12-18

- No changes since `1.4.0-rc02`

#### Important changes since 1.3.0

- Added `setItemSpacing`, `setItemEdgeSpacing` and other spacing setters to `DpadRecyclerView`. Documentation available [here](recipes/spacing.md).
- Added `onViewHolderDeselected` to `OnViewHolderSelectedListener`
- Added `OnFocusLostListener` to observe focus losses of `DpadRecyclerView`
- Added `DpadRecyclerView.setAlignmentLookup` to customize the alignment for individual positions and bypassing the default alignment configurations
- Improved `Modifier.dpadClickable` to support long clicks
- New `DpadScrollableLayout` for screens that need a scrollable header at the same level as a `DpadRecyclerView`
- Testing: Added `KeyEvents.longClick` to trigger long clicks from tests

### 1.4.0-rc02

2024-11-15

#### Bug fixes

- Fixed concurrent modification exception when some listeners remove themselves: ([#285](https://github.com/rubensousa/DpadRecyclerView/pull/285))
- Fixed child layout listener not being removed: ([#285](https://github.com/rubensousa/DpadRecyclerView/pull/285))

### 1.4.0-rc01

2024-11-11

#### Bug fixes

- Fixed selection being reset if it's requested immediately after updating the adapter: ([#283](https://github.com/rubensousa/DpadRecyclerView/pull/283))

### 1.4.0-beta03

2024-10-17

#### New features

- Added `onViewHolderDeselected` to `OnViewHolderSelectedListener` ([#280](https://github.com/rubensousa/DpadRecyclerView/pull/280))

#### API changes

- Added `addOnChildLaidOutListener`, `removeOnChildLaidOutListener` and `clearOnChildLaidOutListeners` 
to replace `setOnChildLaidOutListener` ([#280](https://github.com/rubensousa/DpadRecyclerView/pull/280))

#### Bug fixes

- Fixed focus not leaving `DpadRecyclerView` in some cases: ([#278](https://github.com/rubensousa/DpadRecyclerView/pull/278))

### 1.4.0-beta02

2024-10-08

#### Bug fixes

- Fixed focus order not being consistent in some cases: ([#275](https://github.com/rubensousa/DpadRecyclerView/pull/275))

### 1.4.0-beta01

2024-10-05

#### Bug fixes

- Do not allow scrolling more than the layout size when `AlignmentLookup` is used: ([#270](https://github.com/rubensousa/DpadRecyclerView/pull/270))

### 1.4.0-alpha06

2024-09-05

#### New features

- Added `setItemSpacing`, `setItemEdgeSpacing` and other spacing setters to `DpadRecyclerView` ([#263](https://github.com/rubensousa/DpadRecyclerView/pull/263))
  Documentation available [here](recipes/spacing.md).

- Added independent min and max edge spacings to `DpadLinearSpacingDecoration` and `DpadGridSpacingDecoration` ([#263](https://github.com/rubensousa/DpadRecyclerView/pull/263))

#### API changes

- Now `DpadDragHelper` exposes `fromUser` in `onDragStopped`, which allows distinguishing between user or programmatic stop requests ([#264](https://github.com/rubensousa/DpadRecyclerView/pull/264))


### 1.4.0-alpha05

2024-08-30

**Note**: This version includes an update to `androidx.recyclerview` 1.4.0-beta01, which requires compile SDK set to 35

#### New features

- Added `OnFocusLostListener` to observe focus losses of `DpadRecyclerView` ([#258](https://github.com/rubensousa/DpadRecyclerView/pull/258))
- Added `dpadRecyclerView.setAlignmentLookup` to customize the alignment for individual positions ([#259](https://github.com/rubensousa/DpadRecyclerView/pull/259))

#### Compose

- Improved `Modifier.dpadClickable` to support long clicks ([#261](https://github.com/rubensousa/DpadRecyclerView/pull/261))
- Fix `Modifier.dpadClickable` sending FOCUS_ENTER after the click ([#261](https://github.com/rubensousa/DpadRecyclerView/pull/261))

#### Testing

- Added `KeyEvents.longClick` to trigger long clicks from tests ([#261](https://github.com/rubensousa/DpadRecyclerView/pull/261))

#### Bug fixes

- Fix item decorations not being updated whenever the adapter contents change ([#260](https://github.com/rubensousa/DpadRecyclerView/pull/260))

### 1.4.0-alpha04

2024-08-21

#### Bug fixes

- Fix header alignment when `DpadScrollableLayout` triggers a new layout ([#255](https://github.com/rubensousa/DpadRecyclerView/pull/255))

### 1.4.0-alpha03

2024-08-14

#### Bug fixes

- Fix header not being initially shown sometimes with `DpadScrollableLayout` ([#253](https://github.com/rubensousa/DpadRecyclerView/pull/253))


### 1.4.0-alpha02

2024-08-14

#### Bug fixes

- Fix issue with header alignment when `DpadScrollableLayout` receives a layout request during scroll ([#251](https://github.com/rubensousa/DpadRecyclerView/pull/251))

### 1.4.0-alpha01

2024-08-12

#### New features

- `DpadScrollableLayout` for screens that need a scrollable header at the same level as a `DpadRecyclerView` ([#248](https://github.com/rubensousa/DpadRecyclerView/pull/248))

#### Bug fixes

- Fixed crash when the layout manager name is set via XML ([#247](https://github.com/rubensousa/DpadRecyclerView/pull/247))

## Version 1.3.0

### 1.3.0

2024-08-08

#### Important changes since 1.2.0

- Added `DpadComposeFocusViewHolder` that allows sending the focus state down to Composables
- Added `Modifier.dpadClickable` for playing the click sound after clicking on a Composable. 
- Allow skipping layout requests during scroll with `setLayoutWhileScrollingEnabled(false)`
- New `addOnViewFocusedListener` to observe focus changes independently from selection changes.
- Added `DpadDragHelper` for drag and drop support. Documentation available [here](recipes/dragdrop.md).
- Now `recyclerView.setFocusableDirection(FocusableDirection.CIRCULAR)` can also be used in linear layouts that don't fill the entire space.
- Added `DpadStateRegistry` that assists in saving and restoring the scroll state or view state of ViewHolders

### 1.3.0-rc03

2024-07-23

#### Bug fixes

- Fixed layout inconsistency after some predictive animations ([#240](https://github.com/rubensousa/DpadRecyclerView/pull/240))
- Fixed rare crash when removing an item due to adapter changes while a scroll action is pending ([#240](https://github.com/rubensousa/DpadRecyclerView/pull/240))

### 1.3.0-rc02

2024-07-11

#### Bug fixes

- Fixed `DpadRecyclerView` not immediately stopping the search for the pivot in some scenarios ([#238](https://github.com/rubensousa/DpadRecyclerView/pull/238))

### 1.3.0-rc01

2024-07-08

- Reverted default of `setLayoutWhileScrollingEnabled()` back to true.

### 1.3.0-beta02

2024-06-20

#### Bug fixes

- Fixed `OnViewFocusedListener` not working correctly for a parent RecyclerView when a nested RecyclerView doesn't have a listener registered ([#229](https://github.com/rubensousa/DpadRecyclerView/pull/229))
- Fixed `DpadRecyclerView` losing focus when adapter is cleared ([#232](https://github.com/rubensousa/DpadRecyclerView/pull/232))

### 1.3.0-beta01

2024-06-17

#### Dependency updates

- Updated library to Kotlin 2.0
- Updated Compose ui libraries to `1.7.0-beta03`

#### New Features

- Added `DpadDragHelper` for drag and drop support ([#216](https://github.com/rubensousa/DpadRecyclerView/pull/216)). Documentation available [here](recipes/dragdrop.md).
- Now `recyclerView.setFocusableDirection(FocusableDirection.CIRCULAR)` can also be used in linear layouts that don't fill the entire space. ([#225](https://github.com/rubensousa/DpadRecyclerView/pull/225)

#### Improvements

- Now `focusOutFront` and `focusOutBack` are enabled by default due to feedback from library users ([#223](https://github.com/rubensousa/DpadRecyclerView/pull/223))
- Improved focus behavior for grids with uneven spans that have incomplete rows ([#224](https://github.com/rubensousa/DpadRecyclerView/pull/224))

### 1.3.0-alpha04

2024-06-04

#### New Features

- Added `DpadSelectionSnapHelper` to improve selection on touch events. ([#215](https://github.com/rubensousa/DpadRecyclerView/pull/215/files))
- Added `isFocusable` to `DpadComposeFocusViewHolder` to allow disabling focus for some items. 

#### Bug fixes

- Fixed initial selection being always at position 0, even when that view is not focusable.
- Fixed some rows not receiving focus in grids using `DpadSpanSizeLookup`. ([#217](https://github.com/rubensousa/DpadRecyclerView/issues/217))
- Fixed fast scrolling not working correctly in some grids using `DpadSpanSizeLookup`. ([#218](https://github.com/rubensousa/DpadRecyclerView/issues/218))

### 1.3.0-alpha03

2024-05-31

#### New Features

- Added `DpadStateRegistry` that assists in saving and restoring the scroll state or view state of ViewHolders ([#45](https://github.com/rubensousa/DpadRecyclerView/issues/45))

#### Improvements

- Disable layout passes during scroll events by default. This is an attempt to fix ([#207](https://github.com/rubensousa/DpadRecyclerView/issues/207))
  To fallback to the previous behavior, please use `setLayoutWhileScrollingEnabled(true)`

#### Bug fixes

- Fixed issue with grid layouts with different spans after item removals. ([#210](https://github.com/rubensousa/DpadRecyclerView/issues/210))
- Fixed `DpadRecyclerView` losing focus in some cases when adapter contents are updated during scroll events. ([#206](https://github.com/rubensousa/DpadRecyclerView/issues/206))

### 1.3.0-alpha02

2024-03-23

#### Bug fixes

- Fixed grids not aligning to the keyline for the last row in some cases. ([#203](https://github.com/rubensousa/DpadRecyclerView/issues/203))

### 1.3.0-alpha01

2024-03-17

#### New Features

- Added `DpadComposeFocusViewHolder` that allows sending the focus state down to Composables ([#193](https://github.com/rubensousa/DpadRecyclerView/issues/193))
- Added `Modifier.dpadClickable` for playing the click sound after clicking on a Composable. Fix for: ([/b/268268856](https://issuetracker.google.com/issues/268268856))
- Allow skipping layout requests during scroll with `setLayoutWhileScrollingEnabled(false)` ([#196](https://github.com/rubensousa/DpadRecyclerView/issues/196))
- New `addOnViewFocusedListener` to observe focus changes independently from selection changes. ([#197](https://github.com/rubensousa/DpadRecyclerView/issues/197))

#### API Changes

- `DpadAbstractComposeViewHolder` is now removed. Replace it with either `DpadComposeFocusViewHolder` or `DpadComposeViewHolder`.

## Version 1.2.0

### 1.2.0

2024-03-13

- No changes since 1.2.0-rc01

#### Important changes since 1.1.0

- Added new `RecyclerViewCompositionStrategy.DisposeOnRecycled` for compose interop
  to re-use compositions when views are detached and attached from the window again.
- Added new `setSelectedSubPosition` that allows passing a callback for the target alignment ([#43](https://github.com/rubensousa/DpadRecyclerView/issues/43))
- Added support for scrollbars
- Added `DpadScroller` for scrolling without any alignment. Typical use case is for long text displays (terms & conditions and consent pages).

### 1.2.0-rc01

2024-02-03

#### Bug fixes

- Fixed focus being sent to the wrong item when scrolling with touch events before pressing a key ([#188](https://github.com/rubensousa/DpadRecyclerView/issues/188))

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
