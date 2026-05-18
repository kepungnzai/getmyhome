package com.appcoreopc.getmyhome

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
import androidx.compose.ui.res.painterResource
import com.appcoreopc.getmyhome.data.const.AppDestinations
import com.appcoreopc.getmyhome.ui.theme.BackgroundDark
import com.appcoreopc.getmyhome.ui.theme.PrimaryPurple
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.TextSecondary
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.appcoreopc.getmyhome.ui.theme.CardGradientEnd
import com.appcoreopc.getmyhome.ui.theme.CardGradientStart
import androidx.compose.runtime.saveable.rememberSaveable

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
    val userId by viewModel.userId.collectAsState()

    viewModel.setUserId("user-123")

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
                SettingsScreen(
                    isLoading = isLoading,
                    userProfile = userProfile,
                    gradientBrush = gradientBrush,
                    onUpdateClick = { updatedProfile ->
                        viewModel.saveUserProfile(userId, updatedProfile)
                    }
                )
            }
        }
    }
}



