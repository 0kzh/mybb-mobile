package com.polunom.hfmobile.pmdisplay;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
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
import com.polunom.hfmobile.BrowserActivity;
import com.polunom.hfmobile.EditorActivity;
import com.polunom.hfmobile.FixedWebView;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.LightboxActivity;
import com.polunom.hfmobile.ObservableScrollView;
import com.polunom.hfmobile.PMDisplayActivity;
import com.polunom.hfmobile.ProfileActivity;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.ReputationActivity;
import com.polunom.hfmobile.ThreadDisplayActivity;
import com.polunom.hfmobile.threaddisplay.PChildViewHolder;
import com.polunom.hfmobile.threaddisplay.PParentViewHolder;
import com.polunom.hfmobile.threaddisplay.PostDisplayFragment.Post;
import com.polunom.hfmobile.threaddisplay.PostDisplayFragment.PostDetails;
import com.polunom.hfmobile.threaddisplay.PostDisplayFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PMDisplayFragment extends Fragment implements ExpandableRecyclerAdapter.ExpandCollapseListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private int pmId;
    public static Post PM;
    public int pages;
    private String mUrl;
    private ObservableScrollView sv;
    private String source;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PMDisplayFragment() {
    }

    public static PMDisplayFragment newInstance(int columnCount) {
        PMDisplayFragment fragment = new PMDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pmId = getArguments().getInt("PM_ID");
        listPMs();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        LinearLayout parent = (LinearLayout) view.findViewById(R.id.postList);

        //inflate views
        View postHolder = inflater.inflate(R.layout.fragment_post, null); //can reuse
        View options = inflater.inflate(R.layout.fragment_inbox_child, null);
        final PParentViewHolder vh = new PParentViewHolder(postHolder);
        try {
            vh.mItem = PM;
            final Post post = PM;
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
                    ((PMDisplayActivity) getActivity()).showProgress(false);
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

        PMChildViewHolder vhChild = new PMChildViewHolder(options);
        final PostDetails pd = PM.getDetails();
        final View view1 = view;

        vhChild.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        vhChild.mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), EditorActivity.class);
                i.putExtra("ACTION", "pmto_" + pmId);
                startActivityForResult(i, 1);
            }
        });

        vhChild.mReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(getContext(), EditorActivity.class);
            i.putExtra("ACTION", "pmfrom_" + pmId);
            startActivityForResult(i, 1);
            }
        });

        parent.addView(postHolder);
        parent.addView(options);
        return view;
    }

    public void listPMs(){
        try {
            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
            String s = browser.html.replaceAll(">\\s+",">").replaceAll("\\s+<","<").trim();
            Document doc = Jsoup.parse(s);
            doc.outputSettings().indentAmount(0).prettyPrint(false);

            if (doc.select(".pages").size() != 0) {
                pages = Integer.parseInt(doc.select(".pages").text().split("\\(")[1].split("\\)")[0]);
            } else {
                pages = 1;
            }

            Element e = doc.select("#post_").get(0);
            //post info
            int id = -1; //no post id
            String postContent = e.select(".post_content").html();
            //strip tabs and breaks
            postContent = postContent.replace("\n", "");
            postContent = postContent.replace("\t", "");
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

            //pretty pms!
            if(postContent.contains("<blockquote>")) {
                d.outputSettings().indentAmount(0).prettyPrint(false);
                Elements quotes = d.select("blockquote");
                String formattedQuotes = "";
                for (int i = quotes.size() - 1; i >= 0; i--) {
                    String quote = "<blockquote>" + quotes.get(i).html().replaceAll(">\\s+", ">").replaceAll("\\s+<", "<").trim() + "</blockquote>";
                    if (i == quotes.size() - 1) {
                        formattedQuotes += quote + "<br>";
                    } else {
                        String replaceWith = "<blockquote>" + quotes.get(i + 1).html().replaceAll(">\\s+", ">").replaceAll("\\s+<", "<").trim() + "</blockquote>";
                        quote = quote.replace(replaceWith, "");
                        formattedQuotes += quote;
                        if (i != 0) {
                            formattedQuotes += "<br>";
                        }
                    }
                }

                String toReplace = "<blockquote>" + quotes.get(0).html().replaceAll(">\\s+", ">").replaceAll("\\s+<", "<").trim() + "</blockquote>";
                postContent = postContent.replace(toReplace, formattedQuotes).replace("</cite><br>", "</cite>");
            }
            Log.e("asdf", postContent);

            String time = e.select("tr").get(0).select(".float_left").text();

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
            PM = post;
        } catch(Exception exp){
            //happens when activity is destroyed when asynctask still running
        }
    }

    @Override
    public void onParentExpanded(int parentPosition) {

    }

    @Override
    public void onParentCollapsed(int parentPosition) {

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