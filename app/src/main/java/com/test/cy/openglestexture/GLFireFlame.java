package com.test.cy.openglestexture;

import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Random;

/**
 * Created by Administrator on 2015/4/16.
 */
public class GLFireFlame {

    private final static String TAG = "GL_FIRE_FLAME";

    GLFireFlameParticle[] mGLFireFlameParticles = new GLFireFlameParticle[PARTICLE_NUM];

    private final static int PARTICLE_NUM = 100;

    public GLFireFlame(Context context){
       for (int i = 0; i< PARTICLE_NUM; i++){
           mGLFireFlameParticles[i] = new GLFireFlameParticle(context);

           // initialize starting position and speed.
           float[] startPos = new float[3];
           startPos[0] = (float)Math.random() * 2 - 1;
           startPos[1] = 0.0f;
           startPos[2] = 0.0f;

           float[] speed = new float[3];
           speed[0] = ((float)Math.random() * 2 - 1) / 20;
           speed[1] = (float)Math.random() / 10;
           speed[2] = 0.0f;

           float lifePoint = (float)Math.random()*2000 + 1000;
           float hSpeed = (float)Math.random()/2000.0f;
           if (speed[0]>0)
                hSpeed *=-1;
           mGLFireFlameParticles[i].initialize(startPos, speed, lifePoint, hSpeed);
       }
    }

    public void update(){

        for (int i = 0; i< PARTICLE_NUM; i++){
            mGLFireFlameParticles[i].update();

            if (mGLFireFlameParticles[i].lifePoint < 0){
                float[] startPos = new float[3];
                startPos[0] = (float)Math.random() * 2 - 1;
                startPos[1] = 0.0f;
                startPos[2] = 0.0f;

                float[] speed = new float[3];
                speed[0] = ((float)Math.random() * 2 - 1) / 20;
                speed[1] = (float)Math.random() / 10;
                speed[2] = 0.0f;

                float lifePoint = (float)Math.random()*2000 + 1000;
                float hSpeed = (float)Math.random()/2000.0f;
                if (speed[0]>0)
                    hSpeed *=-1;
                mGLFireFlameParticles[i].initialize(startPos, speed, lifePoint, hSpeed);
            }
        }
    }

    public void draw(float[] mViewMatrix, float[] mProjectionMatrix, float[] mModelMatrix){
        for (int i = 0; i< PARTICLE_NUM; i++){
            float[] tMM = new float[16];
            Matrix.translateM(tMM, 0, mModelMatrix, 0, mGLFireFlameParticles[i].currentX,
                    mGLFireFlameParticles[i].currentY,
                    mGLFireFlameParticles[i].currentZ);
            mGLFireFlameParticles[i].draw(mViewMatrix, mProjectionMatrix, tMM);
        }
    }
}
