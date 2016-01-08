/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage.sample.activity;

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

import jp.co.cyberagent.android.gpuimage.Configure;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoPassTextureSamplingFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;
import timber.log.Timber;

import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;


public class GPUImageBeauty2Filter extends GPUImageTwoPassTextureSamplingFilter {
    public static final String VERTEX_SHADER =
	    "attribute vec4 position;\n" +
		    "attribute vec4 inputTextureCoordinate;\n" +
		    "const int GAUSSIAN_SAMPLES = 9;\n" +
		    "uniform float texelWidthOffset;\n" +
		    "uniform float texelHeightOffset;\n" +
		    "varying vec2 textureCoordinate;\n" +
		    "varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +
		    "void main()\n" +
		    "{\n" +
		    "gl_Position = position;\n" +
		    "textureCoordinate = inputTextureCoordinate.xy;\n" +
		    "// Calculate the positions for the blur\n" +
		    "int multiplier = 0;\n" +
		    "vec2 blurStep;\n" +
		    "vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
		    "for (int i = 0; i < GAUSSIAN_SAMPLES; i++)\n" +
		    "{\n" +
		    "multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));\n" +
		    "// Blur in x (horizontal)\n" +
		    "blurStep = float(multiplier) * singleStepOffset;\n" +
		    "blurCoordinates[i] = inputTextureCoordinate.xy + blurStep;\n" +
		    "}\n" +
		    "}\n";

    public static final String FRAGMENT_SHADER =
	    "uniform sampler2D inputImageTexture;\n" +
		    "const lowp int GAUSSIAN_SAMPLES = 9;\n" +
		    "varying highp vec2 textureCoordinate;\n" +
		    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +
		    "\n" +
		    "uniform mediump float distanceNormalizationFactor;\n" +
		    " const float smoothDegree = 0.6;\n" +
		    "\n" +
		    "void main()\n" +
		    "{\n" +
		    "lowp vec4 centralColor;\n" +
		    "lowp float gaussianWeightTotal;\n" +
		    "lowp vec4 sum;\n" +
		    "lowp vec4 sampleColor;\n" +
		    "lowp float distanceFromCentralColor;\n" +
		    "lowp float gaussianWeight;\n" +
		    "highp vec4 origin = texture2D(inputImageTexture,textureCoordinate);\n" +
		    "\n" +
		    "centralColor = texture2D(inputImageTexture, blurCoordinates[4]);\n" +
		    "gaussianWeightTotal = 0.18;\n" +
		    "sum = centralColor * 0.18;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[0]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[1]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[2]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[3]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[5]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[6]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[7]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "sampleColor = texture2D(inputImageTexture, blurCoordinates[8]);\n" +
		    "distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
		    "gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);\n" +
		    "gaussianWeightTotal += gaussianWeight;\n" +
		    "sum += sampleColor * gaussianWeight;\n" +
		    "\n" +
		    "highp vec4 bilateral = sum / gaussianWeightTotal;\n" +
		    "     highp vec4 smooth;\n" +
		    "     lowp float r = origin.r;\n" +
		    "     lowp float g = origin.g;\n" +
		    "     lowp float b = origin.b;\n" +
		    "     if (r > 0.3725 && g > 0.1568 && b > 0.0784 && r > b && (max(max(r, g), b) - min(min(r, g), b)) > 0.0588 && abs(r-g) > 0.0588) {\n" +
		    "         smooth = (1.0 - smoothDegree) * (origin - bilateral) + bilateral;\n" +
		    "     }\n" +
		    "     else {\n" +
		    "         smooth = origin;\n" +
		    "     }\n" +
		    "     smooth.r = log(1.0 + 0.2 * smooth.r)/log(1.2);\n" +
		    "     smooth.g = log(1.0 + 0.2 * smooth.g)/log(1.2);\n" +
		    "     smooth.b = log(1.0 + 0.2 * smooth.b)/log(1.2);\n" +
		    "gl_FragColor = smooth;\n" +
		    "}\n";


    private float distanceNormalizationFactor = 1f;
    private float texelSpacingMultiplier = 1f;

    /**
     * Construct new BilateralFilter with default distanceNormalizationFactor of 8.0.
     */
    public GPUImageBeauty2Filter() {
	this(8.0f);
    }

    public GPUImageBeauty2Filter(float distanceNormalizationFactor) {
	super(VERTEX_SHADER, FRAGMENT_SHADER, VERTEX_SHADER, FRAGMENT_SHADER);
	this.distanceNormalizationFactor = distanceNormalizationFactor;
	this.texelSpacingMultiplier = 4.0f;

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

    protected void initTexelOffsets() {
	long startT = System.nanoTime();

	float ratio = getHorizontalTexelOffsetRatio();
	GPUImageFilter filter = mFilters.get(0);
	int distanceNormalizationFactor = GLES20.glGetUniformLocation(filter.getProgram(), "distanceNormalizationFactor");
	filter.setFloat(distanceNormalizationFactor, this.distanceNormalizationFactor);

	int texelWidthOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelWidthOffset");
	int texelHeightOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelHeightOffset");
	filter.setFloat(texelWidthOffsetLocation, ratio / mOutputWidth);
	filter.setFloat(texelHeightOffsetLocation, 0);


	ratio = getVerticalTexelOffsetRatio();
	filter = mFilters.get(1);
	distanceNormalizationFactor = GLES20.glGetUniformLocation(filter.getProgram(), "distanceNormalizationFactor");
	filter.setFloat(distanceNormalizationFactor, this.distanceNormalizationFactor);


	texelWidthOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelWidthOffset");
	texelHeightOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelHeightOffset");

	filter.setFloat(texelWidthOffsetLocation, 0);
	filter.setFloat(texelHeightOffsetLocation, ratio / mOutputHeight);
	Timber.d("initTexelOffsets cost: " + TimeUnit.NANOSECONDS.toMillis(
		System.nanoTime() - startT));
    }



    /**
     * A normalization factor for the distance between central color and sample color.
     *
     * @param distanceNormalizationFactor
     */
    public void setDistanceNormalizationFactor(float distanceNormalizationFactor) {
	this.distanceNormalizationFactor = distanceNormalizationFactor;
	runOnDraw(new Runnable() {
	    @Override
	    public void run() {
		initTexelOffsets();
	    }
	});
    }

    /**
     * A scaling for the size of the applied blur, default of 4.0
     *
     * @param texelSpacingMultiplier
     */
    public void setTexelSpacingMultiplier(float texelSpacingMultiplier) {
	this.texelSpacingMultiplier = texelSpacingMultiplier;
	runOnDraw(new Runnable() {
	    @Override
	    public void run() {
		initTexelOffsets();
	    }
	});
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
	super.onOutputSizeChanged(Configure.WIDTH, Configure.HEIGHT);
	initTexelOffsets();
    }

    public float getVerticalTexelOffsetRatio() {
	return texelSpacingMultiplier;
    }

    public float getHorizontalTexelOffsetRatio() {
	return texelSpacingMultiplier;
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
	adjustImageScaling();

	runPendingOnDrawTasks();
	if (!mIsInitialized) {
	    return;
	}

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



}
