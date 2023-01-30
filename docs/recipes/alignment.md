# Alignment Recipes


## Centering views

Example of centering views in a vertical `DpadRecyclerView`:

<img width="600" alt="image" src="https://user-images.githubusercontent.com/10662096/200148368-8d09094d-0703-466b-9568-cd2c4f81a2e2.png">

```kotlin
ParentAlignment(offsetRatio = 0.5f)
ChildAlignment(offsetRatio = 0.5f)
```

## Including padding in child alignment

In case you want to include padding for the alignment position, set the `includePadding` to true:

```kotlin
ChildAlignment(offsetRatio = 0.0f, includePadding = true)
```

<img width="600" alt="image" src="https://user-images.githubusercontent.com/10662096/200148453-c629fd46-f37a-42c7-91ea-4dc24d7c4c02.png">

Padding will only be considered in the same orientation of the `DpadRecyclerView` and when the ratio is either `0.0f` or `1.0f`:

* start/top padding for horizontal/vertical when `offsetRatio` is 0.0f
* end/bottom padding for horizontal/vertical when `offsetRatio` is 1.0f

## Sub position alignment

You can define custom sub positions for every `ViewHolder` to align its children differently.
Each sub position alignment is essentially an extension of `ChildAlignment`.

<img width="600" alt="image" src="https://user-images.githubusercontent.com/10662096/200148760-7af0b610-b24e-4b9f-9c4b-f5ba54b83234.png">

In this example, the sub positions 1 and 2 are both aligned by the first view and not themselves.

To achieve this, make sure your `ViewHolder` implements `DpadViewHolder` and return the configuration in `getAlignments`:


```kotlin linenums="1"
class ExampleViewHolder(
    view: View
) : RecyclerView.ViewHolder(view), DpadViewHolder {

    private val alignments = ArrayList<ViewHolderAlignment>()

    init {
        alignments.apply {
            add(
                ViewHolderAlignment(
                    offsetRatio = 0.0f,
                    alignmentViewId = R.id.firstView,
                    focusViewId = R.id.firstView
                )
            )
            add(
                ViewHolderAlignment(
                    offsetRatio = 0.0f,
                    alignmentViewId = R.id.firstView,
                    focusViewId = R.id.secondView
                )
            )
            add(
                ViewHolderAlignment(
                    offsetRatio = 0.0f,
                    alignmentViewId = R.id.firstView,
                    focusViewId = R.id.thirdView
                )
            )
        }
    }

    override fun getAlignments(): List<ViewHolderAlignment> {
        return alignments
    }

}
```