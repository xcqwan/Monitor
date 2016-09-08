package com.luedongtech.monitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by zombie on 14/12/4.
 */
public class MonitorService extends Service {
    public static final int WHAT_TIME = 1;
    public static final int WHAT_LOG = 2;

    private static final String TAG = "MonitorService";
    protected static MonitorService Instance = null;
    private static String AppFileName = "";
    private static String UserID = "";
    private static int CacheSize = 5;
    private static String NewFileBroad = "com.luedongtech.monitor.newlogfile";
    private static String MacAddress = "";

    private IBinder mIBinder = new Binder();
    private LinkedList<String> writeCache = new LinkedList<String>();
    private int writeCacheSize = 0;
    private boolean isWrite = false;

    private String basePath = Environment.getExternalStorageDirectory().getPath();

    private DateChangeReceiver mDateChangeReceiver;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_TIME:
                    String datetime = msg.getData().getString("datetime");
                    MonitorUtils.calcTimeCompens(datetime);
                    break;
                case WHAT_LOG:
                    String content = msg.getData().getString("content");
                    pushToCache(content);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化, appName为存放日志目录名称
     * @param context
     * @param appName
     */
    public static void Init(Context context, String appName, String userID) {
        AppFileName = appName;
        UserID = userID;
        if (Instance != null) {
            return;
        }
        MacAddress = getLocalMacAddress(context);
        if (MacAddress != null) {
            MacAddress = MacAddress.replaceAll(":", "");
        }
        context.startService(new Intent(context, MonitorService.class));
    }

    /**
     * 设置当前用户ID
     * @param userID
     */
    public static void SetUserID(String userID) {
        if (Instance != null) {
            Instance.writeAllCache();
        }
        UserID = userID;
    }

    /**
     * 设置写入频率, 每size条日志写一次
     * @param size
     */
    public static void SetCacheSize(int size) {
        if (size > 0) {
            CacheSize = size;
        }
    }

    /**
     * 获取需上传文件地址列表
     * @return
     */
    public static ArrayList<String> GetUpLoadFileList() {
        ArrayList<String> logFileList = new ArrayList<String>();
        if (Instance != null) {
            logFileList = Instance.getUpLoadFileList();
        }
        return logFileList;
    }

    /**
     * 获取当前写入文件地址
     * @return
     */
    public static String GetCurrentLogFile() {
        if (Instance != null) {
            return Instance.getCurrentLogFile();
        }
        return "";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate 开启服务");
        super.onCreate();

        Instance = this;
        new ServiceTimeTask(mHandler).execute();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        mDateChangeReceiver = new DateChangeReceiver();
        registerReceiver(mDateChangeReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand 开启服务");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy 销毁服务");
        //服务被干掉时, 将缓存的全部写入文件
        pushToCache("monitor service onDestroy");
        writeAllCache();
        super.onDestroy();
        unregisterReceiver(mDateChangeReceiver);

        startService(new Intent(this, MonitorService.class));
    }

    protected Handler getHandler() {
        return mHandler;
    }

    /**
     * 需上传文件列表, 除当前写入文件
     * @return
     */
    protected ArrayList<String> getUpLoadFileList() {
        ArrayList<String> uploadFileList = new ArrayList<String>();
        File dir = new File(basePath + "/" + AppFileName);
        if (dir.exists()) {
            File files[] = dir.listFiles();
            String logFilePath = getCurrentLogFile();
            for (File file : files) {
                String filePath = file.getAbsolutePath();
                if (!filePath.equals(logFilePath) && file.length() > 0) {
                    uploadFileList.add(filePath);
                }
            }
        }

        return uploadFileList;
    }

    /**
     * 当前写入log文件
     * @return
     */
    protected String getCurrentLogFile() {
        String dateHourStr = MonitorUtils.getDateHourStr();
        File dir = new File(basePath + "/" + AppFileName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir.getPath() + "/" + UserID + "_" + dateHourStr + "_" + MacAddress);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return "";
    }

    /**
     * 将缓存全写入文件
     */
    protected void writeAllCache() {
        if (writeCacheSize > 0) {
            writeToFile();
        }
    }

    /**
     * 写入缓存
     * @param content
     */
    private synchronized void pushToCache(String content) {
        if (content == null) {
            return;
        }
        writeCache.addLast(content);
        writeCacheSize++;
        if (writeCacheSize >= CacheSize) {
            writeToFile();
        }
    }

    /**
     * 写入日志到文件
     */
    private void writeToFile() {
        if (isWrite) {
            return;
        }

        File file = getWriteFile();
        isWrite = true;
        StringBuilder writeSB = new StringBuilder();
        while (!writeCache.isEmpty()) {
            String line = writeCache.removeFirst();
            if (writeSB.length() + line.length() > Integer.MAX_VALUE) {
                try {
                    FileUtils.write(file, writeSB.toString(), true);
                    writeSB = new StringBuilder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeSB.append("\n" + line);
        }
        writeCacheSize = 0;
        try {
            FileUtils.write(file, writeSB.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isWrite = false;
    }

    /**
     * 获取每个小时写入的文件
     * @return
     */
    private File getWriteFile() {
        String dateHourStr = MonitorUtils.getDateHourStr();
        File dir = new File(basePath + "/" + AppFileName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir.getPath() + "/" + UserID + "_" + dateHourStr + "_" + MacAddress);
        if (!file.exists()) {
            try {
                String headerContent = getDeviceInfo();
                FileUtils.write(file, headerContent, false);
                sendBroadcast(new Intent(NewFileBroad));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        }
        return file;
    }

    /**
     * 获取设备信息
     * @return
     */
    private String getDeviceInfo() {
        TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String imei = null;
        try {
            imei = mTm.getDeviceId();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        String numer = mTm.getLine1Number();

        Map<String, String> deviceInfo = new HashMap<String, String>();
        deviceInfo.put("IMEI", imei);
        deviceInfo.put("MacAddress", getLocalMacAddress(this));
        deviceInfo.put("MODEL", Build.MODEL);
        deviceInfo.put("BRAND", Build.BRAND);
        deviceInfo.put("numer", numer);
        deviceInfo.put("SDK_INT", Build.VERSION.SDK_INT + "");
        deviceInfo.put("DISPLAY", Build.DISPLAY);
        deviceInfo.put("APP_VERSION", getVersionCode(this) + "");
        return deviceInfo.toString();
    }

    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * 获取应用版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private class DateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            new ServiceTimeTask(mHandler).execute();
        }
    }
}
