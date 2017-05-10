/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.colin.ftptest03;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.net.Socket;
import java.io.*;
import java.util.*;

/**
 *
 * @author Administrator
 */
public class FTPClient {
    private Handler handler; // 异步传输消息
    private Socket connectSocket;//控制连接，用于传送和响应命令
    private Socket dataSocket;//数据连接，用于数据传输
    private BufferedReader inData;//控制连接中用于读取返回信息的数据流
    private BufferedWriter outData;//控制连接中用于传送用户命令的数据流
    private String response = null;//将返回信息封装成字符串
    private String remoteHost;//远程主机名
    private int remotePort;//通信端口号
    private String remotePath;//远程路径
    private String user;//用户名
    private String passWord;//用户口令
    File rootPath = new File("/");//根路径
    File currentPath = rootPath;//当前路径
    private boolean logined;//判断是否登录服务器的标志
    private boolean debug;

    public FTPClient(Handler handler) {
        remoteHost = "118.89.47.194";  //
        remotePort = 21;  //默认端口21
        remotePath = "/"; //默认根目录 /
        user = ""; //
        passWord = ""; //
        logined = false;
        debug = false;
        this.handler=handler;
    }

    //设置服务器域名（IP地址）
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    //返回服务器域名（IP地址）
    public String getRemoteHost() {
        return remoteHost;
    }
    //设置端口
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    //返回端口
    public int getRemotePort() {
        return remotePort;
    }
    //The remote directory path
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
    /// The current remote directory path.
    public String getRemotePath() {
        return remotePath;
    }
    //用户名
    public void setUser(String user) {
        this.user = user;
    }
    //密码
    public void setPW(String password) {
        this.passWord = password;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Socket connect() {
        Message message=new Message();
        try {
            if (connectSocket == null) {

                connectSocket = new Socket(remoteHost, remotePort);
                inData = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));//输入信息(字符输入流)

                outData = new BufferedWriter(new OutputStreamWriter(connectSocket.getOutputStream()));//输出信息(字符输出流)
            }
            response = readLine();
        } catch (Exception e) {
            //System.out.println("连接失败");
            message.what=2; //连接失败
            handler.sendMessage(message);
        }
        if(response.startsWith("220")) {
            //System.out.println("连接成功");
            message.what=3;
            handler.sendMessage(message);
        }
        return connectSocket;
    }

    public void login() {
        Message message=new Message();
        try {
            if (connectSocket == null) {
                System.out.println("尚未连接");
                return;
            }
            sendCommand("USER " + user);
            response = readLine();
            if (!response.startsWith("331")) {
                cleanup();

               // System.out.println("Error:用户名或密码错误！" + response);
               // System.out.println(response);
                return;
            }
            sendCommand("PASS " + passWord);
            response = readLine();
            if (!response.startsWith("230")) {
                cleanup();
                message.what=-1;
                handler.sendMessage(message);
                //System.out.println("Error:用户名或密码错误！" + response);
                //System.out.println(response);
                return;
            }
            logined = true;
            //System.out.println("登录成功");
            message.what=1;
            handler.sendMessage(message);
            cwd(remotePath);
        } catch (Exception e) {
            //System.out.println("登录失败");
            message.what=0;
            handler.sendMessage(message);
        }
    }

    //输入用户名和密码登录
    public void login(String host,int port,String name,String password)
    {
        remoteHost=host;
        remotePort=port;
        user=name;
        passWord=password;
        connect();
        login();
    }
    //获取远程服务器的目录列表
    public ArrayList<String> list(String mask,int i) throws IOException {
        if (!logined) {
            System.out.println("服务器尚未连接。");
        //login();
        }
        ArrayList<String> fileList = new ArrayList<String>();
        try {
            dataSocket = createDataSocket();
            if (mask == null || mask.equals("") || mask.equals(" ")) {
                sendCommand("LIST");
            } else {
                sendCommand("LIST " + mask);
            }
            response = readLine();
            if (!response.startsWith("1")) {
                System.out.println(response);
            }
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            String line;
            while ((line = dataIn.readLine()) != null) {
                fileList.add(line);
            }

            dataIn.close();//关闭数据流
            dataSocket.close();//关闭数据连接 
            response = readLine();

            System.out.println("List Complete.");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fileList;
    }
    public FTPFile[] list(String mask) throws IOException {
        ArrayList<String> mlist=list(mask,0);
        FTPFile[] arr=FTPFile.getArr(mlist);
        return arr;
    }
    ///
    /// Close the FTP connection.
    /// 退出登录并终止连接QUIT
    public synchronized void close() throws IOException {
        try {
            sendCommand("QUIT ");
        } finally {
            cleanup();
            System.out.println("正在关闭......");
        }
    }

    private void cleanup() {
        try {
            inData.close();
            outData.close();
            connectSocket.close();
            //connectSocket = null;
            logined = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    ///
    /// If the value of mode is true, set binary mode for downloads.
    /// Else, set Ascii mode.
    ///
    ///
    public void setBinaryMode(Boolean mode) throws IOException {

        if (mode) {
            sendCommand("TYPE I ");
        } else {
            sendCommand("TYPE A ");
        }
        response = readLine();
        if (!response.startsWith("200")) {
            throw new IOException("Caught Error " + response);
        }
    }
    //显示当前远程工作目录PWD
    public synchronized String pwd() throws IOException {
        sendCommand("XPWD ");
        String dir = null;
        response = readLine();
        if (response.startsWith("257")) {         //服务器响应信息如：257 "/C:/TEMP/" is current directory.截取两引号之间的内容
            int fristQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', fristQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(fristQuote + 1, secondQuote);
            }
        }
        System.out.println(""+dir);
        return dir;
    }
    //CWD 改变远程系统的工作目录
    public synchronized boolean cwd(String dir) throws IOException {
        if (dir.equals("/")) {//根路径
            System.out.println("当前路径是根目录！");
        }
        if (!logined) {
            login();
        }
        sendCommand("CWD " + dir);
        response = readLine();
        if (response.startsWith("250 ")) {
            return true;
        } else {
            return false;
        }
    }
    //上传文件
    public synchronized boolean upload(String localFileName) throws IOException {
        Message message=new Message();
        dataSocket = createDataSocket();
        int i = localFileName.lastIndexOf("/");
        if (i == -1) {
            i = localFileName.lastIndexOf("\\");
        }
        String element_1 = "";
        if (i != -1) {
            element_1 = localFileName.substring(i + 1);
        }
        sendCommand("STOR " + element_1);
        response = readLine();
        if (!response.startsWith("1")) {
            System.out.println(response);
        }
        FileInputStream dataIn = new FileInputStream(localFileName);
        BufferedOutputStream dataOut = new BufferedOutputStream(dataSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        do {
            bytesRead = dataIn.read(buffer);
            if (bytesRead != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
        } while (bytesRead != -1);
        dataOut.flush();
        dataOut.close();
        dataIn.close();
        dataSocket.close();//关闭此数据连接
        response = readLine();
        if(response.startsWith("226"))
        {
            message.what=11;
            handler.sendMessage(message);
        }
        return (response.startsWith("226"));
    }
    //下载文件  RETR
    public synchronized boolean download(String remoteFile, String localFile) throws IOException {
        Message message=new Message();
        dataSocket = createDataSocket();
        sendCommand("RETR " + remoteFile);
        response = readLine();
        if (!response.startsWith("1")) {
            System.out.println(response);
        }
        System.out.println(localFile);
        BufferedInputStream dataIn = new BufferedInputStream(dataSocket.getInputStream());
        new File(localFile).createNewFile();
        FileOutputStream fileOut = new FileOutputStream(localFile);
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        do {
            bytesRead = dataIn.read(buffer);
            if (bytesRead != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
        } while (bytesRead != -1);
        fileOut.flush();
        fileOut.close();
        dataSocket.close();
        response = readLine();
        if(response.startsWith("226"))
        {
            message.what=12;
            handler.sendMessage(message);
        }
        return (response.startsWith("226"));
    }
    


    
    //在远程服务器上创建一个目录
    public void mkdir(String dirName) throws IOException {

        if (!logined) {
            login();
        }

        sendCommand("XMKD " + dirName); // 创建目录
        response = readLine();
        if (!response.startsWith("257")) {      //FTP命令发送过程发生异常
            System.out.println( response);
        } else {
             System.out.println("创建目录成功！");        //成功创建目录
        }

    }
    //删除远程个服务器上的一个目录
    public void rmdir(String dirName) throws IOException {
        if (!logined) {                 //如果尚未与服务器连接，则连接服务器
            login();
        }

        sendCommand("XRMD " + dirName);
        response = readLine();
        if (!response.startsWith("250")) {     //FTP命令发送过程发生异常
            System.out.println(response);
        } else {
            System.out.println("删除目录成功！");         //成功删除目录
        }

    }
    //建立数据连接
    private Socket createDataSocket() throws IOException {

        sendCommand("PASV ");               //采用Pasv模式（被动模式），由服务器返回数据传输的临时端口号，使用该端口进行数据传输
        response = readLine();
        if (!response.startsWith("227")) {      //FTP命令传输过程发生异常
            System.out.println(response);
        }
        String clientIp = "";
        int port = -1;
        int opening = response.indexOf('(');               //采用Pasv模式服务器返回的信息如“227 Entering Passive Mode (127,0,0,1,64,2)”
        int closing = response.indexOf(')', opening + 1);  //取"()"之间的内容：127,0,0,1,64,2 ，前4个数字为本机IP地址，转换成127.0.0.1格式
        if (closing > 0) {                                 //端口号由后2个数字计算得出：64*256+2=16386
            String dataLink = response.substring(opening + 1, closing);

            StringTokenizer arg = new StringTokenizer(dataLink, ",", false);
            clientIp = arg.nextToken();

            for (int i = 0; i < 3; i++) {
                String hIp = arg.nextToken();
                clientIp = clientIp + "." + hIp;
            }
            port = Integer.parseInt(arg.nextToken()) * 256 + Integer.parseInt(arg.nextToken());
        }

        return new Socket(clientIp, port);
    }
//用于读取服务器返回的响应信息
    private String readLine() throws IOException {
        String line = inData.readLine();
        if (debug) {
            System.out.println("< " + line);
        }
        return line;
    }
//用于发送命令
    private void sendCommand(String line) {
        if (connectSocket == null) {
            System.out.println("FTP尚未连接");         //未建立通信链接，抛出异常警告
        }
        try {
            outData.write(line + "\r\n");               //发送命令
            outData.flush();                            //刷新输出流
            if (debug) {
                System.out.println("> " + line);        //同时控制台输出相应命令信息，以便分析
            }
        } catch (Exception e) {
            connectSocket = null;
            System.out.println(e);
            return;
        }
    }
}
