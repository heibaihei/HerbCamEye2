package com.herb.java.gleffect;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by zj-db0519 on 2018/5/14.
 */

public class HBIRender implements SurfaceTexture.OnFrameAvailableListener {
    private static final int MSG_RENDER_INIT = 1;
    private static final int MSG_RENDER_UPDATE = 2;
    private static final int MSG_RENDER_DEINIT = 3;


    private Context mContext;
    private TextureView mTextureView;
    private int mOESTextureId;
    private SurfaceTexture mOESSurfaceTexture = null;

    private HandlerThread mRenderThreadHandler;
    private Handler mRenderHandler;

    private HBFilterEngine mFilterEngine;
    private float[] transformMatrix = new float[16];

    /** About EGL Display */
    private EGL10 mEgl = null;
    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;
    private EGLConfig[] mEGLConfig = new EGLConfig[1];
    private EGLSurface mEglSurface;
    private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;

    public void initialRender(TextureView textureView, Context context) {
        mContext = context;
        mTextureView = textureView;
        mOESTextureId = HbGlUtils.createOESTextureObj();

        mOESSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mOESSurfaceTexture.setOnFrameAvailableListener(this);

        mRenderThreadHandler = new HandlerThread("IRender Thread");
        mRenderThreadHandler.start();

        mRenderHandler = new Handler(mRenderThreadHandler.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_RENDER_INIT: {
                            EGLContextInitial(mTextureView.getSurfaceTexture());
                            mFilterEngine = new HBFilterEngine(mOESTextureId, mContext);
                        }
                        return;
                    case MSG_RENDER_UPDATE:
                        drawFrame();
                        return;
                    case MSG_RENDER_DEINIT:
                        return;
                    default:
                        return;
                }
            }
        };

        mRenderHandler.sendEmptyMessage(MSG_RENDER_INIT);
    }

    private void EGLContextInitial(SurfaceTexture surfaceTexture) {
        mEgl = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY)
            throw new RuntimeException("eglGetDisplay failed! " + mEgl.eglGetError());

        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEGLDisplay, version))
            throw new RuntimeException("eglInitialize failed! " + mEgl.eglGetError());

        int[] attributes = {
                //颜色缓冲区R、G、B、A分量的位数
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE,8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                //颜色缓冲区所有颜色分量的位数
                EGL10.EGL_BUFFER_SIZE, 32,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };

        int[] configsNum = new int[1];
        if (!mEgl.eglChooseConfig(mEGLDisplay, attributes, mEGLConfig, 1, configsNum)) {
            throw new RuntimeException("eglChooseConfig failed! " + mEgl.eglGetError());
        }

        mEglSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig[0], surfaceTexture, null);
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        //创建上下文，EGL10.EGL_NO_CONTEXT表示不和别的上下文共享资源
        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, mEGLConfig[0], EGL10.EGL_NO_CONTEXT, contextAttribs);

        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY || mEGLContext == EGL10.EGL_NO_CONTEXT){
            throw new RuntimeException("eglCreateContext fail failed! " + mEgl.eglGetError());
        }
        //指定mEGLContext为当前系统的EGL上下文，第一个mEglSurface表示绘图表面，第二个表示读取表面
        if (!mEgl.eglMakeCurrent(mEGLDisplay,mEglSurface, mEglSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed! " + mEgl.eglGetError());
        }
    }

    private void drawFrame() {
        if (mOESSurfaceTexture != null) {
            mOESSurfaceTexture.updateTexImage();
            mOESSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        mEgl.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGLContext);
        glContextInitial(mTextureView.getWidth(), mTextureView.getHeight());
        if (mFilterEngine.drawTexture(transformMatrix) == true) {
            mEgl.eglSwapBuffers(mEGLDisplay, mEglSurface);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mRenderHandler != null)
            mRenderHandler.sendEmptyMessage(MSG_RENDER_UPDATE);
    }

    public SurfaceTexture getOESSurfaceTexture() { return mOESSurfaceTexture; }

    private void glContextInitial(int width, int height) {
        GLES20.glViewport(0,0, width, height);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 1f, 0f, 0f);
    }
}
