package hci.project.ads

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hci.project.ads.databinding.ActivityTypingTestBinding

class TypingTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTypingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.taskText.text = "Task 1 - Description or content goes here."
    }
}
