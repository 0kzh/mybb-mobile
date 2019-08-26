package com.polunom.hfmobile.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.PMDisplayActivity;
import com.polunom.hfmobile.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static java.security.AccessController.getContext;

public class CheckForPMs extends BroadcastReceiver {
    private Context c;
    private String returnTo;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        HFBrowser browser = (HFBrowser) context.getApplicationContext();
        String s = browser.html;
        returnTo = browser.getUrl();
        if(!browser.isActivityVisible()) {
            new checkPMs(context).execute();
        }else{
            if(s!= null){
                parsePMs(context, s);
            }
        }
        new NotificationJobScheduler(context).start(context);
    }

    public class checkPMs extends AsyncTask<Void, Void, Void> {

        private final Context context;

        checkPMs(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            HFBrowser browser = (HFBrowser) context.getApplicationContext();
            browser.loadUrl("https://hackforums.net/member.php?action=login&pm");
            browser.html = null;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HFBrowser browser = (HFBrowser) context.getApplicationContext();
                while (browser.html == null) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) context.getApplicationContext();
            String s = browser.html;
            parsePMs(context, s);
            //set page back
            browser.setUrl(returnTo);
        }
    }

    private void parsePMs(Context context, String html) {
        Document doc = Jsoup.parse(html);
        Element pmNotice = doc.select("#pm_notice").first();
        if (pmNotice != null) {
            //pm exists!
            String number = pmNotice.text().split("You have ")[1].split(" unread")[0];
            String from = pmNotice.text().split("from ")[1].split(" titled")[0];
            String title = pmNotice.text().split("titled ")[1];
            int id = Integer.parseInt(pmNotice.select("a[href*=\"action=read\"]").attr("href").split("pmid=")[1]);

            int num;
            if (number.equals("one")) {
                num = 1;
            } else {
                num = Integer.parseInt(number);
            }

            Log.e("asdf", num + " unread");

            //get shared preferences
            SharedPreferences sharedPref = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
            if (sharedPref.contains("unread_pms")) {
                int unreadPms = sharedPref.getInt("unread_pms", Context.MODE_PRIVATE);
                if (num > unreadPms) {
                    //make notification
                    Intent notificationIntent = new Intent(context, PMDisplayActivity.class);
                    notificationIntent.putExtra("PM_ID", id);
                    notificationIntent.putExtra("PM_NAME", title);
                    notificationIntent.putExtra("PM_PAGE", 1);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,
                            0, notificationIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);

                    NotificationManager nm = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);

                    Resources res = context.getResources();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                    builder.setContentIntent(contentIntent)
                            .setSmallIcon(R.drawable.logo)
                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.logo))
                            .setTicker(title)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle("New PM from " + from)
                            .setContentText(title);
                    Notification n = builder.getNotification();

                    n.defaults |= Notification.DEFAULT_ALL;
                    nm.notify(0, n);
                }
            }

            //set curr num as new unread count
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("unread_pms", num);
            editor.commit();
        }
    }
}