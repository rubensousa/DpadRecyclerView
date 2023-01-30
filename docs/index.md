# Welcome to MkDocs

For full documentation visit [mkdocs.org](https://www.mkdocs.org).

## Commands

* `mkdocs new [dir-name]` - Create a new project.
* `mkdocs serve` - Start the live-reloading docs server.
* `mkdocs build` - Build the documentation site.
* `mkdocs -h` - Print help message and exit.


```kotlin linenums="1"
class ExampleViewHolder(view: View)
  : RecyclerView.ViewHolder(view), DpadViewHolder {

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

``` kotlin
ParentAlignment(offset = resources.dpToPx(16), offsetRatio = 0.0f)
```

## Project layout

    mkdocs.yml    # The configuration file.
    docs/
        index.md  # The documentation homepage.
        ...       # Other markdown pages, images and other files.
