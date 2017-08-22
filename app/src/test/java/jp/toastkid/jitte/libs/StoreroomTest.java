package jp.toastkid.jitte.libs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import jp.toastkid.jitte.libs.storage.Storeroom;

import static org.junit.Assert.assertEquals;

/**
 * {@link Storeroom}'s test case.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner.class)
public class StoreroomTest {

    /** Test object. */
    private Storeroom storeroom;

    /**
     * Initialize test object before each test method.
     */
    @Before
    public void setUp() {
        storeroom = new Storeroom(RuntimeEnvironment.application, "test");
    }

    /**
     * Check {@link Storeroom#assignNewFile(String)}.
     */
    @Test
    public void test_assignNewFile() {
        assertEquals(
                "あま_しお_ちゃ_ん.html",
                storeroom.assignNewFile("あま/しお\\ちゃ|ん.html").getName()
        );
    }
}
