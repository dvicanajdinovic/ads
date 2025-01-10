package hci.project.ads

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)  // Link it to the layout you create for settings
    }
}