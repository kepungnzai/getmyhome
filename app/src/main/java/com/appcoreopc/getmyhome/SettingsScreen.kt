package com.appcoreopc.getmyhome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.data.local.UserProfile
import com.appcoreopc.getmyhome.ui.theme.PrimaryPurple
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.TextSecondary
import com.appcoreopc.getmyhome.components.UserProfileCard

@Composable
fun SettingsScreen(
    isLoading: Boolean,
    userProfile: List<UserProfile>,
    gradientBrush: Brush,
    onUpdateClick: (UserProfile) -> Unit
) {
    var editableProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(userProfile) {
        userProfile.firstOrNull()?.let { profile ->
            editableProfile = profile
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
    } else if (userProfile.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No profile found", color = TextSecondary, fontSize = 18.sp)
        }
    } else {
        editableProfile?.let { profile ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
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
                                    text = "What matter most when choosing your property",
                                    color = TextPrimary.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Key metrics",
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Adjust the value in the list",
                                    color = TextPrimary.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = "What matters to me are",
                        color = PrimaryPurple,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    UserProfileCard(
                        userProfile = profile,
                        onPropertyPriceChange = { editableProfile = editableProfile?.copy(propertyPrice = it) },
                        onPropertyPriceIncreaseChange = { editableProfile = editableProfile?.copy(propertyPriceIncrease = it) },
                        onProximityAmenitiesChange = { editableProfile = editableProfile?.copy(proximityAmenities = it) },
                        onProximitySchoolsChange = { editableProfile = editableProfile?.copy(proximitySchools = it) },
                        onProximityTrainStationChange = { editableProfile = editableProfile?.copy(proximityTrainStation = it) },
                        onNaturalHazardRiskChange = { editableProfile = editableProfile?.copy(naturalHazardRisk = it) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { editableProfile?.let { onUpdateClick(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Update",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}