package com.sperm.fan.spermvideoprocess;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import org.opencv.android.OpenCVLoader;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_REQUEST_CODE = 2;
    private static final int READ_REQUEST_CODE = 3;
    private static final int ALL_REQUEST_CODE =99;  //申请列表中所有权限

    private List<LocalMedia> selectList = new ArrayList<>();

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    List<String> mPermissionList = new ArrayList<>();

    private Button button;
    private TextView textView;
    private Button detectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        staticLoadCVLibraries();
        init();
        getPermission();
    }

    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    //初始化
    public void init(){
        button = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.videoPath);
        detectButton =(Button)findViewById(R.id.start);

        button.setOnClickListener(new Button.OnClickListener() {//设置监听函数
            @Override
            public void onClick(View view) {
             getVideo();
            }
        });

        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDetect();
            }
        });
    }

    public void goDetect(){
        Intent intent= new Intent(MainActivity.this,Detect.class);
        String s = String.valueOf(textView.getText());
        if(s.equals("")){
            Toast.makeText(MainActivity.this,"请先选择视频!",Toast.LENGTH_SHORT).show();
        }else{
            intent.putExtra("videoPath",s);
            startActivity(intent);
        }
    }

    public void getVideo(){
        PictureSelector.create(MainActivity.this).openGallery(PictureMimeType.ofVideo())
               // .theme(themeId)// 主题样式设置 具体参考 values/styles
                //.maxSelectNum(maxSelectNum)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .selectionMode( PictureConfig.SINGLE)// 多选 or 单选
                //.previewImage(cb_preview_img.isChecked())// 是否可预览图片
                .previewVideo(false)// 是否可预览视频
                //.enablePreviewAudio(cb_preview_audio.isChecked()) // 是否可播放音频
                .isCamera(false)// 是否显示拍照按钮
                .enableCrop(false)// 是否裁剪
                //.compress(cb_compress.isChecked())// 是否压缩
                .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
               // .withAspectRatio(aspect_ratio_x, aspect_ratio_y)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                .isGif(false)// 是否显示gif图片
                .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
                .circleDimmedLayer(false)// 是否圆形裁剪
                .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .openClickSound(false)// 是否开启点击声音
                //.selectionMedia(false)// 是否传入已选图片
                .previewEggs(false)//预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                //.cropCompressQuality(90)// 裁剪压缩质量 默认为100
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                //.rotateEnabled() // 裁剪是否可旋转图片
                //.scaleEnabled()// 裁剪是否可放大缩小图片
                //.videoQuality()// 视频录制质量 0 or 1
                //.videoSecond()////显示多少秒以内的视频or音频也可适用
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    //选择图像或视频回调函数，将地址存储在list中
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectList.clear();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    for (LocalMedia media : selectList) {
                        Log.i("图片-----》", media.getPath());
                    }
                    if(selectList.size()>0){
                        LocalMedia media = selectList.get(0);
                        String pictureType = media.getPictureType();
                        int mediaType = PictureMimeType.pictureToVideo(pictureType);
                        if(mediaType==2){
                            textView.setText(media.getPath());
                            goDetect();
                            //Toast.makeText(MainActivity.this,String.valueOf(media.getPath()),Toast.LENGTH_LONG).show();
                            //PictureSelector.create(MainActivity.this).externalPictureVideo(media.getPath());
                        }
                    }else{
                        //Toast.makeText(MainActivity.this,"Fail",Toast.LENGTH_LONG).show();
                    };
                    break;
            }
        }
    }

    //获取权限（版本判断）
    public void getPermission() {
        if(Build.VERSION.SDK_INT>=23){
            requesetPermission();
        }
    }
    //申请权限
    private void requesetPermission(){
        mPermissionList.clear();
        for(int i=0;i<permissions.length;i++){
            if(ContextCompat.checkSelfPermission(MainActivity.this,permissions[i])!=PackageManager.PERMISSION_DENIED){
                mPermissionList.add(permissions[i]);
            }
        }

        if(mPermissionList.isEmpty()){

        }else{
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, ALL_REQUEST_CODE);
        }
    }

    //重写权限申请回调回调函数
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        if(requestCode==ALL_REQUEST_CODE){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                    if (showRequestPermission) {
                        Toast.makeText(MainActivity.this,"权限申请失败",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
