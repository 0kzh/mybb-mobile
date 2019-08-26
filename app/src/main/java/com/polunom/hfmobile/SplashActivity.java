package com.polunom.hfmobile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.polunom.hfmobile.notifications.CheckForPMs;
import com.polunom.hfmobile.notifications.NotificationJobScheduler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new checkLoggedIn().execute();
    }

    public class checkLoggedIn extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/member.php?action=login");
            browser.html = null;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String s = browser.html;
//            int maxLogSize = 1000;
//            for(int i = 0; i <= s.length() / maxLogSize; i++) {
//                int start = i * maxLogSize;
//                int end = (i+1) * maxLogSize;
//                end = end > s.length() ? s.length() : end;
//                Log.e("asdf", s.substring(start, end));
//            }
            Document doc = Jsoup.parse(s);
            if(doc.html().contains("You are already currently logged in")){
                browser.loggedIn = true;
                Intent i = new Intent(SplashActivity.this, MainActivity.class);

                Intent alarmIntent = new Intent(SplashActivity.this, CheckForPMs.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(SplashActivity.this, 0, alarmIntent, 0);
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);

                finish();
                startActivity(i);
            }else{
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                finish();
                startActivity(i);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        HFBrowser.activityResumed();
        overridePendingTransition(0,0);
    }

    @Override
    public void onPause() {
        super.onPause();
        HFBrowser.activityPaused();
        overridePendingTransition(0,0);
    }
}