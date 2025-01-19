package hci.project.ads

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val nextTaskIndex = intent.getIntExtra("nextTaskIndex", -1)

        val btnProceed = findViewById<Button>(R.id.btnProceed)
        btnProceed.setOnClickListener {
            if (nextTaskIndex == -1) {
                Toast.makeText(this, "No more tasks. Session completed!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                val intent = Intent(this, TaskActivity::class.java)
                intent.putExtra("taskIndex", nextTaskIndex)
                startActivity(intent)
                finish()
            }
        }
    }
}
