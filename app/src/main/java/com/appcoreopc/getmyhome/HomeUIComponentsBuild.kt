package com.appcoreopc.getmyhome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.ui.theme.BackgroundDark
import com.appcoreopc.getmyhome.ui.theme.CardGradientEnd
import com.appcoreopc.getmyhome.ui.theme.CardGradientStart
import com.appcoreopc.getmyhome.ui.theme.PrimaryPurple
import com.appcoreopc.getmyhome.ui.theme.SurfaceDark
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.TextSecondary
import androidx.compose.foundation.layout.Box

@Composable
fun HomeUIComponentsBuild(
    location: String,
    onLocationChange: (String) -> Unit,
    propertyType: String,
    onPropertyTypeChange: (String) -> Unit,
    useGraphQL: Int,
    reportContent: String?,
    viewModel: HomeViewModel
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(CardGradientStart, CardGradientEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Search Properties",
                        color = TextPrimary.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get My Home",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Find your dream home",
                        color = TextPrimary.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Search Form
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text("Location", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            OutlinedTextField(
                value = propertyType,
                onValueChange = onPropertyTypeChange,
                label = { Text("Property Type", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

            }

            Button(
                onClick = {
                    when (useGraphQL) {
                        0 -> viewModel.searchPropertyGraphQL(location, propertyType)
                        1 -> viewModel.analyzeStreamGraphQL(location, propertyType)
                        else -> viewModel.searchPropertyREST(location, propertyType)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Search Now", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = { viewModel.generateReportGraphQL() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Text("Report", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (reportContent != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(top = 16.dp)
                ) {
                    items(reportContent.split("\n")) { line ->
                        Text(
                            text = line,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
