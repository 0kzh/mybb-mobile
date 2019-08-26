package com.polunom.hfmobile;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.polunom.hfmobile.threaddisplay.PostDisplayFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static com.polunom.hfmobile.threaddisplay.PostDisplayFragment.POSTS;
import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.THREADS;
import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;

public class ThreadDisplayActivity extends AppCompatActivity
        implements PostDisplayFragment.OnFooterInteractionListener{

    private int id, page, postId;
    private String returnToPage;
    private PostDisplayFragment pdf;
    public static SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        id = getIntent().getIntExtra("THREAD_ID", 0);
        postId = getIntent().getIntExtra("POST_ID", 0);
        String name = getIntent().getStringExtra("THREAD_NAME");
        page = getIntent().getIntExtra("PAGE", 1);
        returnToPage = getIntent().getStringExtra("FORUM_PAGE");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                HFBrowser browser= (HFBrowser) getApplicationContext();
                String url = browser.getUrl().replace("https://hackforums.net/", "");
                if(url.contains("lastpost")){
                    url = url.replace("&action=lastpost", "&page="+pdf.pages);
                }

                nextPage(url);
            }
        });

        new initThread(id, page, false).execute();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newReply);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ThreadDisplayActivity.this, EditorActivity.class);
                i.putExtra("ACTION", "reply");
                startActivityForResult(i, 1);
            }
        });
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
            if (resultCode == Activity.RESULT_OK) {
                //go to last post
                HFBrowser browser = (HFBrowser) getApplicationContext();
                String url = browser.getWebViewUrl().replace("https://hackforums.net/", "");
                url = url.split("pid=")[0] + "action=lastpost";
                nextPage(url);
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //if async task was cancelled due to editor call
                if(POSTS == null || POSTS.isEmpty()){
                    new initThread(id, 1, false).execute();
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

    @Override
    public void onBackPressed() {
        HFBrowser browser = (HFBrowser) getApplicationContext();
        Log.e("asdf", "thread -> " + returnToPage);
        browser.setUrl(returnToPage);
        for(ForumThread t : THREADS){
            if(t.id == id){
                if(t.icon.contains("new")){
                    t.icon = t.icon.replace("new", "");
                }else if(t.icon.equals("https://hackforums.net/images/modern_bl/dot_folder.gif")){
                    t.icon = "https://hackforums.net/images/modern_bl/folder.gif";
                }
            }
        }
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
            for(ForumThread t : THREADS){
                if(t.id == id){
                    if(t.icon.contains("new")){
                        t.icon = t.icon.replace("new", "");
                    }else if(t.icon.equals("https://hackforums.net/images/modern_bl/dot_folder.gif")){
                        t.icon = "https://hackforums.net/images/modern_bl/folder.gif";
                    }
                }
            }
            finish();

        } else if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            HFBrowser browser= (HFBrowser) getApplicationContext();
            String url = browser.getUrl().replace("https://hackforums.net/", "");
            if(url.contains("lastpost")){
                url = url.replace("&action=lastpost", "&page="+pdf.pages);
            }

            nextPage(url);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(String action) {
        HFBrowser browser = (HFBrowser) getApplicationContext();
        String url = browser.getUrl().replace("https://hackforums.net/", "");
        if(url.contains("lastpost")){
            url = url.replace("&action=lastpost", "&page="+pdf.pages);
        }

        int currPage = 1;
        if(url.contains("&page=")){
            currPage = Integer.parseInt(url.split("&page=")[1]);
        }
        if(action.equals("next")){
            if(currPage < pdf.pages) {
                nextPage(url.replace("&page=" + currPage, "&page=" + (currPage + 1)));
            }
        }else if(action.equals("prev")){
            if(currPage > 1) {
                nextPage(url.replace("&page=" + currPage, "&page=" + (currPage - 1)));
            }
        }else if(action.equals("jump")){
//            if(currPage > 1) {
//                nextPage(url.replace("&page=" + currPage, "&page=" + (currPage - 1)));
//            }
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Enter page (1-"+pdf.pages+")");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setRawInputType(Configuration.KEYBOARD_12KEY);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Put actions for OK button here
                    int page = Integer.parseInt(input.getText().toString());
                    if(!(page < 1 || page > pdf.pages)){
                        nextPage("jumpto&page="+page);
                    }else{
                        Toast.makeText(getApplicationContext(), "Out of page range", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do nothing
                }
            });
            alert.show();
        }else if(action.equals("last")){
            nextPage(url.replace("&page=" + currPage, "&action=lastpost"));
        }else if(action.equals("first")){
            nextPage(url.replace("&page=" + currPage, "&page=1"));
        }else {
            //other action, new reply
            Intent i = new Intent(ThreadDisplayActivity.this, EditorActivity.class);
            i.putExtra("ACTION", "replyfrom_" + action);
            startActivityForResult(i, 1);
        }
    }

    public class initThread extends AsyncTask<Integer, Void, Void> {

        private final int id, page;
        private final boolean scrollToTop;
        initThread(Integer i, Integer p, Boolean b1) {
            id = i;
            page = p;
            scrollToTop = b1;
        }

        @Override
        protected void onPreExecute(){
            mSwipeRefreshLayout.setRefreshing(true);
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if(page == -1){
                browser.loadUrl("https://hackforums.net/showthread.php?tid=" + id + "&action=lastpost");
            }else{
                browser.loadUrl("https://hackforums.net/showthread.php?tid=" + id + "&page=" + page);
            }
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
                POSTS.clear();

                Bundle args = new Bundle();
                args.putInt("POST_ID", postId);
                pdf = new PostDisplayFragment();
                pdf.setArguments(args);
                setupFragment(pdf);
                mSwipeRefreshLayout.setRefreshing(false);
            }catch(IllegalArgumentException exp){
                //occurs when refreshing and scrolling down at same time
            }
        }
    }

    protected void setupFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_posts, fragment);
            fragmentTransaction.commit();
        }catch(Exception ex){

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
//            POSTS.remove(POSTS.size()-1);
            POSTS.clear();
            pdf.listPosts();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void nextPage(String url){
        mSwipeRefreshLayout.setRefreshing(true);
        if(url.contains("jumpto")){
            HFBrowser browser= (HFBrowser) getApplicationContext();
            String temp = browser.getUrl().replace("https://hackforums.net/", "");
            if(temp.contains("lastpost")){
                temp = temp.replace("&action=lastpost", "&page="+pdf.pages);
            }

            url = url.replace("jumpto", temp.split("&page=")[0]);
        }
        new navigate(url).execute();
    }

}