Changelog
=========

* v0.3 - add `getWidth()` and `getHeight()`

* v0.2 - no more activity
    * `GLTestUtils` no longer needs an `Activity` in `initialize()`, in fact that method is now private
        the dummy `GLSurfaceView` used is now attached directly to the `WindowManager`
    * added dependency on `com.android.support.test:runner:0.5` to get access to the `InstrumentationRegistry`
    * renamed the package to `ro.mirceanistor.gltestutils` to better reflect the artifact group id
      
* v0.1 - initial release
    * provide `runOnGLThreadAndWait()` method by clobbering the content of a given activity
