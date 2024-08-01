@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rupiyawise


import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rupiyawise.ui.theme.boblue
import com.example.rupiyawise.ui.theme.bonewhite
import com.example.rupiyawise.ui.theme.gradient
import com.example.rupiyawise.ui.theme.loblue
import com.example.rupiyawise.ui.theme.white
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch


data class Goal(val name: String, val amount: String, val monthly:String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RupiyawiseApp(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    var goals by remember { mutableStateOf(listOf<Goal>()) }
    var isMicActive by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val speechRecognizer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenText = results?.get(0) ?: ""
            isMicActive = false
            handleVoiceCommand(spokenText, navController)
        }
    }

    val recordAudioPermissionState = rememberPermissionState(
        "android.permission.RECORD_AUDIO"
    )

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            BottomSheetContent(
                onDismiss = {
                    scope.launch {
                        sheetState.bottomSheetState.hide()
                    }
                },
                onSaveGoal = { newGoal ->
                    goals = goals + newGoal
                    scope.launch {
                        sheetState.bottomSheetState.hide()
                    }
                }
            )
        },
        sheetShape = RoundedCornerShape(16.dp),
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                MainContent(
                    paddingValues = paddingValues,
                    goals = goals,
                    onAddGoalClick = {
                        scope.launch {
                            sheetState.bottomSheetState.expand()
                        }
                    }
                )
                FloatingMicButton(
                    isMicActive = isMicActive,
                    onMicClick = {
                        when {
                            recordAudioPermissionState.status.isGranted -> {
                                isMicActive = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                                }
                                speechRecognizer.launch(intent)
                            }
                            recordAudioPermissionState.status.shouldShowRationale -> {
                                spokenText = "Permission is needed for speech recognition."
                            }
                            else -> {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        }
                    },
                    spokenText = spokenText
                )
            }
        }
    )
}

@Composable
fun MainContent(paddingValues: PaddingValues, goals: List<Goal>, onAddGoalClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bonewhite)
            .padding(paddingValues)
            .padding(16.dp)
    ) {

        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { BalanceCard() }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { MonthlyBudgetCard(
            1550,5000
        ) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ExpenseIncomeCards()}
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { GoalsSection(goals, onAddGoalClick) }
    }
}

@Composable
fun BalanceCard() {
   Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)

            .background(
                gradient,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Available Balance",
                modifier = Modifier
                    .padding(bottom = 15.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                "₹ 3,578",
                modifier = Modifier
                    .padding(bottom = 20.dp),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "See details",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonthlyBudgetCard(
    current: Int,
    total: Int
) {
    val progress = current.toFloat() / total.toFloat()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .background(
                white,
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = (-1).dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = boblue,
                spotColor = boblue
            )
            .padding(16.dp)
            .fillMaxWidth()
            .height(150.dp),
    ) {
        Text(
            text = "Monthly budget",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Text(
                    text = "$current",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                    color = loblue
                )
                Text(
                    text = "left of $total",
                    style = MaterialTheme.typography.bodyLarge,
                    color = loblue
                )
            }


            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawArc(
                         gradient,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                    drawArc(
                        color = white,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }


        }
    }
}


@Composable
fun ExpenseIncomeCards() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FinanceCard(
            title = "Expense",
            amount = 2000,
            iconContent = { Icon(painter = painterResource(id = R.drawable.arrow_up), contentDescription = "Expense increasing", tint = white)},
        )
        FinanceCard(
            title = "Income",
            amount = 1000,
            iconContent = { Icon(painter = painterResource(id = R.drawable.arrow_down), contentDescription = "Income decreasing", tint = white) },
        )
    }
}

@Composable
fun FinanceCard(
    title: String,
    amount: Int,
    iconContent: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .height(170.dp)
            .width(170.dp)
            .shadow(
                0.8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = boblue,
                spotColor = boblue
            ),
        colors = CardDefaults.cardColors(containerColor = white)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount.toString(),
                color = loblue,
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(loblue),
                    contentAlignment = Alignment.Center
                ) {
                    iconContent()
                }
            }
        }
    }
}

