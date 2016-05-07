package nst.example.gltestapp;

import android.opengl.GLES20;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by mirceanis on 2016-05-06.
 * Example of usage of the GLTestUtils class
 */
public class MainActivityTest {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {

        GLTestUtils.initialize(mActivityRule.getActivity());

    }

    @Test
    public void testSomethingOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(true);
            }
        });
    }

    @Test
    public void testSomethingAfterGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(true);
            }
        });
        Assert.assertTrue(true);
    }

    @Test
    public void testSomethingFailingOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue("I failed on the GL thread", false);
            }
        });
        Assert.assertTrue(true);
    }

    @Test
    public void testShaderCompilationOnTestThread() throws Exception {
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        Assert.assertTrue("shader handle is non zero", vertexShaderHandle != 0);
    }

    @Test
    public void testShaderCompilationOnUIThread() throws Exception {

        final Object lock = new Object();
        final boolean[] done = {false};

        mActivityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                Assert.assertTrue("shader handle is non zero", vertexShaderHandle != 0);
                done[0] = true;
                lock.notifyAll();
            }
        });

        // Wait for the ui thread task to finish

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(lock) {
            while (!done[0]) {
                lock.wait();
            }
        }

    }

    @Test
    public void testShaderCompilationOnGLThread() throws Exception {
        GLTestUtils.runOnGLThreadAndWait(new Runnable() {
            @Override
            public void run() {
                int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                Assert.assertTrue("shader handle is non zero", vertexShaderHandle != 0);
            }
        });
        Assert.assertTrue(true);
    }

    @After
    public void tearDown() throws Exception {

        GLTestUtils.release();

    }
}