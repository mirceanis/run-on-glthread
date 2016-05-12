# run-on-glthread
an attempt to minimize the boilerplate needed to run OpenGL commands in an android test

# import

```groovy
dependencies {
    ...
    androidTestCompile 'ro.mirceanistor:run-on-glthread:0.2'
}
```

# usage

The library provides a `GLTestUtils` class that has a `runOnGLThreadAndWait(Runnable)` method
It needs to be `release`d after the tests are done to avoid leaks

```java
    
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

    @After
    public void tearDown() throws Exception {

        GLTestUtils.release();

    }
    
```

# contributing

No rules. Whatever makes testing OpenGL easier on Android