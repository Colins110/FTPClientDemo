package com.example.colin.ftptest03;

/**
 * Created by colin on 2017/4/23 0023.
 */

public interface DownloadListener{
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
