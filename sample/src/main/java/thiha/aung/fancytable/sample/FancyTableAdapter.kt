package thiha.aung.fancytable.sample

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import thiha.aung.fancytable.BaseFancyTableAdapter
import kotlin.math.roundToInt

class FancyTableAdapter(
    private val context: Context,
    private val numDockedRows: Int,
    private val numDockedColumns: Int,
    private val enableOneColumnRows: Boolean
) : BaseFancyTableAdapter() {

    private val widthFirstColumn: Int
    private val width: Int
    private val height: Int

    init {
        width = dpToPx(60)
        height = width
        widthFirstColumn = dpToPx(120)

    }

    private fun dpToPx(dps: Int) = (context.resources.displayMetrics.density * dps).roundToInt()

    override fun getRowCount() = 20

    override fun getColumnCount() = 20

    override fun getNumDockedRows() = numDockedRows

    override fun getNumDockedColumns() = numDockedColumns

    override fun isOneColumnRow(row: Int): Boolean {
        return if (enableOneColumnRows) row % 5 == 0 else false
    }

    override fun isRowShadowShown() = true

    override fun isColumnShadowShown() = !enableOneColumnRows

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        return when (getItemViewType(row, column)) {
            TYPE_NORMAL -> {
                getNormalView(row, column, convertView, parent)
            }
            TYPE_FIRST_COLUMN -> {
                getViewFirstColumn(row, column, convertView, parent)
            }
            else -> throw Exception("There is no view type for table view. Row: $row, Column: $column")
        }
    }

    private fun getViewFirstColumn(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: parent.inflate(R.layout.item_first_column)

        view.findViewById<View>(R.id.divider).visibility =
            if (row < numDockedRows) View.GONE else View.VISIBLE

        view.setBackgroundResource(
            if (isOneColumnRow(row)) R.color.colorPrimary else R.color.colorAccent
        )

        val text = "$row. ${row}x$column"
        view.findViewById<TextView>(android.R.id.text1).text = text

        return view
    }

    private fun getNormalView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: parent.inflate(R.layout.item_normal)

        view.findViewById<View>(R.id.divider).visibility =
            if (row < numDockedRows) View.GONE else View.VISIBLE

        view.setBackgroundResource(
            if (column < numDockedColumns) R.color.colorAccent else android.R.color.white
        )

        val text = "${row}x$column"
        view.findViewById<TextView>(android.R.id.text1).text = text

        return view
    }

    override fun getWidth(column: Int): Int {
        return if (column == 0) widthFirstColumn else width
    }

    override fun getHeight(row: Int): Int {
        return height
    }

    override fun getItemViewType(row: Int, column: Int): Int {
        return if (column == 0) 1 else 0
    }

    override fun getViewTypeCount() = 2

    companion object {
        const val TYPE_NORMAL = 0
        const val TYPE_FIRST_COLUMN = 1
    }


}