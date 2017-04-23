package com.example.asusmeitu.emotion.activitys;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import com.example.asusmeitu.emotion.R;
import com.example.asusmeitu.emotion.adapter.PictrueAdapter;

import java.io.File;

public class GallyActivity extends AppCompatActivity {

    GridView gridView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gally);

        gridView = (GridView) findViewById(R.id.gridView);
        File file = new File(Environment.getExternalStorageDirectory(), "emotion");
        File[] files = file.listFiles(); //应该是文件展示
        PictrueAdapter pictureAdapter = new PictrueAdapter(GallyActivity.this, files);
        gridView.setAdapter(pictureAdapter);
    }
}
