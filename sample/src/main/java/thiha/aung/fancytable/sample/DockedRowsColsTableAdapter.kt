package thiha.aung.fancytable.sample

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import thiha.aung.fancytable.dockedrowscolstable.BaseDockedRowsColsTableAdapter
import kotlin.math.roundToInt

class DockedRowsColsTableAdapter(
    private val context: Context,
    private val numDockedRows: Int,
    private val numDockedCols: Int
) : BaseDockedRowsColsTableAdapter() {

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

    override fun getNumDockedColumns() = numDockedCols

    override fun isHeader(row: Int): Boolean {
        return row % 5 == 0
    }

    override fun isRowShadowShown() = true

    override fun isColumnShadowShown() = false

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        return when (getItemViewType(row, column)) {
            TYPE_NORMAL -> {
                getNormalView(row, column, convertView, parent)
            }
            TYPE_FIRST_COL -> {
                getViewFirstColumn(row, column, convertView, parent)
            }
            else -> throw Exception("There is no view type for table view. Row: $row, Column: $column")
        }
    }

    private fun getViewFirstColumn(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: parent.inflate(R.layout.item_first_column)

        setCellViewConfigs(row, view)

        val text = "$row. ${row}x$column"
        view.findViewById<TextView>(android.R.id.text1).text = text

        return view
    }

    private fun getNormalView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: parent.inflate(R.layout.item_normal)

        setCellViewConfigs(row, view)

        val text = "${row}x$column"
        view.findViewById<TextView>(android.R.id.text1).text = text

        return view
    }

    private fun setCellViewConfigs(row: Int, view: View){
        if (row < numDockedRows) {
            view.setBackgroundResource(R.color.colorAccent)
            view.findViewById<View>(R.id.divider).visibility = View.GONE
        } else {
            view.setBackgroundResource(android.R.color.white)
            view.findViewById<View>(R.id.divider).visibility = View.VISIBLE
        }
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
        const val TYPE_FIRST_COL = 1
    }


}