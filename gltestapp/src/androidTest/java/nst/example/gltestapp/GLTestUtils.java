package nst.example.gltestapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;

/**
 * Created by mnistor on 2016-05-06.
 */
public class GLTestUtils {

    private static GLTestUtils mInstance;

    /**
     * the crux
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * activity to hold the crux
     */
    private Activity mActivity;


    protected GLTestUtils() {
        //quick, quick, hide!!
    }

    protected GLTestUtils(Activity activity) {
        mActivity = activity;
    }

    /**
     * You gotta start somewhere.
     * This method is best called in your test setup
     * It's gonna be heavy so don't forget to {@link #release()}!
     */
    public static synchronized void initialize(Activity activity) {
        mInstance = new GLTestUtils(activity);
    }

    /**
     * Tell android that you're not into GLThreading any more so allow this to be cleaned up.
     */
    public static synchronized void release() {
        mInstance = null;
    }

    public static void runOnGLThreadAndWait(Runnable payload) {

        if (mInstance == null) {
            throw new RuntimeException("have you called initialize() in your test setup() method?");
        }

        mInstance.doRunOnGLThread(payload);
    }

    private void doRunOnGLThread(Runnable payload) {
        //TODO: do the actual running
    }

}