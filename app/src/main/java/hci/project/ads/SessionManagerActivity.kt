package hci.project.ads

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SessionManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_manager)

        val btnNextSession: Button = findViewById(R.id.btnNextSession)
        btnNextSession.setOnClickListener {
            val isFinalSession = intent.getBooleanExtra("isFinalSession", false)

            if (isFinalSession) {
                // If it's the final session, navigate back to the main screen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Otherwise, just proceed to the next task
                finish() // Close this activity
            }
        }
    }
}