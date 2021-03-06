/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.Camera.Size;
import android.opengl.GLES10;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import timber.log.Timber;

public class OpenGlUtils {
    public static final int NO_TEXTURE = -1;

    public static int loadTexture(final Bitmap img, final int usedTexId) {
        return loadTexture(img, usedTexId, true);
    }

    public static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        long startT = System.nanoTime();
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GL_TEXTURE_EXTERNAL_OES, 0, img, 0);
        } else {
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, usedTexId);
            GLUtils.texSubImage2D(GL_TEXTURE_EXTERNAL_OES, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        Timber.d("loadTexture1 cost: " + TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - startT));
        return textures[0];
    }
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    public static  void dumpGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
            Timber.d("** " + op + ": glError " + error);
    }



    public static int loadFrameTexture(final ByteBuffer data, final Size size, int usedTexId) {
        Timber.d("createFramebufferObject, tex id:" + usedTexId);

        int[] textures = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenFramebuffers(1, textures, 0);
            dumpGlError("glGenFramebuffers");
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, textures[0]);
            dumpGlError("glBindFramebuffer");
            // Qualcomm recommends clear after glBindFrameBuffer().
            GLES20.glClearColor(0.643f, 0.776f, 0.223f, 1.0f);
            GLES20.glClearDepthf(1.0f);

            ////////////////////////////////////////////////////////////////////////
            // Bind the texture to the generated Framebuffer Object.
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_EXTERNAL_OES,
                    usedTexId,
                    0);
        } else {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, textures[0]);
            // Qualcomm recommends clear after glBindFrameBuffer().
            GLES20.glClearColor(0.643f, 0.776f, 0.223f, 1.0f);
            GLES20.glClearDepthf(1.0f);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_EXTERNAL_OES,
                    usedTexId,
                    0);
            textures[0] = usedTexId;
        }
        dumpGlError("glFramebufferTexture2D");
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) !=
                GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Timber.d(" Created FBO and attached to texture");
        } else {
            Timber.d( " FBO created and attached to texture.");
        }
        return textures[0];
    }

    public static int loadTexture(final IntBuffer data, final Size size, final int usedTexId) {
        long startT = System.nanoTime();
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size.width,
                    size.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, size.width,
                    size.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        Timber.d("loadTexture2 cost: " + TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - startT));
        return textures[0];
    }

    private static void bindTexture(final int usedTexId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, usedTexId);
    }

    public static int loadTextureAsBitmap(final IntBuffer data, final Size size, final int usedTexId) {
        Bitmap bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Config.ARGB_8888);
        return loadTexture(bitmap, usedTexId);
    }

    public static int loadShader(final String strSource, final int iType) {
        long startT = System.nanoTime();
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        }
        Timber.d("loadShader cost: " + TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - startT));
        return iShader;
    }

    public static int loadProgram(final String strVSource, final String strFSource) {
        long startT = System.nanoTime();

        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES20.glCreateProgram();

        GLES20.glAttachShader(iProgId, iVShader);
        GLES20.glAttachShader(iProgId, iFShader);

        GLES20.glLinkProgram(iProgId);

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        GLES20.glDeleteShader(iFShader);
        Timber.d("loadProgram cost: " + TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - startT));
        return iProgId;
    }

    public static float rnd(final float min, final float max) {
        float fRandNum = (float) Math.random();
        return min + (max - min) * fRandNum;
    }
}
