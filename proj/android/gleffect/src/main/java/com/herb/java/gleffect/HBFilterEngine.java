package com.herb.java.gleffect;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;


public class HBFilterEngine {

    public static final String POSITION_ATTRIBUTE = "aPosition";
    public static final String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";
    public static final String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    public static final String TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";

    /** 每行前两个值为顶点坐标，后两个为纹理坐标 */
    private static final float[] vertexData = {
            1f, 1f, 1f, 1f,
            -1f, 1f, 0f, 1f,
            -1f, -1f, 0f, 0f,
            1f, 1f, 1f, 1f,
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f
    };

    private Context mContext;
    private FloatBuffer mBuffer;
    private int mOESTextureId = -1;
    private int vertexShader = -1;
    private int fragmentShader = -1;
    private int mShaderProgram = -1;

    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;

    public HBFilterEngine(int OESTextureId, Context context) {
        mContext = context;
        mOESTextureId = OESTextureId;

        mBuffer = createBuffer(vertexData);
        vertexShader = HbGlUtils.loadShader(GL_VERTEX_SHADER, HBFileUtil.readShaderResource(mContext, R.raw.base_vertex_shader));
        fragmentShader = HbGlUtils.loadShader(GL_FRAGMENT_SHADER, HBFileUtil.readShaderResource(mContext, R.raw.base_fragment_shader));
        mShaderProgram = HbGlUtils.linkProgram(vertexShader, fragmentShader);
    }

    public boolean drawTexture(float[] transformMatrix) {
        aPositionLocation = glGetAttribLocation(mShaderProgram, HBFilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, HBFilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, HBFilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, HBFilterEngine.TEXTURE_SAMPLER_UNIFORM);

        glActiveTexture(GLES20.GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        glUniform1i(uTextureSamplerLocation, 0);
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        if (mBuffer != null) {
            //将顶点和纹理坐标传给顶点着色器
            mBuffer.position(0);

            /** 使能顶点属性: 顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值 */
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mBuffer);

            mBuffer.position(2);
            /** 纹理坐标从位置2开始读取，纹理坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值 */
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mBuffer);

            /** 绘制两个三角形（6个顶点）*/
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }

        return true;
    }

    /**
     * 将顶点和纹理坐标数据使用FloatBuffer来存储，防止内存回收
     * */
    public FloatBuffer createBuffer(float[] vertexData) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(vertexData, 0, vertexData.length).position(0);
        return buffer;
    }
}

