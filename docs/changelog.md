# Changelog

## 1.0.0-alpha04 (2023-02-??)

### Improvements

### Bug fixes

### Testing

- `DisableIdleTimeoutRule` will now wait for idle input after the test is over to avoid dispatching key events to other tests

## 1.0.0-alpha03 (2023-01-26)

- Replaced `DpadLayoutManager` with new `PivotLayoutManager` for proper customization of layout logic ([#10](https://github.com/rubensousa/DpadRecyclerView/issues/10)), ([#16](https://github.com/rubensousa/DpadRecyclerView/issues/16))
- Added support for findFirstVisibleItemPosition and findLastVisibleItemPosition ([#23](https://github.com/rubensousa/DpadRecyclerView/issues/23))
- Added support for recycling children on detach ([#17](https://github.com/rubensousa/DpadRecyclerView/issues/17))

## 1.0.0-alpha02 (2022-12-10)

- Allow extending from `DpadRecyclerView`
- Removed `RecyclerView.canScrollHorizontally` and `RecyclerView.canScrollVertically` since they're not used and clients can create them themselves

## 1.0.0-alpha01 (2022-11-06)

- Initial alpha release