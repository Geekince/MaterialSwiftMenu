package com.kince.widget.listenter;

/**
 * Created by Kince183 on 2017/7/13.
 */
public interface ProgressListener {

    /**
     *
     */
    void progressStart();

    /**
     *
     * @param progress
     */
    void progressStatus(int progress);

    /**
     *
     */
    void progressEnd();

}
