package com.polunom.hfmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polunom.hfmobile.flist.TabFragment;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabFragment.OnForumInteractionListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView headName;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("HackForums");

        new initIndex().execute();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setBackgroundColor(Color.rgb(51, 51, 51));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        String[] tabs = {"General", "Hack", "Tech", "Code", "Game", "Groups", "Web", "GFX", "Market", "Money"};

        for(int i = 0; i < tabs.length; i++){
            Bundle args = new Bundle();
            args.putString("name", tabs[i]);
            Fragment frag = new TabFragment();
            frag.setArguments(args);
            adapter.addFragment(frag, tabs[i]);
        }

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onListFragmentInteraction(TabFragment.Forum f) {
        Intent i = new Intent(MainActivity.this, ForumDisplayActivity.class);
        i.putExtra("FORUM_ID", f.getId());
        i.putExtra("FORUM_NAME", f.getName());
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(0,0);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_pm) {
            Intent i = new Intent(MainActivity.this, InboxDisplayActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_my_threads) {

        } else if (id == R.id.nav_my_posts) {

        } else if (id == R.id.nav_new_posts) {

        } else if (id == R.id.nav_user_cp) {

        } else if (id == R.id.nav_search) {

        } else if (id == R.id.nav_members) {

        } else if (id == R.id.nav_logout) {
            new doLogOut().execute();
        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class initIndex extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/index.php");
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
            Document doc = Jsoup.parse(s);
            String name = doc.select("#panel a[href^=\"https://hackforums.net/member.php?action=profile\"]").text();

            LinearLayout parent = (LinearLayout) headName.getParent();

            //fix annoying status bar overlapping nav header
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) parent.getLayoutParams();
                params.setMargins(0, getStatusBarHeight(), 0, 0);
                parent.setLayoutParams(params);
            }

            headName.setTypeface(null, Typeface.BOLD);
            headName.setText(name);
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            setupViewPager(viewPager);

            tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    public class doLogOut extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/index.php");
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
            Document doc = Jsoup.parse(s);
            String link = doc.select("a[onclick=\"return confirm('Are you sure you want to logout?');\"]").attr("href");
            new verifyLogOut(link).execute();
        }
    }
    public class verifyLogOut extends AsyncTask<String, Void, Void> {

        private final String link;
        verifyLogOut(String i) {
            link = i;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl(link);
            browser.html = null;
        }

        @Override
        protected Void doInBackground(String... strings) {
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
            Document doc = Jsoup.parse(s);
            if(doc.html().contains("Hello There, Guest!")){
                //logged out
                browser.loggedIn = false;
                Intent i = new Intent(MainActivity.this, SplashActivity.class);
                finish();
                startActivity(i);
            }
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
