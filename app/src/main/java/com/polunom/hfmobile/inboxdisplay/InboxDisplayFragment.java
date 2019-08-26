package com.polunom.hfmobile.inboxdisplay;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.polunom.hfmobile.CustomLinearLayoutManager;
import com.polunom.hfmobile.EndlessScrollListener;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.InboxDisplayActivity;
import com.polunom.hfmobile.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InboxDisplayFragment extends Fragment implements ExpandableRecyclerAdapter.ExpandCollapseListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    public static List<Message> MESSAGES = new ArrayList<>();
    private OnPMInteractionListener mListener;
    private InboxRecyclerViewAdapter mAdapter;
    public RecyclerView v;
    private int mCurrPage, mLastPage;
    private EndlessScrollListener listener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (ex. upon screen orientation changes).
     */
    public InboxDisplayFragment() {
    }

    public static InboxDisplayFragment newInstance(int columnCount) {
        InboxDisplayFragment fragment = new InboxDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listMessages();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {

            v = (RecyclerView) view;
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(context));
            mAdapter = new InboxRecyclerViewAdapter(getContext(), MESSAGES, mListener);
            recyclerView.setAdapter(mAdapter);
            //infiniscroll implementation
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.newPM);
            listener = new EndlessScrollListener((CustomLinearLayoutManager) recyclerView.getLayoutManager(), fab) {
                @Override
                public void onLoadMore(int current_page) {
                    try {
                        if(mCurrPage < mLastPage) {
                            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
                            String url = browser.getUrl().replace("https://hackforums.net/", "");

                            if (!url.contains("&page=")) {
                                //currently on first page
                                ((InboxDisplayActivity) getActivity()).nextPage(url + "&page=2");
                            } else {
                                //get current page and go to next
                                int currPage = Integer.parseInt(url.split("&page=")[1]);
                                ((InboxDisplayActivity) getActivity()).nextPage(url.replace("&page=" + currPage, "&page=" + (currPage + 1)));
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

    @Override
    public void onResume(){
        super.onResume();
    }

    public void listMessages(){
        try {
            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);

            mLastPage = Integer.parseInt(doc.select(".pages").get(0).text().split("\\(")[1].split("\\)")[0]);
            String page = ((InboxDisplayActivity) getActivity()).currPage;
            if(page.contains("page=")){
                mCurrPage = Integer.parseInt(page.split("page=")[1]);
            }else{
                mCurrPage = 1;
            }

            Elements messages = doc.select(".tborder").last().select("tbody tr");
            for(Element message : messages){
                try {
                    String icon = message.select("td[width=\"1%\"] img").get(0).attr("src");
                    Element data = message.select("a[href*=\"action=read\"]").get(0);
                    int id = Integer.parseInt(data.attr("href").split("pmid=")[1]);
                    String title = data.text();

                    String sender = message.select("a[href*=\"action=profile\"]").text();
                    String senderGroup = message.select("a[href*=\"action=profile\"] span").attr("class");
                    String date = message.select("span[style*=\"text-align:right\"]").text();
                    //parse date
                    DateFormat output = new SimpleDateFormat("MMM dd");
                    if(date.contains("Today")){
                        date = date.replace("Today, ", "");
                        if(date.startsWith("0")){
                            date = date.replaceFirst("0", "");
                        }
                    }else if(date.contains("Yesterday")){
                        //return simplified
                        date = output.format(yesterday());
                    }else {
                        DateFormat input = new SimpleDateFormat("MM-dd-yyyy, HH:mm a");
                        Date d = input.parse(date);
                        date = output.format(d);
                    }

                    List<MessageDetails> msgList = new ArrayList<>();
                    Message msg = new Message(id, icon, title, sender, senderGroup, date, msgList);
                    MESSAGES.add(msg);
                    MessageDetails td = new MessageDetails(id, msg);
                    msgList.add(td);
                }catch(Exception e){
                    //break if element isn't post
                }
            }

            if(mCurrPage < mLastPage){
                List<MessageDetails> tempChild = new ArrayList<>(); //mandatory for progress spinner
                Message temp = new Message(-1, "", "", "", "", "", new ArrayList<MessageDetails>()); //mandatory for progress spinner
                MESSAGES.add(temp); //mandatory for progress spinner
                tempChild.add(new MessageDetails(-1, temp)); //mandatory for progress spinner
            }else{
                List<MessageDetails> tempChild = new ArrayList<>();
                Message temp = new Message(-2, "", "", "", "", "", new ArrayList<MessageDetails>());
                MESSAGES.add(temp);
                tempChild.add(new MessageDetails(-2, temp));
            }

            if (listener != null) listener.setLoading(false);

            if (v != null) {
                Parcelable recyclerViewState = v.getLayoutManager().onSaveInstanceState();
                mAdapter = new InboxRecyclerViewAdapter(getContext(), MESSAGES, mListener);
                v.setAdapter(mAdapter);
                v.invalidate();
                v.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            } else {
            }
        } catch(Exception ex){
            //happens when activity is destroyed before asynctask finishes
        }
    }

    public InboxRecyclerViewAdapter getAdapter(){
        return mAdapter;
    }

    @Override
    public void onParentExpanded(int parentPosition) {

    }

    @Override
    public void onParentCollapsed(int parentPosition) {

    }

    public static class Message implements Parent<MessageDetails>{
        public int id, posts;
        public String icon, name, sender, senderGroup, date;
        public List<MessageDetails> mChildrenList;
        private boolean mInitiallyExpanded;

        public Message(int id, String icon, String name, String sender, String senderGroup, String date, List<MessageDetails> details) {
            this.id = id;
            this.icon = icon;
            this.name = name;
            this.sender = sender;
            this.senderGroup = senderGroup;
            this.date = date;
            this.mChildrenList = details;
        }

        @Override
        public List<MessageDetails> getChildList() {
            return mChildrenList;
        }

        public int getId(){
            return id;
        }

        public String getName(){
            return name;
        }

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
    public static class MessageDetails{
        public int id;
        public Message parentThread;

        public MessageDetails(int id, Message parentThread){
            this.id = id;
            this.parentThread = parentThread;
        }

        public int getId(){ return id; }
        public Message getParent() { return parentThread; }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPMInteractionListener) {
            mListener = (OnPMInteractionListener) context;
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


    public interface OnPMInteractionListener {
        void onListFragmentInteraction(Message t);
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
}