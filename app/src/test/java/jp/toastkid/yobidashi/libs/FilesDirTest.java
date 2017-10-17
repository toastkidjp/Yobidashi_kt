package jp.toastkid.yobidashi.libs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import jp.toastkid.yobidashi.libs.storage.FilesDir;

import static org.junit.Assert.assertEquals;

/**
 * {@link FilesDir}'s test case.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner.class)
public class FilesDirTest {

    /** Test object. */
    private FilesDir filesDir;

    /**
     * Initialize test object before each test method.
     */
    @Before
    public void setUp() {
        filesDir = new FilesDir(RuntimeEnvironment.application, "test");
    }

    /**
     * Check {@link FilesDir#assignNewFile(String)}.
     */
    @Test
    public void test_assignNewFile() {
        assertEquals(
                "あま_しお_ちゃ_ん.html",
                filesDir.assignNewFile("あま/しお\\ちゃ|ん.html").getName()
        );
    }
}
