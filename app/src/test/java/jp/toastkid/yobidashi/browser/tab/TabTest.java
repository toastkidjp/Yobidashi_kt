package jp.toastkid.yobidashi.browser.tab;

import android.support.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * {@link Tab}'s test case.
 *
 * @author toastkidjp
 */
public class TabTest {

    @Test
    public void test() throws IOException {
        final Tab tab = makeTestTab();

        final JsonAdapter<Tab> tabJsonAdapter = makeTabJsonAdapter();
        final String json = check_toJson(tab, tabJsonAdapter);

        check_fromJson(tabJsonAdapter, json);
    }

    private void check_fromJson(JsonAdapter<Tab> tabJsonAdapter, String json) throws IOException {
        final Tab fromJson = tabJsonAdapter.fromJson(json);
        assertEquals("Google",    fromJson.getLastTitle());
        assertEquals("file://~~", fromJson.getThumbnailPath());
        assertEquals("Title",     fromJson.getLatest().title());
        assertEquals("URL",       fromJson.getLatest().url());
    }

    private String check_toJson(Tab tab, JsonAdapter<Tab> tabJsonAdapter) {
        final String json = tabJsonAdapter.toJson(tab);
        assertEquals(
                "{\"histories\":[{\"title\":\"Title\",\"url\":\"URL\"}]," +
                        "\"index\":0,\"lastTitle\":\"Google\",\"thumbnailPath\":\"file://~~\"}",
                json
        );
        return json;
    }

    private JsonAdapter<Tab> makeTabJsonAdapter() {
        final Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(Tab.class);
    }

    @NonNull
    private Tab makeTestTab() {
        final Tab tab = new Tab();
        tab.setThumbnailPath("file://~~");
        tab.setLastTitle("Google");
        tab.addHistory(History.Companion.make("Title", "URL"));
        return tab;
    }
}
