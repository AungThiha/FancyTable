package thiha.aung.fancytable.sample

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_fancy_table.*

class FancyTableActivity : AppCompatActivity() {

    companion object {

        const val DOCKED_ROW_COUNT = "DOCKED_ROW_COUNT"
        const val DOCKED_COLUMN_COUNT = "DOCKED_COLUMN_COUNT"
        const val ENABLE_ONE_COLUMN_ROWS = "ENABLE_ONE_COLUMN_ROWS"

        fun createIntent(
            context: Context,
            dockedRowCount: Int,
            dockedColumnCount: Int,
            enableOneColRows: Boolean
        ) = Intent(context, FancyTableActivity::class.java).apply {
            putExtra(DOCKED_ROW_COUNT, dockedRowCount)
            putExtra(DOCKED_COLUMN_COUNT, dockedColumnCount)
            putExtra(ENABLE_ONE_COLUMN_ROWS, enableOneColRows)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fancy_table)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        with(intent) {
            tbl.adapter = FancyTableAdapter(
                this@FancyTableActivity,
                getIntExtra(DOCKED_ROW_COUNT, 0),
                getIntExtra(DOCKED_COLUMN_COUNT, 0),
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
