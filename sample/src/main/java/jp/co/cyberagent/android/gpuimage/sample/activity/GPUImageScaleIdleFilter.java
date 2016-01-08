/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage.sample.activity;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.Configure;
import jp.co.cyberagent.android.gpuimage.GPUImageBilateralFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;


public class GPUImageScaleIdleFilter extends GPUImageFilterGroup {

    private float distanceNormalizationFactor = 1f;
    private float texelSpacingMultiplier = 1f;
    static final List <GPUImageFilter> list = new ArrayList<>();

    static {
        if (Configure.YUV2RGB_USING_SHADER) {
            list.add(new GPUImageYuvFilter());
        } else {
            list.add(new GPUImageFilter());
        }
        list.add(new GPUImageBeautyFilter(4.0f));
        list.add(new GPUImageFilter());
        list.add(new GPUImageFilter());

    }

    public GPUImageScaleIdleFilter() {
	super(list);
    }
}
