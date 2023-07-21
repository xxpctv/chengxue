package com.github.tvbox.osc.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
 
import androidx.core.content.FileProvider;
 
import com.github.tvbox.osc.R;
 
import org.json.JSONException;
import org.json.JSONObject;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
 
public class UpdateManager {
    // 应用程序Context
    private Context mContext;
    // 是否是最新的应用,默认为false
    private boolean isNew = false;
    private boolean intercept = false;
    // 下载安装包的网络路径
    private String apkUrl = "http://3.36.153.51:52828/%E8%BF%85%E9%9B%B7%E4%BA%91%E7%9B%98/apk/"
            + "舒夏影视.apk";
    // 保存APK的文件夹
    private static String savePath;
    //apk文件的绝对路径
    private static String saveFileName ;
    // 下载线程
    private Thread downLoadThread;
    private int progress;// 当前进度
    TextView text;
    // 进度条与通知UI刷新的handler和msg常量
    private ProgressBar mProgress;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
 
    public UpdateManager(Context context) {
        mContext = context;
        //获取存储路径
        savePath = String.valueOf(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))+"/";
//这里是一个易出问题点，android9以后对外部权限的改动，不能使用getExternalStorageDirectory()去获取路径，否则会提示not found 
    //要在AndroidManifest.xml中的application标签中声明android:requestLegacyExternalStorage="true"
    }
 
    /**
     * 检查是否更新的内容
     */
    //获取应用的版本号versionCode
    //对于versionCode新版本和老版本一定是不一样的，这个详细见打包项目成.apk部分
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }
 
    
    public void checkVersion() {
        try {
            //下载网站的json数据包，根据数据包的版本来判断当前版本是否需要被替换
            //这里的StreamUtil().getjson()是我自己写的一个工具类，里面的getjson()方法能获取给定uri的json文件，小伙伴们可以自行搜索或者自力更生写一个工具类
            String json = new StreamUtil().getjson("http://3.36.153.51:52828/%E8%BF%85%E9%9B%B7%E4%BA%91%E7%9B%98/"
                    + "update.json");
            JSONObject jo = new JSONObject(json);
            int versionCode = jo.getInt("versionCode");//获取版本代号
            Log.i("本软件版本号", Integer.toString(UpdateManager.getVersionCode(mContext)));
            Log.i("服务器软件版本号", Integer.toString(versionCode));
            if (UpdateManager.getVersionCode(mContext) < versionCode) {
                //本地版本小于服务器版本，存在新版本
                isNew = false;
            } else {
                isNew = true;
            }
            checkUpdateInfo();//判断isNew，检测是否需要更新操作
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
 
    public void checkUpdateInfo() {
        if (isNew) {
            return;
        } else {
            showUpdateDialog();
        }
    }
 
    /**
     * 显示更新程序对话框，供主程序调用
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage("舒夏影视有最新版本，是否下载!");
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDownloadDialog();
            }
 
        });
        builder.setNegativeButton("以后再说",
                new DialogInterface.OnClickListener() {
 
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//弹出的对话框销毁
                    }
                });
 
        builder.create().show();
    }
 
    /**
     * 显示下载进度的对话框
     */
    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
 
        builder.setView(v);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intercept = true;//取消下载的标志位
            }
        });
        builder.show();
        downloadApk();
    }
 
    /**
     * 从服务器下载APK安装包
     */
    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }
 
    //下载安装包的进程，同步更新安装包的下载进度，以及下载完成后的通知安装
    private Runnable mdownApkRunnable = new Runnable() {
 
        @Override
        public void run() {
            URL url;
            try {
                //对sd卡进行状态的判断，如果相等的话表示当前的sdcard挂载在手机上并且是可用的
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 
                    url = new URL(apkUrl);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream ins = conn.getInputStream();
                    //创建安装包所在的前置文件夹
                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    //创建安装包的文件，安装包将下载入这个文件夹中
                    long time = System.currentTimeMillis();//当前时间的毫秒数
                    saveFileName = savePath
                            +time+"_ARKUpdate.apk";
                    File apkFile = new File(saveFileName);
                    //创建该文件夹的输入流
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte[] buf = new byte[1024];
                    while (!intercept) {//对取消下载的标志位进行判断，如果一直没有被打断即没有点击取消下载按钮则继续下载
                        int numread = ins.read(buf);//返回读入的字节个数并将读到的字节内容放入buf中
                        count += numread;
                        progress = (int) (((float) count / length) * 100);//当前进度，用来更新progressBar的进度
 
                        // 通知主线程更新下载进度
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                        if (numread <= 0) {
                            // 下载完成通知安装
                            mHandler.sendEmptyMessage(DOWN_OVER);
                            break;
                        }
                        //已经全部读入，不需要再读入字节为-1的内容
                        fos.write(buf, 0, numread);//从fos中写入读出的字节个数到buf中
                    }
 
                    fos.close();
                    ins.close();
                }
                else
                    return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
 
    /**
     * 安装APK内容，这些代码主要是对文件进行操作，需要理解文件的读写，但是不了解也没关系
     * 一般安装apk以及下载apk部分短时间内不会有什么变化，需要用的时候改改路径复制粘贴就行
     */
    //在官方7.0的以上的系统中，尝试传递 file://URI可能会触发FileUriExposedException。所以这里用到了FIleProvider，具体步骤上面已经给出
    private void installAPK() {
        //这里给出了一个不使用FIleProvider即更新失败的
 
//        File apkFile = new File(saveFileName);
//        if (!apkFile.exists()) {//不存在即没有下载则返回
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()),
//                "application/vnd.android.package-archive");//根据apk路径自动安装apk
//        mContext.startActivity(intent);
 
//        打开apk文件的格式
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(android.content.Intent.ACTION_VIEW);
//        intent.setDataAndType(uri,"application/vnd.android.package-archive");
 
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置启动模式，四种之一
        File apkFile = new File(saveFileName);
 
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// 前面代表的是当前操作系统的版本号，后面是给定的版本号，Ctrl鼠标放置显示版本号
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //记得修改com.xxx.fileprovider与androidmanifest相同
            // 获取的是应用唯一区分的id即applicationId
            uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");// 打开apk文件
        } else {
            uri = Uri.parse("file://" + apkFile.toString());
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
 
    ;
 
    //消息通知部分  
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
 
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
 
                case DOWN_OVER:
                    installAPK();
                    break;
 
                default:
                    break;
            }
        }
 
    };
 
}
