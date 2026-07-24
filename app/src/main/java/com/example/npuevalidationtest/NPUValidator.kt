package com.example.npuevalidationtest

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
            // Check if the NPU enum value exists (it does in 2.1.5)
            val npuAccelerator = Accelerator.NPU
            logCallback("  ✅ Accelerator.NPU enum exists: $npuAccelerator")
        } catch (e: Exception) {
            logCallback("  ❌ Accelerator.NPU not found: ${e.message}")
            return false
        }

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

        // Step 3: Create CompiledModel with NPU accelerator
        logCallback("Step 3: Creating CompiledModel with Accelerator.NPU...")
        try {
            // Create options with NPU accelerator
            val options = CompiledModel.Options(Accelerator.NPU)
            
            // Create environment (required for CompiledModel.create)
            val environment = Environment()
            
            logCallback("  Compiling model with NPU accelerator (this triggers NPU JIT compilation)...")
            
            // Create CompiledModel from asset file
            val compiledModel = CompiledModel.create(
                context.assets,
                "test_model.tflite",
                options,
                environment
            )
            
            logCallback("  ✅ CompiledModel created successfully!")

            // Step 4: Test inference
            logCallback("Step 4: Running inference to verify NPU execution...")
            
            // Get input/output tensor info
            val inputNames = compiledModel.inputTensorNames
            val outputNames = compiledModel.outputTensorNames
            logCallback("  Input tensors: $inputNames")
            logCallback("  Output tensors: $outputNames")

            // Create input/output buffers
            val inputBuffers = compiledModel.createInputBuffers()
            val outputBuffers = compiledModel.createOutputBuffers()
            
            logCallback("  Created ${inputBuffers.size} input buffers, ${outputBuffers.size} output buffers")

            // Fill input with dummy data (1.0f for all elements)
            inputBuffers.forEach { buffer ->
                val floatArray = FloatArray(buffer.capacity / 4)
                floatArray.fill(1.0f) // Simple test pattern
                buffer.rewind()
                buffer.put(floatArray)
                buffer.rewind()
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
                val floatArray = FloatArray(buffer.capacity / 4)
                buffer.rewind()
                buffer.get(floatArray)
                val sample = floatArray.take(5).joinToString(", ")
                logCallback("  Output $index sample: [$sample]")
            }

            // Clean up
            compiledModel.close()
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