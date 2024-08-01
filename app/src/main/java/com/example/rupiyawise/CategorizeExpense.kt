package com.example.rupiyawise

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rupiyawise.ui.theme.loblue
import com.example.rupiyawise.ui.theme.vermilion
import com.example.rupiyawise.ui.theme.white
import kotlin.math.roundToInt

data class CategoryBox(val name: String, val iconResId: Int, var value: Int = 55, val isWide: Boolean = false, var bounds: Rect = Rect.Zero)
data class DraggableItem(val name: String, val value: Int, var offset: Offset = Offset.Zero)

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CategorizeExpenseScreen() {
    val categoryBoxes = remember {
        mutableStateListOf(
            CategoryBox("Shopping", R.drawable.style_linear, value = 342),
            CategoryBox("Entertainment", R.drawable.popcorn_svgrepo_com, value = 289),
            CategoryBox("Medication", R.drawable.heart, value = 969),
            CategoryBox("Food", R.drawable.food_dinner_svgrepo_com, value = 959),
            CategoryBox("Others", R.drawable.oothers, value = 589,isWide = true)
        )
    }

    val draggableItems = remember {
        mutableStateListOf(
            DraggableItem("Mantra", 200),
            DraggableItem("Nojio", 600),
            DraggableItem("MUJI", 200),
            DraggableItem("Meso", 250)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.padding(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categoryBoxes.take(3).forEach { category ->
                CategoryBoxItem(category, Modifier.weight(1f))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryBoxItem(categoryBoxes[3], Modifier.weight(1f))
            CategoryBoxItem(categoryBoxes[4], Modifier.weight(2f))
        }

        // Red draggable items
        draggableItems.forEach { item ->
            DraggableItemComposable(item, categoryBoxes) { updatedItem, newOffset ->
                val index = draggableItems.indexOf(item)
                if (index != -1) {
                    if (newOffset == Offset.Zero) {
                        // Item was dropped in a category, remove it from the list
                        draggableItems.removeAt(index)
                    } else {
                        // Update the item's position
                        draggableItems[index] = updatedItem.copy(offset = newOffset)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBoxItem(category: CategoryBox, modifier: Modifier = Modifier) {
    var boxBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier
            .height(100.dp)
            .shadow(
                1.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .background(white, RoundedCornerShape(8.dp))

            .padding(16.dp)

            .onGloballyPositioned { layoutCoordinates ->
                val boundsInWindow = layoutCoordinates.boundsInWindow()
                boxBounds = boundsInWindow
            },
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = category.iconResId),
                    contentDescription = category.name,
                    modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(loblue)
                )
                Text(
                    "₹ ${category.value}",
                    color = vermilion,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                category.name,
                color = loblue,
                fontSize = 12.sp
            )
        }
    }

    category.bounds = boxBounds
}

@Composable
fun DraggableItemComposable(
    item: DraggableItem,
    categoryBoxes: List<CategoryBox>,
    onDragEnd: (DraggableItem, Offset) -> Unit
) {
    var updatedOffset by remember { mutableStateOf(item.offset) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .offset { IntOffset(updatedOffset.x.roundToInt(), updatedOffset.y.roundToInt()) }
            .shadow(
                1.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(white, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val droppedInCategory = categoryBoxes.find { category ->
                            category.bounds.contains(updatedOffset)
                        }
                        if (droppedInCategory != null) {
                            droppedInCategory.value += item.value
                            // Remove the item from the list of draggable items
                            onDragEnd(item.copy(offset = Offset.Zero), Offset.Zero)
                        } else {
                            // If not dropped in a category, reset to original position
                            onDragEnd(item, item.offset)
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    updatedOffset += dragAmount
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.lulaaa),
                contentDescription = item.name,
                modifier = Modifier.size(24.dp)
                       , colorFilter = ColorFilter.tint(vermilion)
            )
            Text(item.name, color = loblue, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("₹ ${item.value}", color = vermilion, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
