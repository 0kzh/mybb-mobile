package com.polunom.hfmobile;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import java.util.List;

public class GiveRepDialog extends DialogFragment {

    private EditText mEditText;
    private NumberPicker mRepList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final List<String> reps = getArguments().getStringArrayList("reps");
        final String prevRep = getArguments().getString("prevRep");
        final String message = getArguments().getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_giverep, null);

        mEditText = (EditText) view.findViewById(R.id.repMessage);
        if(message != null && !message.isEmpty() && !message.equals("null")){
            mEditText.setText(message);
        }
        mRepList = (NumberPicker) view.findViewById(R.id.numberPicker);
        mRepList.setMinValue(0);
        mRepList.setMaxValue(reps.size()-1);
        String[] values = new String[reps.size()];
        values = reps.toArray(values);
        mRepList.setDisplayedValues(values);
        mRepList.setWrapSelectorWheel(false);
        if(prevRep != null && !prevRep.isEmpty() && !prevRep.equals("null")){
            mRepList.setValue(reps.indexOf(prevRep));
        }

        builder.setView(view)
                .setTitle("Give reputation")
                .setPositiveButton(R.string.action_captcha, null)
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
                        Log.e("asdf", "give rep -> " + browser.getUrl());
                        browser.setUrl(browser.getUrl());
                    }
                }).setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            HFBrowser browser = (HFBrowser) getActivity().getApplicationContext();
                            Log.e("asdf", "give rep -> " + browser.getUrl());
                            browser.setUrl(browser.getUrl());
                            dismiss();
                        }
                        return false;
                    }
                });

        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int length = mEditText.getText().toString().length();
                        if (length >= 10) {
                            mListener.onDialogPositiveClick(mEditText.getText().toString(), reps.get(mRepList.getValue()));
                        } else {
                            mEditText.requestFocus();
                            mEditText.setError("Comment must be at least 10 characters.");
                        }
                    }
                });
            }
        });


        return mAlertDialog;
    }

    public interface RepDialogListener {
        public void onDialogPositiveClick(String captchaCode, String rep);
    }

    RepDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (RepDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement CaptchaDialogListener");
        }
    }
}