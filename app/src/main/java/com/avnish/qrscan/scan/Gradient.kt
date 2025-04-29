package com.avnish.qrscan.scan


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

@Composable
fun SprinkleGradientBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Base background color
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paint = Paint().apply {
                color = Color.White.copy(alpha = 0.3f)
            }
            val numSprinkles = 100  // Number of sprinkles
            for (i in 1..numSprinkles) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                val radius = Random.nextFloat() * 10 + 5  // Random sizes
                drawCircle(
                    color = Color(
                        Random.nextFloat(),
                        Random.nextFloat(),
                        Random.nextFloat(),
                        alpha = 0.5f
                    ),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSprinkleGradient() {
//    SprinkleGradientBackground()
    BlurryGradientBackground()
}

@Composable
fun BlurryGradientBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color.White)) {
        val width = size.width
        val height = size.height

        // First Blurry Spray (Purple)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6200EE).copy(alpha = 0.6f), // Purple with transparency
                    Color.Transparent
                ),
                center = Offset(width * 0.3f, height * 0.3f), // First spray position
                radius = width * 0.4f
            ),
            center = Offset(width * 0.3f, height * 0.3f),
            radius = width * 0.4f
        )

        // Second Blurry Spray (Orange)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF9800).copy(alpha = 0.6f), // Orange with transparency
                    Color.Transparent
                ),
                center = Offset(width * 0.7f, height * 0.7f), // Second spray position
                radius = width * 0.4f
            ),
            center = Offset(width * 0.7f, height * 0.7f),
            radius = width * 0.4f
        )
    }
}
