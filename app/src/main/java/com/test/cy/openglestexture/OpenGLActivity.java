package com.test.cy.openglestexture;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2015/4/11.
 */
public class OpenGLActivity extends Activity {

    class MyGLSurfaceView extends GLSurfaceView{
        public MyGLSurfaceView(Context context){
            super(context);
            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

  //        mRenderer = new LessonTwoRenderer();
            mRenderer = new MyGLRenderer(context);
            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer);
        }
    }
    private MyGLSurfaceView mGLView;
//    private LessonTwoRenderer mRenderer;
    private MyGLRenderer mRenderer;
    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        mGLView = new MyGLSurfaceView(this);

        setContentView(mGLView);
    }

    private float touchDownX, touchDownY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                mRenderer.yAngle = -(touchDownX - e.getX()) / 20.f;
                //  rotX += (touchDownX - e.getX()) /20.f
                mRenderer.xAngle = -(touchDownY - e.getY()) / 20.f;
                touchDownX = e.getX();
                touchDownY = e.getY();
            }
            case MotionEvent.ACTION_DOWN: {
                touchDownX = e.getX();
                touchDownY = e.getY();
            }
        }
        return true;
    }
}
