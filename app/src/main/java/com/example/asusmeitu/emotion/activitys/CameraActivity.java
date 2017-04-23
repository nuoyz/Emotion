package com.example.asusmeitu.emotion.activitys;

//import android.graphics.Camera;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.example.asusmeitu.emotion.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    SurfaceView surfaceview = null;
    Button camera_but = null;
    ImageView iv = null;
    Camera camera = null;
    SurfaceHolder holder = null;
    Camera.Parameters parameters = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camer_layout);//IDE 手动输入是不会自动引入改引入的包的 需要点击Enter 才可以
        surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        camera_but = (Button) findViewById(R.id.camera_but);
        iv = (ImageView) findViewById(R.id.iv);

        holder = surfaceview.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setFixedSize(100, 147);
        holder.setKeepScreenOn(true);
        holder.addCallback(new MySurfaceCallBack());

        camera_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, mPicture );// new TabKePic
            }
        });
    }
    private Camera.PictureCallback mPicture  = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                iv.setImageBitmap(bitmap);
            }
        }
    };
    class TabKePic implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                iv.setImageBitmap(bitmap);
            }
        }
    }

    class  MySurfaceCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            camera = Camera.open();//camera 已经out了
            camera.setDisplayOrientation(getRotation(CameraActivity.this));
            //camera.setPreviewDisplay(holder);
            camera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
            if (camera != null) {
              camera.release();
                camera = null;
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            parameters = camera.getParameters();
            //获取大小集合
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            if (supportedPictureSizes.isEmpty()) {//界面可能有多个
              parameters.setPreviewSize(w, h);
            } else {
                Camera.Size size = supportedPictureSizes.get(0);
                parameters.setPreviewSize(size.width, size.height);
            }
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setPictureSize(w, h);
            parameters.setJpegQuality(80);
            parameters.setPreviewFrameRate(5);
        }
    }
    private int getRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
              degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 180;
                break;
            case Surface.ROTATION_180:
                degree = 90;
                break;
            case Surface.ROTATION_270:
                degree = 90;
                break;
        }
        return  degree;
    }
    @Override
    protected void onPause() {
        super.onPause();
        camera.stopPreview();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (camera != null) {
            camera.startPreview();
        } else {
            try {
               camera = Camera.open();
                camera.setDisplayOrientation(getRotation(CameraActivity.this));
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
             e.printStackTrace();
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    }
}
