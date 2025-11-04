package com.lerchenflo.hallenmanager.sharedUi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * LegendOverlay
 *
 * @param modifier normal Compose modifier for placement (use Modifier.align(Alignment.BottomStart) inside the Box)
 * @param scale current visual scale (same scale used in graphicsLayer)
 * @param gridSpacingInContentPx the grid spacing you draw in CONTENT coordinates (the same value you used when drawing the grid)
 * @param metersPerGrid how many meters correspond to one grid square (e.g. 0.5f)
 */
@Composable
fun LegendOverlay(
    modifier: Modifier = Modifier,
    scale: Float,
    gridSpacingInContentPx: Float,
    metersPerGrid: Float = 0.2f,
    padding: Dp = 12.dp,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    // small safety
    if (gridSpacingInContentPx <= 0f || metersPerGrid <= 0f) {
        Box(modifier = modifier.padding(padding)) {
            Text("scale unavailable", fontSize = 12.sp)
        }
        return
    }

    // Screen pixels per grid square = contentPx * scale
    val gridPxOnScreen = gridSpacingInContentPx * scale

    // meters per screen pixel:
    // metersPerGrid meters per grid square, and gridPxOnScreen screen pixels per grid square
    val metersPerScreenPx = metersPerGrid / gridPxOnScreen

    // desired visual length for ruler in screen px (target range — we'll pick a 'nice' meter close to this)
    val desiredPx = 350f

    // meters represented by desiredPx on screen
    val metersAtDesiredPx = metersPerScreenPx * desiredPx

    // choose a nice round meters length for the ruler (0.1,0.2,0.5,1,2,5,10 ...)
    val niceMeters = chooseNiceStep(metersAtDesiredPx)

    // compute actual pixel length on screen for that 'niceMeters'
    val rulerPx = (niceMeters / metersPerScreenPx).coerceAtLeast(8f) // at least visible

    // Compose UI
    Box(modifier = modifier.padding(padding)) {
        Column {

            // Ruler canvas: draw bar and ticks, then label
            Canvas(
                modifier = Modifier
                    .width((rulerPx / density()).dp) // convert px to dp for Canvas width
                    .height(28.dp)
            ) {
                val w = size.width
                val h = size.height
                val strokeY = h * 0.5f
                // main line
                drawLine(
                    color = barColor,
                    start = Offset(0f, strokeY),
                    end = Offset(w, strokeY),
                    strokeWidth = 2f
                )
                // left tick
                drawLine(
                    color = barColor,
                    start = Offset(0f, strokeY - 6f),
                    end = Offset(0f, strokeY + 6f),
                    strokeWidth = 2f
                )
                // right tick
                drawLine(
                    color = barColor,
                    start = Offset(w, strokeY - 6f),
                    end = Offset(w, strokeY + 6f),
                    strokeWidth = 2f
                )

                // intermediate ticks (optional small ticks)
                val thirds = 4
                for (i in 1 until thirds) {
                    val x = w * (i.toFloat() / thirds)
                    drawLine(
                        color = barColor,
                        start = Offset(x, strokeY - 4f),
                        end = Offset(x, strokeY + 4f),
                        strokeWidth = 1.2f
                    )
                }
            }

            //Spacer(modifier = Modifier.height(4.dp))

            // Label centered under the ruler
            Box(modifier = Modifier.width((rulerPx / density()).dp)) {
                Text(
                    text = Formatter.formatMeters(niceMeters.toDouble()),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/** helper: pick a “nice” value near v (1/2/5 × powers of 10) */
private fun chooseNiceStep(v: Float): Float {
    if (v <= 0f) return 0.1f
    val exp = floor(log10(v.toDouble())).toInt()
    val base = v / 10.0.pow(exp.toDouble())
    val niceBase = when {
        base <= 1.0 -> 1.0
        base <= 2.0 -> 2.0
        base <= 5.0 -> 5.0
        else -> 10.0
    }
    val result = (niceBase * 10.0.pow(exp.toDouble())).toFloat()
    // also clamp to some sensible min/max for UI
    return result.coerceIn(0.0001f, 100000f)
}

/** small helper to get density (px per dp). We compute it once per call so it's cheap. */
@Composable
private fun density(): Float = androidx.compose.ui.platform.LocalDensity.current.density


object Formatter {

    /**
     * Format a length expressed in meters into a human-friendly string using mm/cm/m
     * with [significantDigits] significant digits (default 3).
     *
     * Examples:
     *   formatMeters(2.0)   -> "2 m"
     *   formatMeters(0.5)   -> "0.5 m"
     *   formatMeters(0.005) -> "5 mm"
     */
    fun formatMeters(meters: Double, significantDigits: Int = 3): String {
        if (!meters.isFinite()) return meters.toString()

        val abs = abs(meters)
        return when {
            abs >= 1.0 -> "${formatSignificant(meters, significantDigits)} m"
            abs >= 0.01 -> {
                // show in cm
                val cm = meters * 100.0
                "${formatSignificant(cm, significantDigits)} cm"
            }
            else -> {
                // show in mm
                val mm = meters * 1000.0
                "${formatSignificant(mm, significantDigits)} mm"
            }
        }
    }

    /**
     * Format a meters-per-grid label, e.g. "0.5 m / square"
     */
    fun formatMetersPerSquare(metersPerGrid: Double, significantDigits: Int = 3): String {
        // Keep the unit consistent with formatMeters() but append " / square"
        val formatted = formatMeters(metersPerGrid, significantDigits)
        return "$formatted / square"
    }

    /**
     * Format a positive or negative number to [significantDigits] significant digits.
     * Returns a compact string without unnecessary trailing zeros (e.g. "0.5", "2", "3.14", "1.2e-4").
     *
     * This is a simple multiprecision-free implementation that:
     *  - Uses rounding to significant digits,
     *  - Removes trailing zeros & trailing decimal point from plain decimal representations,
     *  - Falls back to scientific notation for very large/small exponents produced by Double.toString().
     */
    private fun formatSignificant(value: Double, significantDigits: Int): String {
        if (value == 0.0) return "0"
        if (!value.isFinite()) return value.toString()

        val sign = if (value < 0) "-" else ""
        val v = abs(value)
        // exponent base10
        val exp = floor(log10(v)).toInt()
        // scale to keep significantDigits digits before rounding
        val scale = 10.0.pow(significantDigits - 1 - exp)
        val rounded = round(v * scale) / scale

        // Convert to string
        // Use toPlainString-like behavior: prefer normal decimal if exponent within [-6, 20], else scientific from Double.toString
        val roundedStr = if (exp in -6..20) {
            // produce a plain decimal representation and trim trailing zeros
            val raw = toDecimalString(rounded)
            trimTrailingZeros(raw)
        } else {
            // for very small/large numbers, use standard Double.toString() which may produce scientific notation
            val s = rounded.toString()
            // still try to trim trailing zeros for non-scientific forms
            if (s.contains('e') || s.contains('E')) s else trimTrailingZeros(s)
        }

        return sign + roundedStr
    }

    // Convert a Double to a decimal string with enough fractional digits to represent it,
    // without forcing scientific notation (relies on Double.toString for general correctness but
    // ensures we avoid exponential for moderate exponents).
    private fun toDecimalString(d: Double): String {
        // We avoid platform-specific formatting APIs. Double.toString() sometimes yields scientific notation.
        // We'll attempt to format using simple integer + fractional decomposition.
        val longPart = floor(d).toLong()
        val frac = d - longPart
        return if (frac == 0.0) {
            longPart.toString()
        } else {
            // Build fractional digits up to a safe limit
            val maxFracDigits = 12 // sufficient for typical display purposes
            val sb = StringBuilder()
            sb.append(longPart.toString())
            sb.append('.')
            var remainder = frac
            var produced = 0
            while (produced < maxFracDigits) {
                remainder *= 10.0
                val digit = floor(remainder).toInt()
                sb.append(('0'.code + digit).toChar())
                remainder -= digit
                produced++
                if (remainder <= 1e-15) break
            }
            sb.toString()
        }
    }

    // Remove trailing zeros and trailing decimal point
    private fun trimTrailingZeros(s: String): String {
        if (!s.contains('.')) return s
        var end = s.length
        while (end > 0 && s[end - 1] == '0') end--
        if (end > 0 && s[end - 1] == '.') end-- // remove trailing dot
        return s.substring(0, end)
    }
}
