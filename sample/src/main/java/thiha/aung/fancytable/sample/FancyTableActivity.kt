package thiha.aung.fancytable.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_docked_rows_cols_table.*

class FancyTableActivity : AppCompatActivity() {

    companion object {
        const val NUM_DOCKED_ROWS = "num_docked_rows"
        const val NUM_DOCKED_COLS = "num_docked_cols"
        const val ENABLE_ONE_COL_ROWS = "enable_one_col_rows"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docked_rows_cols_table)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        with(intent) {
            tbl.adapter = FancyTableAdapter(this@FancyTableActivity,
                getIntExtra(NUM_DOCKED_ROWS, 0),
                getIntExtra(NUM_DOCKED_COLS, 0),
                getBooleanExtra(ENABLE_ONE_COL_ROWS, false)
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
