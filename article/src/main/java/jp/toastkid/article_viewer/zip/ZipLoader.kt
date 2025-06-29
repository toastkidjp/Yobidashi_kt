package jp.toastkid.article_viewer.zip

import android.os.Build
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author toastkidjp
 */
class ZipLoader(private val articleRepository: ArticleRepository) {

    private val tokenizer = NgramTokenizer()

    private val items = mutableListOf<Article>()

    private val disposable = Job()

    operator fun invoke(inputStream: InputStream) {
        ZipInputStream(inputStream, CHARSET)
            .use { zipInputStream ->
                var nextEntry = zipInputStream.nextEntry
                while (nextEntry != null) {
                    if (!nextEntry.name.contains(".")) {
                        nextEntry = zipInputStream.nextEntry
                        continue
                    }

                    try {
                        extract(zipInputStream, nextEntry)
                    } finally {
                        zipInputStream.closeEntry()
                    }

                    nextEntry = try {
                        zipInputStream.nextEntry
                    } catch (e: IllegalArgumentException) {
                        Timber.e("illegal: ${nextEntry.name}")
                        Timber.e(e)
                        return
                    }
                }
            }
        flush()
    }

    private val acceptableFileExtensions = setOf("md", "txt")

    private fun extract(
        zipInputStream: ZipInputStream,
        nextEntry: ZipEntry
    ) {
        // You don't use `use()` because it makes occur "java.io.IOException: Stream closed".
        zipInputStream.bufferedReader().let {
            val extension = nextEntry.name.split(".").last()
            if (!acceptableFileExtensions.contains(extension)) {
                return@let
            }

            val start = System.currentTimeMillis()
            val content = it.lineSequence().joinToString(System.lineSeparator())
            val title = extractFileName(nextEntry.name)
            val article = (articleRepository.findFirst(title) ?: Article(0)).also { a ->
                a.title = title
                a.contentText = content
                a.length = a.contentText.length
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                article.lastModified = nextEntry.lastModifiedTime.to(TimeUnit.MILLISECONDS)
            }
            Timber.i("${System.currentTimeMillis() - start}[ms] ${article.title}")

            article.bigram = tokenizer(SPACE_REGEX.replace(content, ""), 2) ?: ""
            items.add(article)
            if (items.size > 1000) {
                flush()
            }
        }
    }

    private fun flush() {
        val updateItems = mutableListOf<Article>().also {
            it.addAll(items)
        }
        items.clear()

        CoroutineScope(Dispatchers.IO).launch(disposable) {
            articleRepository.insertAll(updateItems)
        }
    }

    private fun extractFileName(name: String) = name.substring(name.indexOf("/") + 1, name.lastIndexOf("."))

    fun dispose() = disposable.cancel()

    companion object {
        private val CHARSET = Charset.forName("UTF-8")

        private val SPACE_REGEX = Regex("\\s")
    }
}