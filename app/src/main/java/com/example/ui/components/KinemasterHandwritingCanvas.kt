package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NewsAmber
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsRedPrimary

enum class HandwritingTool {
    PEN, ARROW, CIRCLE, RECTANGLE
}

data class DrawnStroke(
    val pathPoints: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val tool: HandwritingTool
)

/**
 * Authentic KineMaster Handwriting & Drawing Layer:
 * Allows user to mark video with finger, draw arrows pointing to breaking news,
 * draw circles around evidence, or highlight text boxes.
 */
@Composable
fun KinemasterHandwritingCanvas(
    isActive: Boolean,
    onSaveAndClose: (strokeCount: Int) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val strokes = remember { mutableStateListOf<DrawnStroke>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedTool by remember { mutableStateOf(HandwritingTool.PEN) }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var strokeThickness by remember { mutableStateOf(6f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("handwriting_layer")
    ) {
        // Transparent touch drawing canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedTool, selectedColor, strokeThickness) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints = currentPoints + change.position
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                strokes.add(
                                    DrawnStroke(
                                        pathPoints = currentPoints,
                                        color = selectedColor,
                                        strokeWidth = strokeThickness,
                                        tool = selectedTool
                                    )
                                )
                                currentPoints = emptyList()
                            }
                        }
                    )
                }
        ) {
            // Render completed strokes
            strokes.forEach { stroke ->
                drawCustomStroke(stroke)
            }

            // Render current drawing stroke in progress
            if (currentPoints.isNotEmpty()) {
                drawCustomStroke(
                    DrawnStroke(
                        pathPoints = currentPoints,
                        color = selectedColor,
                        strokeWidth = strokeThickness,
                        tool = selectedTool
                    )
                )
            }
        }

        // Top / Bottom Floating Toolbar for Handwriting
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .fillMaxWidth(0.96f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, NewsGold)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Tools & Colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tool selectors: Pen, Arrow, Circle, Rect
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            HandwritingTool.PEN to "पेन",
                            HandwritingTool.ARROW to "तीर ↗",
                            HandwritingTool.CIRCLE to "गोला ⭕",
                            HandwritingTool.RECTANGLE to "बॉक्स 🔲"
                        ).forEach { (tool, label) ->
                            val isSel = selectedTool == tool
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NewsRedPrimary else Color(0xFF1E293B))
                                    .border(0.5.dp, if (isSel) NewsGold else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable { selectedTool = tool }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Done & Clear buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                strokes.clear()
                                onClearAll()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "साफ़ करें", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = { onSaveAndClose(strokes.size) },
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF2E7D32), CircleShape)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "सहेजें", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Row 2: Color Palette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("रंग:", color = Color.Gray, fontSize = 10.sp)
                    listOf(
                        Color.Red to "लाल",
                        Color.Yellow to "पीला",
                        Color.Green to "हरा",
                        Color.Cyan to "नीला",
                        Color.White to "सफ़ेद"
                    ).forEach { (color, _) ->
                        val isSel = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSel) 2.5.dp else 1.dp,
                                    color = if (isSel) Color.White else Color.DarkGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "स्क्रीन पर उंगली घुमाएं ☝️",
                        color = NewsAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCustomStroke(stroke: DrawnStroke) {
    if (stroke.pathPoints.isEmpty()) return

    when (stroke.tool) {
        HandwritingTool.PEN -> {
            if (stroke.pathPoints.size == 1) {
                drawCircle(
                    color = stroke.color,
                    radius = stroke.strokeWidth / 2f,
                    center = stroke.pathPoints[0]
                )
            } else {
                val path = Path()
                path.moveTo(stroke.pathPoints[0].x, stroke.pathPoints[0].y)
                for (i in 1 until stroke.pathPoints.size) {
                    val p = stroke.pathPoints[i]
                    path.lineTo(p.x, p.y)
                }
                drawPath(
                    path = path,
                    color = stroke.color,
                    style = Stroke(
                        width = stroke.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        HandwritingTool.ARROW -> {
            val start = stroke.pathPoints.first()
            val end = stroke.pathPoints.last()
            drawLine(
                color = stroke.color,
                start = start,
                end = end,
                strokeWidth = stroke.strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrowhead
            val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
            val arrowLength = 24.0
            val arrowAngle = Math.PI / 6
            val x1 = end.x - (arrowLength * Math.cos(angle - arrowAngle)).toFloat()
            val y1 = end.y - (arrowLength * Math.sin(angle - arrowAngle)).toFloat()
            val x2 = end.x - (arrowLength * Math.cos(angle + arrowAngle)).toFloat()
            val y2 = end.y - (arrowLength * Math.sin(angle + arrowAngle)).toFloat()

            drawLine(color = stroke.color, start = end, end = Offset(x1, y1), strokeWidth = stroke.strokeWidth, cap = StrokeCap.Round)
            drawLine(color = stroke.color, start = end, end = Offset(x2, y2), strokeWidth = stroke.strokeWidth, cap = StrokeCap.Round)
        }

        HandwritingTool.CIRCLE -> {
            val start = stroke.pathPoints.first()
            val end = stroke.pathPoints.last()
            val center = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
            val radius = Math.hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat() / 2f
            drawCircle(
                color = stroke.color,
                radius = radius.coerceAtLeast(10f),
                center = center,
                style = Stroke(width = stroke.strokeWidth)
            )
        }

        HandwritingTool.RECTANGLE -> {
            val start = stroke.pathPoints.first()
            val end = stroke.pathPoints.last()
            val left = minOf(start.x, end.x)
            val top = minOf(start.y, end.y)
            val width = Math.abs(end.x - start.x).coerceAtLeast(10f)
            val height = Math.abs(end.y - start.y).coerceAtLeast(10f)

            drawRect(
                color = stroke.color,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(width, height),
                style = Stroke(width = stroke.strokeWidth)
            )
        }
    }
}
