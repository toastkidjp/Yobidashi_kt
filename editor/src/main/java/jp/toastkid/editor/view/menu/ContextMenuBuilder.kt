package jp.toastkid.editor.view.menu

import androidx.compose.foundation.text.contextmenu.builder.TextContextMenuBuilderScope
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.core.net.toUri
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.editor.view.menu.text.CommaInserter
import jp.toastkid.editor.view.menu.text.ListHeadAdder
import jp.toastkid.editor.view.menu.text.NumberedListHeadAdder
import jp.toastkid.editor.view.menu.text.TableFormConverter
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.translate.TranslationUrlGenerator

class ContextMenuBuilder(
    private val viewModel: EditorTabViewModel,
    private val contentViewModel: ContentViewModel
) {

    operator fun invoke(): TextContextMenuBuilderScope.() -> Unit {
        return {
            if (viewModel.selectedText().isEmpty()) {
                item(key = "Horizontal rule", label = "Horizontal rule") {
                    viewModel.insertText("----${System.lineSeparator()}")
                    close()
                }
                item(key = "Duplicate current line", label = "Duplicate current line") {
                    viewModel.duplicateCurrentLine()
                    close()
                }
                item(key = "Select current line", label = "Select current line") {
                    viewModel.selectCurrentLine()
                    close()
                }
                item(key = "Delete current line", label = "Delete current line") {
                    viewModel.deleteCurrentLine()
                    close()
                }
            } else {
                item(key = "Double quote", label = "Double quote") {
                    viewModel.replaceText("\"${viewModel.selectedText()}\"")
                    close()
                }
                item(key = "Bold", label = "Bold") {
                    viewModel.replaceText("**${viewModel.selectedText()}**")
                    close()
                }
                item(key = "Italic", label = "Italic") {
                    viewModel.replaceText("***${viewModel.selectedText()}***")
                    close()
                }
                item(key = "Strikethrough", label = "Strikethrough") {
                    viewModel.replaceText("~~${viewModel.selectedText()}~~")
                    close()
                }
                item(key = "Ordered list", label = "Ordered list") {
                    val newText = NumberedListHeadAdder().invoke(viewModel.selectedText()) ?: return@item close()
                    viewModel.replaceText(newText)
                    close()
                }
                item(key = "List", label = "To list") {
                    val newText = ListHeadAdder().invoke(viewModel.selectedText(), "-") ?: return@item close()
                    viewModel.replaceText(newText)
                    close()
                }
                item(key = "Task list", label = "To task list") {
                    val newText = ListHeadAdder().invoke(viewModel.selectedText(), "- [ ]") ?: return@item close()
                    viewModel.replaceText(newText)
                    close()
                }
                item(key = "Table", label = "To table") {
                    val newText = TableFormConverter().invoke(viewModel.selectedText())
                    viewModel.replaceText(newText)
                    close()
                }
            }

            item(key = "Add quote", label = "Add quote") {
                val newText = ListHeadAdder().invoke(viewModel.selectedText(), ">") ?: return@item close()
                viewModel.replaceText(newText)
                close()
            }
            item(key = "Code block", label = "Code block") {
                val lineSeparator = System.lineSeparator()
                viewModel.replaceText("```${lineSeparator}${viewModel.selectedText()}${lineSeparator}```")
                close()
            }
            item(key = "Open url", label = "Open url") {
                contentViewModel.open(viewModel.selectedText().toUri())
                close()
            }
            item(key = "Open url on background", label = "Open url on background") {
                contentViewModel.openBackground(viewModel.selectedText().toUri())
                close()
            }
            item(key = "Translate", label = "Translate") {
                contentViewModel.preview(TranslationUrlGenerator().invoke(viewModel.selectedText()))
                close()
            }
            item(key = "Insert thousand separator", label = "Insert thousand separator") {
                val selectedText = viewModel.selectedText()
                val converted = CommaInserter().invoke(selectedText)
                if (converted.isNullOrBlank()) {
                    return@item
                }
                viewModel.replaceText(converted)
                close()
            }

            if (viewModel.selectedText().isNotBlank()) {
                separator()
                item(key = "Preview search", label = "Preview search") {
                    val text = viewModel.selectedText()
                    if (text.isNotBlank()) {
                        contentViewModel.preview(text.toString())
                    }
                    close()
                }
                item(key = "Web search", label = "Web search") {
                    val text = viewModel.selectedText()
                    if (text.isNotBlank()) {
                        contentViewModel.search(text.toString())
                    }
                    close()
                }
            }

            separator()
            item(key = "Show app bar", label = "Show app bar") {
                contentViewModel.showAppBar()
                close()
            }
        }
    }

}