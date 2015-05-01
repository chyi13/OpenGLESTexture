package com.test.cy.openglestexture;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2015/4/16.
 * fire flame particle is a cube and it is red.
 */
public class GLFireFlameParticle {
    //
    private final static float gravity = 0.00098f;
    public float fadingFactor = -20.0f;
    public float lifePoint = 1000.0f;
    public float horizontalFactor = 0.0f;
    // position parameter
    public float currentX, currentY, currentZ;
    public float speedX, speedY, speedZ;

    // rendering parameter
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final FloatBuffer normalBuffer;

    private final int mProgram;
    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in the modelview matrix. */
    private int mMVMatrixHandle;

    /** This will be used to pass in the light position. */
    private int mLightPosHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    /** This will be used to pass in model normal information. */
    private int mNormalHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    static final int COLORS_PER_VERTEX = 4;

    static final int NORMALS_PER_VERTEX = 3;

    private float[] mMVPMatrix = new float[16];

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];

    final float[] squareCoords =
            {
                    // Front face
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,

                    // Right face
                    1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Back face
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,

                    // Left face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,

                    // Top face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Bottom face
                    1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
            };

    final float[] colors =
            {
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,

                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,

                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,

                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,

                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,

                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f,
                    0.609f, 0.164f, 0.0f, 1.0f
            };
    final float[] normals = {
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
    };

    public GLFireFlameParticle(Context context) {
        // vertex
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(squareCoords).position(0);

        // color
        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);

        // normal
        normalBuffer = ByteBuffer.allocateDirect(normals.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer.put(normals).position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                RawResourceReader.readTextFileFromRawResource(context, R.raw.fire_flame_particle_shader));
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                RawResourceReader.readTextFileFromRawResource(context, R.raw.fire_flame_particle_frag_shader));

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

            GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(mProgram, 1, "a_Color");
            GLES20.glBindAttribLocation(mProgram, 2, "a_Normal");

        }
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables


    }

    public void draw(float[] mViewMatrix, float[] mProjectionMatrix, float[] mModelMatrix) {
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgram);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");
        // vertices
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // colors
        GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // normals
        normalBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMALS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, normalBuffer);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        float[] mvpMatrix = new float[16];
        float[] tModelMatrix = new float[16];

        // scale
        float[] scale = new float[16];
        Matrix.setIdentityM(scale, 0);
 //       Matrix.scaleM(scale, 0, 0.1f, 0.1f, 0.1f);
        Matrix.scaleM(scale, 0, lifePoint/ 1000.0f /4 + 0.1f, lifePoint/ 1000.0f /4 + 0.1f, lifePoint/ 1000.0f /4 + 0.1f);
        Matrix.multiplyMM(tModelMatrix, 0, mModelMatrix, 0, scale, 0);

        // mv = view * model
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, tModelMatrix, 0);
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        // mvp = projection * view * model
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // light
        float[] mLightModelMatrix = new float[16];
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the square
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void update() {
        speedX += horizontalFactor;
        speedY += gravity;
        speedZ += 0;

        currentX += speedX;
        currentY += speedY;
        currentZ += speedZ;

        lifePoint += fadingFactor;

    }

    public void initialize(float[] startPos, float[] speed, float life, float hSpeed) {
        currentX = startPos[0];
        currentY = startPos[1];
        currentZ = startPos[2];

        speedX = speed[0];
        speedY = speed[1];
        speedZ = speed[2];

        horizontalFactor = hSpeed;

        lifePoint = life;
    }

}
