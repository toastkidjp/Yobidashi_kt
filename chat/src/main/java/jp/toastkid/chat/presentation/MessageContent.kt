package jp.toastkid.chat.presentation

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.intent.BitmapShareIntentFactory
import jp.toastkid.ui.image.EfficientImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun MessageContent(
    text: String,
    base64Image: String? = null,
    modifier: Modifier
) {
    val viewModel = remember { MessageContentViewModel() }
    val context = LocalContext.current

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
            Box {
                EfficientImage(
                    viewModel.image(),
                    contentDescription = text,
                    placeholder = painterResource(jp.toastkid.chat.R.drawable.ic_gen_ai_image),
                    modifier = Modifier.clickable(onClick = viewModel::openImageDropdownMenu)
                )
                DropdownMenu(
                    viewModel.openingImageDropdownMenu(),
                    onDismissRequest = viewModel::closeImageDropdownMenu
                ) {
                    if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
                        DropdownMenuItem(
                            { Text("Store") },
                            {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val contentResolver = context.contentResolver
                                    val values = ContentValues().also { contentValues ->
                                        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "chat_image_${System.currentTimeMillis()}.png")
                                    }

                                    val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                                        ?: return@launch

                                    contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                                        viewModel.image().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                        outputStream.flush()
                                    }
                                }
                                viewModel.closeImageDropdownMenu()
                            }
                        )
                    }
                    DropdownMenuItem(
                        { Text("Share") },
                        {
                            context.startActivity(BitmapShareIntentFactory().invoke(context, viewModel.image()))
                            viewModel.closeImageDropdownMenu()
                        }
                    )
                }
            }
        }

        LaunchedEffect(base64Image) {
            if (base64Image.isNullOrEmpty()) {
                return@LaunchedEffect
            }

            withContext(Dispatchers.IO) {
                viewModel.loadImage(base64Image)
            }
        }
    }
}