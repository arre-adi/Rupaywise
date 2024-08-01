

package com.example.rupiyawise
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rupiyawise.ui.theme.bonewhite
import com.example.rupiyawise.ui.theme.loblue
import com.example.rupiyawise.ui.theme.white
import org.json.JSONObject
import java.util.Date
import java.text.SimpleDateFormat
import java.util.*
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class Transaction(val date: Date, val amount: Double, val category: String)
data class Expense(val category: String, val amount: Double, val color: Color)

class ExpenseData(jsonString: String) {
    private val transactions: List<Transaction>
    private val firstDate: Date

    init {
        val jsonObject = JSONObject(jsonString)
        val sheet1 = jsonObject.getJSONArray("Sheet1")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val alternativeDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

        transactions = (0 until sheet1.length()).mapNotNull { i ->
            val item = sheet1.getJSONObject(i)
            val dateStr = item.getString("Date")
            val amount = item.getDouble("Amount")  // Removed space after "Amount"
            val category = item.getString("Expense Name")

            val date = try {
                dateFormat.parse(dateStr)
            } catch (e: Exception) {
                alternativeDateFormat.parse(dateStr)
            }

            if (date != null) Transaction(date, amount, category) else null
        }.sortedBy { it.date }

        firstDate = transactions.first().date
    }

    fun getExpenses(isDebit: Boolean, timeFrame: String): List<Expense> {
        val filteredTransactions = when (timeFrame) {
            "Today" -> transactions.filter { isSameDay(it.date, firstDate) }
            "Weekly" -> transactions.filter { isWithinWeek(it.date, firstDate) }
            else -> transactions // "Monthly"
        }

        return filteredTransactions
            .filter { if (isDebit) it.amount < 0 else it.amount > 0 }
            .groupBy { it.category }
            .map { (category, transactions) ->
                Expense(
                    category,
                    transactions.sumOf { kotlin.math.abs(it.amount) },
                    getCategoryColor(category)
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun isWithinWeek(date: Date, startDate: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        return date.after(startDate) && date.before(calendar.time)
    }

    private fun getCategoryColor(category: String): Color {
        return when (category) {
            "Food" -> Color(0xFF4CC452)
            "Medication" -> Color(0xFF2196F3)
            "Entertainment" -> Color(0xFFFFC107)
            "Shopping" -> Color(0xFFC90D4D)
            "Others" -> Color(0xFF9D10E9)
            "Salary" -> Color(0xFF5F9AF0)
            "Friend" -> Color(0xFF91ACE3)
            "Stocks" -> Color(0xFFFF5722)
            "Cashback" -> Color(0xFF001D47)
            else -> Color(0xFF9E9E9E)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseTrackerApp(expenseData: ExpenseData, onExpenseClick: (Expense) -> Unit) {
    var isDebit by remember { mutableStateOf(true) }
    var timeFrame by remember { mutableStateOf("Today") }
    var selectedDate by remember { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mutableStateOf(LocalDate.of(2023, 6, 1))
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(bonewhite)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TabButton(text = "Debit", selected = isDebit) { isDebit = true }
                TabButton(text = "Credit", selected = !isDebit) { isDebit = false }
            }


            Spacer(modifier = Modifier.height(12.dp))

            PieChart(expenses = expenseData.getExpenses(isDebit, timeFrame))

            Spacer(modifier = Modifier.height(12.dp))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DateSelector(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                color = white, // Light green color

                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding( 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TimeFrameButton(text = "Today", selected = timeFrame == "Today") {
                            timeFrame = "Today"
                        }
                        TimeFrameButton(text = "Weekly", selected = timeFrame == "Weekly") {
                            timeFrame = "Weekly"
                        }
                        TimeFrameButton(text = "Monthly", selected = timeFrame == "Monthly") {
                            timeFrame = "Monthly"
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    ExpenseList(
                        expenses = expenseData.getExpenses(isDebit, timeFrame),
                        onItemClick = onExpenseClick,
                    )
                }
            }
    }
}
}
@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) loblue else Color.Transparent
    val textColor = if (selected) bonewhite else loblue

    Button(onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(6.dp),
        ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier
                .padding(horizontal = 10.dp)
        )

    }
}

@Composable
fun TimeFrameButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val textColor = if (selected) Color.White else Color.Gray
    val backgroundColor = if (selected) loblue else Color.Transparent
    Button(onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier
                .padding(horizontal = 10.dp)
        )

    }
    }


@Composable
fun PieChart(expenses: List<Expense>) {
    val totalExpense = expenses.sumOf { it.amount }

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            var startAngle = 0f
            expenses.forEach { expense ->
                val sweepAngle = (expense.amount / totalExpense * 360f).toFloat()
                drawArc(
                    color = expense.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }


            drawCircle(
                color = bonewhite,
                radius = size.minDimension / 4
            )
        }
    }
}

@Composable
fun ExpenseList(expenses: List<Expense>, onItemClick: (Expense) -> Unit, modifier: Modifier = Modifier) {
    val totalExpense = expenses.sumOf { it.amount }

    LazyColumn(modifier = modifier) {
        items(expenses) { expense ->
            ExpenseItem(expense, totalExpense, onItemClick)
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, totalExpense: Double, onItemClick: (Expense) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(white)
            .padding(12.dp)
            .clickable { onItemClick(expense) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier
            .padding(vertical = 12.dp)
            .background(bonewhite))
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(expense.color)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = expense.category,
            style = MaterialTheme.typography.bodyLarge,
            color = expense.color,
            modifier = Modifier.weight(1f)
        )
        Text(

            text = "${(expense.amount / totalExpense * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            color = expense.color
        )


    }
}

fun readJsonFromAssets(context: Context, fileName: String): String {
    val inputStream: InputStream = context.assets.open(fileName)
    return inputStream.bufferedReader().use { it.readText() }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExpenseTrackerAppPreview() {
    val context = LocalContext.current
    val jsonString = readJsonFromAssets(context, "June_Expenses.json")
    val expenseData = ExpenseData(jsonString)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ExpenseTrackerApp(
            expenseData = expenseData,
            onExpenseClick = { /* Handle click event */ }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy")

    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = loblue,
                contentColor = bonewhite
            )
        ) {
            Text(selectedDate.format(formatter))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // You can customize this to show a range of dates or a calendar picker
            for (i in 0..29) {
                val date = LocalDate.of(2023, 6, 1).plusDays(i.toLong())
                DropdownMenuItem(
                    text = { Text(
                        date.format(formatter)
                    )},
                    onClick = {
                        onDateSelected(date)
                        expanded = false
                    }
                )
            }
        }
    }
}