package hci.project.ads


import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hci.project.ads.databinding.ActivityTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var database: DatabaseReference
    private lateinit var videoView: VideoView
    private lateinit var blinkingAd: ImageView
    private lateinit var currentCorrectPicture: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var currentAdType: String
    private lateinit var currentAdPoisition: String
    private lateinit var sortedNumbers: List<Int>
    private lateinit var correctSentenceOrder: String

    private val taskHelper = TaskHelper()
    private val userId = "user_${UUID.randomUUID()}" // Generiraj jednistveni ID za korisnika
    private val adCombinations = taskHelper.loadCombinations()

    private val audioFileNames = taskHelper.loadAudioFileNames()
    private val stringTasks = taskHelper.typingTestPhrases()
    private val correctOrderSentences = taskHelper.correctOrderPhrases()
    private val pictureTypes = taskHelper.loadImageKeywords()
    private val videoAds = taskHelper.loadVideoAdNames()
    private val staticAds = taskHelper.loadStaticAdNames()
    private val blinkingAds = taskHelper.loadBlinkingAdNames()

    private val imagesInDrawable: List<Int> by lazy {
        R.drawable::class.java.fields
            .filter { field -> pictureTypes.any { field.name.startsWith(it) } }
            .map { it.getInt(null) }
    }

    private var isTestMode: Boolean = false
    private var selectedAudioFileName: String? = null
    private var selectedVideoAdName: String? = null
    private var selectedStaticAdName: String? = null
    private var selectedBlinkingAdName: String? = null
    private var currentTaskIndex = 0
    private var startTime: Long = 0L
    private var sequence: List<Int> = emptyList()
    private val sequenceDisplayTime: Long = 5000
    private val randomSequenceLength = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://hci-projekt-cb805-default-rtdb.europe-west1.firebasedatabase.app").reference

        binding.btnSubmitTask.setOnClickListener {
            onSubmitTask()
            showNextTaskDialog()
        }

        isTestMode = intent.getBooleanExtra("isTestMode", false)

        setupRecyclerView()
        loadNextTask()
    }


    private fun setupRecyclerView() {
        val selectedImages = selectRandomImages() // Nasumično odaberi 6 slika
        binding.rvImageSelection.layoutManager = GridLayoutManager(this, 2)
        binding.rvImageSelection.adapter = ImageSelectionAdapter(selectedImages) { _ ->
        }
        binding.rvImageSelection.visibility = View.VISIBLE
        updateImageInstructionText(currentCorrectPicture)
    }

    private fun loadNextTask() {

        if (currentTaskIndex >= adCombinations.size) {
            val intent = Intent(this, SessionManagerActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        currentFocus?.clearFocus()

        initializeAdTypeAdPosition()
        initializeSortSequenceTask()
        setupAudioTask()
        setupRememberSequence()
        setupStringTask()
        setupMathTask()
        setupSentenceTask()
        clearUserInputs()
        hideKeyboard()

        startTime = System.currentTimeMillis()
    }

    private fun initializeAdTypeAdPosition() {
        val (adType, adPosition) = adCombinations[currentTaskIndex]
        currentAdType = adType
        currentAdPoisition = adPosition
        setupAd(adType, adPosition)
    }

    private fun initializeSortSequenceTask() {
        val numbers = generateRandomNumbers()
        val isAscending = Random.nextBoolean()
        sortedNumbers = if (isAscending) {
            numbers.sorted()
        } else {
            numbers.sortedDescending()
        }
        val sortOrderText = if (isAscending) {
            "" +
                    "Sortiraj ove brojeve uzlazno: "
        } else {
            "Sortiraj ove brojeve silazno: "
        }
        binding.numberSortTaskText.text = "$sortOrderText ${numbers.joinToString(", ")}"
    }

    private fun setupRememberSequence() {
        binding.sequenceTextView.visibility = View.GONE
        binding.sequenceShowButton.visibility = View.VISIBLE
        sequence = generateRandomSequence()
        binding.sequenceTextView.text = sequence.joinToString(" ")
        binding.sequenceShowButton.setOnClickListener {
            binding.sequenceTextView.visibility = View.VISIBLE
            binding.sequenceShowButton.visibility = View.GONE
            binding.sequenceUserInputText.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                binding.sequenceTextView.visibility = View.GONE
                binding.sequenceUserInputText.visibility = View.VISIBLE
            }, sequenceDisplayTime)
        }
    }

    private fun setupStringTask() {
        val stringTask = stringTasks.random()
        binding.stringCompareTaskText.text = stringTask
    }

    private fun setupMathTask() {
        val mathTask = generateRandomMathTask()
        binding.mathTaskText.text = mathTask
    }

    private fun clearUserInputs() {
        binding.stringCompareTaskInput.text.clear()
        binding.mathTaskInput.text.clear()
        binding.numberSortTaskInput.text.clear()
        binding.audioTaskInput.text.clear()
        binding.sequenceUserInputText.text.clear()
    }

    private fun generateRandomSequence(): List<Int> {
        val random = Random
        return List(randomSequenceLength) { random.nextInt(0, 10) }
    }

    private fun selectRandomImages(): List<Int> {
        // Odaberi tip fotografije koji treba označiti.
        currentCorrectPicture = pictureTypes.random()

        // Makni sve "non" fotografije
        val filteredImages = imagesInDrawable.filter { id ->
            val resourceName = R.drawable::class.java.fields.find { it.getInt(null) == id }?.name
            resourceName?.startsWith("non") == false
        }

        // Barem dvije ponuđene fotografije u tasku odgovaraju točnom odabiru.
        val matchingImages = filteredImages.filter { id ->
            val resourceName = R.drawable::class.java.fields.find { it.getInt(null) == id }?.name
            resourceName?.contains(currentCorrectPicture) == true
        }.shuffled().take(2)

        // Odaberi ostala moguća rješenja.
        val remainingImages = (filteredImages - matchingImages).shuffled().take(4)

        // Kreiraj konačan odabir.
        val allSelectedImages = (matchingImages + remainingImages).shuffled()

        return allSelectedImages
    }

    private fun updateImageInstructionText(directoryName: String) {
        val instructionText = taskHelper.getInstructionText(directoryName)
        binding.imageTaskInstruction.text = instructionText
    }

    private fun setupAudioTask() {
        selectedAudioFileName = audioFileNames.random()
        val audioTaskContainer = binding.audioTaskContainer
        val playButton = binding.btnPlayAudio

        // Prikaži zadatak
        audioTaskContainer.visibility = View.VISIBLE

        // Dohvati resursni ID audio datoteke na temelju odabranog naziva
        val resId = resources.getIdentifier(selectedAudioFileName, "raw", packageName)

        if (resId != 0) { // Provjeri je li resurs pronađen
            // Poveži MediaPlayer s audio resursom
            mediaPlayer = MediaPlayer.create(this, resId)

            // Postavi listener za reprodukciju
            playButton.setOnClickListener {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                } else {
                    mediaPlayer.pause()
                }
            }
        } else {
            // Ako resurs nije pronađen, sakrij zadatak i prikaži poruku o grešci
            audioTaskContainer.visibility = View.GONE
            Log.e("AudioTask", "Audio resurs nije pronađen za: $selectedAudioFileName")
        }
    }

    private fun resetRecyclerView() {
        // Nasumično odaberi 6 novih slika
        val newSelectedImages = selectRandomImages()

        // Resetiraj adapter sa novim slikama
        binding.rvImageSelection.adapter = ImageSelectionAdapter(newSelectedImages) { _ ->
            // Opcionalno: Ovdje možeš dodati akciju pri selekciji slika
        }

        // Osiguraj da RecyclerView bude vidljiv
        binding.rvImageSelection.visibility = View.VISIBLE
        updateImageInstructionText(currentCorrectPicture)
    }

    private fun generateRandomNumbers(): List<Int> {
        val numbers = List(5) { (1..100).random() }
        return numbers
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

    private fun generateRandomMathTask(): String {
        val number1 = (1..10).random()
        val number2 = (1..10).random()
        val operation = listOf("+", "-", "*").random()

        return "$number1 $operation $number2"
    }

    private fun onSubmitTask() {
        val executionTimeInSeconds = calculateExecutionTime()
        val stringErrors = calculateStringErrors()
        val mathErrors = calculateMathErrors()
        val sortErrors = calculateSortErrors()
        val rememberSequenceErrors = calculateRememberSequenceErrors()
        val totalPictureErrors = calculatePictureErrors()
        val audioErrors = calculateAudioErrors()
        val sentenceOrderErrors = calculateSentenceOrderErrors()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
            Date()
        )

        if (!isTestMode) {
            // Spremi rezultate u bazu.
            val results = mapOf(
                "stringErrors" to stringErrors,
                "mathErrors" to mathErrors,
                "imageErrors" to totalPictureErrors,
                "sortErrors" to sortErrors,
                "audioErrors" to audioErrors,
                "sequenceRememberErrors" to rememberSequenceErrors,
                "sentenceOrderErrors" to sentenceOrderErrors,
                "executionTime" to executionTimeInSeconds,
                "adType" to currentAdType,
                "adPosition" to currentAdPoisition,
                "timestamp" to timestamp
            )
            // Kreni na sljedeći zadatak.
            val testIndex = "test${currentTaskIndex + 1}"
            database.child("results").child(userId).child(testIndex).setValue(results)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

    }

    private fun calculateStringErrors() : Int {
        val stringInput = binding.stringCompareTaskInput.text.toString()
        val correctString = binding.stringCompareTaskText.text.toString()
        return calculateLevenshteinDistance(stringInput, correctString)
    }

    private fun calculateMathErrors() : Int {
        val mathInput = binding.mathTaskInput.text.toString()
        val correctMathInput = evaluateMathTask(binding.mathTaskText.text.toString())
        return if (mathInput == correctMathInput.toString()) 0 else 1
    }

    private fun calculateSortErrors() : Int {
        val userInput = binding.numberSortTaskInput.text.toString()
        val userNumbers = userInput.split(",").mapNotNull { it.trim().toIntOrNull() }
        val missingSortCount = sortedNumbers.size - userNumbers.size
        val sortErrors = userNumbers.zip(sortedNumbers) { userNumber, correctNumber ->
            userNumber != correctNumber
        }.count { it }
        val totalSortErrors = sortErrors + missingSortCount

        return totalSortErrors
    }

    private fun calculateRememberSequenceErrors() : Int {
        val userInputRemember = binding.sequenceUserInputText.text.toString()
        val userNumbersRemember =
            userInputRemember.split(",").mapNotNull { it.trim().toIntOrNull() }

        // Provjeri koliko brojeva korisnik unosi.
        val missingNumbersCount = sequence.size - userNumbersRemember.size

        val sequenceNumberRememberErrors = userNumbersRemember.zip(sequence) { userNumber, correctNumber ->
            userNumber != correctNumber
        }.count { it }

        // Dodaj greške za neunesene brojeve.
        val totalSequenceErrors = sequenceNumberRememberErrors + missingNumbersCount

        return totalSequenceErrors
    }

    private fun calculatePictureErrors() : Int {
        val selectedImages = (binding.rvImageSelection.adapter as? ImageSelectionAdapter)?.selectedImages ?: emptySet()
        val allImages = (binding.rvImageSelection.adapter as? ImageSelectionAdapter)?.getAllImages() ?: emptyList()

        // Filtriramo slike koje sadrže "non" (neispravne slike)
        val nonCarImages = selectedImages.filter { !resources.getResourceEntryName(it).contains(currentCorrectPicture) }

        // Filtriramo ispravne slike
        val correctImages = allImages.filter { resources.getResourceEntryName(it).contains(currentCorrectPicture) }

        // Izračun grešaka:
        // 1. Broj krivo odabranih slika (neispravne slike koje su odabrane)
        val incorrectSelected = nonCarImages.size

        // 2. Broj ispravnih slika koje nisu odabrane
        val unselectedCorrectImages = correctImages.filter { !selectedImages.contains(it) }.size

        // Ukupni broj grešaka
        val totalErrors = incorrectSelected + unselectedCorrectImages

        return totalErrors
    }

    private fun calculateAudioErrors() : Int {
        val userAudioResponse = binding.audioTaskInput.text.toString().trim()
        val correctAudioAnswer = selectedAudioFileName ?: ""
        return if (userAudioResponse.equals(correctAudioAnswer, ignoreCase = true)) 0 else 1
    }

    private fun calculateExecutionTime() : Double {
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        return executionTime / 1000.0 // Pretvorba u sekunde
    }

    private fun proceedToNextTaskActions() {
        currentTaskIndex++

        // Resetiraj RecyclerView sa novim slikama.
        resetRecyclerView()
        loadNextTask()
        binding.scrollView.smoothScrollTo(0, 0)

        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.stringCompareTaskInput.windowToken, 0)
    }

    private fun evaluateMathTask(task: String): Int {
        val parts = task.split(" ")
        val number1 = parts[0].toInt()
        val operation = parts[1]
        val number2 = parts[2].toInt()

        return when (operation) {
            "+" -> number1 + number2
            "-" -> number1 - number2
            "*" -> number1 * number2
            else -> throw IllegalArgumentException("Invalid operation")
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
        selectedStaticAdName = staticAds.random()

        val ad = ImageView(this)

        val resId = resources.getIdentifier(selectedStaticAdName, "drawable", packageName)

        if (resId != 0) {
            ad.setImageResource(resId)
        } else {
            Log.e("StaticAd", "Static ad resource not found for: $selectedStaticAdName")
        }

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300) // Jednake dimenzije
        ad.layoutParams = layoutParams
        ad.scaleType = ImageView.ScaleType.FIT_XY
        setAdPosition(ad, position) // Postavi poziciju
        return ad
    }


    private fun createVideoAd(position: String): View {
        clearAdContainer()
        videoView = VideoView(this)
        selectedVideoAdName = videoAds.random()

        val resId = resources.getIdentifier(selectedVideoAdName, "raw", packageName)

        if (resId != 0) {
            val uri = Uri.parse("android.resource://$packageName/$resId")
            videoView.setVideoURI(uri)
        } else {
            Log.e("VideoAd", "Video resource not found for: $selectedVideoAdName")
        }

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300) // Jednake dimenzije
        videoView.layoutParams = layoutParams

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoView.start()
        }
        videoView.setOnErrorListener { _, what, extra ->
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
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    private fun createBlinkingAd(position: String): View {
        clearAdContainer() // Ukloni postojeće prikaze i animacije
        selectedBlinkingAdName = blinkingAds.random()
        blinkingAd = ImageView(this)

        val resId = resources.getIdentifier(selectedBlinkingAdName, "drawable", packageName)

        if (resId != 0) {
            blinkingAd.setImageResource(resId)
        } else {
            Log.e("BlinkingAd", "Blinking ad resource not found for: $selectedBlinkingAdName")
        }

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300) // Jednake dimenzije
        blinkingAd.layoutParams = layoutParams
        blinkingAd.scaleType = ImageView.ScaleType.FIT_XY
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

    private fun getRandomShuffledSentence(): List<String> {
        correctSentenceOrder = correctOrderSentences.random()
        val words = correctSentenceOrder.split(" ")
        return words.shuffled()
    }

    private fun setupSentenceTask() {
        binding.wordContainer.removeAllViews()
        val shuffledWords = getRandomShuffledSentence()
        shuffledWords.forEach { word ->
            val wordLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val textView = TextView(this).apply {
                text = word
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val spinner = Spinner(this).apply {
                val numbers = (1..shuffledWords.size).toList()
                adapter = ArrayAdapter(this@TaskActivity, android.R.layout.simple_spinner_item, numbers).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            wordLayout.addView(textView)
            wordLayout.addView(spinner)
            binding.wordContainer.addView(wordLayout)
        }
    }

    private fun calculateSentenceOrderErrors(): Int {
        val wordToCorrectPosition = correctSentenceOrder.split(" ").mapIndexed { index, word -> word to (index + 1) }.toMap()
        var errors = 0

        for (i in 0 until binding.wordContainer.childCount) {
            val wordLayout = binding.wordContainer.getChildAt(i) as LinearLayout
            val textView = wordLayout.getChildAt(0) as TextView
            val spinner = wordLayout.getChildAt(1) as Spinner

            val word = textView.text.toString()
            val selectedPosition = spinner.selectedItem as Int
            val correctPosition = wordToCorrectPosition[word]

            if (selectedPosition != correctPosition) {
                errors++
            }
        }

        return errors
    }

    private fun showNextTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Prijeđi na sljedeći test?")
        builder.setMessage("Želite li prijeći na sljedeći set zadataka?")

        builder.setPositiveButton("Da") { dialog, _ ->
            dialog.dismiss()
            proceedToNextTaskActions()
        }

        builder.setNegativeButton("Ne") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}