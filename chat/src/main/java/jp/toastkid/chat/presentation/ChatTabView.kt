package jp.toastkid.chat.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.network.NetworkChecker
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.launch

@Composable
fun ChatTabView() {
    val context = LocalContext.current
    val contentViewModel = (LocalView.current.context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }
    val apiKey = PreferenceApplier(context).chatApiKey()
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
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
    ) {
        SelectionContainer(modifier = Modifier.padding(8.dp)) {
            LazyColumn(state = viewModel.scrollState()) {
                items(viewModel.messages()) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            viewModel.name(it.role),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = viewModel.nameColor(it.role),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .weight(0.2f)
                        )
                        MessageContent(
                            it.text,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .weight(1f)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 4.dp))

                    LaunchedEffect(key1 = viewModel.messages().last().text, block = {
                        coroutineScope.launch {
                            viewModel.scrollState().animateScrollToItem(viewModel.messages().size)
                        }
                    })
                }
            }
        }
    }

    contentViewModel?.replaceAppBarContent {
        TextField(
            viewModel.textInput(),
            label = { Text(viewModel.label(), color = MaterialTheme.colorScheme.onPrimary) },
            maxLines = Int.MAX_VALUE,
            onValueChange = viewModel::onValueChanged,
            keyboardActions = KeyboardActions{
                coroutineScope.launch {
                    viewModel.send()
                }
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
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

    DisposableEffect(Unit/*chatTab*/) {
        /*viewModel.launch(
            //chatTab.chat()
        )*/

        onDispose {
            //viewModel.update(chatTab)
        }
    }
}