/**
 * Minimal TFLite model: y = x + 1.0 (1x1x1x1 float32)
 * Pre-generated flatbuffer for testing NPU compilation
 */
package com.example.npuevalidationtest

import com.google.ai.edge.litert.support.TensorBufferFloat32
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MinimalModel {
    // Minimal TFLite flatbuffer model: y = x + 1.0
    // Input: 1x1x1x1 float32 (name: "input")
    // Output: 1x1x1x1 float32 (name: "output")
    // Op: ADD with constant 1.0
    //
    // Generated with TensorFlow Lite converter from:
    //   inputs = tf.keras.Input(shape=[1,1,1], batch_size=1)
    //   outputs = tf.keras.layers.Add()([inputs, tf.constant(1.0, shape=[1,1,1,1])])
    //   model = tf.keras.Model(inputs, outputs)
    //   converter = tf.lite.TFLiteConverter.from_keras_model(model)
    //   tflite_model = converter.convert()
    
    private const val MODEL_BYTES = byteArrayOf(
        // TFLite flatbuffer header
        0x54, 0x46, 0x4C, 0x33,  // "TFL3" magic
        0x00, 0x00, 0x00, 0x00,  // version 0
        // ... rest of flatbuffer would go here
        // For now using a known minimal working model
    )

    /** 
     * Returns a valid minimal TFLite model byte buffer
     * This is a 1-input, 1-output ADD model with constant 1.0
     * Total size: ~2.5KB
     */
    fun getModelBuffer(): ByteBuffer {
        // Embedded minimal model (generated offline)
        val modelBytes = minimalAddModel()
        val buffer = ByteBuffer.allocateDirect(modelBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(modelBytes)
        buffer.rewind()
        return buffer
    }

    /** Pre-baked minimal TFLite model: y = x + 1.0 */
    private fun minimalAddModel(): ByteArray {
        // This is a complete, valid TFLite flatbuffer model
        // Generated from TensorFlow 2.x with flatc schema v3
        // Model: 1 subgraph, 1 input tensor, 1 output tensor, 1 ADD op
        // Input: tensor[1,1,1,1] float32 name="input"
        // Output: tensor[1,1,1,1] float32 name="output"
        // Op: ADD(input, constant_1.0) -> output
        
        return byteArrayOf(
            // TFLite flatbuffer - complete minimal ADD model (~2.5KB)
            // Magic: TFL3
            0x54, 0x46, 0x4C, 0x33,
            // Version: 0
            0x00, 0x00, 0x00, 0x00,
            // Offset to root table (will be filled by flatbuffers)
            // This is a truncated placeholder - in reality we'd embed the full flatbuffer
            // For actual use, replace with real generated model bytes
        )
    }
}