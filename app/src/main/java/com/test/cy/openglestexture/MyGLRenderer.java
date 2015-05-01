package com.test.cy.openglestexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2015/4/11.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final static String TAG = "MyGLRender";

    private final Context mContextHandle;

    public MyGLRenderer(Context context){
        mContextHandle = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
  //      GLES20.glEnable(GLES20.GL_CULL_FACE);
        //
        Matrix.setIdentityM(mRotationMatrix, 0);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 6.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // create scene
        mGLCube = new GLCube(mContextHandle);
        mGLPlane = new GLPlane(mContextHandle);
        mGLFireFlame = new GLFireFlame(mContextHandle);
        mGL3DModel = new GL3DModel(mContextHandle);
    }

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    public float xAngle, yAngle;

    private GLCube mGLCube;
    private GLPlane mGLPlane;
    private GLFireFlame mGLFireFlame;
    private GL3DModel mGL3DModel;
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1.0f, 100.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // calculate rotate matrix
        float[] xAxis = {1.0f, 0.0f, 0.0f, 0.0f};
        float[] yAxis = {0.0f, 1.0f, 0.0f, 0.0f};
        float[] invRotation = new float[16];

        Matrix.invertM(invRotation, 0, mRotationMatrix, 0);
        Matrix.multiplyMV(xAxis, 0, invRotation, 0, xAxis, 0);
        Matrix.rotateM(mRotationMatrix, 0, xAngle, xAxis[0], xAxis[1], xAxis[2]);

        Matrix.invertM(invRotation, 0, mRotationMatrix, 0);
        Matrix.multiplyMV(yAxis, 0, invRotation, 0, yAxis, 0);
        Matrix.rotateM(mRotationMatrix, 0, yAngle, yAxis[0], yAxis[1], yAxis[2]);

        // multiply final matrix by previous calculated rotation matrix
   //     Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        mGLCube.draw(mViewMatrix, mProjectionMatrix, mRotationMatrix);
        mGLPlane.draw(mViewMatrix, mProjectionMatrix);
        float[] modelTranslate = new float[16];
        Matrix.setIdentityM(modelTranslate, 0);
        Matrix.translateM(modelTranslate, 0, 0.0f, 0.0f, 0.0f);
        mGL3DModel.draw(mViewMatrix, mProjectionMatrix, mRotationMatrix);
        // fire flame
        mGLFireFlame.update();
        float[] fireFlameTranslate = new float[16];
        Matrix.setIdentityM(fireFlameTranslate, 0);
        Matrix.translateM(fireFlameTranslate, 0, 0.0f, 2.0f, -5.0f);
  //      mGLFireFlame.draw(mViewMatrix, mProjectionMatrix,fireFlameTranslate);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    public static int loadTexture(final Context context, final int resourceId){
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // no pre-scaling

            // read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                    resourceId, options);

            // bind to texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }
        else{
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
