package nonelin.download;

public enum DownloadStatus {
    /**
     * 下载成功
     */
    SUCCESS ,
    /**
     * 下载失败
     */
    FAILED ,
    /**
     * 下载暂停
     */
    PAUSED ,
    /**
     * 下载取消
     */
    CANCELED;
}
