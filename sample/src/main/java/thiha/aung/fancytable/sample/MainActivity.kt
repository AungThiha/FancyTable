package thiha.aung.fancytable.sample

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        github.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        github.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AungThiha/FancyTable"))
            startActivity(intent)
        }

        btnOpenTable.setOnClickListener {
            startActivity(FancyTableActivity.createIntent(
                this,
                dockedRowCount.value,
                dockedColumnCount.value,
                rowOneColumn.isChecked
            ))
        }
    }
}
