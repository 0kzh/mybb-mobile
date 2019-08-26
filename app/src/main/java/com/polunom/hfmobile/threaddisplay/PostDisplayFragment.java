package com.polunom.hfmobile.threaddisplay;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.polunom.hfmobile.BrowserActivity;
import com.polunom.hfmobile.FixedWebView;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.LightboxActivity;
import com.polunom.hfmobile.ObservableScrollView;
import com.polunom.hfmobile.ProfileActivity;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.ReputationActivity;
import com.polunom.hfmobile.ThreadDisplayActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PostDisplayFragment extends Fragment implements ExpandableRecyclerAdapter.ExpandCollapseListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private int postId, position, pg, count;
    public static final List<Post> POSTS = new ArrayList<>();
    private List<View> postList;
    public int pages;
    private String mUrl;
    private OnFooterInteractionListener mListener;
    private ObservableScrollView sv;
    private String source;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDisplayFragment() {
    }

    public static PostDisplayFragment newInstance(int columnCount) {
        PostDisplayFragment fragment = new PostDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postId = getArguments().getInt("POST_ID");
        listPosts();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        LinearLayout parent = (LinearLayout) view.findViewById(R.id.postList);
        postList = new ArrayList<>();
        count = 0;

        //inflate views
        for(int i = 0; i < POSTS.size()-1; i++) {
            View postHolder = inflater.inflate(R.layout.fragment_post, null);
            View options = inflater.inflate(R.layout.fragment_post_child, null);
            final PParentViewHolder vh = new PParentViewHolder(postHolder);
            try {
                vh.mItem = POSTS.get(i);
                final Post post = POSTS.get(i);
                vh.mTimestampView.setText(post.getPostTimestamp());

                if (post.getPosterAvatar() != null && !post.getPosterAvatar().isEmpty() && !post.getPosterAvatar().equals("null")) {
                    Glide.with(vh.mAvatar.getContext()).load(post.getPosterAvatar()).into(vh.mAvatar);
                }else{
                    vh.mAvatar.setVisibility(View.GONE);
                }

                if (post.getUserbar() != null && !post.getUserbar().isEmpty() && !post.getUserbar().equals("null")) {
                    Glide.with(vh.mUserbar.getContext()).load(post.getUserbar()).into(vh.mUserbar);
                }else{
                    vh.mUserbarHolder.setVisibility(View.GONE);
                }

                if (post.status != null && !post.status.isEmpty() && !post.status.equals("null")) {
                    vh.mStatusView.setText(post.status);
                }

                vh.mUserMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popupMenu = new PopupMenu(vh.mUserMenu.getContext(), v);
                        final View view = v;
                        popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                        {
                            public boolean onMenuItemClick(MenuItem item)
                            {
                                if(item.getTitle().equals("View profile")){
                                    Intent i = new Intent(view.getContext(), ProfileActivity.class);
                                    i.putExtra("USER_ID", post.authorId);
                                    view.getContext().startActivity(i);
                                }else if(item.getTitle().equals("View reputation")){
                                    Intent i = new Intent(view.getContext(), ReputationActivity.class);
                                    i.putExtra("USER_ID", post.authorId);
                                    view.getContext().startActivity(i);
                                }
                                return true;
                            }
                        });

                        popupMenu.show();
                    }
                });


                String username="";

                if(post.getUserbar() == null || post.getUserbar().isEmpty() || post.getUserbar().equals("null")){
                    //no userbar
                    if(post.status.equals("[exiled@HF:]")){
                        //banned
                        username = "<b><font color=#000000>"+post.author+"</font></b>";
                    }else if(post.status.equals("[closed@HF:]")){
                        //closed
                        username = "<b><font color=#555555>"+post.author+"</font></b>";
                    }else{
                        //normal user
                        username = "<b><font color=#EFEFEF>"+post.author+"</font></b>";
                    }
                }else if(post.getUserbar().equals("https://hackforums.net/images/groupimages/ub3r.png")){
                    //Ub3r
                    username = "<b><font color=#00AAFF>"+post.author+"</font></b>";
                }else if(post.getUserbar().equals("https://hackforums.net/images/groupimages/L33t.png")){
                    //L33t
                    username = "<b><font color=#99FF00>"+post.author+"</font></b>";
                }else if(post.getUserbar().equals("https://hackforums.net/images/groupimages/staff.png")){
                    //Staff
                    username = "<b><font color=#9999FF>"+post.author+"</font></b>";
                }else if(post.getUserbar().equals("https://hackforums.net/images/groupimages/admin.jpg")){
                    //Admin
                    username = "<b><font color=#FF66FF>"+post.author+"</font></b>";
                }else {
                    //Custom usergroup
                    username = "<b><font color=#FFFFFF>"+post.author+"</font></b>";
                }

                String reputation = "";
                if(post.reputation == null){
                    //not defined (Staff)
                    reputation = username;
                }else if(post.reputation > 0){
                    reputation = username + "<font color=#ffffff> (</font><font color=#32CD32>"+post.reputation+"</font><font color=#ffffff>)</font>";
                }else if (post.reputation < 0){
                    reputation = username + "<font color=#ffffff> (</font><font color=#CC3333>"+post.reputation+"</font><font color=#ffffff>)</font>";
                }else if (post.reputation == 0){
                    reputation = username + "<font color=#ffffff> (</font><font color=#666666>"+post.reputation+"</font><font color=#ffffff>)</font>";
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    vh.mAuthorView.setText(Html.fromHtml(reputation, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    vh.mAuthorView.setText(Html.fromHtml(reputation));
                }

                //prepend css header
                String html = "<link rel=\"stylesheet\" type=\"text/css\" " +
                        "href=\"custom.css\" />" +
                        post.postContent;

                vh.mContentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
                vh.mContentView.addJavascriptInterface(new ClickHandler(), "ClickHandler");
                vh.mContentView.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        count++;
                        if(count == postList.size()-1){
                            //after all webviews have finished loading
                            scrollTo(postList.get(position));
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                        showAlert(url);
                        return true;
                    }
                });

                vh.mContentView.setOnLongPressListener(new FixedWebView.LongPressListener() {
                    @Override
                    public void longPressed(String type, String src) {
                        source = src;
                        final CharSequence colors[];
                        if(type.equals("image")) {
                            colors = new CharSequence[]{"Download image"};
                        }else{
                            colors = new CharSequence[]{"View image", "Download image"};
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setItems(colors, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // the user clicked on colors[which]
                                String selected = colors[which].toString();
                                if(selected.equals("Download image")){
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                }else if(selected.equals("View image")){
                                    Intent i = new Intent(getContext(), LightboxActivity.class);
                                    i.putExtra("source", source);
                                    startActivity(i);
                                    getActivity().overridePendingTransition(0, 0);
                                }
                            }
                        });
                        builder.show();
                    }
                });


            } catch (Exception e) {

            }

            PChildViewHolder vhChild = new PChildViewHolder(options);
            final PostDetails pd = POSTS.get(i).getDetails();
            final View view1 = view;
            vhChild.mReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(String.valueOf(pd.getPostId()));
                    }
                }
            });
            vhChild.mLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    HFBrowser browser = (HFBrowser) getContext().getApplicationContext();
                    ClipData clip = ClipData.newPlainText("link", browser.getUrl() + "&pid=" + pd.getPostId() + "#pid" + pd.getPostId());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
            vhChild.mProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(view1.getContext(), ProfileActivity.class);
                    i.putExtra("USER_ID", Integer.parseInt(pd.getAuthorProf().split("uid=")[1]));
                    view1.getContext().startActivity(i);
                }
            });


            parent.addView(postHolder);
            postList.add(postHolder);
            parent.addView(options);
        }

        //add pagination footer
        View footer = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.footer, parent, false);
        ImageView prevPage = (ImageView) footer.findViewById(R.id.prevPage);
        ImageView nextPage = (ImageView) footer.findViewById(R.id.nextPage);
        final TextView pagination = (TextView) footer.findViewById(R.id.pagination);
        String page = pg + "/" + pages;
        if(page != null && !page.isEmpty() && !page.equals("null")){
            pagination.setText("Page " + page);
            int currPage = Integer.parseInt(page.split("/")[0]);
            int lastPage = Integer.parseInt(page.split("/")[1]);
            if(currPage == 1){
                prevPage.setVisibility(View.INVISIBLE);
            }else if(currPage == lastPage){
                nextPage.setVisibility(View.INVISIBLE);
            }
            if(lastPage == 1){
                nextPage.setVisibility(View.INVISIBLE);
            }
        }

        pagination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(pagination.getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.page_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        if(item.getTitle().equals("Go to page")){
                            if (null != mListener) {
                                mListener.onListFragmentInteraction("jump");
                            }
                        }else if(item.getTitle().equals("Last post")){
                            if (null != mListener) {
                                mListener.onListFragmentInteraction("last");
                            }
                        }else if(item.getTitle().equals("First post")){
                            if (null != mListener) {
                                mListener.onListFragmentInteraction("first");
                            }
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });
        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction("prev");
                }
            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction("next");
                }
            }
        });

        parent.addView(footer);
        postList.add(footer);

        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.newReply);
        sv = (ObservableScrollView) view.findViewById(R.id.nestedScrollView);

        //disable swiperefreshlayout if not at top
        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = sv.getScrollY();
                if(scrollY == 0) ((ThreadDisplayActivity)getActivity()).mSwipeRefreshLayout.setEnabled(true);
                else ((ThreadDisplayActivity)getActivity()).mSwipeRefreshLayout.setEnabled(false);

                View view = postList.get(postList.size()-1);
                int scrolled = sv.getHeight()+scrollY;
                int max = view.getTop()+sv.getPaddingBottom();

                if (scrolled > max) {
                    //if bottom reached
                    if(fab.isShown()) fab.hide();
                }
            }
        });
        sv.setOnScrollDirectionListener(new ObservableScrollView.ScrollDirectionListener() {
            @Override
            public void onScrollDown(int pixels) {
                if(!fab.isShown()) fab.show();
            }

            @Override
            public void onScrollUp(int pixels) {
                if(fab.isShown()) fab.hide();
            }
        });

        return view;
    }

    private void scrollTo(View view){
        final View v = view;
        sv.post(new Runnable() {
            @Override
            public void run() {
//                TypedValue tv = new TypedValue();
//                int actionBarHeight = 0;
//                if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
//                }
                sv.scrollTo(0, v.getTop());
            }
        });
    }

    public void listPosts(){
        try {
            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);

            if (doc.select(".pages").size() != 0) {
                pages = Integer.parseInt(doc.select(".pages").text().split("\\(")[1].split("\\)")[0]);
            } else {
                pages = 1;
            }

            Elements posts = doc.select("table[id^=\"post_\"]");
            for (Element e : posts) {
                //post info
                int id = Integer.parseInt(e.attr("id").replace("post_", ""));
                String postContent = e.select(".post_content").html();
                postContent = postContent.replace("src=\"images/", "src=\"https://hackforums.net/images/"); // append hackforums.net to local images (smilies)
                postContent = postContent.replace("max-height:200px", "max-height:250px;"); //prevent text being cut off
                //inject onclick callback to all images
                postContent = postContent.replace("<img", "<img onclick=\"ClickHandler.imageClick(this.src);\"");
                //remove callback from linked images
                Document d = Jsoup.parse(postContent);
                Elements imageLinks = d.select("a img");
                for(Element il : imageLinks){
                    String replacement = il.parent().html().replace(" onclick=\"ClickHandler.imageClick(this.src);\"", "");
                    postContent = postContent.replace(il.parent().html(), replacement);
                }

                String time = getTimestamp(e.select("tr").get(0).select(".float_left").text());

                //author info
                String author = e.select(".post_author strong").text();
                String repString = e.select("strong[class^=\"reputation_\"]").text();
                Integer reputation;
                if (repString != null && !repString.isEmpty() && !repString.equals("null")) {
                    //for most users
                    reputation = Integer.parseInt(e.select("strong[class^=\"reputation_\"]").text());
                } else {
                    //for Staff, whose reputation isn't shown on posts
                    reputation = null;
                }

                int authorId = Integer.parseInt(e.select(".post_author a").attr("href").replace("https://hackforums.net/member.php?action=profile&uid=", ""));
                String avatar = e.select(".post_avatar img").attr("src");
                if (avatar.startsWith("./"))
                    avatar = avatar.replace("./", "https://hackforums.net/");
                String status = e.select(".post_author .smalltext").text();
                String userbar = e.select(".post_author .smalltext img[src*=\"groupimages\"]").attr("src");
                if (userbar.startsWith("images/groupimages"))
                    userbar = userbar.replace("images/groupimages", "https://hackforums.net/images/groupimages");

                PostDetails td = new PostDetails(authorId, id);
                Post post = new Post(id, postContent, time, author, authorId, avatar, userbar, status, reputation, td);
                POSTS.add(post);

            }

            List<PostDetails> tempChild = new ArrayList<>();
            Post temp = new Post(-1, "", "", "", -1, "", "", "", -1, null); //mandatory for progress spinner
            POSTS.add(temp); //mandatory for progress spinner
            tempChild.add(new PostDetails(-1, -1)); //mandatory for progress spinner

            String url = browser.getUrl().replace("https://hackforums.net/", "");

            if (url.contains("&page=")) {
                pg = Integer.parseInt(url.split("&page=")[1]);
            } else if (url.contains("lastpost")) {
                pg = pages;
            }


            getActivity().getSupportFragmentManager()
            .beginTransaction()
                    .detach(PostDisplayFragment.this)
                    .attach(PostDisplayFragment.this)
                    .commitAllowingStateLoss();

            position = 0;

            if(postId != -1){
                //get post position
                for(int i = 0; i < POSTS.size(); i++){
                    Post p = POSTS.get(i);
                    if(p.id == postId){
                        position = i;
                    }
                }
            }

            if (url.contains("lastpost")) {
                position = POSTS.size() - 2;
            }

        } catch(NullPointerException exp){
            //happens when activity is destroyed when asynctask still running
        }
    }

    private String getTimestamp(String tr) {
        if(tr.contains("This post was last modified")){
            String parsed=tr.split(": ")[1].split(" by")[0];
            String time = parsed.substring(parsed.length()-8) + "*";
            String date = parsed.split(" ")[0];
            return date + ", " + time;
        }
        return tr;
    }

    @Override
    public void onParentExpanded(int parentPosition) {

    }

    @Override
    public void onParentCollapsed(int parentPosition) {

    }

    public static class Post{
        public int id, authorId;
        public Integer reputation;
        public String postContent, time, author, avatar, userbar, status;
        private PostDetails pd;
        private boolean mInitiallyExpanded;

        public Post(int id, String postContent, String time, String author, int authorId, String avatar, String userbar, String status, Integer reputation, PostDetails details) {
            this.id = id;
            this.postContent = postContent;
            this.time = time;
            this.author = author;
            this.authorId = authorId;
            this.avatar = avatar;
            this.userbar = userbar;
            this.status = status;
            this.reputation = reputation;
            this.pd = details;
        }

        public int getId(){ return id; }
        public PostDetails getDetails() { return pd; }
        public int getAuthorId() { return authorId; }
        public Integer getReputation() { return reputation;}
        public String getPostHTML() { return postContent; }
        public String getPostTimestamp() { return time; }
        public String getPoster() { return author; }
        public String getUserbar() { return userbar; }
        public String getPosterAvatar() { return avatar; }
        public String getPosterStatus() { return status; }
    }

    public static class PostDetails{
        public int userId, postId;

        public PostDetails(int userId, int postId){
            this.userId = userId;
            this.postId = postId;
        }

        public String getAuthorProf() { return "https://hackforums.net/member.php?action=profile&uid="+userId; }
        public int getPostId(){ return postId; }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFooterInteractionListener) {
            mListener = (OnFooterInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFooterInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFooterInteractionListener {
        void onListFragmentInteraction(String action);
    }

    public void showAlert(final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Go to this url?")
                .setMessage(url)
                .setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getActivity(), BrowserActivity.class);
                        i.putExtra("url", url);
                        startActivity(i);
                    }
                })
                .setNeutralButton("No", null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, so download
                    String fileName = source.substring(source.lastIndexOf('/') + 1);
                    DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(source));
                    request.setAllowedNetworkTypes(
                            DownloadManager.Request.NETWORK_WIFI
                                    | DownloadManager.Request.NETWORK_MOBILE)
                            .setAllowedOverRoaming(false)
                            .setDescription("Downloading image...")
                            .setTitle(fileName)
                            .setNotificationVisibility(
                                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS ,fileName);
                    downloadManager.enqueue(request);
                } else {
                    //permission denied
                    Toast.makeText(getActivity(), "Could not write to file.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    class ClickHandler {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void imageClick(String src) {
            Intent i = new Intent(getContext(), LightboxActivity.class);
            i.putExtra("source", src);
            startActivity(i);
            getActivity().overridePendingTransition(0, 0);
        }
    }
}