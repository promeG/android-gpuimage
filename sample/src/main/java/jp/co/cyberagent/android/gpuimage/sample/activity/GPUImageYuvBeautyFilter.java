/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage.sample.activity;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoPassTextureSamplingFilter;
import timber.log.Timber;


public class GPUImageYuvBeautyFilter extends GPUImageFilterGroup {

    private float distanceNormalizationFactor = 1f;
    private float texelSpacingMultiplier = 1f;
    static final List <GPUImageFilter> list = new ArrayList<>();

    static {
	list.add(new GPUImageYuvFilter());
	list.add(new GPUImageBeautyFilter(4.0f));
    }

    public GPUImageYuvBeautyFilter() {
	super(list);
    }
}
