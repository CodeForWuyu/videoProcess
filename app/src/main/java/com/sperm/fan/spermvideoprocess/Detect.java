package com.sperm.fan.spermvideoprocess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;
import static android.media.MediaMetadataRetriever.OPTION_PREVIOUS_SYNC;


public class Detect extends AppCompatActivity  {

    private String TAG = "Detect";
    private ImageView iv;
    private Button button;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer;
    private String videoPath;
    private TextView textView;

    MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    private Mat[]  mats = new Mat[10];

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        staticLoadCVLibraries();

        initUI();
        initData();

        Log.i(TAG,"启动Detect成功");
        Intent intent = getIntent();
        videoPath = intent.getStringExtra("videoPath");
        Log.i(TAG,"路径为"+videoPath);
        mmr.setDataSource(videoPath);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(0);
                SpermProcess();
            }
        });
    }

    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i(TAG, "Open CV Libraries loaded...");
        }else{
            Log.i(TAG, "load opencv failed");
        }
    }

    //初始化ui控件
    private void initUI(){
//        iv = (ImageView)findViewById(R.id.imageView);
        button = (Button)findViewById(R.id.detect);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        textView = (TextView)findViewById(R.id.info);
        holder = surfaceView.getHolder();
        holder.setFixedSize(surfaceView.getHeight(),surfaceView.getWidth());

    }

    //设置控件的数据信息
    private void initData(){
        mediaPlayer = new MediaPlayer();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });

    }



    //精子检测过程
    private void SpermProcess(){
        new Thread(){
            public void run(){
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("获取图像中...");
                    }
                });

                getMat();//获取mat图像

                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("计算中...");
                    }
                });

                //检测过程

            }
        }.start();
    }

    private void getMat(){
        int count = 0;
        for(int t = 0;t<1000&&count<10;t+=40) {
            Log.i(TAG,"获取bitmap中");
            Bitmap bitmap = mmr.getFrameAtTime((long) (t * 1000), OPTION_CLOSEST);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap,mat);
            mats[count] = new Mat();
            mat.copyTo(mats[count]);
            count++;
            Log.i(TAG,"mat尺寸"+String.valueOf(mat.size()));
        }
    }

    //获取视频帧的函数
    //time单位为毫秒
    private Bitmap getBitmap(int time){
        Log.i(TAG,"正在获取bitmap");
        Bitmap bitmap = mmr.getFrameAtTime((long) (time * 1000), OPTION_CLOSEST);
        return bitmap;
    }

    //播放视频
    private void play(final int currentPosition) {
//		String path = "http://daily3gp.com/vids/family_guy_penis_car.3gp";//指定视频所在路径。
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置视频流类型
        try {
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    int max = mediaPlayer.getDuration();
                    mediaPlayer.seekTo(currentPosition);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
