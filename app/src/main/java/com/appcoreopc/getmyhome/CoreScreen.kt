package com.appcoreopc.getmyhome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.data.const.AppDestinations
import com.appcoreopc.getmyhome.data.local.UserReport
import com.appcoreopc.getmyhome.ui.theme.BackgroundDark
import com.appcoreopc.getmyhome.ui.theme.PrimaryPurple
import com.appcoreopc.getmyhome.ui.theme.SurfaceDark
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.material3.Checkbox
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.appcoreopc.getmyhome.data.local.UserProfile
import com.appcoreopc.getmyhome.ui.theme.CardGradientEnd
import com.appcoreopc.getmyhome.ui.theme.CardGradientStart
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width

@Composable
fun GetMyHomeApp(viewModel: HomeViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestinations.HOME) }
    var location by rememberSaveable { mutableStateOf("") }
    var propertyType by rememberSaveable { mutableStateOf("") }
    var useGraphQL by rememberSaveable { mutableStateOf(1) }
    val reportContent by viewModel.reportContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userReports by viewModel.userReports.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    viewModel.setUserId("user-123")
    val userId by viewModel.userId.collectAsState()

    val gradientBrush = Brush.linearGradient(
        colors = listOf(CardGradientStart, CardGradientEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    LaunchedEffect(currentDestination) {
        if (currentDestination == AppDestinations.INSIGHTS) {
            viewModel.fetchReports(userId)
        } else if (currentDestination == AppDestinations.SETTINGS) {
            viewModel.fetchUserProfile(userId)
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label,
                            tint = if (it == currentDestination) PrimaryPurple else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = it.label,
                            color = if (it == currentDestination) PrimaryPurple else TextSecondary
                        )
                    },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                )
            }
        },
        containerColor = BackgroundDark,
        contentColor = TextPrimary
    ) {
        when (currentDestination) {

            AppDestinations.HOME -> {
                HomeUIComponentsBuild(
                    location = location,
                    onLocationChange = { location = it },
                    propertyType = propertyType,
                    onPropertyTypeChange = { propertyType = it },
                    useGraphQL = useGraphQL,
                    reportContent = reportContent,
                    viewModel = viewModel
                )
            }

            AppDestinations.INSIGHTS -> {
                InsightsScreen(
                    isLoading = isLoading,
                    userReports = userReports,
                    gradientBrush = gradientBrush,
                    viewModel = viewModel
                )
            }
            AppDestinations.SETTINGS -> {
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
                                            text = "What matter most when choosing your property",
                                            color = TextPrimary.copy(alpha = 0.8f),
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Set what is important",
                                            color = TextPrimary,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Pick from the list",
                                            color = TextPrimary.copy(alpha = 0.7f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                text = "User Profile",
                                color = PrimaryPurple,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(userProfile) { profile ->
                            UserProfileCard(userProfile = profile)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: UserReport, viewModel: HomeViewModel) {

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
            Text(
                text = report.currentAnalysis,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UserProfileCard(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileItem(label = "Property Price", value = userProfile.propertyPrice.toString())
            ProfileItem(label = "Property Price Increase", value = userProfile.propertyPriceIncrease.toString())
            ProfileItem(label = "Proximity to Amenities", value = userProfile.proximityAmenities.toString())
            ProfileItem(label = "Proximity to Schools", value = userProfile.proximitySchools.toString())
            ProfileItem(label = "Proximity to Train Station", value = userProfile.proximityTrainStation.toString())
            ProfileItem(label = "Natural Hazard Risk", value = userProfile.naturalHazardRisk.toString())
        }
    }
}

@Composable
    private fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
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



