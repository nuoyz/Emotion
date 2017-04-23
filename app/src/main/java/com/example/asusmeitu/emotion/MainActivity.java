package com.example.asusmeitu.emotion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.asusmeitu.emotion.activitys.CameraActivity;
import com.example.asusmeitu.emotion.activitys.GallyActivity;
import com.google.gson.Gson;
import com.google.gson.internal.Primitives;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Camera camera = null;
    Button btn = null;
    Button takePhoto = null;
    SurfaceView surfaceView = null;
    ImageView preview = null;
    SurfaceHolder holder = null;
    //Button gallyPhoto = null;
    Camera.Parameters parameters = null;
    private Bitmap bitmap = null;
    private EditText mEditText;
    private EmotionServiceClient client;//EmotionServiceClient

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Button button = (Button) findViewById(R.id.camera_but);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });*/
        //首先对 xml 文件中的button等都进行初始化
        preview  = (ImageView) findViewById(R.id.preview);
        takePhoto = (Button) findViewById(R.id.takePhoto);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mEditText = (EditText) findViewById(R.id.editTextResult);
     //开始写相机的应用 在文档中是首先要检查相机 暂时没必要
        //1.对相机进行初始化
        //Log.d(TAG, getOrientaition(MainActivity.this));
        //camera.setDisplayOrientation(getOrientaition(MainActivity.this));//修正方向
        //创建预览类 创建预览类 也需要activity的支持
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setFixedSize(100, 147);
        holder.setKeepScreenOn(true);
        holder.addCallback(new surfaceHolderCallback());
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //点击拍照
        takePhoto = (Button) findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                camera.takePicture(null, null, new takePickerCallback());
            }
        });
        /*gallyPhoto = (Button) findViewById(R.id.gallyPhoto);
        gallyPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent(MainActivity.this, GallyActivity.class);
                startActivity(intent);
            }
        });*/
        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key)) {
            };
        }
    }
    class takePickerCallback implements Camera.PictureCallback {//为什么没有参数
        @Override
        public void onPictureTaken (byte[] data, Camera camera) {
          if (data.length > 0) {//有数据
              Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
              preview.setImageBitmap(bitmap);
              //上面是保存画面 而且应该与存储到sdk不重复 但是视频中是删除了的
              saveSDCard(data);
          }
        }
    }
    private void saveSDCard (byte[] data) {
        File dir = new File(Environment.getExternalStorageDirectory(), "emotion");//原有的直接就是一个路径
        if (!dir.exists()) {
          dir.mkdir();
        }
        String picName = System.currentTimeMillis() + ".jpg";//多个字符双引号 单个字符单引号
        File picFile = new File(dir, picName);
        //字节转成图片
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            FileOutputStream fileOutS = new FileOutputStream(picFile);
            BufferedOutputStream bufferOut = new BufferedOutputStream(fileOutS);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bufferOut);//压缩
            Log.d("bitmap", "camera" + bitmap);
            doRecognise(bitmap);
            fileOutS.flush();
            fileOutS.close();
            bufferOut.flush();
            bufferOut.close();

            Toast.makeText(this, "保存成功2222", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d(TAG, "Error saving SDK: " + e.getMessage());
            e.printStackTrace();
        }


    }
    class surfaceHolderCallback implements SurfaceHolder.Callback {//实现接口不能使用public
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
          try {
            camera = Camera.open();
            camera.setDisplayOrientation(getOrientaition(MainActivity.this));//修正方向
            camera.setPreviewDisplay(holder);
            camera.startPreview();
          } catch (IOException e) {
              Log.d(TAG, "Error setting camera preview: " + e.getMessage());
          }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
          if (camera != null) {
              camera.release();
              camera = null;
          }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            //相机改变通常适用于 中途被暂停等
         /* if (holder.getSurface() == null) {
            return;
          }
            try {
              camera.stopPreview();
            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {

            }*/ //官方文档里的没被采用
            parameters = camera.getParameters();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            if (supportedPictureSizes.isEmpty()) {
                parameters.setPreviewSize(w, h);
            } else {
                Camera.Size size = supportedPictureSizes.get(0);
                parameters.setPreviewSize(size.width, size.height);
            }
            parameters.setPreviewSize(w, h);
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setJpegQuality(100);
            //parameters.setPreviewFormat(80); error
            parameters.setPreviewFrameRate(5);
        }
    }
    private int getOrientaition (Activity activity) {
        int degree = 0;
        int rotatio = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch(rotatio) {
            case Surface.ROTATION_0:
               degree = 90;
               break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return  degree;
    };
    public void doRecognise(Bitmap bitmap) { //识别方法
        // Do emotion detection using auto-detected faces.
        Log.d("Bitmap", "doRecognise" + bitmap);
        try {
            new doRequest(false, bitmap).execute();
        } catch (Exception e) {
            //error
        }
    }
    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> { //RecognizeResult
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;
        private Bitmap bitmap = null;
        public doRequest(boolean useFaceRectangles, Bitmap bitmap) {
            Log.d("Bitmap", "dpRequest" + bitmap);
            this.bitmap = bitmap;
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            Log.d("doInBackground", String.format("doInBackground doInBackground. doInBackground"));
            if (this.useFaceRectangles == false) {
                try {
                    Log.d("doRequest", "FaceRectangles false"+ bitmap);
                    return processWithAutoFaceDetection(bitmap);
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    Log.d("doRequest", "FaceRectangles true");
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (this.useFaceRectangles == false) {
                //mEditText.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
            } else {
                //mEditText.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
            }
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mEditText.append("No emotion detected :(");
                } else {
                    Integer count = 0;
                    // Covert bitmap to a mutable bitmap by copying it
                    Log.d("PostExecute", "PostExecute" + bitmap);
                    Bitmap bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(bitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);

                    for (RecognizeResult r : result) {
                        //Log.d("Result");
                        mEditText.append(String.format("\nFace #%1$d \n", count));
                        mEditText.append(String.format("\t anger: %1$.5f\n", r.scores.anger));
                        mEditText.append(String.format("\t contempt: %1$.5f\n", r.scores.contempt));
                        mEditText.append(String.format("\t disgust: %1$.5f\n", r.scores.disgust));
                        mEditText.append(String.format("\t fear: %1$.5f\n", r.scores.fear));
                        mEditText.append(String.format("\t happiness: %1$.5f\n", r.scores.happiness));
                        mEditText.append(String.format("\t neutral: %1$.5f\n", r.scores.neutral));
                        mEditText.append(String.format("\t sadness: %1$.5f\n", r.scores.sadness));
                        mEditText.append(String.format("\t surprise: %1$.5f\n", r.scores.surprise));
                        //mEditText.append(String.format("\t face rectangle: %d, %d, %d, %d", r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.width, r.faceRectangle.height)); 框选部分暂时不需要
                        faceCanvas.drawRect(r.faceRectangle.left,
                                r.faceRectangle.top,
                                r.faceRectangle.left + r.faceRectangle.width,
                                r.faceRectangle.top + r.faceRectangle.height,
                                paint);
                        count++;
                    }
                    //ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                    //imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                }
                mEditText.setSelection(0);
            }
        }
    }
    private List<RecognizeResult> processWithAutoFaceDetection(Bitmap bitmap) throws EmotionServiceException, IOException {//EmotionServiceException
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();;
        Log.d("WithAutoFaceDetection", "process" + bitmap);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;//FaceRectangle
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);//此处 face 有多个引入
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }
}
