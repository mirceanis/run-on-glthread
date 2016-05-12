package ro.mirceanistor.gltestutils;

import android.app.Instrumentation;
import android.content.Context;
import android.opengl.GLSurfaceView;
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
@SuppressWarnings("unused")
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

    protected GLTestUtils() throws Exception {

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
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST,0);
                wm.addView(mGLSurfaceView, layoutParams);
            }
        });

        // Wait for the renderer to get the GL context.
        synchronized (mGlReadyLock) {
            while (mGL == null) {
                mGlReadyLock.wait();
            }
        }
    }

    /**
     * Tell android that you're not into GLThreading any more so allow this to be cleaned up.
     */
    public static void release() {
        mInstance.detachView();
        mInstance = null;
    }

    /**
     * Posts your {@code payload} on a GLThread and blocks until it's finished running
     * @param payload the Runnable to run
     * @throws Exception if initialized() hasn't been called
     */
    public static void runOnGLThreadAndWait(Runnable payload) throws Exception {

        if (mInstance == null) {
            mInstance = new GLTestUtils();
        }

        mInstance.doRunOnGLThread(payload);
    }

    /**
     * Gets the width of the underlying GLSurfaceView
     * @return the width in pixels
     */
    public static int getWidth() {
        return mInstance.mWidth;
    }

    /**
     * Gets the height of the underlying GLSurfaceView
     * @return the height in pixels
     */
    public static int getHeight() {
        return mInstance.mHeight;
    }

    private void doRunOnGLThread(final Runnable payload) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        mGLSurfaceView.queueEvent(new Runnable() {
            public void run() {
                payload.run();
                latch.countDown();
            }
        });
        latch.await();  // wait for the payload to finish
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