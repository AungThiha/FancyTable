# FancyTable
Android library to implement two-direction scrollable table with `adjustable number of docked/fixed columns and rows` and/or with one-column rows that fill the width of parent which makes it look like `multiple sub-tables with headers` inside the main table.

The library is written based on [TableFixHeaders](https://github.com/InQBarna/TableFixHeaders)

<img src="https://media.giphy.com/media/f6E5vnYcMz0KZBdJLz/giphy.gif" width="320" alt="Demo Gif"/>

# Usage

### Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
### Add the dependency
```groovy
dependencies {
  implementation 'com.github.AungThiha:FancyTable:1.0.0'
}
```
### Declare FancyTable in a layout file
```xml
<thiha.aung.fancytable.FancyTable
  android:layout_width="match_parent"
  android:layout_height="match_parent"/>
```

### Extend BaseFancyTableAdapter for the adapter
```Kotlin
class SampleAdapter : BaseFancyTableAdapter() {

    override fun getRowCount() = 20

    override fun getColumnCount() = 20
    
    override fun getNumDockedRows() = 2

    override fun getNumDockedColumns() = 2

    override fun isOneColumnRow(row: Int): Boolean {
        return if (row == 5) true else false
    }

    override fun isRowShadowShown() = true

    override fun isColumnShadowShown() = false

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        return when (getItemViewType(row, column)) {
            TYPE_NORMAL -> {
                getNormalView(row, column, convertView, parent)
            }
            TYPE_SPECIAL -> {
                getSpecialView(row, column, convertView, parent)
            }
            else -> throw RuntimeException("There is no view type for table view. Row: $row, Column: $column")
        }
    }

    private fun getSpecialView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        TODO("implement creating special view here")
    }

    private fun getNormalView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        TODO("implement creating normal view here")
    }

    override fun getWidth(column: Int) = 20 // width in pixel

    override fun getHeight(row: Int) = 20 // height in pixel

    override fun getItemViewType(row: Int, column: Int)
            = if (row == 0) TYPE_SPECIAL else TYPE_NORMAL

    override fun getViewTypeCount() = 2

    companion object {
        const val TYPE_NORMAL = 0
        const val TYPE_SPECIAL = 1
    }

}
```
### Set adapter onto the table
```Kotlin
fancyTable.adapter = SampleAdapter()
```
