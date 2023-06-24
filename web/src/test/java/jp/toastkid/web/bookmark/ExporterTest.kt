package jp.toastkid.web.bookmark

import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import kotlinx.serialization.json.Json
import okio.buffer
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test of [Exporter].
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class ExporterTest {

    /**
     * Use for read test resources.
     */
    private val classLoader = ExporterTest::class.java.classLoader

    /**
     * Source file path.
     */
    private val sourcePath: String = "bookmark/bookmarkJsons.txt"

    /**
     * Expected file path.
     */
    private val expectedPath: String = "bookmark/expectedExported.html"

    /**
     * Check of [Exporter.invoke].
     */
    @Test
    fun test_invoke() {
        val serializer = Json { ignoreUnknownKeys = true }
        val message = Exporter(readSource()).invoke()
        assertEquals(readExpected(), message.replace("\n", ""))
    }

    /**
     * Read [Bookmark] objects from source.
     */
    private fun readSource(): List<Bookmark> {
        return listOf(
            makeItem(
                "Windows Media",
                "http://www.microsoft.com/isapi/redir.dll?prd=ie&ar=windowsmedia",
                "リンク"
            ),
            makeItem(
                "Okamoto's Homepage",
                "http://www.asahi-net.or.jp/~va5n-okmt/pov/index.html",
                "CG"
            ),
            makeItem(
                "POV-HTML-JP-Manual",
                "http://www.arch.oita-u.ac.jp/a-kse/povjp/",
                "CG"
            ),
            makeItem(
                "PovRay",
                "http://www.mlab.im.dendai.ac.jp/~saitoh/WSCG/index.htm",
                "CG"
            ),
            makeItem(
                "CG",
                "",
                "リンク",
                true
            ),
            makeItem(
                "HotMail の無料サービス",
                "http://www.microsoft.com/isapi/redir.dll?prd=ie&ar=hotmail",
                "リンク"
            ),
            makeItem(
                "Windows",
                "http://www.microsoft.com/isapi/redir.dll?prd=ie&ar=windows",
                "リンク"
            ),
            makeItem(
                "リンクの変更",
                "http://www.microsoft.com/isapi/redir.dll?prd=ie&pver=6&ar=CLinks",
                "リンク"
            ),
            makeItem(
                "Microsoft Office ダウンロード ホーム ページ",
                "http://office.microsoft.com/ja-jp/officeupdate/default.aspx",
                "リンク"
            ),
            makeItem(
                "MSN.co.jp",
                "http://www.microsoft.com/isapi/redir.dll?prd=ie&pver=6&ar=IStart",
                "リンク"
            ),
            makeItem(
                "ラジオ ステーション ガイド",
                "http://www.microsoft.com/isapi/redir.dll?prd=windows&sbp=mediaplayer&plcid=&pver=6.1&os=&over=&olcid=&clcid=&ar=Media&sba=RadioBar&o1=&o2=&o3=",
                "リンク"
            ),
            makeItem(
                "ライトノベル作法研究所",
                "http://www.raitonoveru.jp/index.htm",
                "新しいフォルダ"
            ),
            makeItem(
                "新しいフォルダ",
                "",
                "リンク",
                true
            ),
            makeItem(
                "世界の古典つまみ食い",
                "http://www.geocities.jp/hgonzaemon/index.html",
                "リンク"
            ),
            makeItem(
                "英語諺メニュー",
                "http://www6.plala.or.jp/yhayashi/jkotowazamenu.html",
                "リンク"
            ),
            makeItem(
                "さらに怪しい人名辞典",
                "http://www2u.biglobe.ne.jp/~simone/more.htm",
                "リンク"
            ),
            makeItem(
                "スペイン語 辞書",
                "http://www.casa7.com/spain/diccio/abc.htm",
                "リンク"
            ),
            makeItem(
                "リンク",
                "",
                "root",
                true
            )
        )
    }

    private fun makeItem(title: String, url: String, parent: String, folder: Boolean = false) =
        Bookmark().also {
            it._id = 0
            it.title = title
            it.url = url
            it.parent = parent
            it.folder = folder
        }

    /**
     * Read expected html string.
     */
    private fun readExpected() =
            classLoader?.getResourceAsStream(expectedPath)?.source()?.use { source ->
                source.buffer().use {
                    it.readUtf8().replace("\r\n", "")
                }
            }

}