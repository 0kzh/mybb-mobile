package com.polunom.hfmobile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment;
import com.polunom.hfmobile.userprofile.AboutFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;
import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.USERTHREADS;
import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.USERPOSTS;

public class ProfileActivity extends AppCompatActivity
        implements ThreadDisplayFragment.OnForumInteractionListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView headName;
    private View mProgressView, mHolderView;
    private ViewPagerAdapter mAdapter;
    private String currPage;
    public String threadsHTML, postsHTML, aboutHTML;
    public String threadsURL, postsURL;
    private String returnToPage;
    private ThreadDisplayFragment posts, threads;
    public int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.pToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = getIntent().getIntExtra("USER_ID", 0);
        mProgressView = findViewById(R.id.user_progress);
        mHolderView = findViewById(R.id.appbar);
        HFBrowser browser = (HFBrowser) getApplicationContext();
        returnToPage = browser.getUrl();

        new initProfile(id).execute();

        AppBarLayout scrollView = (AppBarLayout) findViewById(R.id.appbar);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
        params.setBehavior(new NestedScrollViewBehavior());
    }

    @Override
    public void onListFragmentInteraction(ForumThread t, boolean lastpost) {
        Intent i = new Intent(ProfileActivity.this, ThreadDisplayActivity.class);
        i.putExtra("THREAD_ID", t.getId());
        i.putExtra("THREAD_NAME", t.getName());
        i.putExtra("FORUM_PAGE", "1");
        if(t.action.equals("thread")) {
            i.putExtra("POST_ID", -1);
        }else{
            i.putExtra("POST_ID", Integer.parseInt(t.action));
        }
        if (lastpost) {
            i.putExtra("PAGE", -1);
        } else {
            i.putExtra("PAGE", 1);
        }

        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(0, 0);
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

    public void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mHolderView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HFBrowser browser = (HFBrowser) getApplicationContext();
        Log.e("asdf", "profile -> " + returnToPage);
        browser.setUrl(returnToPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            Log.e("asdf", "profile -> " + returnToPage);
            browser.setUrl(returnToPage);
            finish();
        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void setupViewPager(ViewPager viewPager) {
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        viewPager.setAdapter(adapter);
//    }

    private ViewPagerAdapter setupFragment(int index, ViewPager viewPager, ViewPagerAdapter adapter){
        if(index == 0){
            Bundle args = new Bundle();
            args.putString("USER_ID", String.valueOf(id));
            Fragment about = new AboutFragment();
            about.setArguments(args);
            adapter.addFragment(about, "");
        }else if(index == 1){
            Bundle args = new Bundle();
            args.putString("ACTION", "threads");
            threads = new ThreadDisplayFragment();
            threads.setArguments(args);
            threads.USERTHREADS.clear();
            adapter.addFragment(threads, "");
        }else if(index == 2){
            Bundle args = new Bundle();
            args.putString("ACTION", "posts");
            posts = new ThreadDisplayFragment();
            posts.setArguments(args);
            posts.USERPOSTS.clear();
            adapter.addFragment(posts, "");
        }
        return adapter;
    }

    public class initProfile extends AsyncTask<Void, Void, Void> {

        int userID;

        initProfile(int i){
            userID = i;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/member.php?action=profile&uid="+userID);
            browser.html = null;
            showProgress(true);
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
            aboutHTML = browser.html;
            Document doc = Jsoup.parse(aboutHTML);
            String name = doc.select("#panel a[href^=\"https://hackforums.net/member.php?action=profile\"]").text();
            viewPager = (ViewPager) findViewById(R.id.pViewpager);
            mAdapter = setupFragment(0, viewPager, new ViewPagerAdapter(getSupportFragmentManager()));
            new findThreads(id).execute();
        }
    }
    public class findThreads extends AsyncTask<Void, Void, Void> {

        int userID;

        findThreads(int i){
            userID = i;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/search.php?action=finduserthreads&uid="+userID);
            browser.html = null;
            showProgress(true);
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
            threadsHTML = browser.html;
            threadsURL = browser.getWebViewUrl();
            viewPager = (ViewPager) findViewById(R.id.pViewpager);
            mAdapter = setupFragment(1, viewPager, mAdapter);
            new findPosts(id).execute();
        }
    }
    public class findPosts extends AsyncTask<Void, Void, Void> {

        int userID;

        findPosts(int i){
            userID = i;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/search.php?action=finduser&uid="+userID);
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
            postsHTML = browser.html;
            postsURL = browser.getWebViewUrl();
            viewPager = (ViewPager) findViewById(R.id.pViewpager);
            mAdapter = setupFragment(2, viewPager, mAdapter);

            viewPager.setAdapter(mAdapter);
            tabLayout = (TabLayout) findViewById(R.id.profileOptions);
            tabLayout.setupWithViewPager(viewPager);
//            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_posts));
            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_pm));
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_profile);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_threads);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_posts);

            showProgress(false);
        }
    }

    public class navigate extends AsyncTask<Void, Void, Void> {

        private final String url, action;
        navigate(String action, String url) {
            this.url = url;
            this.action = action;
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
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if(action.equals("threads")){
                threadsHTML = browser.html;
                threadsURL = browser.getWebViewUrl();
                USERTHREADS.remove(USERTHREADS.size()-1);
                threads.listUserThreads();
            }else if(action.equals("posts")){
                postsHTML = browser.html;
                postsURL = browser.getWebViewUrl();
                USERPOSTS.remove(USERPOSTS.size()-1);
                posts.listUserPosts();
            }
        }
    }

    public void nextPage(String action, String url){
        new navigate(action, url).execute();
        currPage = "https://hackforums.net/"+url;
    }
}
