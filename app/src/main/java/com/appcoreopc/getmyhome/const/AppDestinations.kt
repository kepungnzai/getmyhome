package com.appcoreopc.getmyhome.data.const
import com.appcoreopc.getmyhome.R
enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    INSIGHTS("Insights", R.drawable.ic_favorite),
    SETTINGS("Settings", R.drawable.ic_account_box),
}