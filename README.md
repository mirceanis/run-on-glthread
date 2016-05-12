# run-on-glthread
an attempt to minimize the boilerplate needed to run OpenGL commands in an android test

# import

```groovy
dependencies {
    ...
    androidTestCompile 'ro.mirceanistor:run-on-glthread:0.1'
}
```

# usage

The library provides a `GLTestUtils` class that has a `runOnGLThreadAndWait(Runnable)` method
It needs to be initialized with a blank activity and released after tests finish:

```java

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(SomeBlankActivity.class);

    @Before
    public void setUp() throws Exception {

        GLTestUtils.initialize(mActivityRule.getActivity());

    }
    
    @Test
    public void testShaderCreationOnGLThread() throws Exception {
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
    
```

# limitations

The activity passed at `initialize()` gets clobbered, having its content view replaced with a mock `GLSurfaceView`
This will probably be addressed in a future release.

# contributing

No rules. Whatever makes testing OpenGL easier on Android