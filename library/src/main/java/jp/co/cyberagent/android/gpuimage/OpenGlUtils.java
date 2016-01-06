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
import android.opengl.GLES30;
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
            GLES30.glGenTextures(1, textures, 0);
            GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GL_TEXTURE_EXTERNAL_OES, 0, img, 0);
        } else {
            GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, usedTexId);
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
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR)
            Timber.d("** " + op + ": glError " + error);
    }



    public static int loadFrameTexture(final ByteBuffer data, final Size size, int usedTexId) {
        Timber.d("createFramebufferObject, tex id:" + usedTexId);

        int[] textures = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenFramebuffers(1, textures, 0);
            dumpGlError("glGenFramebuffers");
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, textures[0]);
            dumpGlError("glBindFramebuffer");
            // Qualcomm recommends clear after glBindFrameBuffer().
            GLES30.glClearColor(0.643f, 0.776f, 0.223f, 1.0f);
            GLES30.glClearDepthf(1.0f);

            ////////////////////////////////////////////////////////////////////////
            // Bind the texture to the generated Framebuffer Object.
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_EXTERNAL_OES,
                    usedTexId,
                    0);
        } else {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, textures[0]);
            // Qualcomm recommends clear after glBindFrameBuffer().
            GLES30.glClearColor(0.643f, 0.776f, 0.223f, 1.0f);
            GLES30.glClearDepthf(1.0f);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_EXTERNAL_OES,
                    usedTexId,
                    0);
            textures[0] = usedTexId;
        }
        dumpGlError("glFramebufferTexture2D");
        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) !=
                GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Timber.d(" Created FBO and attached to texture");
        } else {
            Timber.d( " FBO created and attached to texture.");
        }
        return textures[0];
    }

    public static int loadTexture(final ByteBuffer data, final Size size, final int usedTexId) {
        long startT = System.nanoTime();
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexImage2D(GL_TEXTURE_EXTERNAL_OES, 0, GLES30.GL_LUMINANCE, size.width,
                    size.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data);

           /* bindTexture(usedTexId);
            GLES30.glUniform1i(usedTexId, 0);*/
        } else {
            GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, usedTexId);
            GLES30.glTexSubImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, 0, 0, size.width,
                    size.height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        Timber.d("loadTexture2 cost: " + TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - startT));
        return textures[0];
    }

    private static void bindTexture(final int usedTexId) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, usedTexId);
    }

    public static int loadTextureAsBitmap(final IntBuffer data, final Size size, final int usedTexId) {
        Bitmap bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Config.ARGB_8888);
        return loadTexture(bitmap, usedTexId);
    }

    public static int loadShader(final String strSource, final int iType) {
        long startT = System.nanoTime();
        int[] compiled = new int[1];
        int iShader = GLES30.glCreateShader(iType);
        GLES30.glShaderSource(iShader, strSource);
        GLES30.glCompileShader(iShader);
        GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES30.glGetShaderInfoLog(iShader));
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
        iVShader = loadShader(strVSource, GLES30.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES30.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES30.glCreateProgram();

        GLES30.glAttachShader(iProgId, iVShader);
        GLES30.glAttachShader(iProgId, iFShader);

        GLES30.glLinkProgram(iProgId);

        GLES30.glGetProgramiv(iProgId, GLES30.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES30.glDeleteShader(iVShader);
        GLES30.glDeleteShader(iFShader);
        Timber.d("loadProgram cost: " + TimeUnit.NANOSECONDS
                .toMillis(System.nanoTime() - startT));
        return iProgId;
    }

    public static float rnd(final float min, final float max) {
        float fRandNum = (float) Math.random();
        return min + (max - min) * fRandNum;
    }
}
