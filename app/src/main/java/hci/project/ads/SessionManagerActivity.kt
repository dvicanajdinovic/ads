package hci.project.ads

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hci.project.ads.databinding.ActivitySessionManagerBinding

class SessionManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use binding to inflate the layout
        binding = ActivitySessionManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener for the button
        binding.btnReturnToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