@Composable
fun GoalsSection(goals: List<Goal>, onAddGoalClick: () -> Unit) {
    Text(
        text = "Goals",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(goals) { goal ->
        GoalCard(goal)
    }
        item { AddGoalCard(onAddGoalClick) }

    }
}

@Composable
fun GoalCard(goal: Goal) {
    Card(
        modifier = Modifier
            .size(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = goal.name, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = goal.amount, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = goal.monthly, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AddGoalCard(onAddClick: () -> Unit) {
   Box(
        modifier = Modifier
            .size(150.dp)
            .clickable(onClick = onAddClick)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            ),

    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Goal",
                tint = loblue,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Goal",
                style = MaterialTheme.typography.bodyLarge,
                color = loblue,
            )
        }
    }
}


@Composable
fun BottomSheetContent(onDismiss: () -> Unit, onSaveGoal: (Goal) -> Unit) {
    var goalName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
    var monthlyAmount by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(white)
            .padding(16.dp)
    ) {
        Text(
            text = "Add New Goal",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = goalName,
            onValueChange = { goalName = it },
            label = { Text("Goal Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        OutlinedTextField(
            value = goalAmount,
            onValueChange = { goalAmount = it },
            label = { Text("Goal Amount") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )      )

        OutlinedTextField(
            value = monthlyAmount,
            onValueChange = { monthlyAmount = it },
            label = { Text("Monthly Contribution") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )      )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                containerColor = loblue,
                contentColor = white
            ),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = loblue,
                    contentColor = white
                ),
                onClick = {
                    if (goalName.isNotBlank() && goalAmount.isNotBlank()) {
                        onSaveGoal(Goal(goalName, goalAmount, monthlyAmount))
                    }
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                Text("Set Goal")
            }
        }
    }
}

@Composable
fun FloatingMicButton(
    isMicActive: Boolean,
    onMicClick: () -> Unit,
    spokenText: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (spokenText.isNotEmpty()) {
                SpokenTextBox(spokenText = spokenText)
                Spacer(modifier = Modifier.height(16.dp))
            }
            FloatingActionButton(
                onClick = onMicClick,
                containerColor = loblue,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = if (isMicActive) R.drawable.baseline_mic_off_24 else R.drawable.baseline_mic_24),
                    contentDescription = if (isMicActive) "Turn off mic" else "Turn on mic",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SpokenTextBox(spokenText: String) {
    Box(
        modifier = Modifier
            .widthIn(max = 250.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .shadow(4.dp)
            .padding(12.dp)
    ) {
        Text(
            text = spokenText,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

fun handleVoiceCommand(command: String, navController: NavHostController) {
    when {
        command.contains("statistics", ignoreCase = true) -> {
            navController.navigate(BottomBarScreen.Statistics.route)
        }
        command.contains("sort expense", ignoreCase = true) -> {
            navController.navigate(BottomBarScreen.Categorize.route)
        }
        command.contains("reels", ignoreCase = true) -> {
            navController.navigate(BottomBarScreen.Reels.route)
        }
        // Add more voice commands as needed
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScreenWithFloatingMic(
    content: @Composable () -> Unit,
    navController: NavHostController
) {
    var isMicActive by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val speechRecognizer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenText = results?.get(0) ?: ""
            isMicActive = false
            handleVoiceCommand(spokenText, navController)
        }
    }

    val recordAudioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        FloatingMicButton(
            isMicActive = isMicActive,
            onMicClick = {
                when {
                    recordAudioPermissionState.status.isGranted -> {
                        isMicActive = true
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                        }
                        speechRecognizer.launch(intent)
                    }
                    recordAudioPermissionState.status.shouldShowRationale -> {
                        spokenText = "Permission is needed for speech recognition."
                    }
                    else -> {
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                }
            },
            spokenText = spokenText
        )
    }
}