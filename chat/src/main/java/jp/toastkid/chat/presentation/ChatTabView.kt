package jp.toastkid.chat.presentation

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.network.NetworkChecker
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.scroll.StateScrollerFactory
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.ui.menu.context.common.CommonContextMenuToolbarFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun ChatTabView() {
    val context = LocalContext.current
    val contentViewModel = (LocalView.current.context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }
    val apiKey = remember { PreferenceApplier(context).chatApiKey() }
    if (apiKey.isNullOrBlank()) {
        contentViewModel?.snackShort("API Key is invalid, Please set available API key for Gemini.")
        return
    }

    if (NetworkChecker().isUnavailableWiFi(context)) {
        contentViewModel?.snackShort("This function requires network connection.")
        return
    }

    val viewModel = remember { ChatTabViewModel(apiKey) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier.padding(8.dp)
    ) {
        CompositionLocalProvider(
            LocalTextToolbar provides CommonContextMenuToolbarFactory().invoke(LocalView.current)
        ) {
            SelectionContainer {
                LazyColumn(state = viewModel.scrollState()) {
                    items(viewModel.messages()) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(modifier = Modifier.weight(0.2f)) {
                                Icon(
                                    painterResource(id = jp.toastkid.lib.R.drawable.ic_share),
                                    contentDescription = stringResource(id = jp.toastkid.lib.R.string.share),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clickable {
                                            context.startActivity(
                                                ShareIntentFactory().invoke(it.text, "Chat_${System.currentTimeMillis()}.txt")
                                            )
                                        }
                                )
                                Icon(
                                    painterResource(id = jp.toastkid.lib.R.drawable.ic_clip),
                                    contentDescription = stringResource(id = jp.toastkid.lib.R.string.clip),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clickable {
                                            Clipboard.clip(context, it.text)
                                            contentViewModel?.snackShort(
                                                context.getString(
                                                    jp.toastkid.lib.R.string.message_clip_to,
                                                    "\"${it.text}\""
                                                )
                                            )
                                        }
                                )
                                Text(
                                    viewModel.name(it.role),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = viewModel.nameColor(it.role),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                )
                            }
                            MessageContent(
                                it.text,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .weight(1f)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 4.dp))
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = viewModel.autoScrollingKey(), block = {
        coroutineScope.launch {
            viewModel.scrollState().animateScrollToItem(viewModel.messages().size)
        }
    })

    contentViewModel?.replaceAppBarContent {
        TextField(
            viewModel.textInput(),
            label = { Text(viewModel.label(), color = MaterialTheme.colorScheme.onPrimary) },
            maxLines = Int.MAX_VALUE,
            onValueChange = viewModel::onValueChanged,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.secondary
            ),
            keyboardActions = KeyboardActions{
                coroutineScope.launch {
                    viewModel.send()
                }
            },
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                imeAction = ImeAction.Send
            ),
            modifier = Modifier
                .focusRequester(viewModel.focusRequester())
                .fillMaxWidth()
                .semantics { contentDescription = "Input message box." }
        )

        LaunchedEffect(key1 = Unit, block = {
            viewModel.launch(
                //chatTab.chat()
            )
        })
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = lifecycleOwner, block = {
        withContext(Dispatchers.IO) {
            contentViewModel?.receiveEvent(StateScrollerFactory().invoke(viewModel.scrollState()))
        }
        contentViewModel?.event?.collect {
            if (it is ShareEvent) {
                context.startActivity(
                    ShareIntentFactory()(
                        viewModel.exportableContent(),
                        "Chat_${DateFormat.format("yyyyMMdd-HH-mm-ss", Calendar.getInstance())}"
                    )
                )
            }
        }
    })
}