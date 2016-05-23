package ro.mirceanistor.gltestutils;

import android.app.Instrumentation;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.view.WindowManager;

import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mirceanis on 2016-05-06.
 * <p>
 * A utility class that provides a way to enqueue runnables on a GLThread during an instrumentation test
 * </p>
 */

public class GLTestUtils {

    private static GLTestUtils mInstance;
    private GL10 mGL;

    /**
     * Essentially this is used to enqueue events on it's associated GLThread
     */
    private GLSurfaceView mGLSurfaceView;
    private Context testContext = InstrumentationRegistry.getContext();
    private Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private int mWidth = 0;
    private int mHeight = 0;

    protected GLTestUtils() throws RuntimeException {

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView = new GLSurfaceView(testContext);
                mGLSurfaceView.setId(R.id.z_gl_surface_view_id_used_for_testing);
                mGLSurfaceView.setEGLContextClientVersion(2);

                // Attach the renderer to the view
                mGLSurfaceView.setRenderer(new DummyRenderer());
                mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

                WindowManager wm = (WindowManager) testContext.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST, 0);
                wm.addView(mGLSurfaceView, layoutParams);
            }
        });

        // Wait for the renderer to get the GL context.
        synchronized (mGlReadyLock) {
            while (mGL == null) {
                try {
                    mGlReadyLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Tell android that you're not into GLThreading any more so allow this to be cleaned up.
     */
    public static void release() {
        if (mInstance != null) {
            mInstance.detachView();
            mInstance = null;
        }
    }

    /**
     * Posts your {@code payload} on a GLThread and blocks until it's finished running
     * In case this is the first call to GLTestUtils, it also initializes the underlying mechanics
     *
     * @param payload the Runnable to run. May be {@code null}
     * @throws Exception captured while running your payload
     */
    public static void runOnGLThreadAndWait(@Nullable Runnable payload) throws Exception {

        if (mInstance == null) {
            mInstance = new GLTestUtils();
        }

        mInstance.doRunOnGLThread(payload);
    }

    /**
     * Gets the width of the underlying GLSurfaceView
     * In case this is the first call to GLTestUtils, it also initializes the underlying mechanics
     *
     * @return the width in pixels
     * @throws RuntimeException when wrong happens during init
     */
    public static int getWidth() throws RuntimeException {
        if (mInstance == null) {
            mInstance = new GLTestUtils();
        }
        return mInstance.mWidth;
    }

    /**
     * Gets the height of the underlying GLSurfaceView
     * In case this is the first call to GLTestUtils, it also initializes the underlying mechanics
     *
     * @return the height in pixels
     * @throws RuntimeException when wrong happens during init
     */
    public static int getHeight() throws RuntimeException {
        if (mInstance == null) {
            mInstance = new GLTestUtils();
        }
        return mInstance.mHeight;
    }

    private void doRunOnGLThread(@Nullable final Runnable payload) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Exception[] problems = {null};
        mGLSurfaceView.queueEvent(new Runnable() {
            public void run() {
                if (payload != null) {
                    try {
                        payload.run();
                    } catch (Exception ex) {
                        //collect exceptions from the GLThread
                        problems[0] = ex;
                    }
                }
                latch.countDown();
            }
        });

        latch.await();  // wait for the payload to finish

        //throw any exception that occurred on the GLThread
        if (problems[0] != null) {
            throw problems[0];
        }
    }

    private void detachView() {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                WindowManager wm = (WindowManager) testContext.getSystemService(Context.WINDOW_SERVICE);
                wm.removeView(mGLSurfaceView);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // dummy renderer
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Dummy renderer, exposes the GL context
     */
    private static final Object mGlReadyLock = new Object();

    private class DummyRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onDrawFrame(GL10 gl) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            synchronized (mGlReadyLock) {
                mGL = gl;
                mWidth = width;
                mHeight = height;
                mGlReadyLock.notifyAll();
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        }
    }

}