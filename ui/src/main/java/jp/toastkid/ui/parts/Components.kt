package jp.toastkid.ui.parts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun CheckableRow(
    textId: Int,
    checked: () -> Boolean,
    onSwitch: (Boolean) -> Unit,
    iconTint: Color? = null,
    iconId: Int? = null
) {
    val currentChecked = rememberUpdatedState(checked)
    val currentOnSwitch = rememberUpdatedState(onSwitch)

    val onClick = remember {
        {
            val isChecked = currentChecked.value()
            currentOnSwitch.value(isChecked.not())
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        if (iconId != null && iconTint != null) {
            Icon(
                painterResource(id = iconId),
                tint = iconTint,
                contentDescription = stringResource(id = textId),
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
        )
        Checkbox(
            checked = checked(),
            onCheckedChange = { onClick() },
            modifier = Modifier.width(44.dp)
        )
    }
}

@Composable
fun SwitchRow(
    textId: Int,
    checked: () -> Boolean,
    onSwitch: (Boolean) -> Unit,
    iconTint: Color? = null,
    iconId: Int? = null
) {
    val currentChecked = rememberUpdatedState(checked)
    val currentOnSwitch = rememberUpdatedState(onSwitch)

    val onClick = remember {
        {
            val isChecked = currentChecked.value()
            currentOnSwitch.value(isChecked.not())
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        if (iconId != null && iconTint != null) {
            Icon(
                painterResource(id = iconId),
                tint = iconTint,
                contentDescription = stringResource(id = textId),
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            stringResource(id = textId),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked(),
            onCheckedChange = { onClick() },
            modifier = Modifier.width(44.dp)
        )
    }
}