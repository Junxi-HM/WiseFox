package com.example.wisefox.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*


@Composable
fun HomeScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .border(width = 1.dp, color = Color(0xFF000000))
        ) {
            Row() {
                statisticCard(R.string.earnings, 100)
                Spacer(modifier = Modifier.height(30.dp))
                statisticCard(R.string.expenses, 10)
            }
        }
    }
}

@Composable
fun statisticCard(name: Int, value: Int) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(50.dp)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)

    ) {
        Column() {
            Text(text = stringResource(name), color = TextSecondary)
            Text(text = value.toString(), color = TextSecondary)
        }
    }
}