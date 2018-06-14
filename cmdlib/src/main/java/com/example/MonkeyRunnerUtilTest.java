package com.example;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

/**
 * Created by admin on 2017/8/1.
 */

public class MonkeyRunnerUtilTest {
    private static MonkeyRunnerUtil util;
    private static List<String> activitys = new ArrayList<>();
    private static String pakageName;
    private static long totalTime = 15 * 60 * 1000;   //默认强制跳转时长为15分钟一次

    /**
     * @param args
     */
    public static void main(String[] args) {  //pakageName pythonPath runnerPath activityPaths
        // TODO Auto-generated method stub

        pakageName = "org.sojex.finance";
        String pythonPath = "C:\\Users\\admin\\Desktop\\program.py";
        String runnerPath = "D:\\developer\\sdk\\tools\\monkeyrunner.bat";
//        String activityPaths[] = {pakageName + "/.MainActivity", pakageName + "/.LoadingActivity"};
//        String activityPaths[] = {"org.sojex.finance/.active.me.LoginActivity"};

//        pakageName = args[0];
//        String pythonPath = args[1];
//        String runnerPath = args[2];
//        String json = args[3];
        String json = "[ { \"activity\": \"active.me.jiaoyibao.BankCardsActivity\", \"scale\":1 }, { \"activity\": \"active.markets.quotes.SettingGlodenSectionActivity\", \"scale\": 1}, { \"activity\": \"active.message.NewMessageDetailActivity\",\"scale\": 1},{ \"activity\": \"active.me.jiaoyibao.BenefitHistoryActivity\",\"scale\": 1},{ \"activity\": \"active.me.jiaoyibao.JiaoyibaoActivity\",\"scale\": 1},{ \"activity\": \"active.me.jiaoyibao.WithdrawalActivity\",\"scale\": 1}]";
        List<ActivityConfigureBean> list = new ArrayList<>();


        JSONArray array = JSONArray.fromObject(json);
        String activityPaths[] = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            ActivityConfigureBean bean = (ActivityConfigureBean) JSONObject.toBean(object, ActivityConfigureBean.class);
            System.out.println("activity=" + bean.getActivity());
            list.add(bean);
            for (int j = 0; j < bean.getScale(); j++) {
                activitys.add(bean.getActivity());
            }
            activityPaths[i] = pakageName + "/." + bean.getActivity();
            System.out.println("bean.activity=" + bean.getActivity());
        }

        util = new MonkeyRunnerUtil.Builder().pakageName(pakageName).pythonPath(pythonPath).runnerPath(runnerPath).activityPaths(activityPaths).build();


        Timer timer = new Timer();
        timer.schedule(new MyTask(), 1000, 5000);   //5秒一次监听跳出
        timer.schedule(new MyTask1(), 1000, totalTime / activityPaths.length);  //指定时间间隔跳转指定页面 时间间隔由配置的页面数确定

//        util.startActivityByJava("3483170");
//        util.startActivityByPython(util.runnerPath,util.pythonPath);
//        System.out.println("isAlive="+util.watchActivity());

    }

    static String[] activityPaths = new String[1];

    static class MyTask extends java.util.TimerTask {
        public void run() {
//            int which = new Random().nextInt(activitys.size());
            System.out.println("check out");
//            activityPaths[0] = pakageName + "/." + activitys.get(which);
//            util.setActivityPaths(activityPaths);
            util.checkForStartActivity();   //监听跳出则跳转指定的页面
        }
    }

    static class MyTask1 extends java.util.TimerTask {
        public void run() {
            int which = new Random().nextInt(activitys.size());   //随机取数 以达到不同的覆盖比例
            System.out.println("force jump activity:" + activitys.get(which));
            activityPaths[0] = pakageName + "/." + activitys.get(which);
            util.setActivityPaths(activityPaths);
            util.forceJumpActivity();
        }
    }
}
