# FancyTable

An android library

### Features
1. Both vertically and horizontally scrollable
2. Adjustable numbers of docked/fixed rows and columns
3. Ability to add rows that fill the width of the parent so that they look like headers and make the table look like it has multiple sub-tables within
4. Shadows under the horizontal docked area and vertical docked area
5. Ability to disable and enable all of the above
6. Recycling of views

<img src="https://media.giphy.com/media/f6E5vnYcMz0KZBdJLz/giphy.gif" width="320" alt="Demo Gif"/>

# Usage

### Add the jcenter repository to your build file
Note that this is added inside `allprojects`, not inside `buildscript`
```groovy
allprojects {
  repositories {
    ...
    jcenter()
  }
}
```
### Add the dependency
```groovy
dependencies {
  implementation 'thiha.aung.fancytable:fancytable:1.0.1'
}
```
### Declare FancyTable in a layout file
```xml
<thiha.aung.fancytable.FancyTable
  android:layout_width="match_parent"
  android:layout_height="match_parent"/>
```

### Extend BaseFancyTableAdapter for the adapter
You can create as many view types as you want. For the purpose of demonstration, there are only 2 view types in this example.
Using ViewHolder(s) is also recommended.
```Kotlin
class SampleAdapter : BaseFancyTableAdapter() {

    override fun getRowCount() = 20

    override fun getColumnCount() = 20
    
    override fun getDockedRowCount() = 2

    override fun getDockedColumnCount() = 2

    override fun isRowOneColumn(row: Int): Boolean {
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
---
The library is written based on [TableFixHeaders](https://github.com/InQBarna/TableFixHeaders)
