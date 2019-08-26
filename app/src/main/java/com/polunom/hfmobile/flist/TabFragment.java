package com.polunom.hfmobile.flist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment implements ExpandableRecyclerAdapter.ExpandCollapseListener{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private String name;
    public static final List<Forum> GENERAL = new ArrayList<Forum>();
    public static final List<Forum> HACK = new ArrayList<Forum>();
    public static final List<Forum> TECH = new ArrayList<Forum>();
    public static final List<Forum> CODE = new ArrayList<Forum>();
    public static final List<Forum> GAME = new ArrayList<Forum>();
    public static final List<Forum> GROUPS = new ArrayList<Forum>();
    public static final List<Forum> WEB = new ArrayList<Forum>();
    public static final List<Forum> GFX = new ArrayList<Forum>();
    public static final List<Forum> MARKET = new ArrayList<Forum>();
    public static final List<Forum> MONEY = new ArrayList<Forum>();
    private OnForumInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TabFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TabFragment newInstance(int columnCount) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = getArguments().getString("name");
        if(GENERAL.size() == 0) {
            listForums(new int[]{1, 7, 45, 88, 151, 80, 53, 141, 156, 105, 241});
        }

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            RecyclerView v = (RecyclerView) view;
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL);
            mDividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.divider_line));
            v.addItemDecoration(mDividerItemDecoration);
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            if(name == "General"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), GENERAL, mListener));
            }else if(name == "Hack"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), HACK, mListener));
            }else if(name == "Tech"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), TECH, mListener));
            }else if(name == "Code"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), CODE, mListener));
            }else if(name == "Game"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), GAME, mListener));
            }else if(name == "Groups"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), GROUPS, mListener));
            }else if(name == "Web"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), WEB, mListener));
            }else if(name == "GFX"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), GFX, mListener));
            }else if(name == "Market"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), MARKET, mListener));
            }else if(name == "Money"){
                recyclerView.setAdapter(new IndexItemRecyclerViewAdapter(getContext(), MONEY, mListener));
            }


        }
        return view;
    }

    public void listForums(int[] ids){
        HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
        String s = browser.html;
        Document doc = Jsoup.parse(s);

        for(int i = 0; i < ids.length; i++) {
            Elements forums = doc.select("#tabmenu_" + ids[i] + " tbody[id^=\"cat_\"]").select("strong a[href^=\"forumdisplay.php?fid=\"]");
            for(Element e : forums){
                int id = Integer.parseInt(e.attr("href").replace("forumdisplay.php?fid=", ""));
                String name = e.text();
                String imageUrl = e.parent().parent().parent().select("img").attr("src");
                Elements subforums = e.parent().parent().select(".smalltext li a");
                List<Subforum> sf = new ArrayList<>();
                sf.clear();
                for(int j = 0; j < subforums.size(); j++){
                    int myId = Integer.parseInt(subforums.get(j).attr("href").replace("forumdisplay.php?fid=", ""));
                    String text = subforums.get(j).text();
                    Subforum sub = new Subforum(myId, text);
                    sf.add(sub);

                }

                Forum forum = new Forum(id, name, imageUrl, sf);

                if(ids[i] == 1 || ids[i] == 7){
                    GENERAL.add(forum);
                }else if(ids[i] == 45){
                    HACK.add(forum);
                }else if(ids[i] == 88){
                    TECH.add(forum);
                }else if(ids[i] == 151){
                    CODE.add(forum);
                }else if(ids[i] == 80){
                    GAME.add(forum);
                }else if(ids[i] == 53){
                    GROUPS.add(forum);
                }else if(ids[i] == 141){
                    WEB.add(forum);
                }else if(ids[i] == 156){
                    GFX.add(forum);
                }else if(ids[i] == 105){
                    MARKET.add(forum);
                }else if(ids[i] == 241){
                    MONEY.add(forum);
                }
            }
        }
    }

    @Override
    public void onParentExpanded(int parentPosition) {

    }

    @Override
    public void onParentCollapsed(int parentPosition) {

    }

    public static class Forum implements Parent<Subforum>{
        private int id;
        public String name;
        public final String image;
        private List<Subforum> mChildrenList;
        private int mParentNumber;
        private boolean mInitiallyExpanded;

        public Forum(int id, String name, String image, List<Subforum> sf) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.mChildrenList = sf;
        }

        public String getParentText() {
            return name;
        }

        public void setParentText(String parentText) {
            name = parentText;
        }

        public int getParentNumber() {
            return mParentNumber;
        }

        public void setParentNumber(int parentNumber) {
            mParentNumber = parentNumber;
        }

        @Override
        public List<Subforum> getChildList() {
//            for(Subforum s : mChildrenList){
//                Log.e("asdf", name + ": " + s.getName());
//            }
            return mChildrenList;
        }

        public void setChildItemList(List<Subforum> childItemList) {
            mChildrenList = childItemList;
        }

        public int getId(){
            return id;
        }

        public String getName(){
            return name;
        }

        @Override
        public boolean isInitiallyExpanded() {
            return mInitiallyExpanded;
        }

        public void setInitiallyExpanded(boolean initiallyExpanded) {
            mInitiallyExpanded = initiallyExpanded;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static class Subforum{
        public int id;
        public String name;

        public Subforum(int id, String name){
            this.id = id;
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public int getId(){
            return id;
        }
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
        void onListFragmentInteraction(Forum f);
    }
}