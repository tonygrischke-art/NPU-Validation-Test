package com.example.npuevalidationtest

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.*
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer

class NPUValidator {

    companion object {
        private const val TAG = "NPUValidator"
    }

    /** Test if NPU delegate can be created and used with a real TFLite model from assets */
    fun testNPUCompilation(context: Context, logCallback: (String) -> Unit): Boolean {
        logCallback("=== NPU Validation Test Started ===")

        // Step 1: Check if NPU accelerator is available
        logCallback("Step 1: Checking NPU accelerator availability...")
        try {
            val npuAccelerator = Accelerator.NPU
            logCallback("  ✅ Accelerator.NPU enum exists: $npuAccelerator")
        } catch (e: Exception) {
            logCallback("  ❌ Accelerator.NPU not found: ${e.message}")
            return false
        }

        // Step 2: Check available accelerators via Environment
        logCallback("Step 2: Checking available accelerators via Environment...")
        try {
            val environment = Environment.create()
            val availableAccelerators = environment.getAvailableAccelerators()
            logCallback("  Available accelerators: $availableAccelerators")
            val hasNpu = availableAccelerators.contains(Accelerator.NPU)
            if (!hasNpu) {
                logCallback("  ⚠️ NPU not in available accelerators (may not be supported on this device/emulator)")
            } else {
                logCallback("  ✅ NPU is available!")
            }
            environment.close()
        } catch (e: Exception) {
            logCallback("  ❌ Failed to check available accelerators: ${e.message}")
        }

        // Step 3: Load TFLite model from assets
        logCallback("Step 3: Loading TFLite model from assets...")
        try {
            val inputStream = context.assets.open("test_model.tflite")
            val modelBytes = inputStream.readAllBytes()
            inputStream.close()

            logCallback("  Model loaded: ${modelBytes.size} bytes (${modelBytes.size / 1024} KB)")
        } catch (e: Exception) {
            logCallback("  ❌ Failed to load model: ${e.message}")
            logCallback("  Stack: ${e.stackTraceToString()}")
            return false
        }

        // Step 4: Create CompiledModel with NPU accelerator
        logCallback("Step 4: Creating CompiledModel with Accelerator.NPU...")
        try {
            // Create options with NPU accelerator
            val options = CompiledModel.Options(Accelerator.NPU)

            // Create environment
            val environment = Environment.create()

            logCallback("  Compiling model with NPU accelerator (this triggers NPU JIT compilation)...")

            // Create CompiledModel from asset file
            val compiledModel = CompiledModel.create(
                context.assets,
                "test_model.tflite",
                options,
                environment
            )

            logCallback("  ✅ CompiledModel created successfully!")

            // Step 5: Test inference
            logCallback("Step 5: Running inference to verify NPU execution...")

            // Create input/output buffers using the correct API (no args)
            val inputBuffers = compiledModel.createInputBuffers()
            val outputBuffers = compiledModel.createOutputBuffers()

            logCallback("  Created ${inputBuffers.size} input buffers, ${outputBuffers.size} output buffers")

            // Fill input with dummy data (1.0f for all elements)
            inputBuffers.forEach { buffer ->
                buffer.writeFloat(FloatArray(buffer.capacity / 4) { 1.0f })
            }

            // Run inference - this executes on NPU
            logCallback("  Running inference (NPU execution)...")
            val startTime = System.nanoTime()
            compiledModel.run(inputBuffers, outputBuffers)
            val endTime = System.nanoTime()
            val durationMs = (endTime - startTime) / 1_000_000

            logCallback("  ✅ Inference completed in ${durationMs} ms!")

            // Print output sample
            outputBuffers.forEachIndexed { index, buffer ->
                val outputData = buffer.readFloat()
                val sample = outputData.take(5).joinToString(", ")
                logCallback("  Output $index sample: [$sample]")
            }

            // Clean up
            compiledModel.close()
            environment.close()
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