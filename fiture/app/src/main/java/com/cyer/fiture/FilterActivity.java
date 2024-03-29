package com.cyer.fiture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cyer.fiture.util.FileUtils;
import com.cyer.fiture.util.GPUImageFilterTools;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PICK_IMAGE = 0x001;

    private GPUImageView mGPUImageView;
    private SeekBar mSeekBar;
    private TextView mFilterNameTv;

    private GPUImageFilter mNoImageFilter = new GPUImageFilter();
    private GPUImageFilter mCurrentImageFilter = mNoImageFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filter);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initView();
        //openGallery();
    }

    private void initView() {
        mGPUImageView = findViewById(R.id.gpuimage);
        mSeekBar = findViewById(R.id.tone_seekbar);
        mFilterNameTv = findViewById(R.id.filter_name_tv);
        mGPUImageView.setOnClickListener(this);
        mFilterNameTv.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        findViewById(R.id.compare_iv).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.close_iv).setOnClickListener(this);
        findViewById(R.id.save_iv).setOnClickListener(this);

        File file=new File(getIntent().getStringExtra("originPath"));
        Uri uri=Uri.fromFile(file);
        mGPUImageView.setImage(uri);
        mGPUImageView.setFilter(mCurrentImageFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.gpuimage:
                openGallery();
                break;*/
            case R.id.filter_name_tv:
                GPUImageFilterTools.showDialog(this, mOnGpuImageFilterChosenListener);
                break;
            case R.id.close_iv:
                finish();
                break;
            case R.id.save_iv:
                saveImage();
                setResult(-1,getIntent());
                finish();
                break;
        }
    }
    /*private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }*/

    private void saveImage() {
        String fileName = getIntent().getIntExtra("pos",0) + ".jpg";
//        mGPUImageView.saveToPictures(getExternalFilesDir(null).toString(), fileName, mOnPictureSavedListener);

        File file=new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/fiture/"+fileName);
        if (file.exists()){
            file.delete();
        }
        //getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+
        mGPUImageView.saveToPictures("fiture", fileName, mOnPictureSavedListener);
        while (!file.exists()){

        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data!= null) {
            mGPUImageView.setImage(data.getData());
            mGPUImageView.setFilter(mCurrentImageFilter);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.compare_iv) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mGPUImageView.setFilter(mNoImageFilter);
                        break;
                    case MotionEvent.ACTION_UP:
                        mGPUImageView.setFilter(mCurrentImageFilter);
                        break;
                }
            }
            return true;
        }
    };

    private GPUImageFilterTools.OnGpuImageFilterChosenListener mOnGpuImageFilterChosenListener = new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
        @Override
        public void onGpuImageFilterChosenListener(GPUImageFilter filter, String filterName) {
            switchFilterTo(filter);
            mFilterNameTv.setText(filterName);
        }
    };

    private void switchFilterTo(GPUImageFilter filter) {
        if (mCurrentImageFilter == null
                || (filter != null && !mCurrentImageFilter.getClass().equals(filter.getClass()))) {
            mCurrentImageFilter = filter;
            mGPUImageView.setFilter(mCurrentImageFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mCurrentImageFilter);
            mSeekBar.setVisibility(mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
        } else {
            mSeekBar.setVisibility(View.GONE);
        }
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mFilterAdjuster != null) {
                mFilterAdjuster.adjust(progress);
            }
            mGPUImageView.requestRender();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private GPUImageView.OnPictureSavedListener mOnPictureSavedListener = new GPUImageView.OnPictureSavedListener() {
        @Override
        public void onPictureSaved(Uri uri) {
            String filePath = FileUtils.getRealFilePath(FilterActivity.this, uri);
            //Log.d(TAG, "save to " + filePath);
            Toast.makeText(FilterActivity.this, "Saved: " + filePath, Toast.LENGTH_SHORT).show();
        }
    };
}
