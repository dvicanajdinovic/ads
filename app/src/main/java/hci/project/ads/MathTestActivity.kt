package hci.project.ads

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hci.project.ads.databinding.ActivityMathTestBinding

class MathTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMathTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.taskText.text = "Task 2 - Description or content goes here."
    }
}
