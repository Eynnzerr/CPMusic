package com.example.mymusicplayer.service;

import android.os.Environment;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

//直接借鉴了郭霖大神
public class DownloadTask {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener listener;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    private int lastProgress;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    public void download(String downloadUrl)
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Download", "异步下载任务: id = " + downloadUrl);
                int status = 0;
                InputStream inputStream = null;
                RandomAccessFile savedFile = null;
                File file = null;
                try{
                    long downloadedLength = 0;
                    //???为什么必须是这个名字？就连把/换成别的都不行。
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
                    file = new File(directory + fileName);
                    if( file.exists() )
                    {
                        downloadedLength = file.length();
                    }
                    long contentLength = getContentLength(downloadUrl);
                    Log.d("download",contentLength+"");
                    if(contentLength == 0)
                        status = TYPE_FAILED;
                    if(contentLength == downloadedLength)
                        status = TYPE_SUCCESS;
                    OkHttpClient client=new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("RANGE","bytes="+downloadedLength+"-")
                            .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.864.37")
                            .url(downloadUrl)
                            .build();
                    Response response = client.newCall(request).execute();
                    if(response != null){
                        inputStream = response.body().byteStream();
                        savedFile = new RandomAccessFile(file,"rw");
                        savedFile.seek(downloadedLength);
                        byte[] b=new byte[1024];
                        int total=0;
                        int len;
                        while( ( len = inputStream.read(b) ) != -1 ){
                            if (isCanceled) {
                                status = TYPE_CANCELED;
                            } else if (isPaused) {
                                Log.d("Download", "下载暂停: "+Thread.currentThread().getId());
                                status = TYPE_PAUSED;
                            } else {
                                //Log.d("download","本次读取长度为" + len);
                                total += len;
                                savedFile.write(b, 0, len);
                                int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                                publishProgress(progress);
                            }
                        }
                        Log.d("download","total = " + total);
                    }
                    response.body().close();
                    status = TYPE_SUCCESS;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally {
                    try {
                        if( inputStream !=null )
                            inputStream.close();
                        if(savedFile!=null)
                            savedFile.close();
                        if(isCanceled&&file!=null)
                            file.delete();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                //承担了原来onPostExecute的功能
                switch (status)
                {
                    case TYPE_SUCCESS:
                        listener.onSuccess();
                        break;
                    case TYPE_FAILED:
                        listener.onFailed();
                        break;
                    case TYPE_PAUSED:
                        listener.onPaused();
                        break;
                    case TYPE_CANCELED:
                        listener.onCanceld();
                        break;
                }
            }
        }).start();
    }

    /*
    public void download2(String downloadUri)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://music.163.com/song/media/outer/")
                .build();
        DownloadFileService downloadFileService = retrofit.create(DownloadFileService.class);
        Call<ResponseBody> dataCall = downloadFileService.downloadFile(downloadUri);
        //异步请求
        dataCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if( response.isSuccessful() )
                {
                    long contentLength = response.body().contentLength();
                    Log.d("OnResponse","contentLength = " + contentLength);

                    InputStream inputStream = null;
                    RandomAccessFile savedFile = null;
                    File file = null;
                    try{
                        long downloadedLength = 0;
                        String fileName = downloadUri;
                        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
                        file = new File(directory + fileName);
                        if( file.exists() )
                        {
                            downloadedLength = file.length();
                        }
                        Log.d("download",contentLength+"");
                        if(response != null){
                            inputStream = response.body().byteStream();
                            savedFile = new RandomAccessFile(file,"rw");
                            savedFile.seek(downloadedLength);
                            byte[] b=new byte[2048];
                            int total = 0;
                            int len;
                            while( ( len = inputStream.read(b) ) != -1 ){
                                if (isCanceled) {
                                } else if (isPaused) {
                                    Log.d("Download", "下载暂停: "+Thread.currentThread().getId());
                                } else {
                                    Log.d("download","本次读取长度为" + len);
                                    total += len;
                                    savedFile.write(b, 0, len);
                                    int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                                    publishProgress(progress);
                                }
                            }
                            Log.d("download","total = " + total);
                        }
                        response.body().close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            if( inputStream !=null )
                                inputStream.close();
                            if(savedFile!=null)
                                savedFile.close();
                            if(isCanceled&&file!=null)
                                file.delete();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("OnFailure","请求失败呜呜呜");
            }
        });
    }*/

    //承担了原来onProgressUpdate的功能
    private void publishProgress(int progress)
    {
        if( progress > lastProgress )
        {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    public void pauseDownload(){
        isPaused=true;

    }
    public void cancelDownload(){
        isCanceled=true;
    }

    private long getContentLength(String downloadUrl) throws IOException, ExecutionException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .addHeader("Accept-Encoding", "identity")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.864.37")
                .build();

        /*
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                Log.d("getContentLength","爬取文件失败");
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                if( response.isSuccessful() )
                {
                    long contentLength = response.body().contentLength();
                    response.close();
                    Log.d("getContentLength","读取文件大小为:" + contentLength);
                }
            }
        });*/


        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Long> future = executorService.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Response response = client.newCall(request).execute();
                if( response != null && response.isSuccessful() ){
                    long contentLength = response.body().contentLength();
                    response.close();
                    Log.d("getContentLength",contentLength+"");
                    return contentLength;
                }
                return 0L;
            }
        });
        executorService.shutdown();
        return future.get();

    }
}
