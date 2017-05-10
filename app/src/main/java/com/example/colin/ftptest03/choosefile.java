package com.example.colin.ftptest03;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 选择上传文件
 * Created by colin on 2017/5/10 0010.
 */

public class choosefile extends AppCompatActivity{
    private int connectFlag=0; //连接状态
    private static final String TAG = "MainActivity";
    private File root;
    private localfileAdapter adapter; //适配器
    private List<File> filelist=new ArrayList<>(); //保存当前目录下的文件列表
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent mintent=getIntent();
        connectFlag=mintent.getIntExtra("stata",0);
        root = Environment.getExternalStorageDirectory();
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        filelist.clear();
        List<File> tem=Arrays.asList(root.listFiles());
        filelist.addAll(tem);
        adapter=new localfileAdapter(filelist);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        /**
         * 设置文件点击事件
         */
        adapter.setOnItemClickListener(new localfileAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int data, String name) {
                //Toast.makeText(MainActivity.this,name,Toast.LENGTH_SHORT).show();
                root=new File(root,name);

                if(root.isDirectory()){
                    filelist.clear();
                    List<File> tem=Arrays.asList(root.listFiles());
                    filelist.addAll(tem);
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    root=root.getParentFile();
                }
            }
        });
        adapter.setOnItemLongClickListener(new localfileAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, int position, String name) {
                if(connectFlag==0) //未连接
                {
                    Intent intent=new Intent();
                    intent.putExtra("stata","尚未连接");
                    setResult(RESULT_CANCELED,intent);
                    finish();
                    return;
                }
                File file=new File(root,name); //要上传的文件
                if(file.isFile())
                {
                    //上传文件
                    Intent intent=new Intent();
                    intent.putExtra("name",file.getAbsolutePath());
                    setResult(RESULT_OK,intent);
                    finish();
                }
                else
                {
                    //暂不支持上传文件夹
                    Intent intent=new Intent();
                    intent.putExtra("state","暂不支持上传文件夹");
                    setResult(RESULT_CANCELED,intent);
                    finish();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(root.equals(Environment.getExternalStorageDirectory())){
            Intent intent=new Intent();
            intent.putExtra("stata","取消上传");
            setResult(RESULT_CANCELED,intent);
            finish();
            return;
        }
        Log.d(TAG,root.getAbsolutePath());
        root=root.getParentFile();
        filelist.clear();
        List<File> tem=Arrays.asList(root.listFiles());
        filelist.addAll(tem);
        adapter.notifyDataSetChanged();
    }
}
