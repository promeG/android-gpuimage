/**
 * @author wysaid
 * @mail admin@wysaid.org
 *
*/

package jp.co.cyberagent.android.gpuimage.sample.activity;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;


public class GPUImageYuvIdleFilter extends GPUImageFilterGroup {

    private float distanceNormalizationFactor = 1f;
    private float texelSpacingMultiplier = 1f;
    static final List <GPUImageFilter> list = new ArrayList<>();

    static {
	list.add(new GPUImageYuvFilter());
	list.add(new GPUImageFilter());
    }

    public GPUImageYuvIdleFilter() {
	super(list);
    }
}
