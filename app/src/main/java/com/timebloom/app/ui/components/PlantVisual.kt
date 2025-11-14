package com.timebloom.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.timebloom.app.data.local.entity.GrowthStage
import kotlin.math.sin

@Composable
fun PlantVisual(
    modifier: Modifier = Modifier,
    growthStage: GrowthStage,
    plantColor: String,
    isHealthy: Boolean = true
) {
    // Animation for subtle plant movement (like wind)
    val infiniteTransition = rememberInfiniteTransition(label = "plant_sway")
    val swayOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val color = Color(plantColor.toColorInt())
    val unhealthyColor = Color.Gray

    Box(modifier = modifier.size(120.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val centerY = size.height

            when (growthStage) {
                GrowthStage.SEED -> drawSeed(centerX, centerY - 20, color)
                GrowthStage.SPROUT -> drawSprout(centerX + swayOffset, centerY, color)
                GrowthStage.PLANT -> drawPlant(centerX + swayOffset, centerY, color)
                GrowthStage.FLOWER -> drawFlower(centerX + swayOffset, centerY, color)
                GrowthStage.FRUIT -> drawFruit(centerX + swayOffset, centerY, color)
                GrowthStage.WITHERING -> drawWithering(centerX, centerY, unhealthyColor)
                GrowthStage.DEAD -> drawDead(centerX, centerY, unhealthyColor)
            }
        }
    }
}

private fun DrawScope.drawSeed(x: Float, y: Float, color: Color) {
    // Simple oval seed
    drawCircle(
        color = color.copy(alpha = 0.8f),
        radius = 15f,
        center = Offset(x, y)
    )
    drawCircle(
        color = color,
        radius = 12f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawSprout(x: Float, y: Float, color: Color) {
    // Stem
    val stemPath = Path().apply {
        moveTo(x, y)
        lineTo(x, y - 40)
    }
    drawPath(
        path = stemPath,
        color = color,
        style = Stroke(width = 4f)
    )

    // Small leaves
    drawCircle(
        color = color.copy(alpha = 0.7f),
        radius = 8f,
        center = Offset(x - 10, y - 30)
    )
    drawCircle(
        color = color.copy(alpha = 0.7f),
        radius = 8f,
        center = Offset(x + 10, y - 25)
    )
}

private fun DrawScope.drawPlant(x: Float, y: Float, color: Color) {
    // Main stem
    val stemPath = Path().apply {
        moveTo(x, y)
        lineTo(x, y - 70)
    }
    drawPath(
        path = stemPath,
        color = color.copy(alpha = 0.9f),
        style = Stroke(width = 5f)
    )

    // Multiple leaves
    val leafPositions = listOf(
        Offset(x - 15, y - 50),
        Offset(x + 15, y - 45),
        Offset(x - 12, y - 35),
        Offset(x + 12, y - 30)
    )

    leafPositions.forEach { pos ->
        drawOval(
            color = color,
            topLeft = Offset(pos.x - 10, pos.y - 5),
            size = androidx.compose.ui.geometry.Size(20f, 12f)
        )
    }
}

private fun DrawScope.drawFlower(x: Float, y: Float, color: Color) {
    // Stem
    val stemPath = Path().apply {
        moveTo(x, y)
        lineTo(x, y - 80)
    }
    drawPath(
        path = stemPath,
        color = Color(0xFF4CAF50),
        style = Stroke(width = 5f)
    )

    // Leaves
    drawOval(
        color = Color(0xFF4CAF50),
        topLeft = Offset(x - 20, y - 50),
        size = androidx.compose.ui.geometry.Size(20f, 12f)
    )
    drawOval(
        color = Color(0xFF4CAF50),
        topLeft = Offset(x + 5, y - 45),
        size = androidx.compose.ui.geometry.Size(20f, 12f)
    )

    // Flower petals (5 petals in circle)
    val flowerCenter = Offset(x, y - 80)
    for (i in 0 until 5) {
        val angle = (i * 72) * Math.PI / 180
        val petalX = flowerCenter.x + 15 * kotlin.math.cos(angle).toFloat()
        val petalY = flowerCenter.y + 15 * sin(angle).toFloat()

        drawCircle(
            color = color,
            radius = 10f,
            center = Offset(petalX, petalY)
        )
    }

    // Flower center
    drawCircle(
        color = Color(0xFFFFC107),
        radius = 8f,
        center = flowerCenter
    )
}

private fun DrawScope.drawFruit(x: Float, y: Float, color: Color) {
    // Stem with branches
    val mainStem = Path().apply {
        moveTo(x, y)
        lineTo(x, y - 70)
    }
    drawPath(
        path = mainStem,
        color = Color(0xFF795548),
        style = Stroke(width = 6f)
    )

    // Branches
    val branches = listOf(
        Pair(Offset(x, y - 50), Offset(x - 20, y - 55)),
        Pair(Offset(x, y - 60), Offset(x + 20, y - 65))
    )

    branches.forEach { (start, end) ->
        drawLine(
            color = Color(0xFF795548),
            start = start,
            end = end,
            strokeWidth = 4f
        )
    }

    // Leaves
    drawOval(
        color = Color(0xFF4CAF50),
        topLeft = Offset(x - 25, y - 60),
        size = androidx.compose.ui.geometry.Size(20f, 12f)
    )
    drawOval(
        color = Color(0xFF4CAF50),
        topLeft = Offset(x + 15, y - 70),
        size = androidx.compose.ui.geometry.Size(20f, 12f)
    )

    // Fruits
    drawCircle(
        color = color,
        radius = 12f,
        center = Offset(x - 20, y - 50)
    )
    drawCircle(
        color = color,
        radius = 12f,
        center = Offset(x + 20, y - 60)
    )

    // Shine on fruits
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = 4f,
        center = Offset(x - 18, y - 52)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = 4f,
        center = Offset(x + 22, y - 62)
    )
}

private fun DrawScope.drawWithering(x: Float, y: Float, color: Color) {
    // Drooping stem
    val stemPath = Path().apply {
        moveTo(x, y)
        quadraticTo(
            x - 15, y - 25,
            x - 20, y - 40
        )
    }
    drawPath(
        path = stemPath,
        color = color,
        style = Stroke(width = 4f)
    )

    // Wilted leaves
    drawOval(
        color = color.copy(alpha = 0.5f),
        topLeft = Offset(x - 30, y - 45),
        size = androidx.compose.ui.geometry.Size(15f, 8f)
    )
    drawOval(
        color = color.copy(alpha = 0.5f),
        topLeft = Offset(x - 15, y - 30),
        size = androidx.compose.ui.geometry.Size(15f, 8f)
    )
}

private fun DrawScope.drawDead(x: Float, y: Float, color: Color) {
    // Fallen/dead plant remains
    val deadStemPath = Path().apply {
        moveTo(x - 20, y - 10)
        lineTo(x + 20, y - 5)
    }
    drawPath(
        path = deadStemPath,
        color = color.copy(alpha = 0.3f),
        style = Stroke(width = 3f)
    )

    // Dead leaves on ground
    drawOval(
        color = color.copy(alpha = 0.2f),
        topLeft = Offset(x - 15, y - 15),
        size = androidx.compose.ui.geometry.Size(10f, 6f)
    )
    drawOval(
        color = color.copy(alpha = 0.2f),
        topLeft = Offset(x + 5, y - 12),
        size = androidx.compose.ui.geometry.Size(10f, 6f)
    )
}