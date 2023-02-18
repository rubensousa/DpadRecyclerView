# Alignment

This section is meant to be an overview to the concepts of `ParentAlignment` and `ChildAlignment`.

For more detailed examples, check [this](recipes/alignment.md) section.

## Parent Alignment

The parent alignment is the first configuration required for aligning views in `DpadRecyclerView`.
This configuration will take care of calculating the anchor position for your ViewHolders.

Consider the following example for a **vertical** `DpadRecyclerView`:

<img width="600" alt="image" src="https://user-images.githubusercontent.com/10662096/200148271-a0a4cb4c-d134-4d08-a146-f35e468f79dd.png">

The red circle is centered both horizontally and vertically and serves as the anchor for all ViewHolders.

To create this configuration, you would do the following:

```kotlin
ParentAlignment(offsetRatio = 0.5f)
```

You can also create a top anchor:

<img width="600" alt="image" src="https://user-images.githubusercontent.com/10662096/200148312-8f7a698e-226f-467e-8c57-f077839baf2e.png">

In this case, the configuration would be:

```kotlin
ParentAlignment(offset = 16.dp.toPx(), offsetRatio = 0f)
```

Both `offset` and `offsetRatio` start counting from the minimum edge of the `DpadRecyclerView`. For horizontal orientations, this would be the start and for vertical orientation this would be the top.

### Aligning to an edge

By default, the views at the minimum and maximum edges won't be aligned to the keyline position specified by `ParentAlignment`. If you want to change this behavior, you need to change the `edge` argument of `ParentAlignment`:

```kotlin
ParentAlignment(edge = ParentAlignment.Edge.MIN_MAX)
```

* To align every view: `ParentAlignment.Edge.NONE`
* To align every view except the ones at the minimum edge: `ParentAlignment.Edge.MIN`
* To align every view except the ones at the maximum edge: `ParentAlignment.Edge.MAX`
* To align every view except the ones at the minimum edge and maximum edge (default behavior): `ParentAlignment.Edge.MIN_MAX`

## Child Alignment

The `ChildAlignment` class will take care of calculating the anchor position for your `ViewHolder` views.

Consider the following example for a **horizontal** `DpadRecyclerView`:

![Start alignment](img/start_alignment.png)

The blue circle shows the keyline position defined by `ParentAlignment` and the green circles shows the anchor position of each child
defined by `ChildAlignment`.

In this case, the combined configuration would be:

```kotlin
ParentAlignment(offset = 24.dp.toPx(), offsetRatio = 0f)
ChildAlignment(offsetRatio = 0f)
```

## Examples

For more detailed examples, check the [recipes section](recipes/alignment.md).