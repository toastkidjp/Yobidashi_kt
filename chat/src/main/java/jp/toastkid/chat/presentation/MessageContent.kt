package jp.toastkid.chat.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.ui.image.EfficientImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun MessageContent(
    text: String,
    base64Image: String? = null,
    modifier: Modifier
) {
    val viewModel = remember { MessageContentViewModel() }

    Column(modifier) {
        text.split("\n").forEach {
            val listLine = it.startsWith("* ")
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (listLine) {
                    Text(
                        "・ ",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Text(
                    viewModel.lineText(listLine, it),
                    fontSize = 16.sp
                )
            }
        }

        if (viewModel.showImage(base64Image)) {
            EfficientImage(
                viewModel.image(),
                contentDescription = text,
            )
        }

        LaunchedEffect(base64Image) {
            if (base64Image.isNullOrEmpty()) {
                return@LaunchedEffect
            }

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.loadImage(base64Image)
            }
        }
    }
}