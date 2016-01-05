/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage.sample.activity;

import android.opengl.GLES20;

import java.util.concurrent.TimeUnit;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoPassTextureSamplingFilter;
import timber.log.Timber;


public class GPUImageBeautyFilter extends GPUImageTwoPassTextureSamplingFilter {

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
    public GPUImageBeautyFilter() {
	this(8.0f);
    }

    public GPUImageBeautyFilter(float distanceNormalizationFactor) {
	super(VERTEX_SHADER, FRAGMENT_SHADER, VERTEX_SHADER, FRAGMENT_SHADER);
	this.distanceNormalizationFactor = distanceNormalizationFactor;
	this.texelSpacingMultiplier = 4.0f;
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
	super.onOutputSizeChanged(width, height);
	initTexelOffsets();
    }

    public float getVerticalTexelOffsetRatio() {
	return texelSpacingMultiplier;
    }

    public float getHorizontalTexelOffsetRatio() {
	return texelSpacingMultiplier;
    }
}
