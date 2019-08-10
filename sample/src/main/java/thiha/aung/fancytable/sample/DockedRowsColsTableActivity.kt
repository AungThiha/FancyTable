package thiha.aung.fancytable.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_docked_rows_cols_table.*

class DockedRowsColsTableActivity : AppCompatActivity() {

    companion object {
        const val NUM_DOCKED_ROWS = "num_docked_rows"
        const val NUM_DOCKED_COLS = "num_docked_cols"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docked_rows_cols_table)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val numDockedRows = intent.getIntExtra(NUM_DOCKED_ROWS, 0)
        val numDockedCols = intent.getIntExtra(NUM_DOCKED_COLS, 0)
        tbl.adapter = DockedRowsColsTableAdapter(this, numDockedRows, numDockedCols)
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
