package com.common.lib.camera.record;

import android.annotation.TargetApi;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class ShaderViewDraw {

    private static final String NO_FILTER_VERTEX_SHADER = ""
            + "attribute vec4 position;\n"
            + "attribute vec2 inputTextureCoordinate;\n" + " \n"
            + "varying vec2 textureCoordinate;\n" + " \n" + "void main()\n"
            + "{\n" + "    gl_Position = position;\n"
            + "    textureCoordinate = inputTextureCoordinate;\n" + "}";
    private static final String NO_FILTER_FRAGMENT_SHADER = ""
            + "#extension GL_OES_EGL_image_external : require\n"
            + "varying highp vec2 textureCoordinate;\n"
            + " \n"
            + "uniform samplerExternalOES inputImageTexture;\n"
            + " \n"
            + "void main()\n"
            + "{\n"
            + "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    private int mGLAttribPosition;
    private int mGLAttribTextureCoordinate;
    private int mGLUniformTexture;

    private short[] drawOrder = {0, 1, 2, 2, 0, 3};

    private final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4;

    private float[] squareCoords;

    private float[] textureVertices;

    private int mTextureId;

    private void resetBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

    }

    private void initShader() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, NO_FILTER_VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                NO_FILTER_FRAGMENT_SHADER);
        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        mGLAttribPosition = GLES20.glGetAttribLocation(mProgram, "position");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mProgram,
                "inputTextureCoordinate");
        mGLUniformTexture = GLES20.glGetUniformLocation(mProgram,
                "inputImageTexture");
    }

    private void initBuffer(int textureType) {
        squareCoords = new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f}/*new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f}*/;
//        if (isBackCamera) {
//            textureVertices = new float[]{0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f};
//        } else {
//            textureVertices = new float[]{1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f}/*new float[]{0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f}*/;
//        }

        if (textureType == Constants.VIDEO_DEGREE_0) {
            textureVertices = new float[]{0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f};
        } else if (textureType == Constants.VIDEO_DEGREE_90) {
            textureVertices = new float[]{0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f};
        } else if (textureType == Constants.VIDEO_DEGREE_180) {
            textureVertices = new float[]{1f, 1f, 1f, 0f, 0f, 0f, 0f, 1f};
        } else {
            textureVertices = new float[]{1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f};
        }

        resetBuffer();
        ByteBuffer bb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        bb.order(ByteOrder.nativeOrder());
        drawListBuffer = bb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    public ShaderViewDraw() {
        initShader();
    }

    public void resetTextureID(int textureId, int textureType) {
        this.mTextureId = textureId;
        initBuffer(textureType);
    }


    public void draw() {
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GL10.GL_BLEND);

        GLES20.glUseProgram(mProgram);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate,
                COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride,
                textureVerticesBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);

        drawListBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    /**
     * terminatinng, this should be called in GL context
     */
    public void release() {
        if (mProgram >= 0)
            GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
    }

}
