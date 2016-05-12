package nst.example.gltestutils;

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
public class GLTestUtils {

    private static GLTestUtils mInstance;
    private GL10 mGL;

    /**
     * Essentially this is used to enqueue events on it's associated GLThread
     */
    private GLSurfaceView mGLSurfaceView;
    private Context testContext = InstrumentationRegistry.getContext();
    private Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    protected GLTestUtils() throws Exception {

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView = new GLSurfaceView(testContext);
                mGLSurfaceView.setId(R.id.my_gl_surface_view);
                mGLSurfaceView.setEGLContextClientVersion(2);

                // Attach the renderer to the view
                mGLSurfaceView.setRenderer(new DummyRenderer());
                mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

                WindowManager wm = (WindowManager) testContext.getSystemService(Context.WINDOW_SERVICE);
                wm.addView(mGLSurfaceView, new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST,0));
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
     * You gotta start somewhere.
     * This method is best called in your test setup
     * It's gonna be heavy so don't forget to {@link #release()}!
     * @throws Exception if an error occurs
     */
    public static synchronized void initialize() throws Exception {
        mInstance = new GLTestUtils();
    }

    /**
     * Tell android that you're not into GLThreading any more so allow this to be cleaned up.
     */
    public static synchronized void release() {
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