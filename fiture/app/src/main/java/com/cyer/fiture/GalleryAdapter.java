package com.cyer.fiture;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.LinearViewHolder> {

    private Context mContext;
    private OnItemClickListener mListener;
    private ArrayList<String> mList;
    private int pos;

    public GalleryAdapter(Context context,OnItemClickListener listener,ArrayList<String> list){
        mContext=context;
        mListener=listener;
        mList=list;

        pos=0;
    }

    @NonNull
    @Override
    public GalleryAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.gallery_item, viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapter.LinearViewHolder viewHolder, final int i) {
        File file = new File(mList.get(i));
        /*设置选中状态*/
        if (pos == i) {
            viewHolder.itemView.setBackgroundResource(R.drawable.gitem_border);
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.gitem_border_tr);
        }
        Glide.with(mContext).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().into(viewHolder.iv);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pos=i;
                notifyDataSetChanged();
                mListener.onClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class LinearViewHolder extends RecyclerView.ViewHolder{

        private ImageView iv;
        public LinearViewHolder(@NonNull View itemView) {
            super(itemView);
            iv=itemView.findViewById(R.id.iv_gitem);
        }
    }

    public interface OnItemClickListener{
        void onClick(int i);
    }
}
