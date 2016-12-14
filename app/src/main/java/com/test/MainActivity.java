package com.test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;


public class MainActivity extends Activity implements OnClickListener{

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    String max;

    AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    int blockSize;// = 256;
    Button startStopButton;
    Button btDownload;
    TextView tvPitch;
    boolean started = false;
    boolean CANCELLED_FLAG = false;

    MediaPlayer player;

    RecordAudio recordTask;
    ImageView imageViewDisplaySectrum;
    MyImageView imageViewScale;
    Bitmap bitmapDisplaySpectrum;

    Canvas canvasDisplaySpectrum;

    Paint paintSpectrumDisplay;
    Paint paintScaleDisplay;
    static MainActivity mainActivity;
    LinearLayout activity_main;
    int width;
    int height;
    int left_Of_BimapScale;
    int left_Of_DisplaySpectrum;
    private final static int ID_BITMAPDISPLAYSPECTRUM = 1;
    private final static int ID_IMAGEVIEWSCALE = 2;

    //Pitch
    Handler handler = null;

    //File Download
    // declare the dialog as a member field of your activity
    android.app.ProgressDialog mProgressDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.get(size);
        width = display.getWidth();
        height = display.getHeight();

        blockSize = 256;
        //Download Files
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        //left_Of_BimapScale = main.getC.getLeft();
     //   MyImageView scale = (MyImageView)activity_main.findViewById(ID_IMAGEVIEWSCALE);
      //  ImageView bitmap = (ImageView)activity_main.findViewById(ID_BITMAPDISPLAYSPECTRUM);
        if(imageViewScale!=null) {
            left_Of_BimapScale = imageViewScale.getLeft();
        }
        if(imageViewDisplaySectrum!=null) {
            left_Of_DisplaySpectrum = imageViewDisplaySectrum.getLeft();
        }
    }
    private class RecordAudio extends AsyncTask<Void, double[], Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT, frequency,
                    channelConfiguration, audioEncoding, bufferSize);
            int bufferReadResult;
            short[] buffer = new short[blockSize];
            double[] toTransform = new double[blockSize];
            try {
                audioRecord.startRecording();
                playAudio("/data/data/com.test/recorded2.mp3");

            } catch (IllegalStateException e) {
                Log.e("Recording failed", e.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            while (started) {

                if (isCancelled() || (CANCELLED_FLAG == true)) {

                    started = false;
                    //publishProgress(cancelledResult);
                    Log.d("doInBackground", "Cancelling the RecordTask");
                    break;
                } else {
                    bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                    //List<Integer> list = new ArrayList<Integer>();

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                       // list.add((int)buffer[i]);

                    }

                    //DoubleSummaryStatistics stat = Arrays.stream(toTransform).summaryStatistics();
                    //double max = Arrays.stream(toTransform).max().getAsDouble();

                    //max = Collections.max(list).toString();
                    //updateTextView(max);

                    //handler = new Handler();
                    //handler.post(updateTextView);

                    //TEXT UPDATE **1
                    transformer.ft(toTransform);

/*
                    for(int i = 0; i < frequency; i++){
                        double tmp=0;
                        if(toTransform[i]>tmp){
                            tmp = toTransform[i];
                            max = Integer.toString(i);
                        }
                    }
*/
                    double[] re = new double[blockSize];
                    double[] im = new double[blockSize];
                    double[] magnitude = new double[blockSize];

                    // Calculate the Real and imaginary and Magnitude.
                    for(int i = 0; i < blockSize+1; i++){
                        try {
                            // real is stored in first part of array
                            re[i] = toTransform[i * 2];
                            // imaginary is stored in the sequential part
                            im[i] = toTransform[(i * 2) + 1];
                            // magnitude is calculated by the square root of (imaginary^2 + real^2)
                            magnitude[i] = Math.sqrt((re[i] * re[i]) + (im[i] * im[i]));
                        }catch (ArrayIndexOutOfBoundsException e){
                            Log.e("test", "NULL");
                        }
                    }

                    double peak = -1.0;
                    // Get the largest magnitude peak
                    for(int i = 0; i < blockSize; i++){
                        if(peak < magnitude[i])
                            peak = magnitude[i];
                    }
                    // calculated the frequency
                    max = Double.toString((frequency * peak)/blockSize);

                    publishProgress(toTransform);
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(double[]...progress) {
            Log.e("RecordingProgress", "Displaying in progress");

            Log.d("Test:", Integer.toString(progress[0].length));


            if (width > 512) {
                for (int i = 0; i < progress[0].length; i++) {
                    int x = 2 * i;
                    int downy = (int) (150 - (progress[0][i] * 10));
                    int upy = 150;
                    canvasDisplaySpectrum.drawLine(x, downy, x, upy, paintSpectrumDisplay);
                }
                imageViewDisplaySectrum.invalidate();
            } else {
                for (int i = 0; i < progress[0].length; i++) {
                    int x = i;
                    int downy = (int) (150 - (progress[0][i] * 10));
                    int upy = 150;
                    canvasDisplaySpectrum.drawLine(x, downy, x, upy, paintSpectrumDisplay);
                }
                imageViewDisplaySectrum.invalidate();
            }



        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }

            canvasDisplaySpectrum.drawColor(Color.BLACK);
            imageViewDisplaySectrum.invalidate();

        }
    }
    private Runnable updateTextView = new Runnable() {
        @Override
        public void run() {
            tvPitch.setText(max);
            handler.postDelayed(this, 100);
        }
    } ;

    protected void onCancelled(Boolean result){

        try{
            audioRecord.stop();
        }
        catch(IllegalStateException e){
            Log.e("Stop failed", e.toString());

        }
           /* //recordTask.cancel(true);
            Log.d("FFTSpectrumAnalyzer","onCancelled: New Screen");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
*/
    }

    public void onClick(View v) {
        if (started == true) {
            //started = false;
            CANCELLED_FLAG = true;
            //recordTask.cancel(true);
            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }


            startStopButton.setText("Start");

            canvasDisplaySpectrum.drawColor(Color.BLACK);

        }

        else {
            started = true;
            CANCELLED_FLAG = false;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }

    }
    static MainActivity getMainActivity(){

        return mainActivity;
    }

    public void onStop(){
        super.onStop();
        	/* try{
                 audioRecord.stop();
             }
             catch(IllegalStateException e){
                 Log.e("Stop failed", e.toString());
             }*/
        recordTask.cancel(true);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onStart(){

        super.onStart();
        setLayout2();
        mainActivity = this;

    }

    private void setLayout1() {
        activity_main = new LinearLayout(this);
        activity_main.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        activity_main.setOrientation(LinearLayout.VERTICAL);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        transformer = new RealDoubleFFT(blockSize);

        imageViewDisplaySectrum = new ImageView(this);
        if(width > 512){
            bitmapDisplaySpectrum = Bitmap.createBitmap((int)512,(int)300,Bitmap.Config.ARGB_8888);
        }
        else{
            bitmapDisplaySpectrum = Bitmap.createBitmap((int)256,(int)150,Bitmap.Config.ARGB_8888);
        }
        LinearLayout.LayoutParams layoutParams_imageViewScale = null;
        //Bitmap scaled = Bitmap.createScaledBitmap(bitmapDisplaySpectrum, 320, 480, true);
        canvasDisplaySpectrum = new Canvas(bitmapDisplaySpectrum);
        //canvasDisplaySpectrum = new Canvas(scaled);
        paintSpectrumDisplay = new Paint();
        paintSpectrumDisplay.setColor(Color.GREEN);
        imageViewDisplaySectrum.setImageBitmap(bitmapDisplaySpectrum);
        if(width >512){
            //imageViewDisplaySectrum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams layoutParams_imageViewDisplaySpectrum=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ((MarginLayoutParams) layoutParams_imageViewDisplaySpectrum).setMargins(100, 600, 0, 0);
            imageViewDisplaySectrum.setLayoutParams(layoutParams_imageViewDisplaySpectrum);
            layoutParams_imageViewScale= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //layoutParams_imageViewScale.gravity = Gravity.CENTER_HORIZONTAL;
            ((MarginLayoutParams) layoutParams_imageViewScale).setMargins(100, 20, 0, 0);

        }

        else if ((width >320) && (width<512)){
            LinearLayout.LayoutParams layoutParams_imageViewDisplaySpectrum=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ((MarginLayoutParams) layoutParams_imageViewDisplaySpectrum).setMargins(60, 250, 0, 0);
            //layoutParams_imageViewDisplaySpectrum.gravity = Gravity.CENTER_HORIZONTAL;
            imageViewDisplaySectrum.setLayoutParams(layoutParams_imageViewDisplaySpectrum);

            //imageViewDisplaySectrum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutParams_imageViewScale=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ((MarginLayoutParams) layoutParams_imageViewScale).setMargins(60, 20, 0, 100);
            //layoutParams_imageViewScale.gravity = Gravity.CENTER_HORIZONTAL;
        }

        else if (width < 320){
            	/*LinearLayout.LayoutParams layoutParams_imageViewDisplaySpectrum=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ((MarginLayoutParams) layoutParams_imageViewDisplaySpectrum).setMargins(30, 100, 0, 100);
                imageViewDisplaySectrum.setLayoutParams(layoutParams_imageViewDisplaySpectrum);*/
            imageViewDisplaySectrum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutParams_imageViewScale=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //layoutParams_imageViewScale.gravity = Gravity.CENTER;
        }
        imageViewDisplaySectrum.setId(ID_BITMAPDISPLAYSPECTRUM);
        activity_main.addView(imageViewDisplaySectrum);

        imageViewScale = new MyImageView(this);
        imageViewScale.setLayoutParams(layoutParams_imageViewScale);
        imageViewScale.setId(ID_IMAGEVIEWSCALE);

        //imageViewScale.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        activity_main.addView(imageViewScale);

        startStopButton = new Button(this);
        startStopButton.setText("Start");
        startStopButton.setOnClickListener(this);
        startStopButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        activity_main.addView(startStopButton);

        btDownload = new Button(this);
        btDownload.setText("Download");
        btDownload.setOnClickListener(this);
        btDownload.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        activity_main.addView(btDownload);

        tvPitch = new TextView(this);
        tvPitch.setText("text View");
        tvPitch.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        activity_main.addView(tvPitch);

        setContentView(activity_main);

    }
    public void setLayout2(){
        setContentView(R.layout.activity_main);
        transformer = new RealDoubleFFT(blockSize);
        imageViewDisplaySectrum = (ImageView)findViewById(R.id.imageViewDisplaySectrum);
        imageViewScale = (MyImageView)findViewById(R.id.imageViewScale);
        if(width > 512){
            bitmapDisplaySpectrum = Bitmap.createBitmap((int)512,(int)300,Bitmap.Config.ARGB_8888);
        }
        else{
            bitmapDisplaySpectrum = Bitmap.createBitmap((int)256,(int)150,Bitmap.Config.ARGB_8888);
        }
        LinearLayout.LayoutParams layoutParams_imageViewScale = null;
        //Bitmap scaled = Bitmap.createScaledBitmap(bitmapDisplaySpectrum, 320, 480, true);
        canvasDisplaySpectrum = new Canvas(bitmapDisplaySpectrum);
        //canvasDisplaySpectrum = new Canvas(scaled);
        paintSpectrumDisplay = new Paint();
        paintSpectrumDisplay.setColor(Color.GREEN);
        imageViewDisplaySectrum.setImageBitmap(bitmapDisplaySpectrum);


        startStopButton = (Button)findViewById(R.id.startStopButton);
        startStopButton.setText("Start");
        startStopButton.setOnClickListener(this);

        btDownload = (Button)findViewById(R.id.btDownload);
        btDownload.setText("Download");
        btDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // execute this when the downloader must be fired
                final DownloadFilesTask downloadTask = new DownloadFilesTask(MainActivity.this);
                downloadTask.execute("http://cfile1.uf.tistory.com/media/234A0B33583C114C13C6CE");

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }
        });

        tvPitch = (TextView)findViewById(R.id.tvPitch);

        tvPitch.setText("Your Pitch : " + max);
        handler = new Handler();
        handler.post(updateTextView);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try{
            audioRecord.stop();
        }
        catch(IllegalStateException e){
            Log.e("Stop failed", e.toString());

        }
        recordTask.cancel(true);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try{
            audioRecord.stop();
        }
        catch(IllegalStateException e){
            Log.e("Stop failed", e.toString());

        }
        recordTask.cancel(true);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    //~Custom Imageview Class


    //Media Play
    private void playAudio(String url) throws Exception{
        killMediaPlayer();

        player = new MediaPlayer();
        player.setDataSource(url);
        player.prepare();
        player.start();
    }
    private void killMediaPlayer() {
        if(player != null){
            try {
                player.release();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    //Downloads Class
    public class DownloadFilesTask extends AsyncTask<String, Integer, String> {



        private android.content.Context context;
        private android.os.PowerManager.WakeLock mWakeLock;

        public DownloadFilesTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("/data/data/com.test/recorded2.mp3");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (java.io.IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }
    }
}