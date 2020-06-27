package jp.toastkid.article_viewer.zip

import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import okio.Okio
import timber.log.Timber
import java.io.InputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author toastkidjp
 */
class ZipLoader(private val articleRepository: ArticleRepository) {

    private val CHARSET = Charset.forName("UTF-8")

    private val tokenizer = NgramTokenizer()

    private val id = AtomicInteger()

    private val items = mutableListOf<Article>()

    private val disposable = CompositeDisposable()

    @RequiresApi(Build.VERSION_CODES.N)
    operator fun invoke(inputStream: InputStream) {
        ZipInputStream(inputStream, CHARSET)
            .use { zipInputStream ->
                var nextEntry = zipInputStream.nextEntry
                while (nextEntry != null) {
                    if (!nextEntry.name.contains(".")) {
                        nextEntry = zipInputStream.nextEntry
                        continue
                    }

                    extract(zipInputStream, nextEntry)

                    nextEntry = try {
                        zipInputStream.nextEntry
                    } catch (e: IllegalArgumentException) {
                        Timber.e("illegal: ${nextEntry.name}")
                        Timber.e(e)
                        return
                    }
                }
                zipInputStream.closeEntry()
            }
        flush()
    }

    private fun extract(
        zipInputStream: ZipInputStream,
        nextEntry: ZipEntry
    ) {
        // use() occur java.io.IOException: Stream closed
        Okio.buffer(Okio.source(zipInputStream)).let {
            val start = System.currentTimeMillis()
            val content = it.readUtf8()
            val article = Article(id.incrementAndGet()).also { a ->
                a.title = extractFileName(nextEntry.name)
                a.contentText = content
                a.length = a.contentText.length
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                article.lastModified = nextEntry.lastModifiedTime.to(TimeUnit.MILLISECONDS)
            }
            Timber.i("${System.currentTimeMillis() - start}[ms] ${article.title}")

            article.bigram = tokenizer(article.contentText, 2) ?: ""
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

        Completable.fromAction { articleRepository.insertAll(updateItems) }
            .subscribeOn(Schedulers.io())
            .subscribe({ }, Timber::e)
            .addTo(disposable)
    }

    private fun extractFileName(name: String) = name.substring(name.indexOf("/") + 1, name.lastIndexOf("."))

    fun dispose() = disposable.clear()

}