package com.polunom.hfmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.graphics.Color.parseColor;

public class LoginActivity extends AppCompatActivity implements CaptchaDialog.CaptchaDialogListener {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AppCompatEditText mUnameView;
    private AppCompatEditText mPasswordView;
    private AppCompatEditText mGauthView;
    private View mProgressView;
    private View mLoginFormView;
    private ImageView mLogo;
    private long timeElapsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUnameView = (AppCompatEditText) findViewById(R.id.email);
        mUnameView.setTextColor(parseColor("#EEEEEE"));

        mPasswordView = (AppCompatEditText) findViewById(R.id.password);
        mPasswordView.setTextColor(parseColor("#EEEEEE"));
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(null);
                    return true;
                }
                return false;
            }
        });

        mGauthView = (AppCompatEditText) findViewById(R.id.gauth);
        mGauthView.setTextColor(parseColor("#EEEEEE"));
        mGauthView.getBackground().mutate().setColorFilter(parseColor("#EEEEEE"), PorterDuff.Mode.SRC_ATOP);
        mGauthView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(null);
                    return true;
                }
                return false;
            }
        });

        Button mUnameSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mUnameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(null);
            }
        });

        mLogo = (ImageView) findViewById(R.id.hfLogo);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(String captchaCode) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); //hide keyboard
        if (mAuthTask != null) {
            return;
        }

        // Reset errors
        mUnameView.setError(null);
        mPasswordView.setError(null);
        mGauthView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUnameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String gauth = mGauthView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if(captchaCode != null){
                mAuthTask = new UserLoginTask(email, password, gauth, captchaCode);
            }else {
                mAuthTask = new UserLoginTask(email, password, gauth, null);
            }
            mAuthTask.execute((Void) null);
        }
    }

    private void showProgress(final boolean show) {
        mLogo.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDialogPositiveClick(String captchaCode) {
        attemptLogin(captchaCode);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onRefreshButtonClick(DialogFragment dialog) {
        HFBrowser browser = (HFBrowser) getApplicationContext();
        browser.loadUrl("javascript:captcha.refresh();");
        dialog.dismiss();
        attemptLogin(null);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUname;
        private final String mPassword;
        private final String mGauth;
        private final String mCaptcha;

        UserLoginTask(String email, String password, String gauth, String captcha) {
            mUname = email;
            mPassword = password;
            mGauth = gauth;
            mCaptcha = captcha;
        }

        @Override
        protected void onPreExecute() {
            timeElapsed = System.currentTimeMillis();
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null) {
                if(mCaptcha == null){
                    browser.loadUrl("javascript:document.getElementsByName('username')[0].value = '" + mUname + "';document.getElementsByName('password')[0].value='" + mPassword + "';document.getElementsByName('gauth_code')[0].value='" + mGauth + "';document.getElementsByName('submit')[0].click();");

                }else{
                    browser.loadUrl("javascript:document.getElementsByName('username')[0].value = '" + mUname + "';document.getElementsByName('password')[0].value='" + mPassword + "';document.getElementsByName('gauth_code')[0].value='" + mGauth + "';document.getElementById('imagestring').value='" + mCaptcha + "';document.getElementsByName('submit')[0].click();");
                }

                browser.html = null;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    if((System.currentTimeMillis() - timeElapsed) > 10000){
                        //if login takes more than 10 seconds
                        return false;
                    }else {
                        Thread.sleep(100);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if(success) {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                String s = browser.html;
                Document doc = Jsoup.parse(s);
                if (doc.html().contains("invalid username/password")) {
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setMessage("Invalid Username/Password");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else if (doc.html().contains("The image verification code that you entered was incorrect")) {
                    //problem with image verification
                    String imgUrl = doc.select("#captcha_img").attr("src");
                    Bundle args = new Bundle();
                    args.putString("src", imgUrl);
                    CaptchaDialog dialog = new CaptchaDialog();
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "CaptchaDialog");
                } else if (doc.html().contains("Bad 2-Factor Authentication code")) {
                    //problem with gauth
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setMessage("Bad 2-Factor Authentication Code");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else {
                    browser.loggedIn = true;
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    finish();
                    startActivity(i);
                }
            }else{
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                alertDialog.setMessage("Connection timed out. Please verify internet connectivity.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        HFBrowser.activityResumed();
        overridePendingTransition(0,0);
    }

    @Override
    public void onPause() {
        super.onPause();
        HFBrowser.activityPaused();
        overridePendingTransition(0,0);
    }
}

