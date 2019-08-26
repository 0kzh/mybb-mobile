package com.polunom.hfmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment;
import com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment.Message;
import com.polunom.hfmobile.pmdisplay.PMDisplayFragment;

import static com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment.MESSAGES;

public class InboxDisplayActivity extends AppCompatActivity
                                  implements InboxDisplayFragment.OnPMInteractionListener{

    public String currPage;
    private InboxDisplayFragment idf;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String returnToPage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        HFBrowser browser = (HFBrowser) getApplicationContext();
        returnToPage = browser.getWebViewUrl();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new initInbox(true).execute();
            }
        });

        new initInbox(false).execute();
        currPage = "https://hackforums.net/private.php?fid=1/";

        getSupportActionBar().setTitle("Private Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newPM);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(InboxDisplayActivity.this, EditorActivity.class);
                i.putExtra("ACTION", "pm");
                startActivityForResult(i, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if cancelled
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //if async task was cancelled due to editor call
                if(MESSAGES == null || MESSAGES.isEmpty()){
                    new initInbox(false).execute();
                    currPage = "https://hackforums.net/private.php?fid=1/";
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            Log.e("asdf", "inbox -> " + returnToPage);
            browser.setUrl(returnToPage);
            finish();
        } else if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            new initInbox(true).execute();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HFBrowser browser = (HFBrowser) getApplicationContext();
        Log.e("asdf", "inbox -> " + returnToPage);
        browser.setUrl(returnToPage);
    }

    public void updateList(){
        try{
            //if returning from thread, update as read
            idf.getAdapter().notifyDataSetChanged();
            idf.v.invalidate();
        }catch(Exception e){

        }
    }

    @Override
    public void onListFragmentInteraction(Message t) {
        Intent i = new Intent(InboxDisplayActivity.this, PMDisplayActivity.class);
        i.putExtra("PM_ID", t.getId());
        i.putExtra("PM_NAME", t.getName());
        i.putExtra("PM_PAGE", currPage);

        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    public class initInbox extends AsyncTask<Integer, Void, Void> {

        private final boolean scrollToTop;
        initInbox(Boolean b) {
            scrollToTop = b;
        }

        @Override
        protected void onPreExecute(){
            try {
                if (scrollToTop) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    double sizePerChild = idf.v.getHeight() / 6;
                    double y = (idf.v.getAdapter().getItemCount() * sizePerChild) / dm.ydpi;
                    float msPerInches = (float) (1 / (y / 500));
                    CustomLinearLayoutManager.MILLISECONDS_PER_INCH = msPerInches;

                    idf.v.smoothScrollToPosition(0);
                }
            }catch(NullPointerException ex){

            }
            mSwipeRefreshLayout.setRefreshing(true);
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/private.php?fid=1/");
            browser.html = null;
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
                MESSAGES.clear();
                idf = new InboxDisplayFragment();
                setupFragment(idf);
                mSwipeRefreshLayout.setRefreshing(false);
            }catch(IllegalArgumentException exp){
                //occurs when refreshing and scrolling down at same time
            }
        }
    }

    protected void setupFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_messages, fragment);
            fragmentTransaction.commit();
        }catch(IllegalStateException ex){
            //happens when asynctask isn't finished before user exits activity
        }
    }

    public class navigate extends AsyncTask<Void, Void, Void> {

        private final String url;
        navigate(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/"+url);
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
            MESSAGES.remove(MESSAGES.size()-1); //remove progress spinner after load
            idf.listMessages();
        }
    }

    public void nextPage(String url){
        new navigate(url).execute();
        currPage = "https://hackforums.net/"+url;
    }

}
