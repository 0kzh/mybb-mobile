package com.polunom.hfmobile;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.polunom.hfmobile.userprofile.ReputationFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.polunom.hfmobile.userprofile.ReputationFragment.REPUTATIONS;

public class ReputationActivity extends AppCompatActivity implements GiveRepDialog.RepDialogListener{

    private View mProgressView, mHolderView;
    public String currPage;
    private String returnToPage;
    private ReputationFragment repFrag;
    private ArrayList<String> reps;
    private String returnToUrl;
    public int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reputation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.pToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = getIntent().getIntExtra("USER_ID", 0);
        mProgressView = findViewById(R.id.rep_progress);
        mHolderView = findViewById(R.id.repAppbar);
        HFBrowser browser = (HFBrowser) getApplicationContext();
        returnToPage = browser.getWebViewUrl();

        new initReputation(id).execute();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.giveRep);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reps = new ArrayList<>();
                new prepareRep().execute();
            }
        });
    }

    @Override
    public void onDialogPositiveClick(String message, String rep) {
        String repValue = message.split("\\(")[1].split("\\)")[0];
        new giveRep(Integer.parseInt(repValue.replace("+", "")), message).execute();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HFBrowser browser = (HFBrowser) getApplicationContext();
        Log.e("asdf", "reputation -> " + returnToPage);
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
        int id = item.getItemId();

        if(id == android.R.id.home) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            Log.e("asdf", "reputation -> " + returnToPage);
            browser.setUrl(returnToPage);
            finish();
        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class initReputation extends AsyncTask<Void, Void, Void> {

        int userID;

        initReputation(int i){
            userID = i;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("https://hackforums.net/reputation.php?uid="+userID);
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
            currPage = ((HFBrowser) getApplicationContext()).getWebViewUrl();
            repFrag = new ReputationFragment();
            repFrag.REPUTATIONS.clear();
            setupFragment(repFrag);
            showProgress(false);
        }
    }

    public class prepareRep extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            returnToUrl = browser.getWebViewUrl();
            browser.loadUrl("https://hackforums.net/reputation.php?action=add&uid="+id);
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
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);
            browser.setUrl(returnToUrl);

            if (browser.html != null){
                Elements choices = doc.select("#reputation option");
                for(Element e : choices){
                    reps.add(e.text());
                }
                if(!browser.html.contains("You have already given as many reputation ratings as you are allowed to for today.")) {
                    String message = doc.select("input[name=\"comments\"]").val();
                    String selectedRep = doc.select("option[selected=\"selected\"]").text();
                    Bundle args = new Bundle();
                    args.putStringArrayList("reps", reps);
                    args.putString("message", message);
                    args.putString("prevRep", selectedRep);
                    GiveRepDialog dialog = new GiveRepDialog();
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "RepDialog");
                }else{
                    AlertDialog alertDialog = new AlertDialog.Builder(ReputationActivity.this).create();
                    alertDialog.setTitle("Error giving reputation");
                    alertDialog.setMessage("You have already given as many reputation ratings as you are allowed to for today.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        }
    }
    public class giveRep extends AsyncTask<Integer, Void, Void> {

        int rep;
        String message;

        giveRep(int rep, String message){
            this.rep = rep;
            this.message= message;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("javascript:document.getElementById('reputation').value = '" + rep + "';" +
                            "document.getElementsByName('comments')[0].value='" + message + "';" +
                            "document.getElementsByClassName('button')[0].click();");
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
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String s = browser.html;
            if (browser.html != null){
                if(s.contains("Your reputation rating has successfully been added for this user.")){
                    Toast.makeText(ReputationActivity.this, "Reputation added!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(ReputationActivity.this, "Something went wrong.", Toast.LENGTH_LONG).show();
                }
            }
            Log.e("asdf", "give rep -> " + returnToUrl);
            browser.setUrl(returnToUrl);
        }
    }

    protected void setupFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_rep, fragment);
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
            REPUTATIONS.remove(REPUTATIONS.size()-1); //remove progress spinner after load
            repFrag.listRep();
        }
    }

    public void nextPage(String url){
        new navigate(url).execute();
        currPage = "https://hackforums.net/"+url;
    }
}
