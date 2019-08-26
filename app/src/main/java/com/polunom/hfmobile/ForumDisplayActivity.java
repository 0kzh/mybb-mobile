package com.polunom.hfmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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

import com.polunom.hfmobile.forumdisplay.ForumRecyclerViewAdapter;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.THREADS;

public class ForumDisplayActivity extends AppCompatActivity
                                  implements ThreadDisplayFragment.OnForumInteractionListener{
    private int id;
    public String currPage;
    private ThreadDisplayFragment tdf;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String returnToPage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        id = getIntent().getIntExtra("FORUM_ID", 0);
        String name = getIntent().getStringExtra("FORUM_NAME");
        HFBrowser browser = (HFBrowser) getApplicationContext();
        returnToPage = browser.getWebViewUrl();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new initForum(id, true).execute();
            }
        });

        new initForum(id, false).execute();
        currPage = "https://hackforums.net/forumdisplay.php?fid="+id;

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newThread);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ForumDisplayActivity.this, EditorActivity.class);
                i.putExtra("ACTION", "thread");
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
                if(THREADS == null || THREADS.isEmpty()){
                    new initForum(id, false).execute();
                    currPage = "https://hackforums.net/forumdisplay.php?fid="+id;
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
            Log.e("asdf", "forum -> " + returnToPage);
            browser.setUrl(returnToPage);
            finish();
        } else if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            new initForum(this.id, true).execute();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HFBrowser browser = (HFBrowser) getApplicationContext();
        Log.e("asdf", "forum -> " + returnToPage);
        browser.setUrl(returnToPage);
    }

    public void updateList(){
        try{
            //if returning from thread, update as read
            tdf.mAdapter.notifyDataSetChanged();
            tdf.v.invalidate();
        }catch(Exception e){

        }
    }

    @Override
    public void onListFragmentInteraction(ForumThread t, boolean lastpost) {
        Intent i = new Intent(ForumDisplayActivity.this, ThreadDisplayActivity.class);
        i.putExtra("THREAD_ID", t.getId());
        i.putExtra("THREAD_NAME", t.getName());
        i.putExtra("POST_ID", -1);
        i.putExtra("FORUM_PAGE", currPage);
        if (lastpost) {
            i.putExtra("PAGE", -1);
        } else {
            i.putExtra("PAGE", 1);
        }

        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    public class initForum extends AsyncTask<Integer, Void, Void> {

        private final int id;
        private final boolean scrollToTop;
        initForum(Integer i, Boolean b) {
            id = i;
            scrollToTop = b;
        }

        @Override
        protected void onPreExecute(){
            try {
                if (scrollToTop) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    double sizePerChild = tdf.v.getHeight() / 6;
                    double y = (tdf.v.getAdapter().getItemCount() * sizePerChild) / dm.ydpi;
                    float msPerInches = (float) (1 / (y / 500));
                    CustomLinearLayoutManager.MILLISECONDS_PER_INCH = msPerInches;

                    tdf.v.smoothScrollToPosition(0);
                }
            }catch(NullPointerException ex){

            }
            mSwipeRefreshLayout.setRefreshing(true);
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/forumdisplay.php?fid=" + id);
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
                HFBrowser browser = (HFBrowser) getApplicationContext();
                String s = browser.html;
                Document doc = Jsoup.parse(s);
                String name = doc.select("#panel a[href^=\"https://hackforums.net/member.php?action=profile\"]").text();

                THREADS.clear();

                Bundle args = new Bundle();
                args.putString("ACTION", "forum");
                tdf = new ThreadDisplayFragment();
                tdf.setArguments(args);
                setupFragment(tdf);
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
            fragmentTransaction.replace(R.id.frame_threads, fragment);
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
            THREADS.remove(THREADS.size()-1); //remove progress spinner after load
            tdf.listThreads();
        }
    }

    public void nextPage(String url){
        new navigate(url).execute();
        currPage = "https://hackforums.net/"+url;
    }

}
