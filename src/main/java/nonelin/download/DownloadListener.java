package nonelin.download;

public interface DownloadListener {

    /**
     * 下载取消
     */
    void onCanceled();

    /**
     * 下载暂停
     */
    void onPaused();

    /**
     * 下载失败
     */
    void onFailed();

    /**
     * 下载成功
     */
    void onSuccess();

    /**
     * 更新下载进度
     * @param progress
     */
    void updateProgress(int progress);
}
