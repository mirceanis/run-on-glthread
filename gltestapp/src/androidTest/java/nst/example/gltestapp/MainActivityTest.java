package nst.example.gltestapp;

import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by mnistor on 2016-05-06.
 */
public class MainActivityTest {

    @Rule
    ActivityTestRule mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {

        GLTestUtils.initialize(mActivityRule.getActivity());

    }

    @Test
    public void testSomethingOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @After
    public void tearDown() throws Exception {

        GLTestUtils.release();

    }
}