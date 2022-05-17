package nonelin.download;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class DownloadTask {

    private DownloadListener downloadListener;

    private boolean isCanceled = false;

    private boolean isPaused = false;

    public DownloadTask(DownloadListener listener){
        this.downloadListener = listener;
    }

    public void startDownload(String url , File downloadFile){
        DownloadStatus downloadStatus = download(url , downloadFile);
        switch (downloadStatus){
            case SUCCESS:{
                this.downloadListener.onSuccess();
                break;
            }
            case PAUSED:{
                this.downloadListener.onPaused();
                break;
            }
            case CANCELED:{
                this.downloadListener.onCanceled();
                break;
            }
            default:{
                this.downloadListener.onFailed();
                break;
            }
        }
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        this.isCanceled = true;
    }


    private DownloadStatus download(String url , File downloadFile){
        InputStream is = null;
        RandomAccessFile accessFile = null;
        try {
            //已经下载的长度
            long downloadLength = 0;
            if (downloadFile.exists()){
                downloadLength = downloadFile.length();
            }
            long contentLength = getDownloadFileLength(url);
            if (contentLength == -1){
                return DownloadStatus.FAILED;
            }else if (contentLength == downloadLength){
                //已经下载完成了
                return DownloadStatus.SUCCESS;
            }else{
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .addHeader("RANGE" , "bytes=" + downloadLength + "-")
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (response != null){
                    is = response.body().byteStream();
                    accessFile = new RandomAccessFile(downloadFile , "rw");
                    accessFile.seek(downloadLength);
                    byte[] bytes = new byte[1024];
                    int total = 0;
                    int len;
                    while ((len = is.read(bytes)) != -1){
                        if (isCanceled){
                            return DownloadStatus.CANCELED;
                        }else if (isPaused){
                            return DownloadStatus.PAUSED;
                        }else{
                            total += len;
                            accessFile.write(bytes , 0 , len);
                            int progress = (int) ((total + downloadLength) * 100 / contentLength);
                            this.downloadListener.updateProgress(progress);
                        }
                    }
                    response.body().close();
                    return DownloadStatus.SUCCESS;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (is != null){
                    is.close();
                }
                if (accessFile != null){
                    accessFile.close();
                }
                if (isCanceled && downloadFile != null){
                    downloadFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return DownloadStatus.FAILED;
    }

    private long getDownloadFileLength(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()){
            ResponseBody requestBody = response.body();
            if (requestBody != null){
                long length = requestBody.contentLength();
                requestBody.close();
                return length;
            }
        }
        return -1;
    }
}
