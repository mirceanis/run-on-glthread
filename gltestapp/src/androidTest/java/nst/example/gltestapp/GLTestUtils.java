package nst.example.gltestapp;

import android.app.Activity;

/**
 * Created by mnistor on 2016-05-06.
 */
public class GLTestUtils {

    private static GLTestUtils mInstance;

    /**
     * You gotta start somewhere.
     * This method is best called in your test setup
     * It's gonna be heavy so don't forget to {@link #release()}!
     */
    public static synchronized void initialize(Activity activity) {
        mInstance = new GLTestUtils();
    }

    /**
     * Tell android that you're not into GLThreading any more so allow this to be cleaned up.
     */
    public static synchronized void release() {
        mInstance = null;
    }

    public static void runOnGLThreadAndWait() {

        if (mInstance == null) {
            throw new RuntimeException("have you called initialize() in your test setup() method?");
        }

        //TODO: do the actual running on GLThread stuff
    }

}