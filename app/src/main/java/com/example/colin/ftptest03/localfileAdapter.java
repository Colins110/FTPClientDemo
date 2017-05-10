package com.example.colin.ftptest03;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import static android.content.ContentValues.TAG;

//import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by colin on 2017/4/26 0026.
 */

public class localfileAdapter extends RecyclerView.Adapter<localfileAdapter.ViewHolder>{
    private Context mcontext;
    private List<File> mfilelist;
    private LayoutInflater inflater;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnRecyclerItemLongListener mOnItemLong = null;
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener
    {
        CardView cardView;
        //LinearLayout lview;
        ImageView filetype;
        TextView filename;
        OnRecyclerViewItemClickListener mOnItemClickListener = null;
        OnRecyclerItemLongListener mOnItemLong = null;

       public ViewHolder(View view, OnRecyclerViewItemClickListener mListener,OnRecyclerItemLongListener longListener)
        {
            super(view);
            this.mOnItemClickListener = mListener;
            this.mOnItemLong = longListener;
            cardView =(CardView) view;
            //lview=(LinearLayout) view;
            filetype=(ImageView) cardView.findViewById(R.id.filetype);
            filename=(TextView) cardView.findViewById(R.id.filename);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                mOnItemClickListener.onItemClick(v, getAdapterPosition(),filename.getText().toString());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mOnItemLong != null){
                mOnItemLong.onItemLongClick(v,getPosition(),filename.getText().toString());
            }
            return true;
        }
    }
    public localfileAdapter(List<File> fileList)
    {
        mfilelist=fileList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mcontext== null)
        {
            mcontext=parent.getContext();
        }
        View view= LayoutInflater.from(mcontext).inflate(R.layout.file_item,parent,false);
        ViewHolder holder = new ViewHolder(view, mOnItemClickListener,mOnItemLong);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mfilelist.size()!=0){
            File File=mfilelist.get(position);
            String name=File.getName();
            Log.d(TAG, name);
            holder.filename.setText(name);
            if(File.isDirectory()){
                Glide.with(mcontext).load(R.drawable.folder).into(holder.filetype);
            }
            else if(File.isFile())
            {
                //Glide.with(mcontext).load(R.drawable.file).into(holder.filetype);
                int endindex=name.lastIndexOf('.');
                if(endindex==-1)
                {
                    Glide.with(mcontext).load(R.drawable.file_icon_default).into(holder.filetype);
                }
                else
                {
                    switch (name.substring(endindex+1))
                    {
                        case "mp3":
                            Glide.with(mcontext).load(R.drawable.file_icon_mp3).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_mp3);
                            break;
                        case "apk":
                            Glide.with(mcontext).load(R.drawable.file_icon_apk).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_apk);
                            break;
                        case "ape":
                            Glide.with(mcontext).load(R.drawable.file_icon_ape).into(holder.filetype);
                           //holder.filetype.setImageResource(R.drawable.file_icon_ape);
                            break;
                        case "docx":
                        case "doc":
                            Glide.with(mcontext).load(R.drawable.file_icon_doc).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_doc);
                            break;
                        case "html":
                            Glide.with(mcontext).load(R.drawable.file_icon_html).into(holder.filetype);
                           // holder.filetype.setImageResource(R.drawable.file_icon_html);
                            break;
                        case "ppt":
                            //Glide.with(mcontext).load(R.drawable.file_icon_ppt).into(holder.filetype);
                            holder.filetype.setImageResource(R.drawable.file_icon_ppt);
                            break;
                        case "pdf":
                            Glide.with(mcontext).load(R.drawable.file_icon_pdf).into(holder.filetype);
                           // holder.filetype.setImageResource(R.drawable.file_icon_pdf);
                            break;
                        case "txt" :
                            Glide.with(mcontext).load(R.drawable.file_icon_txt).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_txt);
                            break;
                        case "rar":
                            Glide.with(mcontext).load(R.drawable.file_icon_rar).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_rar);
                            break;
                        case "zip":
                            Glide.with(mcontext).load(R.drawable.file_icon_zip).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_zip);
                            break;
                        case "png":
                        case "jpg":
                        case "gif":
                        case "jpeg":
                            Glide.with(mcontext).load(R.drawable.file_icon_picture).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_picture);
                            break;
                        case "mp4" :
                        case "avi":
                        case "mov":
                        case "wmv":
                        case "mkv":
                        case "rmvb":
                            Glide.with(mcontext).load(R.drawable.file_icon_video).into(holder.filetype);
                            //holder.filetype.setImageResource(R.drawable.file_icon_video);
                            break;
                    }
                }
            }
        }

        /*else
        {
            holder.filename.setText("空");
        }*/
    }

    @Override
    public int getItemCount() {
            return mfilelist.size();
    }

    //define interface
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int data, String name);

    }
    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view, int position, String name);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
    public void setOnItemLongClickListener(OnRecyclerItemLongListener listener){
        this.mOnItemLong =  listener;
    }
}


