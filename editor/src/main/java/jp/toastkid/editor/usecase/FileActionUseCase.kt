/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import jp.toastkid.editor.R
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.storage.ExternalFileAssignment
import java.io.File
import java.util.Calendar

class FileActionUseCase(
    private val context: Context,
    private val contentViewModel: ContentViewModel,
    private val path: MutableState<String>,
    private val textGetter: () -> String,
    private val textSetter: (String) -> Unit,
    private val updateTab: (File) -> Unit,
    val lastSaved: MutableState<Long> = mutableStateOf(0L),
    private val externalFileAssignment: ExternalFileAssignment = ExternalFileAssignment()
) {

    /**
     * Save current content to file.
     */
    fun save(openInputFileNameDialog: MutableState<Boolean>) {
        if (path.value.isNullOrBlank()) {
            openInputFileNameDialog.value = true
            return
        }
        saveToFile(path.value)
    }

    private fun saveToFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }

        val content = textGetter()
        if (content.isEmpty()) {
            contentViewModel.snackShort(R.string.error_content_is_empty)
            return
        }

        file.bufferedWriter().use {
            it.write(content)
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            null
        ) { _, _ ->  }

        snackText("${context.getString(R.string.done_save)}: $filePath")

        //val fileName = file.nameWithoutExtension + "_backup.txt"
        //saveToFile(ExternalFileAssignment()(context, fileName).absolutePath)

        lastSaved.value = file.lastModified()
    }

    /**
     * Read content from file [Uri].
     *
     * @param data [Uri]
     */
    fun readFromFileUri(data: Uri) {
        val context = context ?: return

        FileExtractorFromUri(context, data)?.let {
            if (it == path.value) {
                return
            }

            readFromFile(File(it))
        }
    }

    /**
     * Read content from [File].
     *
     * @param file [File]
     */
    fun readFromFile(file: File) {
        if (!file.exists() || !file.canRead()) {
            snackText(R.string.message_cannot_read_file)
            path.value = ""
            textSetter("")
            return
        }

        val text = file.bufferedReader().use { reader ->
            reader.readText()
        }
        textSetter(text)
        snackText(R.string.done_load)
        path.value = file.absolutePath
        updateTab(file)

        lastSaved.value = file.lastModified()
    }

    fun readCurrentFile() {
        val pathname = path.value
        if (pathname.isBlank()) {
            return
        }
        readFromFile(File(pathname))
    }

    /**
     * Assign new file object.
     *
     * @param fileName
     */
    fun assignNewFile(fileName: String) {
        val context = context ?: return
        var newFile = externalFileAssignment(context, fileName)
        while (newFile.exists()) {
            newFile = externalFileAssignment(
                context,
                "${newFile.nameWithoutExtension}_.txt"
            )
        }
        path.value = newFile.absolutePath
        updateTab(newFile)
        saveToFile(path.value)
    }

    /**
     * Show snackbar with specified id text.
     *
     * @param id
     */
    private fun snackText(@StringRes id: Int) {
        contentViewModel?.snackShort(id)
    }

    /**
     * Show message by [com.google.android.material.snackbar.Snackbar].
     *
     * @param message
     */
    private fun snackText(message: String) {
        contentViewModel?.snackShort(message)
    }

    fun getText() = textGetter()

    fun setText(newText: String) = textSetter(newText)

    fun exportToArticleViewer() {
        /*TODO ArticleInsertion(context).invoke(
            makeExportTitle(),
            getText()
        )*/
    }

    private fun makeExportTitle() =
        if (path.value.isEmpty()) Calendar.getInstance().time.toString()
        else path.value.split("/").last()
}