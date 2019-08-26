package com.polunom.hfmobile.userprofile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.ProfileActivity;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.ReputationActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AboutFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private int userID;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public AboutFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AboutFragment newInstance(int columnCount) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userID = Integer.parseInt(getArguments().getString("USER_ID"));

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        return view;
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loadData();
    }

    public void loadData(){
        HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
        String s = ((ProfileActivity) getActivity()).aboutHTML;
        Document doc = Jsoup.parse(s);

        String username, usertitle, userbar, status, action, joinDate, warning, posts, reputation, awards, lastVisit, prestige, reported, reportedDetail, signature, avatar;
        username = usertitle = userbar = status = action = joinDate = warning = posts = reputation = awards = lastVisit = prestige = reported = reportedDetail = signature = avatar = ""; //mass initalize
        ArrayList<String> userbars = new ArrayList<>();

        //get username
        username = doc.select("span[class=\"largetext\"]").text();
        usertitle = doc.select("td[width=\"75%\"] .smalltext").html().split("<br>")[0];
        usertitle = usertitle.replace("&nbsp;", "");

        userbar = doc.select("td[width=\"75%\"] .smalltext img[src*=\"groupimages\"]").attr("src");
        if (userbar.startsWith("images/groupimages"))
            userbar = userbar.replace("images/groupimages", "https://hackforums.net/images/groupimages");

        avatar = doc.select("td[width=\"25%\"] img").attr("src");
        if(avatar.startsWith("./")){
            avatar = avatar.replace("./", "https://hackforums.net/");
        }
        if(avatar.contains("?dateline")){
            avatar = avatar.split("\\?dateline")[0];
        }

        //get status
        status = "Offline";
        if(doc.select(".online").text() != null && !doc.select(".online").text().isEmpty() && !doc.select(".online").text().equals("null")){
            status = "Online";
            //get status detail
            action = doc.select(".online").parents().select(".smalltext").text().split("Online")[1].split("\\(")[1].split("\\)")[0];
        }

        //both user info and additional usergroups/signature block
        Elements headers = doc.select(".thead");

        for(Element header : headers){

            Element data = header.parents().select("tbody").get(0);

            if(header.text().contains("Forum Info")){
                //get parent table
                Iterator<Element> ite = data.select("tr").iterator();
                ite.next(); // first is header

                for(int i = 0; i < data.select("tr").size()-1; i++){
                    Element e = ite.next();
                    String title = e.select("td").get(0).text();
                    String value = e.select("td").get(1).text();
                    if(title.equals("Joined:")){
                        joinDate = value;
                    }else if(title.equals("Last Visit:")){
                        lastVisit = value;
                    }else if(title.equals("Total Posts:")){
                        posts = value.split(" ")[0];
                    }else if(title.equals("Reputation:")){
                        reputation = value.split(" ")[0];
                    }else if(title.equals("Prestige:")){
                        prestige = value;
                    }else if(title.equals("Reported Posts:")){
                        reported = "0";
                        //if user has reported at least 1 post
                        if(!value.equals("0")) {
                            reported = value.split("\\[")[0];
                            reportedDetail = value.split("\\[")[1].replace("]", "");
                        }
                    }else if(title.equals("Awards:")){
                        awards = value.split(" ")[0];
                    }
                }

            }else if(header.text().contains("Additional Usergroups")){
                Elements links = data.select("img");
                for(Element link : links){
                    String src = link.attr("src");
                    if(src.contains("groupimages")){
                        if(!src.contains("hackforums.net")){
                            src = "https://hackforums.net/" + src;
                        }
                        userbars.add(src);
                    }
                }
            }else if(header.text().contains("Signature")){
                signature = data.select(".trow1").html();
            }
        }

        //update layout elements with data
        View v = getView();

        //header elements
        //check if avatar exists
        ImageView avatarView = (ImageView) getActivity().findViewById(R.id.profilePhoto);
        if(!avatar.isEmpty()){
            Glide.with(getContext()).load(avatar).into(avatarView);
        }else{
            avatarView.setVisibility(View.GONE);
        }

        //check if userbar exists
        ImageView userbarView = (ImageView) getActivity().findViewById(R.id.profUserbar);
        if(!userbar.isEmpty()){
            Glide.with(getContext()).load(userbar).into(userbarView);
        }else{
            userbarView.setVisibility(View.GONE);
        }

        if(userbar.isEmpty()){
            //no userbar
            if(usertitle.equals("[exiled@HF:]")){
                //banned
                username = "<font color=#000000>"+username+"</font>";
            }else if(usertitle.equals("[closed@HF:]")){
                //closed
                username = "<font color=#555555>"+username+"</font>";
            }else{
                //normal user
                username = "<font color=#EFEFEF>"+username+"</font>";
            }
        }else if(userbar.equals("https://hackforums.net/images/groupimages/ub3r.png")){
            //Ub3r
            username = "<font color=#00AAFF>"+username+"</font>";
        }else if(userbar.equals("https://hackforums.net/images/groupimages/L33t.png")){
            //L33t
            username = "<font color=#99FF00>"+username+"</font>";
        }else if(userbar.equals("https://hackforums.net/images/groupimages/staff.png")){
            //Staff
            username = "<font color=#9999FF>"+username+"</font>";
        }else if(userbar.equals("https://hackforums.net/images/groupimages/admin.jpg")){
            //Admin
            username = "<font color=#FF66FF>"+username+"</font>";
        }else {
            //Custom usergroup
            username = "<font color=#FFFFFF>"+username+"</font>";
        }

        TextView user = (TextView) getActivity().findViewById(R.id.user);

        ((TextView) getActivity().findViewById(R.id.usertitle)).setText(usertitle);
        TextView postView = (TextView) getActivity().findViewById(R.id.posts);
        postView.setText(posts);
        ((LinearLayout) postView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.profileOptions);
                TabLayout.Tab tab = tabLayout.getTabAt(2);
                tab.select();
            }
        });

        if(posts.equals("1")) {
            ((TextView) getActivity().findViewById(R.id.posts1)).setText("post");
        }

        //add userbars to additional usergroups section

        if(!userbars.isEmpty()) {
            LinearLayout parent = (LinearLayout) v.findViewById(R.id.userbars);
            for (String url : userbars) {
                ImageView imgView = new ImageView(getActivity());
                parent.addView(imgView);

                //convert 151x44 dp to px
                final float scale = getContext().getResources().getDisplayMetrics().density;
                int width = (int) (141 * scale + 0.5f);
                int height = (int) (44 * scale + 0.5f);
                imgView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

                Glide.with(getContext()).load(url).into(imgView);
            }
        }else{
            (v.findViewById(R.id.userbarsHolder)).setVisibility(View.GONE);
        }


        //empty if admin/staff
        TextView repView = (TextView) getActivity().findViewById(R.id.reputation);

        if(!reputation.isEmpty()){
            //set reputation colors
            int rep = Integer.parseInt(reputation);
            if(rep > 0){
                reputation = "<b><font color=#32CD32>"+reputation+"</font></b>";
            }else if(rep < 0){
                reputation = "<b><font color=#CC3333>"+reputation+"</font></b>";
            }else if(rep == 0){
                reputation = "<b><font color=#666666>"+reputation+"</font></b>";
            }

        }else{
            ((LinearLayout) repView.getParent()).setVisibility(View.GONE);
        }

        ((LinearLayout) repView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ReputationActivity.class);
                i.putExtra("USER_ID", userID);
                v.getContext().startActivity(i);
            }
        });

        ((TextView) getActivity().findViewById(R.id.awards)).setText(awards);
        if(awards.equals("1")) {
            ((TextView) getActivity().findViewById(R.id.awards1)).setText("award");
        }

        //remove details if offline
        TextView statusView = (TextView) v.findViewById(R.id.online);
        if(status.equals("Online")){
            status = "<b><font color=#32CD32>"+status+"</font></b>";
            ((TextView) v.findViewById(R.id.onlineDetails)).setText(action);
        }else{
            status = "<b><font color=#777777>"+status+"</font></b>";
            v.findViewById(R.id.onlineDetails).setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            user.setText(Html.fromHtml(username, Html.FROM_HTML_MODE_LEGACY));
            repView.setText(Html.fromHtml(reputation, Html.FROM_HTML_MODE_LEGACY));
            statusView.setText(Html.fromHtml(status, Html.FROM_HTML_MODE_LEGACY));
        } else {
            user.setText(Html.fromHtml(username));
            repView.setText(Html.fromHtml(reputation));
            statusView.setText(Html.fromHtml(status));
        }

        ((TextView) v.findViewById(R.id.lastVisit)).setText(lastVisit);
        ((TextView) v.findViewById(R.id.registration)).setText(joinDate);
        ((TextView) v.findViewById(R.id.prestige)).setText(prestige);
        ((TextView) v.findViewById(R.id.report)).setText(reported);
        if(!reported.equals("0")) {
            ((TextView) v.findViewById(R.id.reportDetails)).setText(reportedDetail);
        }else{
            v.findViewById(R.id.reportDetails).setVisibility(View.GONE);
        }
        ((TextView) v.findViewById(R.id.userId)).setText(String.valueOf(userID));

        //set signature
        WebView webView = (WebView) v.findViewById(R.id.signature);
        if(!signature.isEmpty()) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setOnTouchListener(new View.OnTouchListener() {
                //disable scrolling
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
            webView.setBackgroundColor(Color.argb(1, 0, 0, 0));
            webView.setOnClickListener(null);

            //prepend css header
            String html = "<link rel=\"stylesheet\" type=\"text/css\" " +
                    "href=\"custom.css\" />" +
                    signature;

            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
        }else{
            ((LinearLayout) webView.getParent()).setVisibility(View.GONE);
        }

        //done, hide progress.
        ((ProfileActivity) getActivity()).showProgress(false);

        //call next asynctask for threads

    }
}