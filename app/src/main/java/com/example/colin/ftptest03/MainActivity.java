package com.example.colin.ftptest03;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MainActivity extends AppCompatActivity {
    private String path; //当前路径
    private int connectFlag =0; //连接标志 0 为未连接，1为已连接
    private List<FTPFile> filelist=new ArrayList<>(); //保存当前目录下的文件列表
    private fileAdapter adapter; //适配器
    private DrawerLayout mdrawerLayout;
    private static final int MAX_THREAD_NUMBER = 1;
    private ExecutorService mThreadPool; //我的线程池
    private FtpFactory mftpFactory; //我的ftp工厂方法
    /*private DownloadService.DownloadBinder downloadBinder; //Binder 实例 用于控制下载
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder=(DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name){

        }
    };*/
    private static final String TAG = "MainActivity";
    String hostname;//="118.89.47.194";
    int port;//=21;
    String username;//;
    String password;//;

    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    Toast.makeText(MainActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    connectFlag=1;
                    break;
                case -1:
                    Toast.makeText(MainActivity.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                    break;
                case 11:
                    Toast.makeText(MainActivity.this,"上传完成",Toast.LENGTH_SHORT).show();
                    break;
                case 12:
                    Toast.makeText(MainActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private FTPClient client=new FTPClient(handler);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate executed");
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mdrawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView=(NavigationView) findViewById(R.id.nav_view);
        ActionBar mactionBar=getSupportActionBar();
        mactionBar.setDisplayHomeAsUpEnabled(true); //设置显示HomeAsUp按钮
        mactionBar.setHomeAsUpIndicator(R.drawable.ic_menu);  //设置菜单指示符样式 默认为返回箭头
        navView.setCheckedItem(R.id.ftphost);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mdrawerLayout.closeDrawer(Gravity.START);
                return true;
            }
        });
        FloatingActionButton actionButton=(FloatingActionButton) findViewById(R.id.fab);
        //设置fabbutton的响应事件
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectFlag==0) //未连接
                {
                    // v.setVisibility(View.VISIBLE);  //设置可见性
                    /*Intent intent=new Intent(MainActivity.this,loginActivity.class);
                    startActivityForResult(intent,1);*/
                    LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                    //把activity_login中的控件定义在View中

                    final View textEntryView = factory.inflate(R.layout.login, null);

                    //将LoginActivity中的控件显示在对话框中
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("连接到主机")
                            .setView(textEntryView)
                            .setPositiveButton("登陆", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final EditText hostnameET = (EditText)textEntryView.findViewById(R.id.ip);
                                    final EditText portET = (EditText)textEntryView.findViewById(R.id.port);
                                    final EditText usernameET=(EditText)textEntryView.findViewById(R.id.username);
                                    final EditText passwdET=(EditText)textEntryView.findViewById(R.id.passwd);

                                    //将页面输入框中获得的“用户名”，“密码”转为字符串
                                    if(!TextUtils.isEmpty(hostnameET.getText())&&!TextUtils.isEmpty(portET.getText())&&!TextUtils.isEmpty(usernameET.getText())&&!TextUtils.isEmpty(passwdET.getText()))
                                    {
                                        hostname = hostnameET.getText().toString().trim();
                                        port = Integer.parseInt(portET.getText().toString().trim());
                                        username=usernameET.getText().toString().trim();
                                        password=passwdET.getText().toString().trim();
                                        login(hostname,port,username,password);
                                        }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this,"取消登录",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .create().show();
                   /* while(temflag[0]==0){
                    }*/

                    /*FTPFile[] temlist=getFilelist(); //获取登录的目录列表
                    List<FTPFile> temfilelist=(List<FTPFile>) Arrays.asList(temlist);
                    filelist.clear();
                    filelist.addAll(temfilelist);*/
                }
                else  //已经连接什么都不做
                {
                    // mkdir("conect");
                }
            }
        });




        mThreadPool = Executors.newFixedThreadPool(MAX_THREAD_NUMBER); //初始化我的线程池（最大有MAX_THREAD_NUMBER个线程
        mftpFactory=new FtpFactory();

        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter=new fileAdapter(filelist);
        recyclerView.setAdapter(adapter);

        /**
         * 设置文件点击事件
         */
        adapter.setOnItemClickListener(new fileAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int data,String name) {
                   //Toast.makeText(MainActivity.this,name,Toast.LENGTH_SHORT).show();
                path=name;
                chdir(name);
                freshFilelist();
            }
        });
        adapter.setOnItemLongClickListener(new fileAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, int position, String name) {
               //Toast.makeText(MainActivity.this,"long click",Toast.LENGTH_SHORT).show();
                String dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//获取download路径
                String file=dir+"/"+name;
                Toast.makeText(MainActivity.this,name+"开始下载...",Toast.LENGTH_SHORT).show();
                download(name,file);


            }
        });
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        filelist.clear();
        Log.d(TAG, "onStart executed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume executed");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop executed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy excuted");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart executed");
    }

    //创建菜单，参数1指定菜单布局文件，参数2指定添加到哪个menu对象上，返回true表示显示菜单，false表示不显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                mdrawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.backup:
                Intent intent=new Intent(MainActivity.this,choosefile.class);
                intent.putExtra("stata",connectFlag);
                startActivityForResult(intent,1);

                break;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case 1:
                if(resultCode==RESULT_OK){
                    String name=data.getStringExtra("name");
                    Toast.makeText(MainActivity.this,"开始上传"+name+"...",Toast.LENGTH_SHORT).show();
                    update(name);
                }
                else if(resultCode==RESULT_CANCELED)
                {
                    Toast.makeText(MainActivity.this,data.getStringExtra("stata"),Toast.LENGTH_SHORT).show();
                }

        }
    }

    /**
     * 获取当前目录下的文件和文件的修改日期
     */
    class ftpGetfilelist implements Runnable
    {
        private FTPFile[] filelist;
        public FTPFile[] getFilelist()
        {
            return filelist;
        }

        @Override
        public void run() {
            try {
                filelist=client.list(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 登录的命令方法
     */
    class ftpLogin implements Runnable
    {
        private  String ftphostname;
        private  int ftpport;
        private  String ftpusername;
        private  String ftppasswd;

        public ftpLogin(String hostName, int port, String username, String passwd) {
            this.ftphostname = hostName;
            this.ftpport = port;
            this.ftpusername = username;
            this.ftppasswd = passwd;
        }

        public void infoSetter(String hostName, int port, String username, String passwd)
        {
            this.ftphostname=hostName;
            this.ftpport=port;
            this.ftpusername=username;
            this.ftppasswd=passwd;
        }
        @Override
        public void run() {


                client.login(ftphostname,ftpport,ftpusername,ftppasswd);
            /*} catch (IOException e) {
                e.printStackTrace();
            } /*catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                Toast.makeText(MainActivity.this,"信息错误，登录失败",Toast.LENGTH_SHORT).show();
            }*/
            //登录成功，刷新一下
            FTPFile[] temlist= new FTPFile[0]; //获取登录的目录列表
            try {
                temlist = client.list(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<FTPFile> temfilelist=(List<FTPFile>) Arrays.asList(temlist);
            filelist.clear();
            filelist.addAll(temfilelist);
            runOnUiThread(new Runnable() { //切回主线程
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }
    }


    /**
     * 下载
     */
    class download implements Runnable
    {
        private String remotename;
        private String localname;
        public download(String remoteName,String localName)
        {
            remotename=remoteName;
            localname=localName;
        }
        {}
        @Override
        public void run() {
            try {
                client.download(remotename,localname);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class update implements  Runnable
    {
        private String remotePath;
        private String localName;

        public update(String localname)
        {
            localName=localname;
        }
        @Override
        public void run() {
            try{
                client.upload(localName);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    /**
     * ftp断开连接的命令方法
     */
    /*class ftpDisconnect implements Runnable{
        @Override
        public void run() {
            try {
                client.disconnect(true);  //安全退出
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                e.printStackTrace();
            }
        }
    }*/
    /**
     * 新建文件的命令方法
     */
    /*class ftpMkdir implements Runnable
    {
        private String fileName;

        public ftpMkdir(String fileName) {
            this.fileName = fileName;
        }
        public void infoSetter(String fileName)
        {
            this.fileName=fileName;
        }

        @Override
        public void run() {
            try {
                client.createDirectory(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                e.printStackTrace();
            }
        }
    }*/


    /**
     * 获取当前文件夹路径的命令方法
     */
    /*class ftpGetPath implements Runnable{
        String tem;
        @Override
        public void run() {
            try {

                tem=client.currentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    path=tem;
                }
            });
         }
    }*/

    /**
     * 切换当前目录的命令方法
     */
    class ftpChDir implements Runnable{
        private String path;

        public ftpChDir(String path) {
            this.path = path;
        }

        public void infoSetter(String path)
        {
            this.path=path;
        }
        @Override
        public void run() {

            try {
                client.cwd(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //client.changeDirectory(path);

        }
    }


    /**
     * 返回上级目录的命令方法
     */

    class ftpChDirUp implements  Runnable
    {
        @Override
        public void run() {
            try {
                client.cwd("..");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }







    /**
     * 下载文件的命令方法
     */
    class ftpDownload implements  Runnable{
        private File file;
        private String name;

        public ftpDownload(File file, String name) {
            this.file = file;
            this.name = name;
        }
        public void infoSetter(File file, String name) {
            this.file = file;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                client.download(name,file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 上载文件的命令方法
     */
    class ftpUpload implements  Runnable{
        private File file;
        private String name;

        public ftpUpload(File file, String name) {
            this.file = file;
            this.name = name;
        }
        public void infoSetter(File file, String name) {
            this.file = file;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                client.upload(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ftp命令的工厂方法
     * 考虑使用单例设计模式
     */
        class FtpFactory{
        public Runnable createftpLogin(String hostName, int port, String username, String passwd)
        {
            return new ftpLogin(hostName,port,username,passwd);
        }
        public Runnable createftpChDir(String path)
        {
            return new ftpChDir(path);
        }
        public Runnable createftpChDirUp()
        {
            return new ftpChDirUp();
        }

        public  Runnable createftpGetfilelist()
        {
            return new ftpGetfilelist();
        }

    }

    /**
     * 将ftp命令方法加入进程池
     */

    private void freshFilelist()
    {
        mThreadPool.execute(new ftpFreshFileList());
    }
    private void login(String hostName, int port, String username, String passwd)
    {
        mThreadPool.execute(mftpFactory.createftpLogin(hostName,port,username,passwd));
    }

    private void chdir(String path)
    {
        mThreadPool.execute(mftpFactory.createftpChDir(path));
    }
    private void chdirUp()
    {
        mThreadPool.execute(mftpFactory.createftpChDirUp());
    }

    private void download(String remotename,String localname) {
        mThreadPool.execute(new download(remotename,localname));}
    private void update(String localname){
        mThreadPool.execute(new update(localname));
    }
    /*private void getPath()
    {
        mThreadPool.execute(new ftpGetPath());

        //mThreadPool.execute(mftpFactory.createftpGetPath());
    }*/

    private FTPFile[] getFilelist() {
        ftpGetfilelist tem=new ftpGetfilelist();
        mThreadPool.execute(tem);
        return tem.getFilelist();
    }

    class ftpFreshFileList implements  Runnable
    {
        @Override
        public void run() {
            FTPFile[] temlist= new FTPFile[0]; //获取登录的目录列表
            try {
                temlist = client.list(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<FTPFile> temfilelist=(List<FTPFile>) Arrays.asList(temlist);
            filelist.clear();
            filelist.addAll(temfilelist);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        mThreadPool.execute(new goBack());
    }

    class goBack implements Runnable
    {
        @Override
        public void run() {
            if(connectFlag==0)
            {finish();
                Log.d(TAG, "connectFlag==0");}
            else {
                try {
                    String mpath = client.pwd();
                    if (mpath.equals("/"))
                        finish();
                    else {
                        client.cwd("..");
                    }
                    FTPFile[] temlist = new FTPFile[0]; //获取登录的目录列表
                    try {
                        temlist = client.list(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    List<FTPFile> temfilelist = (List<FTPFile>) Arrays.asList(temlist);
                    filelist.clear();
                    filelist.addAll(temfilelist);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
