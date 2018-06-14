package com.example.admin.cmdtest;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;


public class MainActivity extends AppCompatActivity {
    TextView tv_toactivity;
    static TelephonyManager TelephonyMgr;

    String[] deviceIds = {"860BCML223E7", "MFLVR8AECMSKZSQG"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_toactivity = (TextView) findViewById(R.id.tv_toactivity);
        tv_toactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity("");
            }
        });
        Log.i("IMEI", getIMIEStatus(this));
        Log.i("AndroidId", getAndroidId(this));
        TelephonyManager tm = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        Log.i("SimSerialNumber", getSerialNumber());
//        new MonkeyUtil().startActivity(TelephonyMgr.getSimSerialNumber());
    }


    public void startActivity(String path) {
        Log.i("adb devices", "startActivity");
        Runtime run = Runtime.getRuntime();
        Process proc;
        try {
            proc = run.exec("adb shell dumpsys activity top");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }
            Log.i("adb devices", stringBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("adb devices", e.getMessage());
        }
    }

    // IMEI码
    private static String getIMIEStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }


    // Android Id
    private static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }


    /**
     * getSerialNumber
     *
     * @return result is same to getSerialNumber1()
     */

    public static String getSerialNumber() {

        String serial = null;

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);

            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception e) {

            e.printStackTrace();

        }

        return serial;
    }

    public boolean watchActivity() {
        boolean isAlive = false;
        Runtime run = Runtime.getRuntime();
        Process proc;
        try {
            proc = run.exec("adb shell dumpsys activity top");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                if (line.contains("org.sojex.finance")) {
                    isAlive = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isAlive;
    }
}
