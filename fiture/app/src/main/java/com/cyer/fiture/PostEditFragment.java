package com.cyer.fiture;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.getExternalStoragePublicDirectory;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostEditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private File file;

    private int i;

    public int gItemPos;

    private static final String TAG = "ChoAct";

    private ChooseActivity chooseActivity;

    private EditText edDesc;
    private TextView tvWordcount;

    private ArrayList<String> pathList;
    private ImageView ivPreview;
    private ImageButton btnPublish;

    private GalleryAdapter galleryAdapter;
    private RecyclerView recyclerView;
    private OnFragmentInteractionListener mListener;

//    private ImageView ivUploading;
    private LinearLayout llUploading;


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 0://response成功
                    llUploading.setVisibility(View.GONE);
                    getActivity().finish();
                    break;
                case 1://response失败
                    llUploading.setVisibility(View.GONE);
                    Toast.makeText(chooseActivity,"发送失败，请检查网络后重试",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public PostEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostEditFragment newInstance(String param1, String param2) {
        PostEditFragment fragment = new PostEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    public static boolean createDir(String dirPath){
        try{
            File file=new File(dirPath);
            if(file.getParentFile().exists()){
                file.mkdir();
                return true;
            }
            else {
                createDir(file.getParentFile().getAbsolutePath());
                file.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_camera, container, false);
        chooseActivity=(ChooseActivity) getActivity();

        createDir(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/fiture");
        pathList=new ArrayList<String>();
        for (int i=0;i<chooseActivity.getSelectPath().size();i++){
//            String newPath=getActivity().getExternalFilesDir(null).toString()+"/"+i+".jpg";
            String newPath=getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/fiture/"+i+".jpg";

            copyFile(chooseActivity.getSelectPath().get(i),newPath);
            pathList.add(newPath);
//            chooseActivity.getSelectPath().get(i);
        }
        chooseActivity.setSelectPath(pathList);

        //pathList=chooseActivity.getSelectPath();

        //设置标题栏
        TextView titleEdit= v.findViewById(R.id.tv_titleEdit);
        TextPaint tp = titleEdit.getPaint();
        tp.setFakeBoldText(true);

        //Description
        edDesc=v.findViewById(R.id.ed_desc);
        tvWordcount=v.findViewById(R.id.tv_desc_wordcount);
        edDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                int len=s.length();
                tvWordcount.setText(len+"/140");
            }
        });

        llUploading=v.findViewById(R.id.ll_uploading);


        //图片预览区
        ivPreview=v.findViewById(R.id.iv_preview);
        file = new File(pathList.get(0));
//        Glide.with(this).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerCrop().into(ivPreview);
        Glide.with(this).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivPreview);

        //编辑按钮
        Button btnCut=v.findViewById(R.id.btn_cut);
        btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri=Uri.fromFile(file);
                photoClip(uri);
            }
        });

        Button btnFilter=v.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chooseActivity, FilterActivity.class);
                intent.putExtra("originPath", pathList.get(gItemPos));
                intent.putExtra("pos", gItemPos);
                chooseActivity.startActivityForResult(intent,4);
            }
        });

        //图片选择区
        gItemPos=0;
        recyclerView =v.findViewById(R.id.rv_gallery);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        galleryAdapter=new GalleryAdapter(getContext(), new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onClick(int i) {
                gItemPos=i;
                file = new File(pathList.get(i));
                Glide.with(chooseActivity).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivPreview);
            }
        }, pathList);

        recyclerView.setAdapter(galleryAdapter);

        btnPublish=v.findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPublish();
            }
        });

        return v;
    }
    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        /*intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);*/

        // outputX outputY 是裁剪图片宽高
        /*intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);*/
        //intent.putExtra("editingPos",gItemPos);
        intent.putExtra("return-data", true);
        chooseActivity.startActivityForResult(intent, 3);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        file = new File(pathList.get(gItemPos));
        //Toast.makeText(chooseActivity,"DO",Toast.LENGTH_SHORT).show();
        Glide.with(chooseActivity).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivPreview);

        galleryAdapter.notifyItemChanged(gItemPos);

        /*if(requestCode == 4){//滤镜
            Glide.with(this).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivPreview);
        }*/
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void compressImage(String filePath) {
        Bitmap image=BitmapFactory.decodeFile(filePath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024> 1024) {  //循环判断如果压缩后图片是否大于1024kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片

        File file=new File(filePath);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*发布操作*/
    private void doPublish(){
        llUploading.setVisibility(View.VISIBLE);
        compressImage(pathList.get(0));
        String content=edDesc.getText().toString();
        File file=new File(pathList.get(0));
        //Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        String result=upload(content,2,file);

    }
    public String upload(String content,int authorId, File file) {

        if(!file.exists()){
            return null;
        }
        String filename = file.getName();
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("multipartFile", filename, fileBody)
                .build();


        // http://47.100.32.92:9888/v1/fitures/release?content=test%20up%201&authorId=1
        Request request = new Request.Builder()
                .url("http://47.100.32.92:9888/v1/fitures/release?content="+content+"&authorId="+authorId)
                .post(requestBody)
                .build();


        try {
            final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
            //response = client.newCall(request).execute();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = 1;
                    message.obj = "time over";
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonString = response.body().string();
                    Log.d(TAG," upload jsonString ="+jsonString);

                    if(!response.isSuccessful()){
                        Log.d(TAG," upload error");
                        Message message = new Message();
                        message.what = 1;
                        message.obj = jsonString;
                        handler.sendMessage(message);
                    }else{
                        /*JSONObject jsonObject = new JSONObject(jsonString);
                        int errorCode = jsonObject.getInt("errorCode");
                        if(errorCode == 0){
                            Log.d(TAG," upload data ="+jsonObject.getString("data"));
                            return jsonObject.getString("data");
                        }else {
                            //throw new NetworkException("upload error code "+errorCode+",errorInfo="+jsonObject.getString("errorInfo"));
                        }*/
                        Message message = new Message();
                        message.what = 0;
                        message.obj = "?";
                        handler.sendMessage(message);
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG,"Exception ",e);
        }
        return null;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
