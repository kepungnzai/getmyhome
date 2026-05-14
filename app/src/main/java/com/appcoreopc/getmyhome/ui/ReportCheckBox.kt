import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ReportCheckbox(checked: Boolean) {
    var isChecked by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // 2. The Checkbox component
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it } // Update state when clicked
        )
    }
}