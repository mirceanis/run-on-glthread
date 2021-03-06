package ro.mirceanistor.gltestutils.sample;

import android.opengl.GLES20;
import android.support.test.filters.Suppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import ro.mirceanistor.gltestutils.GLTestUtils;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by mirceanis on 2016-05-06.
 * Example of usage of the GLTestUtils class
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSomethingOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                assertTrue(true);
            }
        });
    }

    @Test
    public void testSomethingAfterGLThread() throws Exception {
        final boolean[] something = {false};

        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                something[0] = true;
            }
        });

        Assert.assertTrue("something should be true", something[0]);
    }

    /**
     * An example of test running GL code on a GL thread
     * @throws Exception
     */
    @Test
    public void testShaderCreationOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                Assert.assertTrue("shader handle is non zero", vertexShaderHandle != 0);
            }
        });
    }

    /**
     * An example of test that uses width and height in a useless manner
     * @throws Exception
     */
    @Test
    public void testHasNonZeroDimensions() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue("width is supposed to be non zero", GLTestUtils.getWidth() != 0);
            }
        });

        //should also work outside of the GL context
        Assert.assertTrue("height is supposed to be non zero", GLTestUtils.getHeight() != 0);
    }

    /**
     * GLTestUtils needs to be released
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        //There must be a release, otherwise it will leak
        GLTestUtils.release();
    }

    // this test fails and brings down the whole test suite.
    // unsuppress if you need to see how it fails
    @Suppress
    @Test
    public void testSomethingFailingOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                assertTrue("I failed on the GL thread", false);
            }
        });
        assertTrue(true);
    }

    // this test fails and brings down the whole test suite.
    // unsuppress if you need to see how it fails
    @Suppress
    @Test
    public void testShaderCompilationOnTestThread() throws Exception {
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        assertTrue("shader handle is non zero", vertexShaderHandle != 0);
    }

    // this test fails and brings down the whole test suite.
    // unsuppress if you need to see how it fails
    @Suppress
    @Test
    public void testShaderCompilationOnUIThread() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        mActivityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                Assert.assertTrue("shader handle is non zero", vertexShaderHandle != 0);
                latch.countDown();
            }
        });

        // Wait for the ui thread task to finish
        latch.await();

    }


}