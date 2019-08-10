package thiha.aung.fancytable.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDockedRowsColsTable.setOnClickListener {
            val intent = Intent(this, DockedRowsColsTableActivity::class.java).apply {
                putExtra(DockedRowsColsTableActivity.NUM_DOCKED_ROWS, 2)
                putExtra(DockedRowsColsTableActivity.NUM_DOCKED_COLS, 2)
            }
            startActivity(intent)
        }
    }
}
