package com.example.npuevalidationtest

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.*
import com.google.ai.edge.litert.support.TensorBufferFloat32
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NPUValidator {

    companion object {
        private const val TAG = "NPUValidator"
    }

    /** Test if NPU delegate can be created and used with a real TFLite model from assets */
    fun testNPUCompilation(context: Context, logCallback: (String) -> Unit): Boolean {
        logCallback("=== NPU Validation Test Started ===")

        // Step 1: Check if NPU delegate is available
        logCallback("Step 1: Checking NPU delegate availability...")
        val npuDelegateAvailable = NPUDelegateFactory.isAvailable()
        logCallback("  NPUDelegateFactory.isAvailable() = $npuDelegateAvailable")

        if (!npuDelegateAvailable) {
            logCallback("  ❌ NPU delegate NOT available on this device")
            return false
        }
        logCallback("  ✅ NPU delegate available")

        // Step 2: Load TFLite model from assets
        logCallback("Step 2: Loading TFLite model from assets...")
        val modelBuffer: ByteBuffer
        try {
            val inputStream = context.assets.open("test_model.tflite")
            val modelBytes = inputStream.readAllBytes()
            inputStream.close()
            
            modelBuffer = ByteBuffer.allocateDirect(modelBytes.size)
            modelBuffer.order(ByteOrder.nativeOrder())
            modelBuffer.put(modelBytes)
            modelBuffer.rewind()
            
            logCallback("  Model loaded: ${modelBytes.size} bytes (${modelBytes.size / 1024} KB)")
        } catch (e: Exception) {
            logCallback("  ❌ Failed to load model: ${e.message}")
            logCallback("  Stack: ${e.stackTraceToString()}")
            return false
        }

        // Step 3: Try to compile with NPU accelerator
        logCallback("Step 3: Compiling model with Accelerator.NPU...")
        try {
            val options = LitertInterpreterOptions()
                .setAccelerator(LitertInterpreterOptions.Accelerator.NPU)
                .setNumThreads(1)

            val interpreter = LitertInterpreter(modelBuffer, options)
            logCallback("  ✅ Interpreter created with NPU accelerator")

            // Step 4: Test compilation (this triggers NPU compilation)
            logCallback("Step 4: Testing NPU compilation with dummy inference...")
            
            // Get input/output tensor info
            val inputShape = interpreter.getInputTensor(0).shape
            val outputShape = interpreter.getOutputTensor(0).shape
            logCallback("  Input shape: ${inputShape.joinToString(",")}")
            logCallback("  Output shape: ${outputShape.joinToString(",")}")

            // Create input tensor buffer
            val inputBuffer = TensorBufferFloat32(inputShape.toIntArray())
            val inputArray = FloatArray(inputBuffer.buffer.capacity() / 4)
            for (i in inputArray.indices) inputArray[i] = 1.0f // Simple test input
            inputBuffer.buffer.put(inputArray)
            inputBuffer.buffer.rewind()

            val outputBuffer = TensorBufferFloat32(outputShape.toIntArray())

            // Run inference - this triggers NPU compilation
            logCallback("  Running inference (triggers NPU compilation)...")
            interpreter.run(inputBuffer, outputBuffer)
            logCallback("  ✅ NPU inference successful!")

            val outputArray = FloatArray(outputBuffer.buffer.capacity() / 4)
            outputBuffer.buffer.get(outputArray)
            logCallback("  Output sample: ${outputArray.take(5).joinToString(", ")}")

            interpreter.close()
            logCallback("=== NPU Validation Test PASSED ===")
            return true

        } catch (e: Exception) {
            logCallback("  ❌ NPU compilation/execution failed: ${e.message}")
            logCallback("  Stack: ${e.stackTraceToString()}")
            logCallback("=== NPU Validation Test FAILED ===")
            return false
        }
    }
}

fun Throwable.stackTraceToString(): String {
    val sw = java.io.StringWriter()
    val pw = java.io.PrintWriter(sw)
    printStackTrace(pw)
    return sw.toString()
}