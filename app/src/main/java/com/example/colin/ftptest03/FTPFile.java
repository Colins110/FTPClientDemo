package com.example.colin.ftptest03;

import java.util.ArrayList;

/**
 * Created by colin on 2017/4/30 0030.
 */
public class FTPFile {
    public static final  int TYPE_DIRECTORY=1;
    public static final int TYPE_FILE=0;
    String name;
    int type; //0为文件，1为文件夹
    public FTPFile(String info)
    {
        String[] arr=info.split(" ");
        name=arr[arr.length-1];
        char t=info.charAt(0);
        if(t=='-')
            type=0;
        else if(t=='d')
            type=1;
    }
    static FTPFile[] getArr(ArrayList<String> info)
    {
        FTPFile[] arr=new FTPFile[info.size()];
        for(int i=0;i<info.size();i++)
        {
            arr[i]=new FTPFile(info.get(i));
        }
        return arr;
    }
    public String getName()
    {
        return name;
    }
    public int getType()
    {
        return type;
    }
}
