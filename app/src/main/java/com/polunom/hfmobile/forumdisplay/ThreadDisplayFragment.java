package com.polunom.hfmobile.forumdisplay;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.polunom.hfmobile.CustomLinearLayoutManager;
import com.polunom.hfmobile.CustomViewDivider;
import com.polunom.hfmobile.EndlessScrollListener;
import com.polunom.hfmobile.ForumDisplayActivity;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.ProfileActivity;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.SharedPreference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ThreadDisplayFragment extends Fragment implements ExpandableRecyclerAdapter.ExpandCollapseListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    public static List<ForumThread> THREADS = new ArrayList<>();
    public static List<ForumThread> USERTHREADS = new ArrayList<>();
    public static List<ForumThread> USERPOSTS = new ArrayList<>();
    private OnForumInteractionListener mListener;
    public static ForumRecyclerViewAdapter mAdapter;
    public RecyclerView v;
    private String action;
    private EndlessScrollListener listener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (ex. upon screen orientation changes).
     */
    public ThreadDisplayFragment() {
    }

    public static ThreadDisplayFragment newInstance(int columnCount) {
        ThreadDisplayFragment fragment = new ThreadDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        action = getArguments().getString("ACTION");
        if(action.equals("forum")){
            listThreads();
        } else if(action.equals("threads")){
            listUserThreads();
        } else if(action.equals("posts")){
            listUserPosts();
        }

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {

            v = (RecyclerView) view;

            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(context));
            if(action.equals("forum")) {
                mAdapter = new ForumRecyclerViewAdapter(getContext(), THREADS, mListener);
            }else if (action.equals("threads")){
                mAdapter = new ForumRecyclerViewAdapter(getContext(), USERTHREADS, mListener);
            }else if (action.equals("posts")){
                mAdapter = new ForumRecyclerViewAdapter(getContext(), USERPOSTS, mListener);
            }
            recyclerView.setAdapter(mAdapter);
            //infiniscroll implementation
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.newThread);
            listener = new EndlessScrollListener((CustomLinearLayoutManager) recyclerView.getLayoutManager(), fab) {
                @Override
                public void onLoadMore(int current_page) {
                    try {
                        HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
                        String url;
                        if(action.equals("threads")){
                            url =  ((ProfileActivity) getActivity()).threadsURL.replace("https://hackforums.net/", "");
                        }else if(action.equals("posts")){
                            url =  ((ProfileActivity) getActivity()).postsURL.replace("https://hackforums.net/", "");
                        }else{
                            url = browser.getUrl().replace("https://hackforums.net/", "");
                        }

                        if(!url.contains("&page=")){
                            //currently on first page
//                            Log.e("asdf", url+"&page=2");
                            if(action.equals("forum")){
                                ((ForumDisplayActivity) getActivity()).nextPage(url+"&page=2");
                            }else{
                                ((ProfileActivity) getActivity()).nextPage(action, url+"&page=2");
                            }
                        }else{
                            //get current page and go to next
                            int currPage = Integer.parseInt(url.split("&page=")[1]);
                            if(action.equals("forum")){
                                ((ForumDisplayActivity) getActivity()).nextPage(url.replace("&page="+currPage, "&page="+(currPage+1)));
                            }else{
                                ((ProfileActivity) getActivity()).nextPage(action, url.replace("&page="+currPage, "&page="+(currPage+1)));
                            }
//                            Log.e("asdf", url.replace("&page="+currPage, "&page="+(currPage+1)));
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

    @Override
    public void onResume(){
        super.onResume();
    }

    public void listThreads(){
        try {
            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);

            int lastPage = Integer.parseInt(doc.select(".pages").get(0).text().split("\\(")[1].split("\\)")[0]);
            String page = ((ForumDisplayActivity) getActivity()).currPage;
            int currPage;
            if(page.contains("page=")){
                currPage = Integer.parseInt(page.split("page=")[1]);
            }else{
                currPage = 1;
            }

            Elements threads = doc.select("table[style=\"clear: both;\"] tbody tr");

            for (Element e : threads) {
                try {
                    String icon = e.select("td[width=\"2%\"] img").attr("src");
                    Element data = e.select("a[id^=\"tid_\"]").get(0);
                    int id = Integer.parseInt(data.attr("id").replace("tid_", ""));

                    String name = data.text();
                    boolean stickied = false;
                    if(e.select("td").get(1).attr("class").contains("forumdisplay_sticky")){
                        stickied = true;
                    }

                    int posts = Integer.parseInt(e.select("a[href^=\"javascript:MyBB.whoPosted(\"]").text().replace(",", ""));
                    String author = e.select(".author a").text();

                    String authorProf = e.select(".author a").attr("href");
                    int lastPosterId = Integer.parseInt(e.select(".lastpost a[href^=\"https://hackforums.net/member.php\"]")
                            .attr("href")
                            .replace("https://hackforums.net/member.php?action=profile&uid=", ""));
                    String lastPostAuthor = e.select(".lastpost a[href^=\"https://hackforums.net/member.php\"]").text();
                    String lastPostTime = e.select(".lastpost").html().split("<br>")[0].replace("\"", "");
                    List<ThreadDetails> tdList = new ArrayList<>();
                    ForumThread thread = new ForumThread(id, icon, name, posts, author, tdList, stickied, "forum");

                    //prevent duplicates
                    boolean exists = false;

                    for(ForumThread ft : THREADS){
                        //cannot use .contains(thread) or linkedhashset bc tdlist is different
                        if(ft.id == thread.id){
                            exists = true;
                            break;
                        }
                    }

                    //also check for hidden threads
                    SharedPreference sharedPreference = new SharedPreference();
                    List<ForumThread> hiddenThreads = sharedPreference.getHiddenThreads(getContext());
                    if (hiddenThreads != null) {
                        for (ForumThread ft : hiddenThreads) {
                            if(ft.id == thread.id){
                                exists = true;
                                break;
                            }
                        }
                    }


                    if(!exists) {
                        THREADS.add(thread);
                        ThreadDetails td = new ThreadDetails(id, authorProf, lastPosterId, lastPostTime, lastPostAuthor, thread);
                        tdList.add(td);
                    }
                } catch (Exception exp) {

                }
            }

            if(currPage <= lastPage){
                List<ThreadDetails> tempChild = new ArrayList<>(); //mandatory for progress spinner
                ForumThread temp = new ForumThread(-1, "", "", -1, "", tempChild, false, ""); //mandatory for progress spinner
                THREADS.add(temp); //mandatory for progress spinner
                tempChild.add(new ThreadDetails(-1, "", -1, "", "", temp)); //mandatory for progress spinner
            }else{
                List<ThreadDetails> tempChild = new ArrayList<>();
                ForumThread temp = new ForumThread(-2, "", "", -2, "", tempChild, false, "");
                THREADS.add(temp);
                tempChild.add(new ThreadDetails(-2, "", -2, "", "", temp));
            }

            if (listener != null) listener.setLoading(false);

            if (v != null) {
                Parcelable recyclerViewState = v.getLayoutManager().onSaveInstanceState();
                mAdapter = new ForumRecyclerViewAdapter(getContext(), THREADS, mListener);
                v.setAdapter(mAdapter);
                v.invalidate();
                v.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            } else {
            }
        } catch(Exception ex){
            //happens when activity is destroyed before asynctask finishes
        }
    }

    public void listUserThreads(){
        try {
            String s = ((ProfileActivity) getActivity()).threadsHTML;
            Document doc = Jsoup.parse(s);

            int lastPage;
            try {
                lastPage = Integer.parseInt(doc.select(".pages").get(0).text().split("\\(")[1].split("\\)")[0]);
            }catch(IndexOutOfBoundsException exp){
                lastPage = -1;
            }

            String page = ((ProfileActivity) getActivity()).threadsURL;
            int currPage;
            if(page.contains("page=")){
                currPage = Integer.parseInt(page.split("page=")[1]);
            }else{
                currPage = 1;
            }

            Elements threads = doc.select(".tborder tbody tr");

            for (Element e : threads) {
                try {
                    String icon = e.select("td[width=\"2%\"] img").attr("src");
                    Element data = e.select("a[id^=\"tid_\"]").get(0);
                    int id = Integer.parseInt(data.attr("id").replace("tid_", ""));
                    String name = data.text();
                    int posts = Integer.parseInt(e.select("a[href^=\"javascript:MyBB.whoPosted(\"]").text().replace(",", ""));

                    String author = e.select(".author a").text();
                    String authorProf = e.select(".author a").attr("href");

                    Element lastPostParent = e.select("a[href*=\"lastpost\"]").parents().select(".smalltext").get(0);

                    int lastPosterId = Integer.parseInt(lastPostParent.select("a[href^=\"https://hackforums.net/member.php\"]")
                            .attr("href")
                            .replace("https://hackforums.net/member.php?action=profile&uid=", ""));
                    String lastPostAuthor = lastPostParent.select("a[href^=\"https://hackforums.net/member.php\"]").text();
                    String lastPostTime = lastPostParent.html().split("<br>")[0].replace("\"", "");

                    List<ThreadDetails> tdList = new ArrayList<>();
                    ForumThread thread = new ForumThread(id, icon, name, posts, author, tdList, false, "thread");

                    //prevent duplicates
                    boolean exists = false;
                    for(ForumThread ft : USERTHREADS){
                        //cannot use .contains(thread) or linkedhashset bc tdlist is different
                        if(ft.id == thread.id){
                            exists = true;
                            break;
                        }
                    }

                    //also check for hidden threads
                    SharedPreference sharedPreference = new SharedPreference();
                    List<ForumThread> hiddenThreads = sharedPreference.getHiddenThreads(getContext());
                    if (hiddenThreads != null) {
                        for (ForumThread ft : hiddenThreads) {
                            if(ft.id == thread.id){
                                exists = true;
                                break;
                            }
                        }
                    }

                    if(!exists) {
                        USERTHREADS.add(thread);
                        ThreadDetails td = new ThreadDetails(id, authorProf, lastPosterId, lastPostTime, lastPostAuthor, thread);
                        tdList.add(td);
                    }
                } catch (Exception exp) {

                }
            }

            if(currPage <= lastPage){
                List<ThreadDetails> tempChild = new ArrayList<>(); //mandatory for progress spinner
                ForumThread temp = new ForumThread(-1, "", "", -1, "", tempChild, false, ""); //mandatory for progress spinner
                USERTHREADS.add(temp); //mandatory for progress spinner
                tempChild.add(new ThreadDetails(-1, "", -1, "", "", temp)); //mandatory for progress spinner
            }else{
                List<ThreadDetails> tempChild = new ArrayList<>();
                ForumThread temp = new ForumThread(-2, "", "", -2, "", tempChild, false, "");
                USERTHREADS.add(temp);
                tempChild.add(new ThreadDetails(-2, "", -2, "", "", temp));
            }

            if (listener != null) listener.setLoading(false);

            if (v != null) {
                Parcelable recyclerViewState = v.getLayoutManager().onSaveInstanceState();
                mAdapter = new ForumRecyclerViewAdapter(getContext(), USERTHREADS, mListener);
                v.setAdapter(mAdapter);
                v.invalidate();
                v.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            } else {
            }
        } catch(NullPointerException ex){
            //happens when activity is destroyed before asynctask finishes
        }
    }

    public void listUserPosts(){
        try {
            String s = ((ProfileActivity) getActivity()).postsHTML;
            Document doc = Jsoup.parse(s);

            int lastPage;
            try {
                lastPage = Integer.parseInt(doc.select(".pages").get(0).text().split("\\(")[1].split("\\)")[0]);
            }catch(IndexOutOfBoundsException exp){
                lastPage = -1;
            }

            String page = ((ProfileActivity) getActivity()).postsURL;
            int currPage;
            if(page.contains("page=")){
                currPage = Integer.parseInt(page.split("page=")[1]);
            }else{
                currPage = 1;
            }


            Elements threads = doc.select(".tborder tbody tr");

            for (Element e : threads) {
                try {
                    String icon = e.select("td[width=\"2%\"] img").get(0).attr("src");
                    Element data = e.select("a[href^=\"showthread.php\"]").get(1);
                    int id = Integer.parseInt(data.attr("href").split("tid=")[1].split("&")[0]);
                    String postId = data.attr("href").split("pid=")[1].split("#")[0];
                    String name = data.text();

                    int posts = Integer.parseInt(e.select("a[href^=\"javascript:MyBB.whoPosted(\"]").text().replace(",", ""));

                    String author = e.select("em").text();
                    String authorProf = "post";

                    //replace last post data with forum
                    int lastPosterId = Integer.parseInt(e.select("a[href^=\"forumdisplay.php\"]").attr("href").split("fid=")[1]); //forum id
                    String lastPostAuthor = e.select("a[href^=\"forumdisplay.php\"]").text(); //forum name
                    String lastPostTime = "In forum:";

                    List<ThreadDetails> tdList = new ArrayList<>();
                    ForumThread thread = new ForumThread(id, icon, name, posts, author, tdList, false, postId);

                    //prevent duplicates
                    boolean exists = false;
                    for(ForumThread ft : USERPOSTS){
                        //cannot use .contains(thread) or linkedhashset bc tdlist is different
                        if(ft.id == thread.id){
                            exists = true;
                            break;
                        }
                    }

                    //also check for hidden threads
                    SharedPreference sharedPreference = new SharedPreference();
                    List<ForumThread> hiddenThreads = sharedPreference.getHiddenThreads(getContext());
                    if (hiddenThreads != null) {
                        for (ForumThread ft : hiddenThreads) {
                            if(ft.id == thread.id){
                                exists = true;
                                break;
                            }
                        }
                    }

                    if(!exists) {
                        USERPOSTS.add(thread);
                        ThreadDetails td = new ThreadDetails(id, authorProf, lastPosterId, lastPostTime, lastPostAuthor, thread);
                        tdList.add(td);
                    }
                }catch(Exception exp){

                }
            }

            if(currPage <= lastPage){
                List<ThreadDetails> tempChild = new ArrayList<>(); //mandatory for progress spinner
                ForumThread temp = new ForumThread(-1, "", "", -1, "", tempChild, false, ""); //mandatory for progress spinner
                USERPOSTS.add(temp); //mandatory for progress spinner
                tempChild.add(new ThreadDetails(-1, "", -1, "", "", temp)); //mandatory for progress spinner
            }else{
                List<ThreadDetails> tempChild = new ArrayList<>();
                ForumThread temp = new ForumThread(-2, "", "", -2, "", tempChild, false, "");
                USERPOSTS.add(temp);
                tempChild.add(new ThreadDetails(-2, "", -2, "", "", temp));
            }
            if (listener != null) listener.setLoading(false);

            if (v != null) {
                Parcelable recyclerViewState = v.getLayoutManager().onSaveInstanceState();
                mAdapter = new ForumRecyclerViewAdapter(getContext(), USERPOSTS, mListener);
                v.setAdapter(mAdapter);
                v.invalidate();
                v.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            } else {
            }
        } catch(NullPointerException ex){
            //happens when activity is destroyed before asynctask finishes
        }
    }

    @Override
    public void onParentExpanded(int parentPosition) {

    }

    @Override
    public void onParentCollapsed(int parentPosition) {

    }

    public static class ForumThread implements Parent<ThreadDetails>{
        public int id, posts;
        public String name, author, icon, action;
        public boolean stickied;
        public List<ThreadDetails> mChildrenList;
        private boolean mInitiallyExpanded;

        public ForumThread(int id, String icon, String name, int posts, String author, List<ThreadDetails> details, boolean stickied, String source) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.posts = posts;
            this.author = author;
            this.mChildrenList = details;
            this.stickied = stickied;
            this.action = source;
        }

        @Override
        public List<ThreadDetails> getChildList() {
//            for(Subforum s : mChildrenList){
//                Log.e("asdf", name + ": " + s.getName());
//            }
            return mChildrenList;
        }

        public int getId(){
            return id;
        }

        public String getName(){
            return name;
        }

        public String getIcon() { return icon; }

        @Override
        public boolean isInitiallyExpanded() {
            return false;
        }

        public void setInitiallyExpanded(boolean initiallyExpanded) {
            mInitiallyExpanded = initiallyExpanded;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static class ThreadDetails{
        public int id, lastPosterId;
        public String authorProf, lastPostAuthor, lastPostTime;
        public ForumThread parentThread;

        public ThreadDetails(int id, String authorProf, int lastPosterId, String lastPostTime, String lastPostAuthor, ForumThread parentThread){
            this.id = id;
            this.authorProf = authorProf;
            this.lastPosterId = lastPosterId;
            this.lastPostAuthor = lastPostAuthor;
            this.lastPostTime = lastPostTime;
            this.parentThread = parentThread;
        }

        public String getAuthorProf() { return authorProf; }
        public int getLastPosterId() { return lastPosterId; }
        public int getId(){ return id; }
        public ForumThread getParent() { return parentThread; }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnForumInteractionListener) {
            mListener = (OnForumInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnForumInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnForumInteractionListener {
        void onListFragmentInteraction(ForumThread t, boolean lastpost);
    }
}