package com.luedongtech.monitor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zombie on 14/12/15.
 */
public class ServiceTimeTask extends AsyncTask<Void, Void, String> {
    public static final String SERVICE_TIME_API = "http://3000kr.cn:8080/misc/datetime";
    private Handler mhandler;

    public ServiceTimeTask(Handler handler) {
        mhandler = handler;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String datetime = null;

        // 新建HttpPost对象
        HttpGet httpGet = new HttpGet(SERVICE_TIME_API);
        // 获取HttpClient对象
        HttpClient httpClient = new DefaultHttpClient();
        // 请求超时
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        // 读取超时
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
        int try_times = 0;
        try {
            while (try_times < 3) {
                // 获取HttpResponse实例
                HttpResponse httpResp = httpClient.execute(httpGet);
                // 判断是够请求成功
                if (httpResp.getStatusLine().getStatusCode() == 200) {
                    // 获取返回的数据
                    HttpEntity respEntity = httpResp.getEntity();
                    String resp = EntityUtils.toString(respEntity, "UTF-8");
                    JSONObject json = new JSONObject(resp);
                    Iterator<String> keys = json.keys();
                    Map<String, String> result = new HashMap<String, String>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        result.put(key, json.getString(key));
                    }
                    datetime = result.get("datetime");
                    break;
                } else {
                    try_times ++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datetime;
    }

    @Override
    protected void onPostExecute(String datetime) {
        if (datetime == null || datetime.isEmpty()) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("datetime", datetime);
        Message message = new Message();
        message.what = MonitorService.WHAT_TIME;
        message.setData(bundle);
        mhandler.sendMessage(message);
    }
}
