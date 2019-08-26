package com.polunom.hfmobile;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CaptchaDialog extends DialogFragment {

    private ImageView mImageView;
    private WebView mWebView;
    private Button refreshBtn;
    private EditText mEditText;
    private boolean redirect, loadingFinished;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String src = getArguments().getString("src");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_captcha, null);
        mEditText = (EditText) view.findViewById(R.id.captchaCode);
        mWebView = (WebView) view.findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                mWebView.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                loadingFinished = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(!redirect){
                    loadingFinished = true;
                }

                if(loadingFinished && !redirect){
                    Bitmap b = Bitmap.createBitmap(200, 60, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(b);
                    view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    view.draw(c);
                } else{
                    redirect = false;
                }

            }
        });

        String data = "<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:70%25; margin-top: 25px} </style></head><body><img src='" + "http://hackforums.net/" + src + "'/></body></html>";
        mWebView.loadData(data, "text/html",  "UTF-8");

        refreshBtn = (Button) view.findViewById(R.id.refresh);
        refreshBtn.setWidth(mWebView.getWidth());
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRefreshButtonClick(CaptchaDialog.this);
            }
        });

        builder.setView(view)
                .setTitle("Captcha required")
                .setPositiveButton(R.string.action_captcha, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(mEditText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(CaptchaDialog.this);
                    }
                });
        return builder.create();
    }

    public interface CaptchaDialogListener {
        public void onDialogPositiveClick(String captchaCode);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onRefreshButtonClick(DialogFragment dialog);
    }

    CaptchaDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CaptchaDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement CaptchaDialogListener");
        }
    }
}