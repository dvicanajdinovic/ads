package hci.project.ads

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hci.project.ads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Pristupi FloatingActionButton-u kroz contentMain binding objekt
        binding.appBarMain.contentMain.fabTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java).apply {
                putExtra("isTestMode", false) }
            startActivity(intent)
        }

        binding.appBarMain.contentMain.buttonStartTrial.setOnClickListener {
            Toast.makeText(this, "Testni Način Rada: Samo 1 test će se izvršiti.", Toast.LENGTH_LONG).show()

            // Označi je li korisnik odabrao testni način.
            val intent = Intent(this, TaskActivity::class.java).apply {
                putExtra("isTestMode", true)
            }
            startActivity(intent)
        }
    }
}
