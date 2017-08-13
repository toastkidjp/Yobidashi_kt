package jp.toastkid.yobidashi.browser.tab;

import android.support.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner.class)
public class TabListTest {

    @Test
    public void test() throws IOException, JSONException {
        final TabList tabList = TabList.Companion.loadOrInit(RuntimeEnvironment.application);
        tabList.add(new Tab());
        tabList.add(makeTab());

        final JsonAdapter<TabList> adapter = new Moshi.Builder().build().adapter(TabList.class);
        final String json = adapter.toJson(tabList);
        assertEquals(0, new JSONObject(json).getInt("index"));

        final TabList fromJson = adapter.fromJson(json);
        assertEquals(2, fromJson.size());

        final Tab tab1 = fromJson.get(1);
        History latest = tab1.getLatest();
        assertEquals("title2", latest.title());
        assertEquals("url2",   latest.url());
        assertEquals("thumbnailPath", tab1.getThumbnailPath());
    }

    @NonNull
    private Tab makeTab() {
        final Tab tab = new Tab();
        tab.addHistory(History.Companion.make("title",  "url"));
        tab.addHistory(History.Companion.make("title2", "url2"));
        tab.setThumbnailPath("thumbnailPath");
        return tab;
    }
}