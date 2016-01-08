/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.TimeUnit;

import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;
import timber.log.Timber;

import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;


public class GPUImageInsideScaleFilter extends GPUImageFilter {

    public static final String VERTEX_SHADER =
		    "attribute vec4 position;\n" +
		    "attribute vec4 inputTextureCoordinate;\n" +
		    "varying vec2 textureCoordinate;\n" +
		    "void main() {\n" +
		    "  gl_Position = position;\n" +
		    "  textureCoordinate = inputTextureCoordinate.xy;\n" +
		    "}\n";

    public static final String FRAGMENT_SHADER =
	    "#extension GL_OES_EGL_image_external : require\n" +
		    "precision mediump float;\n" +
		    "varying vec2 textureCoordinate;\n" +
		    "uniform samplerExternalOES inputImageTexture;\n" +
		    "void main() {\n" +
		    "  gl_FragColor = texture2D(inputImageTexture, textureCoordinate).rgba;\n" +  // (GL2CameraEye)
		    "}\n";




    public GPUImageInsideScaleFilter() {
	super(VERTEX_SHADER, FRAGMENT_SHADER);
	mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
		.order(ByteOrder.nativeOrder())
		.asFloatBuffer();
	mGLCubeBuffer.put(CUBE).position(0);

	mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
		.order(ByteOrder.nativeOrder())
		.asFloatBuffer();
    }

    @Override
    public void onInit() {
	super.onInit();
	initTexelOffsets();
    }

    int mImageWidth = Configure.WIDTH;
    int mImageHeight = Configure.HEIGHT;
    static final float CUBE[] = {
	    -1.0f, -1.0f,
	    1.0f, -1.0f,
	    -1.0f, 1.0f,
	    1.0f, 1.0f,
    };
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    Rotation mRotation = Rotation.ROTATION_90;

    private float addDistance(float coordinate, float distance) {
	return coordinate == 0.0f ? distance : 1 - distance;
    }


    private void adjustImageScaling() {
	Timber.d("adjustImageScaling(): ");
	long startT = System.nanoTime();
	float outputWidth = mOutputWidth;
	float outputHeight = mOutputHeight;
	if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
	    outputWidth = mOutputHeight;
	    outputHeight = mOutputWidth;
	}

	float ratio1 = outputWidth / mImageWidth;
	float ratio2 = outputHeight / mImageHeight;
	float ratioMax = Math.max(ratio1, ratio2);
	int imageWidthNew = Math.round(mImageWidth * ratioMax);
	int imageHeightNew = Math.round(mImageHeight * ratioMax);

	float ratioWidth = imageWidthNew / outputWidth;
	float ratioHeight = imageHeightNew / outputHeight;

	float[] cube = CUBE;
	float[] textureCords = TextureRotationUtil
		.getRotation(mRotation, false, false);
	if (true) {
	    float distHorizontal = (1 - 1 / ratioWidth) / 2;
	    float distVertical = (1 - 1 / ratioHeight) / 2;
	    textureCords = new float[]{
		    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
		    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
		    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
		    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
	    };
	} else {
	    cube = new float[]{
		    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
		    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
		    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
		    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
	    };
	}

	mGLCubeBuffer.clear();
	mGLCubeBuffer.put(cube).position(0);
	mGLTextureBuffer.clear();
	mGLTextureBuffer.put(textureCords).position(0);
	Timber.d("adjustImageScaling cost: " + TimeUnit.NANOSECONDS.toMillis(
		System.nanoTime() - startT));
    }


    int count = 0;

    @Override
    protected void onDrawArraysPre() {
	super.onDrawArraysPre();
	if (false) {
	    long startT = System.nanoTime();

	    long startT2 = System.nanoTime();
	    int width = mOutputWidth;
	    int height = mOutputHeight;
	    ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
	    buf.order(ByteOrder.LITTLE_ENDIAN);
	    buf.rewind();
	    GLES20.glReadPixels(0, 0, width, height,
		    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);

	    ByteBuffer yuvBuf = ByteBuffer.allocateDirect(width * height * 4 * 2 / 3);
	    GPUImageNativeLibrary.rgb2Yuv420p(buf.array(), width, height,
		    yuvBuf.array());

	    OpenGlUtils.dumpGlError("glReadPixels22");
	    Timber.d(
		    "glReadPixels cost2222: " + buf.array().length + "  :   " + TimeUnit.NANOSECONDS
			    .toMillis(
				    System.nanoTime() - startT2) + "     " + width + " : " + height);


	    count++;

	    if (true && count % 200 == 0) {
		long startT3 = System.nanoTime();
		BufferedOutputStream bos = null;
		try {
		    bos = new BufferedOutputStream(new FileOutputStream(
			    Environment.getExternalStorageDirectory() + File.separator + String.valueOf(System.currentTimeMillis()) + ".png"));
		    Bitmap bmp = Bitmap.createBitmap(mOutputWidth, mOutputHeight, Bitmap.Config.ARGB_8888);
		    buf.rewind();
		    bmp.copyPixelsFromBuffer(buf);
		    bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
		    bmp.recycle();
		    Timber.d("save to png cost  " + TimeUnit.NANOSECONDS.toMillis(
			    System.nanoTime() - startT3));


		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} finally {
		    if (bos != null)
			try {
			    bos.close();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}
	    }
	}
    }


    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
	    final FloatBuffer textureBuffer) {
	//GLES20.glViewport(0, 0, 1080, 1800);

	GLES20.glUseProgram(mGLProgId);
	runPendingOnDrawTasks();
	if (!mIsInitialized) {
	    return;
	}
	adjustImageScaling();

	mGLCubeBuffer.position(0);
	GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
	GLES20.glEnableVertexAttribArray(mGLAttribPosition);
	mGLTextureBuffer.position(0);
	GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
		mGLTextureBuffer);
	GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
	if (textureId != OpenGlUtils.NO_TEXTURE) {
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
	    GLES20.glUniform1i(mGLUniformTexture, 0);
	}
	onDrawArraysPre();
	GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	GLES20.glDisableVertexAttribArray(mGLAttribPosition);
	GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    protected void initTexelOffsets() {
	long startT = System.nanoTime();

	Timber.d("initTexelOffsets cost: " + TimeUnit.NANOSECONDS.toMillis(
		System.nanoTime() - startT));
    }



    @Override
    public void onOutputSizeChanged(int width, int height) {
	super.onOutputSizeChanged(width, height);
	initTexelOffsets();
    }
}
