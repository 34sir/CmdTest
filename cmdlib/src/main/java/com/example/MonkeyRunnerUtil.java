package com.example;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.monkeyrunner.adb.AdbBackend;
import com.android.monkeyrunner.adb.AdbMonkeyDevice;
import com.android.monkeyrunner.core.IMonkeyDevice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by admin on 2017/8/1.
 */

public class MonkeyRunnerUtil {
    private AdbMonkeyDevice device;
    private AdbBackend adb;

    private MonkeyRunnerUtil() {

    }

    private MonkeyRunnerUtil(MonkeyRunnerUtil target) {
        this.runnerPath = target.runnerPath;
        this.pythonPath = target.pythonPath;
        this.pakageName = target.pakageName;
        this.activityPaths = target.activityPaths;
    }

    /**
     * 对多设备监听跳出，并执行跳转
     */
    public void checkForStartActivity() {
        System.out.println("----checkForStartActivity------");
        Runtime run = Runtime.getRuntime();
        Process proc;
        try {
            proc = run.exec("adb devices");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {//遍历所有的设备id
                if ((line.contains("device") || line.contains("offline")) && !line.contains("devices")) {  //排除第一行和最后一行的数据
                    String deviceId = line.replace("device", "").trim();
                    if (line.contains("device")) {
                        deviceId = line.replace("device", "").trim();
                    }
                    if (line.contains("offline")) {
                        deviceId = line.replace("offline", "").trim();
                    }
                    System.out.println("DeviceId=" + deviceId + "----isAlive=" + watchActivity(deviceId));
                    if (!watchActivity(deviceId)) {  // 对某一设备监听是否跳出
                        startActivityByJava(deviceId);  // 跳出则跳往指定的页面
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String pakageName = "org.sojex.finance";

    /**
     * 判断某个设备口袋贵金属是否在前台
     *
     * @param deviceId
     * @return
     */
    public boolean watchActivity(String deviceId) {
        boolean isAlive = false;
        Runtime run = Runtime.getRuntime();
        Process proc;
        try {
//            proc = run.exec("adb shell dumpsys activity top");
            //监听指定的设备
            proc = run.exec("adb -s " + deviceId + " shell dumpsys activity top");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                if (line.contains(pakageName)) {
                    isAlive = true;
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isAlive;
    }


    /**
     * java 启动monkeyrunner
     *
     * @param
     */
    public void startActivityByJava() {
        if (adb == null) {
            AndroidDebugBridge.addDeviceChangeListener(new DeviceChangeListener());
            adb = new AdbBackend();
            //这里需要注意一下adb的类型
        }
    }

    /**
     * java 启动monkeyrunner
     *
     * @param deviceId 设备id
     */
    public void startActivityByJava(String deviceId) {
        if (adb == null) {
            adb = new AdbBackend();
            //这里需要注意一下adb的类型
        }
        //      参数分别为自己定义的等待连接时间和设备id
        device = (AdbMonkeyDevice) adb.waitForConnection(1000, deviceId);
        //添加启动权限
        String action = "android.intent.action.MAIN";
        Collection<String> categories = new ArrayList<String>();
        categories.add("android.intent.category.LAUNCHER");
        //              启动要测试的主界面
        if (device != null) {
            for (int i = 0; i < activityPaths.length; i++) {
                System.out.println("startactivity :" + activityPaths[i]);
                device.startActivity(null, action, null, null, categories,
                        new HashMap<String, Object>(), activityPaths[i], 0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            device.dispose();  // 需要dispose操作 否则无法再次启动activity
        } else {
            startActivityByPython(runnerPath, pythonPath);   //如果java启动失败就python启动
        }
    }

    /**
     * 强制跳转activity
     *
     * @param
     */
    public void forceJumpActivity() {
        System.out.println("----forceJumpActivity------");
        Runtime run = Runtime.getRuntime();
        Process proc;
        try {
            proc = run.exec("adb devices");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {//遍历所有的设备id
                if ((line.contains("device") || line.contains("offline")) && !line.contains("devices")) {  //排除第一行和最后一行的数据
                    String deviceId = line.replace("device", "").trim();
                    if (line.contains("device")) {
                        deviceId = line.replace("device", "").trim();
                    }
                    if (line.contains("offline")) {
                        deviceId = line.replace("offline", "").trim();
                    }
                    startActivityByJava(deviceId);
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

        @Override
        public void deviceConnected(IDevice device) {
            System.out.println("SerialNumber=" + device.getSerialNumber());
            System.out.println("State=" + device.getState().name());
            IMonkeyDevice devices = adb.waitForConnection(30000, device.getSerialNumber());
            if (devices != null) {
                //添加启动权限
                String action = "android.intent.action.MAIN";
                Collection<String> categories = new ArrayList<String>();
                categories.add("android.intent.category.LAUNCHER");
                //              启动要测试的主界面
                if (devices != null) {
                    for (int i = 0; i < activityPaths.length; i++) {
                        devices.startActivity(null, action, null, null, categories,
                                new HashMap<String, Object>(), activityPaths[i], 0);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    devices.dispose();  // 需要dispose操作 否则无法再次启动activity
                }
            } else {
                startActivityByPython(runnerPath, pythonPath); //如果java启动失败就python启动
            }
        }

        @Override
        public void deviceDisconnected(IDevice iDevice) {

        }

        @Override
        public void deviceChanged(IDevice iDevice, int i) {

        }
    }

    public String runnerPath, pythonPath;
    public String[] activityPaths = {};

    public void setActivityPaths(String[] activityPaths) {
        this.activityPaths = activityPaths;
    }

    /**
     * cmd结合Python启动activity
     *
     * @param runnerPath monkeyrunner 路径
     * @param pythonPath python文件路径
     */
    public void startActivityByPython(String runnerPath, String pythonPath) {
        //"C:\\Users\\admin\\Desktop\\program.py"
        //"D:\\developer\\sdk\\tools\\monkeyrunner.bat"
        Runtime run = Runtime.getRuntime();
        try {
            run.exec(runnerPath + " " + pythonPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        MonkeyRunnerUtil target;

        public Builder() {
            target = new MonkeyRunnerUtil();
        }

        public Builder runnerPath(String runnerPath) {
            target.runnerPath = runnerPath;
            return this;
        }

        public Builder pythonPath(String pythonPath) {
            target.pythonPath = pythonPath;
            return this;
        }

        public Builder activityPaths(String activityPaths[]) {
            target.activityPaths = activityPaths;
            return this;
        }

        public Builder pakageName(String pakageName) {
            target.pakageName = pakageName;
            return this;
        }

        public MonkeyRunnerUtil build() {
            return new MonkeyRunnerUtil(target);
        }
    }
}
