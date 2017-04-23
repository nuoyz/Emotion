package com.example.asusmeitu.emotion.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.asusmeitu.emotion.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by ASUS on 2017/4/21.
 */

public class PictrueAdapter extends BaseAdapter {

    Activity context;
    File[] files;

    public PictrueAdapter(Activity context, File[] files) {
        this.context = context;
        this.files = files;
    }

    public void setFiles(File[] files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    ViewHodler hodler;

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            hodler = new ViewHodler();
            view = LayoutInflater.from(context).inflate(R.layout.adapter_pictrue, null);
            hodler.iv = (ImageView) view.findViewById(R.id.iv);
            view.setTag(hodler);
        } else {
            hodler = (ViewHodler) view.getTag();
        }

        /**
         * 把图片旋转为正的方向
         */
        Bitmap newbitmap = rotaingImageView(files[i]);

        hodler.iv.setImageBitmap(newbitmap);

//        hodler.iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Dialog dialog = new Dialog(context);
//                dialog.on
//
//
//
//            }
//        });

        return view;
    }

    class ViewHodler {
        ImageView iv;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(File f) {
        //旋转图片 动作
        Matrix matrix = new Matrix();

        int angle = readPictureDegree(f.getAbsolutePath());

        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);

        BitmapFactory.Options opts = new BitmapFactory.Options();//获取缩略图显示到屏幕上
        opts.inSampleSize = 2;
        Bitmap cbitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);

        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(cbitmap, 0, 0, cbitmap.getWidth(), cbitmap.getHeight(), matrix, true);

        return resizedBitmap;
    }

}

