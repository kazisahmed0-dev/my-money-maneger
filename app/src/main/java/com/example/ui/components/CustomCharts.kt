package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.ui.viewmodel.CategoryDistribution
import com.example.ui.viewmodel.MonthSummary
import com.example.util.CurrencyFormatter
import kotlin.math.max

private val ChartSliceColors = listOf(
    Color(0xFF2E7D32), // Dark Green
    Color(0xFF0288D1), // Light Blue
    Color(0xFFE65100), // Orange
    Color(0xFF7B1FA2), // Purple
    Color(0xFFC2185B), // Pink
    Color(0xFFFBC02D), // Yellow
    Color(0xFF00796B), // Teal
    Color(0xFF5D4037), // Brown
    Color(0xFF455A64), // Blue Grey
    Color(0xFFD32F2F)  // Red
)

@Composable
fun IncomeExpenseBarChart(
    monthsData: List<MonthSummary>,
    modifier: Modifier = Modifier,
    currency: String = "৳"
) {
    val maxVal = max(
        monthsData.maxOfOrNull { max(it.income, it.expense) } ?: 1.0,
        1.0
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(IncomeGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Income", style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(ExpenseRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Expense", style = MaterialTheme.typography.labelSmall, color = ExpenseRed)
        }

        // 12 month scrollable or adaptive canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 4.dp)
        ) {
            val width = size.width
            val height = size.height - 24.dp.toPx()
            val monthCount = monthsData.size.coerceAtLeast(1)
            val slotWidth = width / monthCount
            val barWidth = (slotWidth * 0.32f).coerceAtMost(16.dp.toPx())
            val baselineY = height

            // Draw baseline
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, baselineY),
                end = Offset(width, baselineY),
                strokeWidth = 1.dp.toPx()
            )

            monthsData.forEachIndexed { index, m ->
                val centerX = index * slotWidth + slotWidth / 2f

                // Income bar (left)
                val incomeHeight = ((m.income / maxVal) * (height - 10.dp.toPx())).toFloat()
                if (incomeHeight > 0) {
                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(centerX - barWidth - 1.dp.toPx(), baselineY - incomeHeight),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Expense bar (right)
                val expenseHeight = ((m.expense / maxVal) * (height - 10.dp.toPx())).toFloat()
                if (expenseHeight > 0) {
                    drawRoundRect(
                        color = ExpenseRed,
                        topLeft = Offset(centerX + 1.dp.toPx(), baselineY - expenseHeight),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // Month short labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthsData.forEach { m ->
                Text(
                    text = m.monthName.take(3),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DonutPieChart(
    distributions: List<CategoryDistribution>,
    modifier: Modifier = Modifier,
    currency: String = "৳",
    centerTitle: String = ""
) {
    if (distributions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No category data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 26.dp.toPx()
                var currentAngle = -90f

                distributions.forEachIndexed { index, item ->
                    val sweepAngle = (item.percentage / 100f * 360f).toFloat()
                    val color = ChartSliceColors[index % ChartSliceColors.size]

                    if (sweepAngle > 0.1f) {
                        drawArc(
                            color = color,
                            startAngle = currentAngle,
                            sweepAngle = sweepAngle - 1f, // small gap
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        )
                    }
                    currentAngle += sweepAngle
                }
            }

            if (centerTitle.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        centerTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend list
        Column(modifier = Modifier.fillMaxWidth()) {
            distributions.forEachIndexed { index, item ->
                val color = ChartSliceColors[index % ChartSliceColors.size]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item.category,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${String.format("%.1f", item.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        CurrencyFormatter.format(item.amount, currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalProgressBar(
    progressPercent: Float,
    modifier: Modifier = Modifier,
    barColor: Color = IncomeGreen,
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(barColor, RoundedCornerShape(4.dp))
        )
    }
}
