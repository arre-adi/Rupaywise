package com.example.rupiyawise


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rupiyawise.ui.theme.limewhite
import com.example.rupiyawise.ui.theme.vermilion

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RupiyawiseApp() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(limewhite)
            .padding(16.dp)
    ) {
        item { BalanceCard() }
        item { BudgetCard() }
        item {
            Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
            ) {
            CashCard("Income", "₹ 1,800.00", Color(0xFFD0E8C0))
            CashCard("Expense", "₹ 1,800.00", Color(0xFFFFC1C1))
         }
        }
    }
}

@Composable
fun BalanceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                vermilion,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Available Balance",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                "₹ 3,578",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BudgetCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(
                Color(0xFFB0BEC5),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Budget for October",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                "₹ 2,478",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}





@Composable
fun CashCard(title: String, amount: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .background(
                backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

