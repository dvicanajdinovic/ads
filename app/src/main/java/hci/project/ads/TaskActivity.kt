package hci.project.ads


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hci.project.ads.databinding.ActivityTaskBinding
import java.util.UUID
import kotlin.random.Random

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var database: DatabaseReference
    private lateinit var videoView: VideoView
    private lateinit var blinkingAd: ImageView
    private lateinit var currentCorrectPicture: String
    private val userId = "user_${UUID.randomUUID()}" // Generiraj jednistveni ID za korisnika

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
    ).shuffled()

    private var currentTaskIndex = 0
    private var startTime: Long = 0L
    private lateinit var currentAdType: String
    private lateinit var currentAdPoisition: String
    private lateinit var sortedNumbers: List<Int>
    private val imageResources = listOf(
        R.drawable.car1,
        R.drawable.car2,
        R.drawable.car3,
        R.drawable.car4,
        R.drawable.non_car1,
        R.drawable.non_car2,
        R.drawable.non_car3,
        R.drawable.non_car4,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://hci-projekt-cb805-default-rtdb.europe-west1.firebasedatabase.app").reference

        setupRecyclerView()
        loadNextTask()
        binding.etStringInput.requestFocus()
        binding.btnSubmitTask.setOnClickListener { onSubmitTask() }
    }

    private fun selectRandomImages(): List<Int> {
        // Lista direktorija
        val directories = listOf("auto", "slon")

        // Nasumično odaberi jedan direktorij
        currentCorrectPicture = directories.random()

        // Ispis radi provjere
        Log.d("SelectRandomImages", "Selected directory: $currentCorrectPicture")

        // Pronađi sve slike u odabranom direktoriju
        val images = when (currentCorrectPicture) {
            "auto" -> listOf(
                R.drawable.car1,
                R.drawable.car2,
                R.drawable.car3,
                R.drawable.car4,
                R.drawable.non_car1,
                R.drawable.non_car2,
                R.drawable.non_car3,
                R.drawable.non_car4
            )
            "slon" -> listOf(
                R.drawable.slon1,
                R.drawable.slon2,
                R.drawable.slon3,
                R.drawable.slon4,
                R.drawable.non_slon1,
                R.drawable.non_slon2,
                R.drawable.non_slon3,
                R.drawable.non_slon4
            )
            else -> emptyList()
        }

        // Nasumično odaberi 6 slika iz tog direktorija
        return images.shuffled().take(6)
    }

    private fun updateImageInstructionText(directoryName: String) {
        val instructionText = when (directoryName) {
            "auto" -> "Izaberi slike gdje se nalazi auto"
            "slon" -> "Izaberi slike gdje se nalazi slon"
            else -> "Izaberi slike"
        }
        binding.tvImageInstruction.text = instructionText
    }

    private fun countIncorrectImages(selectedImages: List<Int>): Int {
        val incorrectImages = selectedImages.filter { resources.getResourceEntryName(it).contains("non") }
        return incorrectImages.size
    }

    private fun resetRecyclerView() {
        // Nasumično odaberi 6 novih slika
        val newSelectedImages = selectRandomImages()

        // Resetiraj adapter sa novim slikama
        binding.rvImageSelection.adapter = ImageSelectionAdapter(newSelectedImages) { imageRes ->
            // Opcionalno: Ovdje možeš dodati akciju pri selekciji slika
        }

        // Osiguraj da RecyclerView bude vidljiv
        binding.rvImageSelection.visibility = View.VISIBLE
        updateImageInstructionText(currentCorrectPicture)
    }

    private fun setupRecyclerView() {
        val selectedImages = selectRandomImages() // Nasumično odaberi 6 slika
        binding.rvImageSelection.layoutManager = GridLayoutManager(this, 2)
        binding.rvImageSelection.adapter = ImageSelectionAdapter(selectedImages) { imageRes ->
        }
        binding.rvImageSelection.visibility = View.VISIBLE
        updateImageInstructionText(currentCorrectPicture)
        // Izračunaj broj pogrešnih slika
        val incorrectCount = countIncorrectImages(selectedImages)
        Log.d("ImageCheck", "Incorrect images count: $incorrectCount")
    }

    private fun generateRandomNumbers(): List<Int> {
        val numbers = List(5) { (1..100).random() }
        return numbers
    }


    private fun loadNextTask() {
        if (currentTaskIndex >= adCombinations.size) {
            Toast.makeText(this, "Session completed!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Dohvati kombinaciju reklame
        val (adType, adPosition) = adCombinations[currentTaskIndex]
        currentAdType = adType
        currentAdPoisition = adPosition

        val numbers = generateRandomNumbers()

        val isAscending = Random.nextBoolean()

        sortedNumbers = if (isAscending) {
            numbers.sorted()
        } else {
            numbers.sortedDescending()
        }

        val sortOrderText = if (isAscending) {
            "Sortiraj ove brojeve uzlazno: "
        } else {
            "Sortiraj ove brojeve silazno: "
        }

        binding.tvNumberTask.text = "$sortOrderText ${numbers.joinToString(", ")}"

        setupAd(adType, adPosition)

        // Dohvati zadatke iz baze
        database.child("tasks").get().addOnSuccessListener { tasksSnapshot ->
            val stringTasks = tasksSnapshot.child("string").children.map { it.value.toString() }
            val mathTasks = tasksSnapshot.child("math").children.map { it.value.toString() }

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
                binding.etNumberInput.text.clear()

                // Započni mjerenje vremena
                startTime = System.currentTimeMillis()
            } else {
                Toast.makeText(this, "Error loading tasks!", Toast.LENGTH_LONG).show()
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
        val stringErrors = calculateLevenshteinDistance(stringInput, correctString)
        val mathErrors = if (mathInput == correctMath) 0 else 1


        // Provjera za slike
        // Odabiremo slike
        val selectedImages = (binding.rvImageSelection.adapter as? ImageSelectionAdapter)?.selectedImages ?: emptySet()
        val allImages = (binding.rvImageSelection.adapter as? ImageSelectionAdapter)?.getAllImages() ?: emptyList()
        val userInput = binding.etNumberInput.text.toString()
        val userNumbers = userInput.split(",").map { it.trim().toIntOrNull() }.filterNotNull()

        val sortErrors = userNumbers.zip(sortedNumbers) { userNumber, correctNumber ->
            userNumber != correctNumber
        }.count { it }

        Log.d("SelectedImages", "$selectedImages")

        // Filtriramo slike koje sadrže "non" (neispravne slike)
        val nonCarImages = selectedImages?.filter { resources.getResourceEntryName(it).contains("non") } ?: emptyList()

        Log.d("NonCarImages", "${nonCarImages.size}")

        // Filtriramo ispravne slike
        val correctImages = allImages.filter { !resources.getResourceEntryName(it).contains("non") }
        Log.d("CorrectImages", "${correctImages.size}")

        // Izračun grešaka:
        // 1. Broj krivo odabranih slika (neispravne slike koje su odabrane)
        val incorrectSelected = nonCarImages.size
        Log.d("IncorrectSelected", "$incorrectSelected")

        // 2. Broj ispravnih slika koje nisu odabrane
        val unselectedCorrectImages = correctImages.filter { !selectedImages.contains(it) }.size
        Log.d("UnselectedCorrectImages", "$unselectedCorrectImages")

        // Ukupni broj grešaka
        val totalErrors = incorrectSelected + unselectedCorrectImages
        Log.d("Errors", "Total errors: $totalErrors")


        // Izračunaj vrijeme izvršavanja
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        val executionTimeInSeconds = executionTime / 1000.0 // Pretvorba u sekunde

        // Spremi rezultate u Firebase
        val results = mapOf(
            "stringErrors" to stringErrors,
            "mathErrors" to mathErrors,
            "imageErrors" to totalErrors,
            "sortErrors" to sortErrors,
            "executionTime" to executionTimeInSeconds,
            "adType" to currentAdType,
            "adPosition" to currentAdPoisition
        )
        val testIndex = "test${currentTaskIndex + 1}"
        database.child("results").child(userId).child(testIndex).setValue(results)

        // Pređi na sljedeći zadatak
        currentTaskIndex++
        // Resetiraj RecyclerView sa novim slikama
        resetRecyclerView()
        loadNextTask()
        binding.scrollView.smoothScrollTo(0, 0)
        binding.etStringInput.requestFocus()

        // Sakrij tipkovnicu
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etStringInput.windowToken, 0)
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

    private fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val lenStr1 = s1.length
        val lenStr2 = s2.length
        val dp = Array(lenStr1 + 1) { IntArray(lenStr2 + 1) }

        for (i in 0..lenStr1) {
            for (j in 0..lenStr2) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = minOf(
                        dp[i - 1][j - 1] + if (s1[i - 1] == s2[j - 1]) 0 else 1, // Zamjena
                        dp[i - 1][j] + 1, // Brisanje
                        dp[i][j - 1] + 1 // Umetanje
                    )
                }
            }
        }

        return dp[lenStr1][lenStr2]
    }

    // Funkcije za stvaranje reklama
    private fun createStaticAd(position: String): View {
        clearAdContainer()
        val ad = ImageView(this)
        ad.setImageResource(R.drawable.static_ad)
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300) // Jednake dimenzije
        ad.layoutParams = layoutParams
        ad.scaleType = ImageView.ScaleType.CENTER_CROP
        setAdPosition(ad, position) // Postavi poziciju
        return ad
    }


    private fun createVideoAd(position: String): View {
        clearAdContainer()
        videoView = VideoView(this)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.video_ad}")
        videoView.setVideoURI(uri)
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300) // Jednake dimenzije
        videoView.layoutParams = layoutParams
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoView.start()
        }
        videoView.setOnErrorListener { mp, what, extra ->
            Log.e("VideoAd", "Error: $what, $extra")
            true
        }
        setAdPosition(videoView, position) // Postavi poziciju
        return videoView
    }

    override fun onPause() {
        super.onPause()
        if (::videoView.isInitialized) {
            videoView.pause()
        }
        if (::blinkingAd.isInitialized) {
            blinkingAd.clearAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
        if (::blinkingAd.isInitialized) {
            blinkingAd.clearAnimation()
        }
    }

    private fun createBlinkingAd(position: String): View {
        clearAdContainer() // Ukloni postojeće prikaze i animacije
        blinkingAd = ImageView(this)
        blinkingAd.setImageResource(R.drawable.blinking_ad) // Postavi sliku za titranje
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300) // Jednake dimenzije
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
