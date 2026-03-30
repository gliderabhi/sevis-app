package com.sevis.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val BigGearColor   = Color(0xFF4FC3F7)
private val SmallGearColor = Color(0xFFFFB300)
private val HubFill        = Color(0xFF0D1117)

private const val BIG_TEETH   = 12
private const val SMALL_TEETH = 8
private const val SMALL_PHASE = 360f / SMALL_TEETH / 2f

/**
 * Sevis brand logo — animated meshing gears with "SEVIS" text above.
 * Text : gear canvas width ratio is locked at 1.3 : 1.
 *
 * @param textSize  font size of the "SEVIS" wordmark; gear size is derived from it
 */
@Composable
fun SevisLogo(
    modifier: Modifier = Modifier,
    textSize: androidx.compose.ui.unit.TextUnit = 36.sp
) {
    // "SEVIS" visual width ≈ 5 chars × textSize + letter-spacing.
    // Gear canvas = that width / 1.3 so text is always 1.3× wider than gears.
    val gearCanvasWidth: Dp = (textSize.value * 6f / 1.3f).dp
    val infinite = rememberInfiniteTransition("sevisLogoGears")
    val bigAngle by infinite.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bigGear"
    )
    val smallAngle = -(bigAngle * BIG_TEETH.toFloat() / SMALL_TEETH.toFloat()) + SMALL_PHASE

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = "SEVIS",
            style = androidx.compose.ui.text.TextStyle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF8C00), Color(0xFFFFD700))
                ),
                fontSize      = textSize,
                fontWeight    = FontWeight.Bold,
                letterSpacing = (textSize.value * 0.18f).sp
            )
        )

        Spacer(Modifier.height(16.dp))

        Canvas(modifier = Modifier.size(gearCanvasWidth, gearCanvasWidth * 0.6f)) {
            val W = size.width
            val H = size.height

            val bigOuter = W * 0.265f
            val bigInner = W * 0.185f
            val bigHub   = W * 0.055f
            val bigPitch = (bigOuter + bigInner) / 2f

            val smOuter  = W * 0.155f
            val smInner  = W * 0.095f
            val smHub    = W * 0.035f
            val smPitch  = (smOuter + smInner) / 2f

            val dist     = bigPitch + smPitch
            val bigCx    = W * 0.31f
            val cy       = H * 0.52f

            drawGear(Offset(bigCx, cy),        bigInner, bigOuter, BIG_TEETH,   bigAngle,   BigGearColor,   bigHub)
            drawGear(Offset(bigCx + dist, cy), smInner,  smOuter,  SMALL_TEETH, smallAngle, SmallGearColor, smHub)
        }
    }
}

// ── Internal drawing helpers ──────────────────────────────────────────────────

internal fun DrawScope.drawGear(
    center:    Offset,
    innerR:    Float,
    outerR:    Float,
    numTeeth:  Int,
    rotateDeg: Float,
    color:     Color,
    hubR:      Float
) {
    drawPath(gearPath(center, innerR, outerR, numTeeth, rotateDeg), color)
    drawCircle(HubFill, innerR * 0.58f, center)
    drawCircle(color.copy(alpha = 0.35f), hubR * 1.4f, center, style = Stroke(1.5f))
    drawCircle(color, hubR * 0.4f, center)
}

internal fun gearPath(
    center:    Offset,
    innerR:    Float,
    outerR:    Float,
    numTeeth:  Int,
    rotateDeg: Float
): Path {
    val path      = Path()
    val step      = (2.0 * PI / numTeeth).toFloat()
    val toothHalf = step * 0.22f
    val rotRad    = (rotateDeg * PI / 180.0).toFloat()

    for (i in 0 until numTeeth) {
        val base = step * i + rotRad
        val rise = base - toothHalf
        val fall = base + toothHalf

        val ix1 = center.x + innerR * cos(rise)
        val iy1 = center.y + innerR * sin(rise)
        if (i == 0) path.moveTo(ix1, iy1) else path.lineTo(ix1, iy1)
        path.lineTo(center.x + outerR * cos(rise), center.y + outerR * sin(rise))
        path.lineTo(center.x + outerR * cos(fall), center.y + outerR * sin(fall))
        path.lineTo(center.x + innerR * cos(fall), center.y + innerR * sin(fall))

        val nextRise = step * (i + 1) + rotRad - toothHalf
        path.lineTo(center.x + innerR * cos(nextRise), center.y + innerR * sin(nextRise))
    }
    path.close()
    return path
}
