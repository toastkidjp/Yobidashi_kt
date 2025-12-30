package jp.toastkid.sensor.presentation.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.sensor.application.model.Destination
import jp.toastkid.sensor.presentation.view.tab.HomeView
import jp.toastkid.sensor.presentation.view.tab.SensorView

@Composable
fun SensorSwitcherView() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val currentDestination = remember { mutableStateOf(Destination.HOME) }

    Surface(
        shadowElevation = 4.dp,
    ) {
        when(currentDestination.value) {
            Destination.HOME -> HomeView()
            else -> SensorView(currentDestination.value)
        }
    }

    LaunchedEffect(Unit) {
        contentViewModel.replaceAppBarContent {
            val color = MaterialTheme.colorScheme.primary
            val tint = MaterialTheme.colorScheme.secondary

            Row {
                Destination.entries.forEach {
                    val alpha = if (it == currentDestination.value) 1f else 0.75f

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .drawBehind {
                                drawRect(color.copy(alpha = alpha))
                            }
                            .clickable {
                                currentDestination.value = it
                            }
                    ) {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label,
                            tint = tint,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            it.label,
                            color = tint,
                            fontSize = 10.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
