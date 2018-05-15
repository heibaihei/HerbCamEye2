package com.herb.java.gleffect;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by zj-db0519 on 2018/5/14.
 */

public class HbGlUtils {

    /**
     *  创建 OES 纹理对象
     * */
    public static int createOESTextureObj() {
        int[] targetTexture = new int[1];

        GLES20.glGenTextures(1, targetTexture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, targetTexture[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        return targetTexture[0];
    }

    /**
     * 根据滤镜脚本、滤镜类型，创建加载生存相应的滤镜对象
     * */
    public static int loadShader(int type, String shaderSource) {
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw new RuntimeException("Create Shader Failed!" + glGetError());
        }

        glShaderSource(shader, shaderSource);
        glCompileShader(shader);
        return shader;
    }

    /**
     * 将定点着色器和片段着色器进行相连
     * */
    public static int linkProgram(int verShader, int fragShader) {
        int program = glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Create Program Failed!" + glGetError());
        }

        glAttachShader(program, verShader);
        glAttachShader(program, fragShader);

        glLinkProgram(program);
        glUseProgram(program);
        return program;
    }
}
