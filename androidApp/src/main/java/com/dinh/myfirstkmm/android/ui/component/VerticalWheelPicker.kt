package com.compamy.robortc.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ronin.horizontal_wheel_picker.HorizontalWheelPicker
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun VerticalWheelPicker(
    modifier: Modifier = Modifier,
    wheelPickerHeight: Dp? = null,
    totalItems: Int,
    initialSelectedItem: Int,
    lineHeight: Dp = 2.dp,
    selectedLineWidth: Dp = 64.dp,
    multipleOfFiveLineWidth: Dp = 40.dp,
    normalLineWidth: Dp = 30.dp,
    selectedMultipleOfFiveLinePaddingBottom: Dp = 0.dp,
    normalMultipleOfFiveLinePaddingBottom: Dp = 6.dp,
    normalLinePaddingBottom: Dp = 8.dp,
    lineSpacing: Dp = 8.dp,
    lineRoundedCorners: Dp = 2.dp,
    selectedLineColor: Color = Color(0xFF00D1FF),
    unselectedLineColor: Color = Color.LightGray,
    fadeOutLinesCount: Int = 4,
    maxFadeTransparency: Float = 0.7f,
    onItemSelected: (Int) -> Unit
) {
    val screenHeightDp = LocalContext.current.resources.displayMetrics.run {
        heightPixels / density
    }.dp
    val effectiveHeight = wheelPickerHeight ?: screenHeightDp

    var currentSelectedItem by remember { mutableIntStateOf(initialSelectedItem) }
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialSelectedItem)

    val visibleItemsInfo by remember { derivedStateOf { scrollState.layoutInfo.visibleItemsInfo } }
    val firstVisibleItemIndex = visibleItemsInfo.firstOrNull()?.index ?: -1
    val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index ?: -1
    val totalVisibleItems = lastVisibleItemIndex - firstVisibleItemIndex + 1
    val middleIndex = firstVisibleItemIndex + totalVisibleItems / 2
    val bufferIndices = totalVisibleItems / 2

    LaunchedEffect(currentSelectedItem) {
        onItemSelected(currentSelectedItem)
    }

    LazyColumn(
        modifier = modifier.height(effectiveHeight),
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(totalItems + totalVisibleItems) { index ->
            val adjustedIndex = index - bufferIndices

            if (index == middleIndex) {
                currentSelectedItem = adjustedIndex
            }

            val lineWidth = when {
                index == middleIndex -> selectedLineWidth
                adjustedIndex % 5 == 0 -> multipleOfFiveLineWidth
                else -> normalLineWidth
            }


            val paddingBottom = when {
                index == middleIndex -> selectedMultipleOfFiveLinePaddingBottom
                adjustedIndex % 5 == 0 -> normalMultipleOfFiveLinePaddingBottom
                else -> normalLinePaddingBottom
            }

            val lineTransparency = calculateLineTransparency(
                index,
                totalItems,
                bufferIndices,
                firstVisibleItemIndex,
                lastVisibleItemIndex,
                fadeOutLinesCount,
                maxFadeTransparency
            )

            Row (verticalAlignment = Alignment.CenterVertically){

                if(index == middleIndex)
                {
                    Text(fontWeight = FontWeight.Bold,text ="${  currentSelectedItem-90}")
                }
                HorizontalLine(
                    lineWidth = lineWidth,
                    lineHeight = lineHeight,
                    paddingRight = paddingBottom,
                    roundedCorners = lineRoundedCorners,
                    indexAtCenter = index == middleIndex,
                    lineTransparency = lineTransparency,
                    selectedLineColor = selectedLineColor,
                    unselectedLineColor = unselectedLineColor
                )

            }


            Spacer(modifier = Modifier.height(lineSpacing))
        }
    }
}

@Composable
private fun HorizontalLine(
    lineHeight: Dp,  // This will be the height of the horizontal line
    lineWidth: Dp,   // This will be the width of the horizontal line
    paddingRight: Dp,
    roundedCorners: Dp,
    indexAtCenter: Boolean,
    lineTransparency: Float,
    selectedLineColor: Color,
    unselectedLineColor: Color
) {

    Box(
        modifier = Modifier
            .height(lineHeight)  // Height of the line
            .width(lineWidth)    // Width of the line
            .clip(RoundedCornerShape(roundedCorners))  // Rounded corners for the line
            .alpha(lineTransparency)  // Transparency for the line
            .background(if (indexAtCenter) selectedLineColor else unselectedLineColor)  // Color based on selection
            .padding(end = paddingRight)  // Optional padding to adjust position if needed
    )
}

private fun calculateLineTransparency(
    lineIndex: Int,
    totalLines: Int,
    bufferIndices: Int,
    firstVisibleItemIndex: Int,
    lastVisibleItemIndex: Int,
    fadeOutLinesCount: Int,
    maxFadeTransparency: Float
): Float {
    val actualCount = fadeOutLinesCount + 1
    val transparencyStep = maxFadeTransparency / actualCount

    return when {
        lineIndex < bufferIndices || lineIndex > (totalLines + bufferIndices) -> 0.0f
        lineIndex in firstVisibleItemIndex until firstVisibleItemIndex + fadeOutLinesCount -> {
            transparencyStep * (lineIndex - firstVisibleItemIndex + 1)
        }

        lineIndex in (lastVisibleItemIndex - fadeOutLinesCount + 1)..lastVisibleItemIndex -> {
            transparencyStep * (lastVisibleItemIndex - lineIndex + 1)
        }

        else -> 1.0f
    }
}

@Preview
@Composable
fun MyWheelPicker() {
    VerticalWheelPicker(
        modifier = Modifier.fillMaxWidth(),
        totalItems = 100,
        initialSelectedItem = 15,
        onItemSelected = { selectedIndex ->
            // Handle item selection
            println("Selected item index: $selectedIndex")
        }
    )
}
