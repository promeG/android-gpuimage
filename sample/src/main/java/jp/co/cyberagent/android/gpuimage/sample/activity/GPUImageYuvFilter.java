/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage.sample.activity;

import android.opengl.GLES30;

import java.util.concurrent.TimeUnit;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoPassTextureSamplingFilter;
import timber.log.Timber;


public class GPUImageYuvFilter extends GPUImageFilter {

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




    public GPUImageYuvFilter() {
	super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    public void onInit() {
	super.onInit();
	initTexelOffsets();
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
