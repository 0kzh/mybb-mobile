package com.polunom.hfmobile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;

public class HFBrowser extends Application {
    private static WebView mWebView;
    private static String murl;
    public static boolean activityVisible;
    public static boolean loggedIn = false;
    public static String html;
    public static String quote;
    public static String pmRecipients, pmTitle, pmQuote;
    public static String userAgent;
    public static String cookies;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate() {
        super.onCreate();
        mWebView = new WebView(getApplicationContext());
        mWebView.addJavascriptInterface(new HtmlHandler(), "HtmlHandler");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        userAgent = webSettings.getUserAgentString();
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebView.loadUrl("javascript:HtmlHandler.handleHtml(document.documentElement.outerHTML);");
                html = null;
            }
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                // Loading started for URL
//            }
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // Redirecting to URL
//                return false;
//            }
        });
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public void loadUrl(String url) {
        Log.e("asdf", "navigating to: " + url);
        mWebView.loadUrl(url);
        if (!url.startsWith("javascript")){
            murl = url;
        }
    }

    public void postData(String data){
        mWebView.postUrl(mWebView.getUrl(), Base64.encode(data.getBytes(), Base64.DEFAULT));
    }

    public void setUrl(String url){
        murl = url;
    }

    public String getUrl(){
        return murl;
    }

    public String getWebViewUrl(){
        return mWebView.getUrl();
    }

    class HtmlHandler {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void handleHtml(String i) {
            HFBrowser.html = i;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void handleQuote(String i) {
            HFBrowser.quote = i;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void handlePM(String str) {
            try {
                JSONArray values = new JSONArray(str);
                pmRecipients = values.getString(0);
                pmTitle = values.getString(1);
                pmQuote = values.getString(2);
            }catch(Exception e){

            }
        }
    }

}
