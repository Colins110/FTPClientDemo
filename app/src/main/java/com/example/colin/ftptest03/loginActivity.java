package com.example.colin.ftptest03;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class loginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        showWaiterAuthorizationDialog() ;
    }
    //显示对话框
    public void showWaiterAuthorizationDialog() {

        //LayoutInflater是用来找layout文件夹下的xml布局文件，并且实例化
        LayoutInflater factory = LayoutInflater.from(loginActivity.this);
        //把activity_login中的控件定义在View中

        final View textEntryView = factory.inflate(R.layout.activity_login, null);

        //将LoginActivity中的控件显示在对话框中
        new AlertDialog.Builder(loginActivity.this)
                //对话框的标题
                .setTitle("连接到主机")
                //设定显示的View
                .setView(textEntryView)
                //对话框中的“登陆”按钮的点击事件
                .setPositiveButton("登陆", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        //获取用户输入的“用户名”，“密码”
                        //注意：textEntryView.findViewById很重要，因为上面factory.inflate(R.layout.activity_login, null)将页面布局赋值给了textEntryView了
                        final EditText hostnameET = (EditText)textEntryView.findViewById(R.id.ip);
                        final EditText portET = (EditText)textEntryView.findViewById(R.id.port);
                        final EditText usernameET=(EditText)textEntryView.findViewById(R.id.username);
                        final EditText passwdET=(EditText)textEntryView.findViewById(R.id.passwd);
                        hostnameET.setText("118.89.47.194");
                        portET.setText("21");
                        usernameET.setText("");
                        passwdET.setText("");
                        //将页面输入框中获得的“用户名”，“密码”转为字符串
                        String hostname = hostnameET.getText().toString().trim();
                        int port = Integer.parseInt(portET.getText().toString().trim());
                        String username=usernameET.getText().toString().trim();
                        String passwd=passwdET.getText().toString().trim();

                        Intent intent=new Intent();
                        intent.putExtra("hostname",hostname);
                        intent.putExtra("port",port);
                        intent.putExtra("username",username);
                        intent.putExtra("passwd",passwd);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                })
                //对话框的“退出”单击事件
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        loginActivity.this.finish();
                    }
                })

                //对话框的创建、显示
                .create().show();
    }
}

