package org.example.project.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.strings.SmartTutorStrings.change_password_title
import org.example.project.strings.SmartTutorStrings.delete_account
import org.example.project.strings.SmartTutorStrings.settings_title
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun SettingsScreen(
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(end = spacing16)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = settings_title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(spacing24))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { onChangePasswordClick() }
                .padding(vertical = spacing16, horizontal = spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.width(spacing12))
            Text(change_password_title, fontSize = 18.sp)
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { onDeleteAccountClick() }
                .size(spacing16)
                .padding(horizontal = spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(spacing12))
            Text(delete_account, fontSize = 8.sp)
        }
    }
}
