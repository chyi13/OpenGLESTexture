package com.test.cy.openglestexture;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Administrator on 2015/4/24.
 */
public class GL3DModel {

    private final static String TAG = "GL3DModel";

    private float testCoords[] = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f
    };
    private float testColors[] = {
            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
    };
    private float testNormals[] = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };
    private short testIndices[] ={
            0, 1, 2,
            0, 2, 3
    };

    private float coords[];
    private float colors[];
    private short indices[];
    private float normals[];

    private int vertexNum, faceNum;

    final public ShortBuffer indexBuffer;
    final public FloatBuffer vertexBuffer;
    final public FloatBuffer normalBuffer;
    final public FloatBuffer colorBuffer;

    private final Context mActivityContext;

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

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];

    public GL3DModel(Context context){
        mActivityContext = context;

        // read from file
        readFromPLYFile(mActivityContext, R.raw.shuttle);
        colors = new float[vertexNum * 4];
        for (int i = 0; i<vertexNum; i++){
            colors[i*4+0] = 0.5f;
            colors[i*4+1] = 0.5f;
            colors[i*4+2] = 0.5f;
            colors[i*4+3] = 1.0f;
        }
        // vertex
        vertexBuffer = ByteBuffer.allocateDirect(coords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(coords).position(0);

        // normals
        normalBuffer = ByteBuffer.allocateDirect(normals.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer.put(normals).position(0);

        // color
        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);

        // index
        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                RawResourceReader.readTextFileFromRawResource(context, R.raw.threed_model_vertex_shader));
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                RawResourceReader.readTextFileFromRawResource(context, R.raw.threed_model_fragment_shader));

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
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // colors
        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // normals
        normalBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, 3,
                GLES20.GL_FLOAT, false,
                0, normalBuffer);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        //
       float[] mMVPMatrix = new float[16];

        // mvp = view * model
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
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

        // indices
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, faceNum * 3, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    }

    public boolean readFromPLYFile(final Context context, final int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        String nextLine;
        int tempPos;
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                if (nextLine.contains("element vertex")) {
                    tempPos = nextLine.indexOf("element vertex") + "element vertex ".length();

                    vertexNum = Integer.parseInt(nextLine.substring(tempPos));
                    coords = new float[vertexNum * 3];

                    normals = new float[vertexNum * 3];
                }
                if (nextLine.contains("element face")) {
                    tempPos = nextLine.indexOf("element face") + "element face ".length();

                    faceNum = Integer.parseInt(nextLine.substring(tempPos));
                    indices = new short[faceNum * 3];
                }
                if (nextLine.contains("end_header")) {
                    break;
                }
            }
            Log.d(TAG, "vn=" + vertexNum + " fn=" + faceNum);

            for (int i = 0; i < vertexNum; i++) {
                nextLine = bufferedReader.readLine();

                String[] values = nextLine.split("\\s+");
                float x = Float.parseFloat(values[0]);
                float y = Float.parseFloat(values[1]);
                float z = Float.parseFloat(values[2]);

                coords[i * 3 + 0] = x * 10;
                coords[i * 3 + 1] = y * 10;
                coords[i * 3 + 2] = z * 10;
            }

            for (int i = 0; i < faceNum; i++) {
                nextLine = bufferedReader.readLine();

                String[] values = nextLine.split("\\s+");

                short n = Short.parseShort(values[0]);
                short a = Short.parseShort(values[1]);
                short b = Short.parseShort(values[2]);
                short c = Short.parseShort(values[3]);

                indices[i * 3 + 0] = a;
                indices[i * 3 + 1] = b;
                indices[i * 3 + 2] = c;

                // calculate normals
                float[] edgeA = new float[3];
                float[] edgeB = new float[3];
                // vector A = a - b.
                edgeA[0] = coords[a * 3 + 0] - coords[b * 3 + 0];
                edgeA[1] = coords[a * 3 + 1] - coords[b * 3 + 1];
                edgeA[2] = coords[a * 3 + 2] - coords[b * 3 + 2];
                // vector B = a - c.
                edgeB[0] = coords[a * 3 + 0] - coords[c * 3 + 0];
                edgeB[1] = coords[a * 3 + 1] - coords[c * 3 + 1];
                edgeB[2] = coords[a * 3 + 2] - coords[c * 3 + 2];

                // cross product A * B
                float[] tempNormal = new float[3];
                tempNormal[0] = edgeA[1] * edgeB[2] - edgeB[1] * edgeA[2];
                tempNormal[1] = -edgeA[0] * edgeB[2] + edgeB[0] * edgeA[2];
                tempNormal[2] = edgeA[0] * edgeB[1] - edgeB[0] * edgeA[1];

                // normalize normal vector
                normalize(tempNormal);
                // normal of vertex(a)
                normals[a*3 + 0] += tempNormal[0];
                normals[a*3 + 1] += tempNormal[1];
                normals[a*3 + 2] += tempNormal[2];
                // normal of vertex(b)
                normals[b*3 + 0] += tempNormal[0];
                normals[b*3 + 1] += tempNormal[1];
                normals[b*3 + 2] += tempNormal[2];
                // normal of vertex(c)
                normals[c*3 + 0] += tempNormal[0];
                normals[c*3 + 1] += tempNormal[1];
                normals[c*3 + 2] += tempNormal[2];
            }

            for (int i = 0; i<normals.length/3; i++){
                float[] tempNormal = {normals[i*3 + 0], normals[i*3 + 1], normals[i*3 + 2]};
                normalize(tempNormal);
                normals[i*3 + 0] = tempNormal[0];
                normals[i*3 + 1] = tempNormal[1];
                normals[i*3 + 2] = tempNormal[2];
            }
            centralize();
        } catch (IOException e) {
            //        return null;
        }
        return true;
    }

    private void normalize(float[] temp) {
        float vLength = 0.0f;
        for (int i = 0; i < temp.length; i++) {
            vLength += temp[i] * temp[i];
        }

        vLength =(float) Math.sqrt(vLength);

        for (int i = 0; i< temp.length; i++){
            temp[i] = temp[i]/vLength;
        }
    }

    private void centralize1(){

    }
    private void centralize(){
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;

        for (int i = 0; i<coords.length/3; i++){
            // x
            if (coords[i*3] < minX)
                minX = coords[i*3];
            if (coords[i*3] > maxX)
                maxX = coords[i*3];
            // y
            if (coords[i*3+1] < minY)
                minY = coords[i*3+1];
            if (coords[i*3+1] > maxY)
                maxY = coords[i*3+1];
            // z
            if (coords[i*3+2] < minZ)
                minZ = coords[i*3+2];
            if (coords[i*3+2] > maxZ)
                maxZ = coords[i*3+2];
        }

        float midX = (maxX + minX) /2;
        float midY = (maxY + minY) /2;
        float midZ = (maxZ + minZ) /2;

        Log.d(TAG, "mx="+midX+" my="+midY+" mz="+midZ);
        float scaleCoef = 10.f/(maxX - minX + maxY - minY + maxZ - minZ);


        for (int i = 0; i<coords.length/3; i++){
            coords[i*3] = scaleCoef * (coords[i*3]-midX);
            coords[i*3+1] = scaleCoef * (coords[i*3+1]-midY);
            coords[i*3+2] = scaleCoef * (coords[i*3+2]-midZ);

            Log.d(TAG,"x="+coords[i*3]+" y="+coords[i*3+1]+" z="+coords[i*3+2]);
        }
    }
}
