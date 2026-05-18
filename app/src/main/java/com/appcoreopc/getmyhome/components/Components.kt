package com.appcoreopc.getmyhome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.HomeViewModel
import com.appcoreopc.getmyhome.data.local.UserProfile
import com.appcoreopc.getmyhome.data.local.UserReport
import com.appcoreopc.getmyhome.ui.theme.PrimaryPurple
import com.appcoreopc.getmyhome.ui.theme.SurfaceDark
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.TextSecondary
import com.appcoreopc.getmyhome.ui.theme.PurpleGrey40

@Composable
fun InsightCard(report: UserReport, viewModel: HomeViewModel) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = report.isChecked,
                    onCheckedChange = { isChecked ->
                        viewModel.updateReportChecked(report.reportId, isChecked)
                    }
                )
                Text(
                    text = report.location,
                    color = PrimaryPurple,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = report.propertyType,
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = report.currentAnalysis,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 5
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = if (isExpanded) "Show less ↑" else "Show more ↓",
                            color = PrimaryPurple,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UserProfileCard(
    userProfile: UserProfile,
    onPropertyPriceChange: (Int) -> Unit,
    onPropertyPriceIncreaseChange: (Int) -> Unit,
    onProximityAmenitiesChange: (Int) -> Unit,
    onProximitySchoolsChange: (Int) -> Unit,
    onProximityTrainStationChange: (Int) -> Unit,
    onNaturalHazardRiskChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileItemRating(label = "Property Price", value = userProfile.propertyPrice, onValueChange = onPropertyPriceChange)
            ProfileItemRating(
                label = "Property Price Increase",
                value = userProfile.propertyPriceIncrease,
                onValueChange = onPropertyPriceIncreaseChange
            )
            ProfileItemRating(
                label = "Proximity to Amenities",
                value = userProfile.proximityAmenities,
                onValueChange = onProximityAmenitiesChange
            )
            ProfileItemRating(label = "Proximity to Schools", value = userProfile.proximitySchools, onValueChange = onProximitySchoolsChange)
            ProfileItemRating(
                label = "Proximity to Train Station",
                value = userProfile.proximityTrainStation,
                onValueChange = onProximityTrainStationChange
            )
            ProfileItemRating(label = "Natural Hazard Risk", value = userProfile.naturalHazardRisk, onValueChange = onNaturalHazardRiskChange)
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = PrimaryPurple,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileItemRating(label: String, value: Int, onValueChange: (Int) -> Unit) {
    val color = when (value) {
        4, 5 -> Color(0xFFFF3B30)
        2, 3 -> Color(0xFFFF9500)
        else -> Color(0xFFFFD60A)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0..5).forEach { index ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (index == value) color else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { onValueChange(index) },
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = index.toString(),
                            color = if (index == value) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

