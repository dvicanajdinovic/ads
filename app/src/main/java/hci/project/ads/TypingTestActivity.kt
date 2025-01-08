package hci.project.ads

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import hci.project.ads.databinding.ActivityTypingTestBinding
import kotlin.random.Random

class TypingTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTypingTestBinding
    private val phrases = listOf(
        "the quick brown fox jumps over the lazy dog",
        "pack my box with five dozen liquor jugs",
        "how razorback jumping frogs can level six piqued gymnasts",
        "the five boxing wizards jump quickly",
        "jackdaws love my big sphinx of quartz",
        "mr jock tv quiz phd bags few lynx",
        "cwm fjord bank glyphs vext quiz",
        "the jay pig fox zebra and my wolves quack",
        "blowzy night frumps vexd jack q",
        "the quick onyx goblin jumps over the lazy dwarf",
        "waltz nymph for quick jigs vex bud",
        "glib jocks quiz nymph to vex dwarf",
        "sphinx of black quartz judge my vow",
        "jinxed wizards pluck ivy from the big quilt",
        "the wizard quickly jumps over the frogs",
        "frogs in the swamp jump quickly over logs",
        "sphinxes are found in deserts",
        "blazing fox jumps over the quick dog",
        "how razorback jumping frogs can level six piqued gymnasts again",
        "the lazy dog sleeps under the quick brown fox"
    )
    private lateinit var currentPhrase: String
    private var startTime: Long = 0
    private var isTypingStarted = false
    private val handler = Handler(Looper.getMainLooper())
    private var elapsedTime: Long = 0
    private var phraseCount: Int = 0 // Counter for correctly typed phrases
    private var wrongPhraseCount: Int = 0 // Counter for wrongly typed phrases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTypingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadNewPhrase()

        binding.typingInput.addTextChangedListener {
            if (!isTypingStarted) {
                isTypingStarted = true
                startTime = System.currentTimeMillis()
                startStopwatch()
            }
        }

        // Handle Enter key press to submit the phrase and show time taken
        binding.typingInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val userInput = binding.typingInput.text.toString().trim()

                // If typing has started and user presses Enter
                if (isTypingStarted) {
                    stopStopwatch()
                    showTime()
                    isTypingStarted = false // Reset typing flag

                    // Check if input matches the current phrase
                    if (userInput.isNotEmpty()) {
                        phraseCount++
                        if (userInput != currentPhrase) wrongPhraseCount++
                    }

                    // Update counters on the screen
                    binding.phraseCountText.text = "Phrases Typed: $phraseCount"
                    binding.wrongPhraseCountText.text = "Wrong Phrases: $wrongPhraseCount"
                }

                // Clear input field and load new phrase
                loadNewPhrase()
                binding.typingInput.text.clear()
                true
            } else {
                false
            }
        }

        // Handle "jump to next phrase" when Enter is pressed without updating counters
        binding.typingInput.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                loadNewPhrase()
                binding.typingInput.text.clear()
                true
            } else {
                false
            }
        }
    }

    private fun loadNewPhrase() {
        currentPhrase = phrases[Random.nextInt(phrases.size)]
        binding.phraseText.text = currentPhrase
        binding.typingInput.text.clear()
        binding.timeTakenText.text = "Time: 0.00 seconds"
        isTypingStarted = false
        elapsedTime = 0
    }

    private fun startStopwatch() {
        handler.post(object : Runnable {
            override fun run() {
                if (isTypingStarted) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    val timeInSeconds = elapsedTime / 1000.0
                    runOnUiThread {
                        binding.timeTakenText.text = "Time: %.2f seconds".format(timeInSeconds)
                    }
                    handler.postDelayed(this, 100)
                }
            }
        })
    }

    private fun stopStopwatch() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun showTime() {
        val timeInSeconds = elapsedTime / 1000.0
        binding.timeTakenText.text = "Final Time: %.2f seconds".format(timeInSeconds)
    }
}
