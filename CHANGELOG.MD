# 1.0.0-alpha03

### New
- Replaced `DpadLayoutManager` with new `PivotLayoutManager` for proper customization of layout logic ([#10](https://github.com/rubensousa/DpadRecyclerView/issues/10)), ([#16](https://github.com/rubensousa/DpadRecyclerView/issues/16))
- Added support for findFirstVisibleItemPosition and findLastVisibleItemPosition ([#23](https://github.com/rubensousa/DpadRecyclerView/issues/23))
- Added support for recycling children on detach ([#17](https://github.com/rubensousa/DpadRecyclerView/issues/17))

# 1.0.0-alpha02

- Allow extending from `DpadRecyclerView`
- Removed `RecyclerView.canScrollHorizontally` and `RecyclerView.canScrollVertically` since they're not used and clients can create them themselves

# 1.0.0-alpha01

- Initial alpha release