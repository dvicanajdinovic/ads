package hci.project.ads

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hci.project.ads.databinding.ActivityTaskBinding

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // OnClickListener for Option 1
        binding.btnTask1.setOnClickListener {
            val intent = Intent(this, TypingTestActivity::class.java)
            startActivity(intent)
        }

        // OnClickListener for Option 2
        binding.btnTask2.setOnClickListener {
            val intent = Intent(this, MathTestActivity::class.java)
            startActivity(intent)
        }

    }
}
