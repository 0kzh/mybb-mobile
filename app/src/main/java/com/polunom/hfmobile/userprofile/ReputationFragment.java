package com.polunom.hfmobile.userprofile;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polunom.hfmobile.CustomLinearLayoutManager;
import com.polunom.hfmobile.EndlessScrollListener;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.ReputationActivity;
import com.polunom.hfmobile.CustomViewDivider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ReputationFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    public static final List<Reputation> REPUTATIONS = new ArrayList<>();
    public RepRecyclerViewAdapter mAdapter;
    public RecyclerView v;
    private int mCurrPage, mLastPage;
    private CustomLinearLayoutManager layoutManager;
    private EndlessScrollListener listener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReputationFragment() {
    }

    public static ReputationFragment newInstance(int columnCount) {
        ReputationFragment fragment = new ReputationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupHeader();
        listRep();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reputation_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {

            v = (RecyclerView) view;

            //custom divider that doesn't include progressbar and footer
            CustomViewDivider mDividerItemDecoration = new CustomViewDivider(view.getContext(), Color.rgb(102,102,102), 1);
            v.addItemDecoration(mDividerItemDecoration);

            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            layoutManager = new CustomLinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new RepRecyclerViewAdapter(REPUTATIONS);
            recyclerView.setAdapter(mAdapter);
            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.giveRep);
            listener = new EndlessScrollListener((CustomLinearLayoutManager) recyclerView.getLayoutManager(), fab) {
                @Override
                public void onLoadMore(int current_page) {
                    try {
                        if(mCurrPage < mLastPage) {
                            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
                            String url = browser.getUrl().replace("https://hackforums.net/", "");

                            if (!url.contains("&page=")) {
                                //currently on first page
                                ((ReputationActivity) getActivity()).nextPage(url + "&page=2");
                            } else {
                                //get current page and go to next
                                int currPage = Integer.parseInt(url.split("&page=")[1]);
                                ((ReputationActivity) getActivity()).nextPage(url.replace("&page=" + currPage, "&page=" + (currPage + 1)));
                            }
                        }else{
                            new Handler().post(new Runnable() {
                                public void run() {
                                    setLoading(false);
                                }
                            });
                        }
                    }catch(IllegalArgumentException exp){
                        //occurs when refreshing and scrolling down
                    }
                }
            };
            recyclerView.setOnScrollListener(listener);
        }
        return view;
    }

    private void setupHeader(){
        HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
        String s = browser.html;
        Document doc = Jsoup.parse(s);

        Element e = doc.select("table[width=\"100%\"]").get(0);

        String username = e.select(".largetext").text();
        String group = e.select(".largetext span").attr("class");

        Element stats = e.select(".smalltext").get(0);
        String usertitle = stats.html().split("<br>")[0];
        ((TextView) getActivity().findViewById(R.id.repUsertitle)).setText(usertitle);

        TextView user = (TextView) getActivity().findViewById(R.id.repUser);
        if(group.equals("group7")){
            //banned
            username = "<font color=#000000>"+username+"</font>";
        }else if(group == null || group.isEmpty() || group.equals("null")){
            //closed, not in a group
            username = "<font color=#555555>"+username+"</font>";
        }else if(group.equals("group2")){
            //normal user
            username = "<font color=#EFEFEF>"+username+"</font>";
        }else if(group.equals("group29")){
            //Ub3r
            username = "<font color=#00AAFF>"+username+"</font>";
        }else if(group.equals("group9")){
            //L33t
            username = "<font color=#99FF00>"+username+"</font>";
        }else if(group.equals("group3")){
            //Staff
            username = "<font color=#9999FF>"+username+"</font>";
        }else if(group.equals("group4")){
            //Admin
            username = "<font color=#FF66FF>"+username+"</font>";
        }else {
            //Custom usergroup
            username = "<font color=#FFFFFF>"+username+"</font>";
        }

        String positives = stats.select("a[href*=\"positive\"]").text();
        String negatives = stats.select("a[href*=\"negative\"]").text();
        String neutrals = stats.select("a[href*=\"neutral\"]").text();

        ((TextView) getActivity().findViewById(R.id.positives)).setText(positives);
        if(positives.equals("1")) {
            ((TextView) getActivity().findViewById(R.id.positives1)).setText("positive");
        }

        ((TextView) getActivity().findViewById(R.id.negatives)).setText(negatives);
        if(negatives.equals("1")) {
            ((TextView) getActivity().findViewById(R.id.negatives1)).setText("negative");
        }

        ((TextView) getActivity().findViewById(R.id.neutrals)).setText(neutrals);
        if(neutrals.equals("1")) {
            ((TextView) getActivity().findViewById(R.id.neutrals1)).setText("neutral");
        }

        //total rep
        int totalRep = Integer.parseInt(e.select(".repbox").text());
        TextView repBox = (TextView) getActivity().findViewById(R.id.repNumber);
        GradientDrawable rect = (GradientDrawable) repBox.getBackground();
        String rep;

        if(totalRep > 0){
            rep = "<b><font color=#32CD32>"+totalRep+"</font></b>";
            rect.setStroke(1, Color.rgb(50, 205, 50));
        }else if(totalRep < 0){
            rep = "<b><font color=#CC3333>"+totalRep+"</font></b>";
            rect.setStroke(1, Color.rgb(204, 51, 51));
        }else{
            rep = "<b><font color=#666666>"+totalRep+"</font></b>";
            rect.setStroke(1, Color.rgb(102, 102, 102));
        }

        if (Build.VERSION.SDK_INT >= 24) {
            user.setText(Html.fromHtml(username, Html.FROM_HTML_MODE_LEGACY));
            repBox.setText(Html.fromHtml(rep, Html.FROM_HTML_MODE_LEGACY));
        } else {
            user.setText(Html.fromHtml(username));
            repBox.setText(Html.fromHtml(rep));
        }
    }

    public void listRep(){
        try {
            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);

            try {
                mLastPage = Integer.parseInt(doc.select(".pages").get(0).text().split("\\(")[1].split("\\)")[0]);
            }catch(IndexOutOfBoundsException exp){
                mLastPage = -1;
            }

            String page = ((ReputationActivity) getActivity()).currPage;
            if(page.contains("page=")){
                mCurrPage = Integer.parseInt(page.split("page=")[1]);
            }else{
                mCurrPage = 1;
            }

            Elements reps = doc.select("table[style=\"clear: both;\"] tbody tr");
            for (Element e : reps) {
                try {
                    //user info
                    Element member = e.select("a[href*=\"member.php\"]").get(0);
                    String username = member.text();
                    int userId = Integer.parseInt(member.attr("href").split("uid=")[1]);
                    String group = member.select("span").attr("class");
                    int reputation = Integer.parseInt(e.select("a[href^=\"reputation.php\"]").text());

                    //message
                    String message = e.select(".repvotemid").text();

                    //date
                    String date = e.select(".repvoteright").text();

                    Reputation rep = new Reputation(message, userId, username, group, reputation, date);
                    REPUTATIONS.add(rep);
                }catch(Exception exp){

                }
            }

            if(mCurrPage < mLastPage){
                Reputation temp = new Reputation("", -1, "", "", -1, ""); //mandatory for progress spinner
                REPUTATIONS.add(temp); //mandatory for progress spinner
            }else{
                Reputation temp = new Reputation("", -2, "", "", -2, ""); //mandatory for progress spinner
                REPUTATIONS.add(temp); //mandatory for progress spinner
            }

            if (listener != null) listener.setLoading(false);

            if (v != null) {
                mAdapter.notifyDataSetChanged();
                v.invalidate();
            }
        } catch(Exception exp){
            //happens when activity is destroyed when asynctask still running
        }
    }

    public static class Reputation{
        public String message, author, authorGroup, date;
        public int authorId;
        public Integer authorRep; //can be null

        public Reputation(String message, int authorId, String author, String authorGroup, Integer authorRep, String date) {
            this.message = message;
            this.author = author;
            this.date = date;
            this.authorId = authorId;
            this.authorGroup = authorGroup;
            this.authorRep = authorRep;
        }
    }
}