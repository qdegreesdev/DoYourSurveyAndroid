package com.qdegrees.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

   private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

   private File mFile;
   private String mPath;
   private String content_type;
   public  UploadCallbacks mListner;


   public ProgressRequestBody(File file,String contentType, UploadCallbacks listener){
      mFile=file;
      content_type=contentType;
      mListner = listener;
   }

   public interface UploadCallbacks {
      void onProgressUpdate(int percentage);
      void onError();
      void onFinish();


   }
   @Override
   public MediaType contentType() {
      return MediaType.parse(content_type+"/*");
   }

   @Override
   public long contentLength() throws IOException {
      return mFile.length();
   }

   @Override
   public void writeTo(BufferedSink sink) throws IOException {
      long fileLength = mFile.length();
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      FileInputStream in = new FileInputStream(mFile);
      long uploaded = 0;

      try {
         int read;
         Handler handler = new Handler(Looper.getMainLooper());
         while ((read = in.read(buffer)) != -1) {

            // update progress on UI thread
            handler.post(new ProgressUpdater(uploaded, fileLength));

            uploaded += read;
            sink.write(buffer, 0, read);
         }
      } finally {
         in.close();
      }
   }

 class ProgressUpdater implements Runnable {

      long mUploaded;
      long mTotal;
      public ProgressUpdater(long uploaded,long fileLength){
         mUploaded=uploaded;
         mTotal = fileLength;
      }

    @Override
    public void run() {
       mListner.onProgressUpdate((int)(100 * mUploaded / mTotal));
    }
 }


}
