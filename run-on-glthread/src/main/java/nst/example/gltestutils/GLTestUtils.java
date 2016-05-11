package nst.example.gltestutils;

import android.app.Activity;
import android.opengl.GLSurfaceView;

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

    @SuppressWarnings("unused")
    protected GLTestUtils() {
        //quick, quick, hide!!
    }

    protected GLTestUtils(Activity activity) throws Exception {

        mActivity = activity;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                // New or different activity, set up for GL.
                mGLSurfaceView = (GLSurfaceView) mActivity.findViewById(R.id.my_gl_surface_view);
                if (mGLSurfaceView == null) {
                    mGLSurfaceView = new GLSurfaceView(mActivity);
                    mGLSurfaceView.setId(R.id.my_gl_surface_view);
                }
                mGLSurfaceView.setEGLContextClientVersion(2);

                // Attach the renderer to the view
                mGLSurfaceView.setRenderer(new DummyRenderer());
                mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

                //and the view to the activity.
                //TODO: find a way to not destroy the content.. it may not be our activity
                mActivity.setContentView(mGLSurfaceView);
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
     * Essentially this is used to enqueue events on it's associated GLThread
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * An activity is used to contain the GLSurfaceView.
     */
    private Activity mActivity;

    /**
     * You gotta start somewhere.
     * This method is best called in your test setup
     * It's gonna be heavy so don't forget to {@link #release()}!
     * @param activity an activity that will have its content replaced with a GLSurfaceView
     *                 This activity will leak unless you call {@link #release()} after your tests are done
     * @throws Exception if an error occurs
     */
    public static synchronized void initialize(Activity activity) throws Exception {
        mInstance = new GLTestUtils(activity);
    }

    /**
     * Tell android that you're not into GLThreading any more so allow this to be cleaned up.
     */
    public static synchronized void release() {
        mInstance = null;
    }

    /**
     * Posts your {@code payload} on a GLThread and blocks until it's finished running
     * @param payload the Runnable to run
     * @throws Exception if initialized() hasn't been called
     */
    public static void runOnGLThreadAndWait(Runnable payload) throws Exception {

        if (mInstance == null) {
            throw new RuntimeException("have you called initialize() in your test setup() method?");
        }

        mInstance.doRunOnGLThread(payload);
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

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            synchronized (mGlReadyLock) {
                mGL = gl;
                mGlReadyLock.notifyAll();
            }
        }
    }

}