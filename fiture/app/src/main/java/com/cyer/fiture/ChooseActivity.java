package com.cyer.fiture;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelector;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class ChooseActivity extends AppCompatActivity implements PostEditFragment.OnFragmentInteractionListener{

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    private static final String TAG = "ChoAct";

    public static int authorId=2;
    private ArrayList<String> mSelectPath;

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(ChooseActivity.this,"this is："+uri,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_choose);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            //权限未授予，申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                    1000);
        }else {
            //权限已授予
        }


        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frcontainer,Camera2BasicFragment.newInstance(),"f1")
                //.addToBackStack("")
                .commit();
    }



    private void requestPermission(final String permission, String rationale, final int requestCode){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ChooseActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //showToast("REQUEST: "+requestCode);
        if(requestCode == REQUEST_IMAGE){
            if(resultCode == RESULT_OK){
                ArrayList<String> tmpList=data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                if (tmpList!=null){
                    if (!tmpList.isEmpty()){
                        mSelectPath=tmpList;
                        StringBuilder sb = new StringBuilder();
                        for(String p: mSelectPath){
                            sb.append(p);
                            sb.append("\n");
                        }
                        //showToast(sb.toString());
                        toPublishFrag();
                    }
                }
            }
        }

        Fragment fr=getSupportFragmentManager().findFragmentByTag("postedit");
        if(requestCode == 3){
            if (fr != null) {
                fr.onActivityResult(requestCode,resultCode,data);
            }
        }

        if(requestCode == 4){//滤镜
            if (fr != null) {
                fr.onActivityResult(requestCode,resultCode,data);
            }
        }
    }

    public void setSelectPath(ArrayList<String> list){
        mSelectPath=list;

        StringBuilder sb = new StringBuilder();
        for(String p: mSelectPath){
            sb.append(p);
            sb.append("\n");
        }
        //showToast(sb.toString());
    }

    public ArrayList<String> getSelectPath(){
        return mSelectPath;
    }

    private void toPublishFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frcontainer, PostEditFragment.newInstance("1","2"),"postedit")
                .addToBackStack("")
                .commit();
    }

    private void showToast(final String text) {
        final Activity activity = this;
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /*    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        }else {
            //boolean showCamera = mShowCamera.getCheckedRadioButtonId() == R.id.show;
            boolean showCamera = true;

            int maxNum = 1;

            *//*if (!TextUtils.isEmpty(mRequestNum.getText())) {
                try {
                    maxNum = Integer.valueOf(mRequestNum.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }*//*
            MultiImageSelector selector = MultiImageSelector.create(ChooseActivity.this);
            selector.showCamera(showCamera);
            selector.count(maxNum);
//            if (mChoiceMode.getCheckedRadioButtonId() == R.id.single) {
//                selector.single();
//            } else {
//                selector.multi();
//            }
            selector.multi();
            selector.origin(mSelectPath);
            selector.start(ChooseActivity.this, REQUEST_IMAGE);
        }
    }*/
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.do_post, menu);
        MenuItem item = menu.findItem(R.id.btn_publish);
        SpannableString spannableString = new SpannableString(item.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(Color.CYAN), 0, spannableString.length(), 0);
        spannableString.setSpan(new RelativeSizeSpan(1.4f), 0, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        item.setTitle(spannableString);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_publish:
                //Toast.makeText(this, "点击添加菜单", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

}