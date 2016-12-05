package jinnycorp.reminderapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class rchecker extends Service {
    RequestQueue queue;
    JsonObjectRequest r;
    int timesReminded = 1;
    String url = "http://76.14.25.135:8000/list";

    private ArrayList<String> getKeys(JSONObject jobj){
        Iterator<?> keysi = jobj.keys();
        ArrayList<String> keys = new ArrayList<String>();

        while(keysi.hasNext()) {
            String key = (String)keysi.next();
            keys.add(key);
        }
        return keys;
    }

    private boolean timeIsCurOrBefore(String tStr){
        int[] tElems = strToIntArr(tStr.split(":"));
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int[] tNow = new int[]{now.get(now.YEAR), now.get(now.MONTH), now.get(now.DAY_OF_MONTH), now.get(now.HOUR_OF_DAY), now.get(now.MINUTE)};
        for(int i = 0; i < tNow.length; i++){
            if(tElems[i] > tNow[i]){
                Log.i("CUSTOML", "index "+i+" is greater in tElems "+tElems[i]+" vs "+tNow[i]);
                return false;
            } else if (tElems[i] < tNow[i]){
                return true;
            }
        }
        return true;
    }

    private int[] strToIntArr(String[] arr) {
        int[] out = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = Integer.parseInt(arr[i]);
        }
        return out;
    }

    private void remind(String rtext) {
        timesReminded++;
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.list)
                .setContentTitle("reminder")
                .setContentText(rtext);

        NotificationManager nMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nMan.notify(timesReminded, nBuilder.build());

        Log.i("CUSTOML", "notified");
    }

    public rchecker() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ScheduledExecutorService s = Executors.newScheduledThreadPool(1);
        Log.i("CUSTOML", "checker started");

        Runnable check = new Runnable(){
            public void run(){
                queue.add(r);
                Log.i("CUSTOML", "checking for reminders");
            }
        };

        s.scheduleAtFixedRate(check, 0, 30, TimeUnit.SECONDS);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this);
        r = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                Log.i("CUSTOML", "got reminder list");
                ArrayList<String> keys = getKeys(res);
                String rtime;
                String key;
                for (int i = 0; i < keys.size(); i++) {
                    key = keys.get(i);
                    try {
                        rtime = res.getString(key);
                        if(timeIsCurOrBefore(rtime)){
                            remind(key);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
