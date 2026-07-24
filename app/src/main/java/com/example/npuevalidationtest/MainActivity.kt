package com.example.npuevalidationtest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var logTextView: TextView
    private lateinit var testNPUButton: Button
    private lateinit var testInferenceButton: Button

    private val logBuffer = StringBuilder()
    private val npuValidator = NPUValidator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        logTextView = findViewById(R.id.logTextView)
        testNPUButton = findViewById(R.id.testNPUButton)
        testInferenceButton = findViewById(R.id.testInferenceButton)

        testNPUButton.setOnClickListener {
            testNPUButton.isEnabled = false
            testInferenceButton.isEnabled = false
            log("Starting NPU validation test...")
            statusText.text = "Testing NPU..."
            
            Thread {
                val success = npuValidator.testNPUCompilation(this) { msg ->
                    runOnUiThread { log(msg) }
                }
                runOnUiThread {
                    testNPUButton.isEnabled = true
                    testInferenceButton.isEnabled = true
                    if (success) {
                        statusText.text = "NPU Validation: PASSED ✅"
                    } else {
                        statusText.text = "NPU Validation: FAILED ❌"
                    }
                }
            }.start()
        }

        testInferenceButton.setOnClickListener {
            log("Inference test button clicked (not implemented yet)")
        }
    }

    private fun log(msg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val line = "[$timestamp] $msg"
        logBuffer.append(line).append("\n")
        logTextView.text = logBuffer.toString()
        // Auto-scroll to bottom
        logTextView.post { logTextView.scrollTo(0, logTextView.bottom) }
        Log.d("NPUTest", msg)
    }
}