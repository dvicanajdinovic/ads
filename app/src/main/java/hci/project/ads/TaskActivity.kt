package hci.project.ads


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hci.project.ads.databinding.ActivityTaskBinding

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var database: DatabaseReference

    private val adCombinations = listOf(
        Pair("static", "top_right"),
        Pair("static", "middle_right"),
        Pair("static", "bottom_right"),
        Pair("video", "top_right"),
        Pair("video", "middle_right"),
        Pair("video", "bottom_right"),
        Pair("blinking", "top_right"),
        Pair("blinking", "middle_right"),
        Pair("blinking", "bottom_right")
    )

    private var currentTaskIndex = 0
    private var startTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://hci-projekt-cb805-default-rtdb.europe-west1.firebasedatabase.app").getReference("tasks")

        loadNextTask()
        binding.btnSubmitTask.setOnClickListener { onSubmitTask() }
    }

    private fun loadNextTask() {
        if (currentTaskIndex >= adCombinations.size) {
            Toast.makeText(this, "Session completed!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Dohvati kombinaciju reklame
        val (adType, adPosition) = adCombinations[currentTaskIndex]
        setupAd(adType, adPosition)

        // Dohvati zadatke iz baze
        database.child("string").get().addOnSuccessListener { stringSnapshot ->
            database.child("math").get().addOnSuccessListener { mathSnapshot ->
                val stringTasks = stringSnapshot.children.map { it.value.toString() }
                val mathTasks = mathSnapshot.children.map { it.value.toString() }

                if (stringTasks.isNotEmpty() && mathTasks.isNotEmpty()) {
                    // Nasumično odaberi zadatke
                    val stringTask = stringTasks.random()
                    val mathTask = mathTasks.random()

                    // Dodaj log za provjeru zadataka
                    Log.d("Task", "String task: $stringTask")
                    Log.d("Task", "Math task: $mathTask")

                    // Postavi zadatke na UI
                    binding.tvStringTask.text = stringTask
                    binding.tvMathTask.text = mathTask

                    // Resetiraj unos korisnika
                    binding.etStringInput.text.clear()
                    binding.etMathInput.text.clear()

                    // Započni mjerenje vremena
                    startTime = System.currentTimeMillis()
                } else {
                    Toast.makeText(this, "Error loading tasks!", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e ->
                // Ako dođe do pogreške prilikom dohvaćanja podataka
                Log.e("Firebase", "Error fetching math tasks: ${e.message}")
                Toast.makeText(this, "Error fetching tasks from Firebase", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            // Ako dođe do pogreške prilikom dohvaćanja podataka
            Log.e("Firebase", "Error fetching string tasks: ${e.message}")
            Toast.makeText(this, "Error fetching tasks from Firebase", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAd(adType: String, adPosition: String) {
        // Prikaz reklame prema tipu i poziciji
        clearAdContainer()
        binding.adContainer.removeAllViews()
        when (adType) {
            "static" -> binding.adContainer.addView(createStaticAd(adPosition))
            "video" -> binding.adContainer.addView(createVideoAd(adPosition))
            "blinking" -> binding.adContainer.addView(createBlinkingAd(adPosition))
        }
        Log.d("Ads", "Ad type: $adType, Position: $adPosition")
    }

    private fun onSubmitTask() {
        // Dohvati korisnički unos
        val stringInput = binding.etStringInput.text.toString()
        val mathInput = binding.etMathInput.text.toString()

        // Dohvati točne odgovore
        val correctString = binding.tvStringTask.text.toString()
        val correctMath = evaluateMathExpression(binding.tvMathTask.text.toString())

        // Izračunaj greške
        val stringErrors = if (stringInput == correctString) 0 else 1
        val mathErrors = if (mathInput == correctMath) 0 else 1

        // Izračunaj vrijeme izvršavanja
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // Spremi rezultate u Firebase
        val results = mapOf(
            "taskIndex" to currentTaskIndex,
            "stringErrors" to stringErrors,
            "mathErrors" to mathErrors,
            "executionTime" to executionTime
        )
        database.child("results").child("user_${System.currentTimeMillis()}").setValue(results)

        // Pređi na sljedeći zadatak
        currentTaskIndex++
        loadNextTask()
    }

    private fun evaluateMathExpression(expression: String): String {
        // Evaluacija jednostavnih matematičkih izraza (samo zbrajanje za sada)
        return try {
            val parts = expression.split("+").map { it.trim().toInt() }
            (parts[0] + parts[1]).toString()
        } catch (e: Exception) {
            "0"
        }
    }

    // Funkcije za stvaranje reklama
    private fun createStaticAd(position: String): View {
        clearAdContainer()
        val ad = ImageView(this)
        ad.setImageResource(R.drawable.static_ad)
        val layoutParams = FrameLayout.LayoutParams(120, 120) // Jednake dimenzije
        ad.layoutParams = layoutParams
        ad.scaleType = ImageView.ScaleType.CENTER_CROP
        setAdPosition(ad, position) // Postavi poziciju
        return ad
    }


    private fun createVideoAd(position: String): View {
        clearAdContainer()
        val videoView = VideoView(this)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.video_ad}")
        videoView.setVideoURI(uri)
        val layoutParams = FrameLayout.LayoutParams(120, 120) // Jednake dimenzije
        videoView.layoutParams = layoutParams
        videoView.start() // Pokreni video
        setAdPosition(videoView, position) // Postavi poziciju
        return videoView
    }

    private fun createBlinkingAd(position: String): View {
        clearAdContainer() // Ukloni postojeće prikaze i animacije
        val blinkingAd = ImageView(this)
        blinkingAd.setImageResource(R.drawable.blinking_ad) // Postavi sliku za titranje
        val layoutParams = FrameLayout.LayoutParams(120, 120) // Jednake dimenzije
        blinkingAd.layoutParams = layoutParams
        blinkingAd.scaleType = ImageView.ScaleType.CENTER_CROP
        // Dodaj animaciju za titranje
        blinkingAd.startAnimation(createBlinkingAnimation())
        setAdPosition(blinkingAd, position) // Postavi poziciju
        return blinkingAd
    }

    private fun createBlinkingAnimation(): Animation {
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.duration = 500 // Pola sekunde za prijelaz
        alphaAnimation.repeatMode = Animation.REVERSE
        alphaAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.isFillEnabled = false // Spriječi zadržavanje stanja
        return alphaAnimation
    }

    private fun setAdPosition(view: View, position: String) {
        val params = view.layoutParams as FrameLayout.LayoutParams

        when (position) {
            "top_right" -> {
                params.gravity = Gravity.TOP or Gravity.END
            }
            "middle_right" -> {
                params.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }
            "bottom_right" -> {
                params.gravity = Gravity.BOTTOM or Gravity.END
            }
            else -> {
                // Default position (top right)
                params.gravity = Gravity.TOP or Gravity.END
            }
        }

        view.layoutParams = params
    }

    private fun clearAdContainer() {
        val adContainer = findViewById<FrameLayout>(R.id.adContainer)
        Log.d("Ads", "Clearing ad container with ${adContainer.childCount} children.")
        for (i in 0 until adContainer.childCount) {
            val view = adContainer.getChildAt(i)
            if (view is ImageView || view is VideoView) {
                Log.d("Ads", "Clearing animation for view: $view")
                view.clearAnimation() // Ovdje se poziva zaustavljanje animacije
            }
        }
        adContainer.removeAllViews()
        Log.d("Ads", "Ad container cleared.")
    }
}
