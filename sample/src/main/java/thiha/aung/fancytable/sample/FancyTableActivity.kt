package thiha.aung.fancytable.sample

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_docked_rows_columns_table.*

class FancyTableActivity : AppCompatActivity() {

    companion object {

        const val NUM_DOCKED_ROWS = "NUM_DOCKED_ROWS"
        const val NUM_DOCKED_COLUMNS = "NUM_DOCKED_COLUMNS"
        const val ENABLE_ONE_COLUMN_ROWS = "ENABLE_ONE_COLUMN_ROWS"

        fun createIntent(
            context: Context,
            numberDockedRows: Int,
            numberDockedCols: Int,
            enableOneColRows: Boolean
        ) = Intent(context, FancyTableActivity::class.java).apply {
            putExtra(NUM_DOCKED_ROWS, numberDockedRows)
            putExtra(NUM_DOCKED_COLUMNS, numberDockedCols)
            putExtra(ENABLE_ONE_COLUMN_ROWS, enableOneColRows)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docked_rows_columns_table)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        with(intent) {
            tbl.adapter = FancyTableAdapter(
                this@FancyTableActivity,
                getIntExtra(NUM_DOCKED_ROWS, 0),
                getIntExtra(NUM_DOCKED_COLUMNS, 0),
                getBooleanExtra(ENABLE_ONE_COLUMN_ROWS, false)
            )
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

}
