package com.test.cy.openglestexture;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2015/4/15.
 */
public class GLPlane {
    private final // TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
            String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.

                    + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                    + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
                    + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
                    + "attribute vec2 a_TexCoordinate;\n"     // Per-vertex texture coordinate information we will pass in.

                    + "varying vec3 v_Position;       \n"		// This will be passed into the fragment shader.
                    + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
                    + "varying vec3 v_Normal;         \n"		// This will be passed into the fragment shader.
                    + "varying vec2 v_TexCoordinate;  \n"     // This will be passed into the fragment shader.

                    // The entry point for our vertex shader.
                    + "void main()                                                \n"
                    + "{                                                          \n"
                    // Transform the vertex into eye space.
                    + "   v_Position = vec3(u_MVMatrix * a_Position);             \n"
                    // Pass through the color.
                    + "   v_Color = a_Color;                                      \n"
                    // Transform the normal's orientation into eye space.
                    + "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));      \n"
                    // gl_Position is a special variable used to store the final position.
                    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                    + "   gl_Position = u_MVPMatrix * a_Position;                 \n"
                    // Pass through the texture coordinate.
                    + "   v_TexCoordinate = a_TexCoordinate;                      \n"
                    + "}                                                          \n";

    private final String fragmentShaderCode =
            "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    + "uniform sampler2D u_Texture;\n"    // The input texture.

                    + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.

                    + "varying vec3 v_Position;		\n"		// Interpolated position for this fragment.
                    + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                    // triangle per fragment.
                    + "varying vec3 v_Normal;         \n"		// Interpolated normal for this fragment.
                    + "varying vec2 v_TexCoordinate;  \n"     // Interpolated texture coordinate per fragment.

                    // The entry point for our fragment shader.
                    + "void main()                    \n"
                    + "{                              \n"
                    // Will be used for attenuation.
                    + "   float distance = length(u_LightPos - v_Position);                      \n"
                    // Get a lighting direction vector from the light to the vertex.
                    + "   vec3 lightVector = normalize(u_LightPos - v_Position);                 \n"
                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);                  \n"
                    // Add attenuation.
                    + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));      \n"
                    // Add ambient lighting
                    + "   diffuse = diffuse + 0.6;"
                    // Multiply the color by the diffuse illumination level to get final output color.
                    + "   gl_FragColor = v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate);\n"
                    + "}                                                                          \n";

    final float[] squareCoords = {
            -50.0f, 0.0f, 50.0f,
            50.0f, 0.0f, -50.0f,
            -50.0f, 0.0f, -50.0f,
            -50.0f, 0.0f, 50.0f,
            50.0f, 0.0f, 50.0f,
            50.0f, 0.0f, -50.0f
    };
    final float[] colors = {
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f
    };
    final float[] normalsData = {
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
    };
    final float[] textureData = {
             0.0f, 0.0f,
            10.0f, 10.0f,
             0.0f, 10.0f,
             0.0f,  0.0f,
            10.0f,  0.0f,
            10.0f, 10.0f,
    };

    private final Context mActivityContext;

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final FloatBuffer normalBuffer;
    private final FloatBuffer textureBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mLightPosHandle;
    private int mNormalHandle;
    private int mTextureHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    /** This will be used to pass in the modelview matrix. */
    private int mMVMatrixHandle;

    private float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int NORMALS_PER_VERTEX = 3;
    static final int TEXTURES_PER_VERTEX = 2;

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];

    public GLPlane(Context context) {
        mActivityContext = context;

        // vertex
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(squareCoords).position(0);

        // color
        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);

        // normal
        normalBuffer = ByteBuffer.allocateDirect(normalsData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer.put(normalsData).position(0);

        // texture
        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.put(textureData).position(0);

        int vertexShaderHandle = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShaderHandle = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, vertexShaderHandle);   // add the vertex shader to program
            GLES20.glAttachShader(mProgram, fragmentShaderHandle); // add the fragment shader to program

            GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(mProgram, 1, "a_Color");
            GLES20.glBindAttribLocation(mProgram, 2, "a_Normal");
            GLES20.glBindAttribLocation(mProgram, 3, "a_TexCoordinate");
        }
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        // load texture
        mTextureHandle = MyGLRenderer.loadTexture(mActivityContext, R.drawable.floor);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }
    public void draw(float[] mViewMatrix, float[] mProjectionMatrix){
        GLES20.glUseProgram(mProgram);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // vertices
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // colors
        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false,
                0, colorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // normals
        normalBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMALS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, normalBuffer);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // texture
        // Pass in the texture coordinate information
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURES_PER_VERTEX, GLES20.GL_FLOAT, false,
                0, textureBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // model matrix
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -5.0f, -10.0f);
     //   float[] scratch = new float[16];
     //   Matrix.multiplyMM(scratch, 0, mModelMatrix, 0, mRotateMatrix, 0);
        //      Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // light
        // light
        float[] mLightModelMatrix = new float[16];
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
//        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the square
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
