package com.polunom.hfmobile;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.polunom.hfmobile.pmdisplay.PMDisplayFragment;
import com.polunom.hfmobile.threaddisplay.PostDisplayFragment;

import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;
import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.THREADS;
import static com.polunom.hfmobile.pmdisplay.PMDisplayFragment.PM;
import static com.polunom.hfmobile.threaddisplay.PostDisplayFragment.POSTS;

public class PMDisplayActivity extends AppCompatActivity{

    private int id;
    private String returnToPage;
    private View mProgressView, mHolderView;
    private PMDisplayFragment pdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgressView = findViewById(R.id.pm_progress);
        mHolderView = findViewById(R.id.pmCoordLayout);

        id = getIntent().getIntExtra("PM_ID", 0);
        String name = getIntent().getStringExtra("PM_NAME");
        returnToPage = getIntent().getStringExtra("PM_PAGE");

        new initPM().execute();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //after reply is posted
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //if async task was cancelled due to editor call
                if(PM == null){
                    Log.e("asdf", "cancelled");
                    new initPM().execute();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    public void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mHolderView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        HFBrowser browser = (HFBrowser) getApplicationContext();
        Log.e("asdf", "pm -> " + returnToPage);
        browser.setUrl(returnToPage);
        //TODO: Add purple PMs
//        for(ForumThread t : THREADS){
//            if(t.id == id){
//                if(t.icon.contains("new")){
//                    t.icon = t.icon.replace("new", "");
//                }else if(t.icon.equals("https://hackforums.net/images/modern_bl/dot_folder.gif")){
//                    t.icon = "https://hackforums.net/images/modern_bl/folder.gif";
//                }
//            }
//        }
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            //return to forumdisplay.php
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.setUrl(returnToPage);
            //TODO: Add purple PMs
//            for(ForumThread t : THREADS){
//                if(t.id == id){
//                    if(t.icon.contains("new")){
//                        t.icon = t.icon.replace("new", "");
//                    }else if(t.icon.equals("https://hackforums.net/images/modern_bl/dot_folder.gif")){
//                        t.icon = "https://hackforums.net/images/modern_bl/folder.gif";
//                    }
//                }
//            }
            finish();

        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class initPM extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/private.php?action=read&pmid="+id);
            browser.html = null;
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Integer... ints) {
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
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                String s = browser.html;
                POSTS.clear();

                Bundle args = new Bundle();
                args.putInt("PM_ID", id);
                pdf = new PMDisplayFragment();
                pdf.setArguments(args);
                setupFragment(pdf);
                showProgress(false);
            }catch(IllegalArgumentException exp){
                //occurs when refreshing and scrolling down at same time
            }
        }
    }

    protected void setupFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_pm, fragment);
            fragmentTransaction.commit();
        }catch(Exception ex){

        }
    }
}