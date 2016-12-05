package jinnycorp.reminderapp;

import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    Button tbutton, dbutton, submit;
    TextView datev;
    EditText rinput;
    int year, month, day, hour, minute;
    boolean dateset, timeset = false;
    RequestQueue queue;
    String url = "http://76.14.25.135:8000";
    Response.Listener<String> resl = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

        }
    };
    Response.ErrorListener errresl = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    TimePickerDialog.OnTimeSetListener timel = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int h, int m) {
            hour = h;
            minute = m;
            timeset = true;
            if(dateset && timeset) {
                setDateView();
            }
        }
    };
    DatePickerDialog.OnDateSetListener datel = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int y, int m, int d) {
            year = y;
            month = m;
            day = d;
            dateset = true;
            if(dateset && timeset) {
                setDateView();
            }
        }
    };

    private void setDateView(){
        datev.setText("Date: " + month+"/"+day+"/"+year+" "+hour+":"+minute);
    }

    private boolean checkerRunning(){
        ActivityManager man = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : man.getRunningServices(Integer.MAX_VALUE)){
            if(rchecker.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        tbutton = (Button) findViewById(R.id.tbutton);
        dbutton = (Button) findViewById(R.id.dbutton);
        submit = (Button) findViewById(R.id.submit);
        datev = (TextView) findViewById(R.id.datev);
        rinput = (EditText) findViewById(R.id.rinput);

        if(!checkerRunning()){
            startService(new Intent(this, rchecker.class));
            Log.i("CUSTOML", "starting checker from app");
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dateset && timeset && rinput.getText().length() > 0){
                    StringRequest r = new StringRequest(Request.Method.POST, url, resl, errresl){
                        protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("reminder", rinput.getText().toString());
                            params.put("time", year+":"+month+":"+day+":"+hour+":"+minute+":");
                            return params;
                        }
                    };
                    queue.add(r);
                } else {
                    Toast.makeText(MainActivity.this, "please enter date and time before sending", Toast.LENGTH_SHORT);
                }
            }
        });

        tbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance(TimeZone.getDefault());
                new TimePickerDialog(MainActivity.this, timel, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
            }
        });
        dbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance(TimeZone.getDefault());
                new DatePickerDialog(MainActivity.this, datel, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
}
