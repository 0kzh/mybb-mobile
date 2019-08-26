package com.polunom.hfmobile;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Arrays;
import java.util.List;

public class EditorActivity extends AppCompatActivity{
    private EditText recipients, title, editor;
    private String action;
    private String returnToPage;

    //actions: thread, reply, reply w/ quote, pm, reply to pm, forward pm
    //identifiers: thread, reply, replyfrom_[id], pm, pmfrom_[id], pmto_[id]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        action = getIntent().getStringExtra("ACTION");
        HFBrowser browser = (HFBrowser) getApplicationContext();
        returnToPage = browser.getUrl();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(action.equals("thread")) {
            getSupportActionBar().setTitle("New Thread");
        } else if(action.startsWith("pm")){
            getSupportActionBar().setTitle("New PM");
        } else {
            getSupportActionBar().setTitle("New Reply");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recipients = (EditText) findViewById(R.id.recipients);
        title = (EditText) findViewById(R.id.title);
        editor = (EditText) findViewById(R.id.editor);
        setToolbarActions();

        if(action.startsWith("reply")){
            findViewById(R.id.title).setVisibility(View.GONE);
            findViewById(R.id.recipients).setVisibility(View.GONE);
            if(action.startsWith("replyfrom_")){
                //get id
                int postId = Integer.parseInt(action.split("_")[1]);
                //set editor actions
                editor.setHint("Loading quote...");
                editor.setEnabled(false);
                //load quote
                new loadQuote(postId).execute();
            }
        }else if(action.startsWith("pmfrom_") || action.startsWith("pmto_")){
            //either replying or forwarding
            int pmId = Integer.parseInt(action.split("_")[1]);
            //set editor actions
            recipients.setHint("Loading...");
            title.setHint("Loading...");
            editor.setHint("Loading previous PMs...");
            recipients.setEnabled(false);
            title.setEnabled(false);
            editor.setEnabled(false);
            //load quote
            new loadQuote(pmId).execute();
        }else if(action.equals("thread")){
            findViewById(R.id.recipients).setVisibility(View.GONE);
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
    public class loadQuote extends AsyncTask<Integer, Void, Void> {

        private final int id;
        loadQuote(int postId) {
            id = postId;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String url;
            //can be postreply, pm reply, or pm forward
            if(action.startsWith("replyfrom_")){
                //post reply
                url = browser.getUrl().replace("showthread.php", "newreply.php").split("&")[0] + "&replyto=" + id;
            }else if(action.startsWith("pmfrom_")){
                //pm reply
                url = browser.getUrl().replace("action=read", "action=send") + "&do=reply";
            }else{
                //pm forward
                url = browser.getUrl().replace("action=read", "action=send") + "&do=forward";
            }
            browser.loadUrl(url);
            browser.html = null;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(action.startsWith("pm")){
                new doPMWait().execute();
            }else {
                new doWait().execute();
            }
        }
    }

    public class doWait extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("javascript:HtmlHandler.handleQuote(document.getElementsByName('message_new')[0].value);");
            browser.quote = null;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.quote == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String q = browser.quote;
            editor.append(q);
            editor.setHint("Your message...");
            editor.setEnabled(true);
        }
    }

    public class doPMWait extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl("javascript: var elements = [];" +
                                        "elements.push(document.getElementsByName('to')[0].value);" +
                                        "elements.push(document.getElementsByName('subject')[0].value);" +
                                        "elements.push(document.getElementsByName('message_new')[0].value);" +
                                        "var json = JSON.stringify(elements);" +
                                        "HtmlHandler.handlePM(json);");
            browser.pmRecipients = browser.pmTitle = browser.pmQuote = null;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.pmRecipients ==  null || browser.pmTitle == null || browser.pmQuote == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) getApplicationContext();

            recipients.setText(browser.pmRecipients);
            recipients.setHint("Recipients");
            recipients.setEnabled(true);

            title.setText(browser.pmTitle);
            title.setHint("Subject");
            title.setEnabled(true);

            String q = browser.pmQuote;
            editor.append(q);
            editor.setHint("Your message...");
            editor.setEnabled(true);
        }
    }

    private void setToolbarActions() {
        //TODO: Use maps instead
        //looking at (and writing) the below code gives me cancer.
        findViewById(R.id.action_font).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prompt user with default myBB fonts
                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Select a font");
                builder.setItems(R.array.fonts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //convert array
                        List<String> fonts = Arrays.asList((getResources().getStringArray(R.array.fonts)));

                        //get user-selected positions (equal if not selected)
                        int start = editor.getSelectionStart();
                        int end = editor.getSelectionEnd();

                        if(start == end) {
                            //append to end
                            editor.getText().insert(end, "[font="+fonts.get(item)+"][/font]");
                        } else {
                            //otherwise, add tags to endpoints of selected region
                            int startIndex = Math.max(start, 0);
                            int endIndex = Math.max(end, 0);
                            String toInsert = "[font="+fonts.get(item)+"]" + editor.getText() + "[/font]";
                            editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                                     toInsert, 0, toInsert.length());
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        findViewById(R.id.action_size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prompt user with font size options
                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Select a size");
                builder.setItems(R.array.text_sizes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //convert array
                        List<String> sizes = Arrays.asList((getResources().getStringArray(R.array.text_sizes)));

                        //get user-selected positions (equal if not selected)
                        int start = editor.getSelectionStart();
                        int end = editor.getSelectionEnd();

                        if(start == end) {
                            //append to end
                            editor.getText().insert(end, "[size="+sizes.get(item).toLowerCase()+"][/size]");
                        } else {
                            //otherwise, add tags to endpoints of selected region
                            int startIndex = Math.max(start, 0);
                            int endIndex = Math.max(end, 0);
                            String toInsert = "[size="+sizes.get(item).toLowerCase()+"]" + editor.getText() + "[/size]";
                            editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                    toInsert, 0, toInsert.length());
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[b][/b]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[b]" + editor.getText() + "[/b]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[i][/i]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[i]" + editor.getText() + "[/i]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[u][/u]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[u]" + editor.getText() + "[/u]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[align=left][/align]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[align=left]" + editor.getText() + "[/align]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[align=center][/align]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[align=center]" + editor.getText() + "[/align]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[align=right][/align]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[align=right]" + editor.getText() + "[/align]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });findViewById(R.id.action_align_justify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[align=justify][/align]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[align=justify]" + editor.getText() + "[/align]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_list_numbered).setOnClickListener(new View.OnClickListener() {
            StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void onClick(View v) {
                final int start = editor.getSelectionStart();
                final int end = editor.getSelectionEnd();

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Enter a list item");
                // prompt user for list item
                final EditText input = new EditText(EditorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!input.getText().toString().equals("null") && input.getText().toString() != null && !input.getText().toString().isEmpty()){
                            stringBuilder.append("[*]" + input.getText().toString() + "\n");
                            findViewById(R.id.action_list_numbered).callOnClick(); //prompt again until left blank
                        }else {
                            if(start == end) {
                                //append to end
                                editor.getText().insert(end, "[list=1]" + "\n" + stringBuilder.toString() +"[/list]");
                            } else {
                                //otherwise, add replace text with list
                                int startIndex = Math.max(start, 0);
                                int endIndex = Math.max(end, 0);
                                String toInsert = "[list=1]" + "\n" + stringBuilder.toString() + "[/list]";
                                editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                        toInsert, 0, toInsert.length());
                            }
                            //reset stringbuilder
                            stringBuilder.setLength(0);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                      dialog.cancel();
                        if(stringBuilder != null && stringBuilder.toString() != null && !stringBuilder.toString().isEmpty() && !stringBuilder.toString().equals("null")){
                            if(start == end) {
                                if(start != 0){
                                    editor.getText().insert(end, "\n" + "[list=1]" + "\n" + stringBuilder.toString() +"[/list]");
                                }else{
                                    editor.getText().insert(end, "[list=1]" + "\n" + stringBuilder.toString() +"[/list]");
                                }
                            } else {
                                //otherwise, add replace text with list
                                int startIndex = Math.max(start, 0);
                                int endIndex = Math.max(end, 0);
                                String toInsert = "[list=1]" + "\n" + stringBuilder.toString() + "[/list]";
                                editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                        toInsert, 0, toInsert.length());
                            }
                            stringBuilder.setLength(0);
                        }else{
                            dialog.cancel();
                        }
                    }
                });
                builder.show();
                input.requestFocus();
                //ugly stuff, but it's the only working solution
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
                    }
                }, 200);
            }
        });
        findViewById(R.id.action_list_bulleted).setOnClickListener(new View.OnClickListener() {
            StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void onClick(View v) {
                final int start = editor.getSelectionStart();
                final int end = editor.getSelectionEnd();

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Enter a list item");
                // prompt user for list item
                final EditText input = new EditText(EditorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!input.getText().toString().equals("null") && input.getText().toString() != null && !input.getText().toString().isEmpty()){
                            stringBuilder.append("[*]" + input.getText().toString() + "\n");
                            findViewById(R.id.action_list_numbered).callOnClick(); //prompt again until left blank
                        }else {
                            if(start == end) {
                                //append to end
                                editor.getText().insert(end, "[list]" + "\n" + stringBuilder.toString() +"[/list]");
                            } else {
                                //otherwise, add replace text with list
                                int startIndex = Math.max(start, 0);
                                int endIndex = Math.max(end, 0);
                                String toInsert = "[list]" + "\n" + stringBuilder.toString() + "[/list]";
                                editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                        toInsert, 0, toInsert.length());
                            }
                            stringBuilder.setLength(0);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                      dialog.cancel();
                        if(stringBuilder != null && stringBuilder.toString() != null && !stringBuilder.toString().isEmpty() && !stringBuilder.toString().equals("null")){
                            if(start == end) {
                                //append to end
                                if(start != 0){
                                    editor.getText().insert(end, "\n" + "[list]" + "\n" + stringBuilder.toString() +"[/list]");
                                }else{
                                    editor.getText().insert(end, "[list]" + "\n" + stringBuilder.toString() +"[/list]");
                                }

                            } else {
                                //otherwise, add replace text with list
                                int startIndex = Math.max(start, 0);
                                int endIndex = Math.max(end, 0);
                                String toInsert = "[list=1]" + "\n" + stringBuilder.toString() + "[/list]";
                                editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                        toInsert, 0, toInsert.length());
                            }
                            stringBuilder.setLength(0);
                        }else{
                            dialog.cancel();
                        }
                    }
                });
                builder.show();
                input.requestFocus();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
                    }
                }, 200);
            }
        });
        findViewById(R.id.action_image).setOnClickListener(new View.OnClickListener() {
            //TODO: add height & width prompt?
            @Override
            public void onClick(View v) {
                final int start = editor.getSelectionStart();
                final int end = editor.getSelectionEnd();

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Enter image URL");
                // prompt user for list item
                final EditText input = new EditText(EditorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        //prepend http:// if necessary
                        if(!url.startsWith("http://") || !url.startsWith("https://")){
                            url = "http://" + url;
                        }

                        if(start == end) {
                            //append to end
                            editor.getText().insert(end, "[img]" + url +"[/img]");
                        } else {
                            //otherwise, add replace text with image
                            int startIndex = Math.max(start, 0);
                            int endIndex = Math.max(end, 0);
                            String toInsert = "[img]" + url + "[/img]";
                            editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                    toInsert, 0, toInsert.length());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
                    }
                }, 200);
            }
        });
        findViewById(R.id.action_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int start = editor.getSelectionStart();
                final int end = editor.getSelectionEnd();

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Enter link URL");
                // prompt user for list item
                final EditText input = new EditText(EditorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String temp = input.getText().toString();
                        //prepend http:// if necessary
                        if(!temp.startsWith("http://") && !temp.startsWith("https://")){
                            temp = "http://" + temp;
                        }

                        final String url = temp;

                        if(start == end) {
                            //make second alert dialog asking for title
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                            builder.setTitle("Title (optional)");
                            final EditText input = new EditText(EditorActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(input);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String title = input.getText().toString();
                                    if(title != null  && !title.toString().isEmpty() && !title.toString().equals("null")){
                                        editor.getText().insert(end, "[url="+url+"]" + title +"[/url]");
                                    }else{
                                        editor.getText().insert(end, "[url]" + url +"[/url]");
                                    }
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editor.getText().insert(end, "[url]" + url +"[/url]");
                                }
                            });
                            builder.show();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                                    input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
                                }
                            }, 200);
                        } else {
                            //otherwise, add link tags around selection
                            int startIndex = Math.max(start, 0);
                            int endIndex = Math.max(end, 0);
                            String toInsert = "[url="+url+"]" + editor.getText() + "[/url]";
                            editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                    toInsert, 0, toInsert.length());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
                    }
                }, 200);
            }
        });
        findViewById(R.id.action_quote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[quote][/quote]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[quote]" + editor.getText() + "[/quote]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if(start == end) {
                    editor.getText().insert(end, "[code][/code]");
                } else {
                    int startIndex = Math.max(start, 0);
                    int endIndex = Math.max(end, 0);
                    String toInsert = "[code]" + editor.getText() + "[/code]";
                    editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                            toInsert, 0, toInsert.length());
                }
            }
        });
        findViewById(R.id.action_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int start = editor.getSelectionStart();
                final int end = editor.getSelectionEnd();

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Enter YouTube link");
                final EditText input = new EditText(EditorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        //prepend http:// if necessary
                        if(!url.startsWith("http://") || !url.startsWith("https://")){
                            url = "http://" + url;
                        }

                        if(start == end) {
                            //append to end
                            editor.getText().insert(end, "[yt]" + url +"[/yt]");
                        } else {
                            //otherwise, add replace text with image
                            int startIndex = Math.max(start, 0);
                            int endIndex = Math.max(end, 0);
                            String toInsert = "[yt]" + url + "[/yt]";
                            editor.getText().replace(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex),
                                    toInsert, 0, toInsert.length());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
                    }
                }, 200);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final HFBrowser browser = (HFBrowser) getApplicationContext();

        if(id == android.R.id.home) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Your changes will not be saved. Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            browser.setUrl(returnToPage);
                            if(action.equals("thread")){
                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_CANCELED, returnIntent);
                                finish();
                            }else{
                                finish();
                            }
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else if (id == R.id.action_post) {
            String url = browser.getUrl();
            int length = editor.getText().toString().length();
            int subLength = title.getText().toString().length();
            int recipLength = recipients.getText().toString().length();

            //if thread or reply
            if(action.equals("thread") || action.startsWith("reply")) {
                if (length >= 25) {
                    if (action.equals("thread")) {
                        //make new thread
                        if(subLength > 0) {
                            if (url.contains("forumdisplay.php")) {
                                Toast.makeText(getApplicationContext(), "Posting thread...", Toast.LENGTH_LONG).show();
                                new initThread().execute();
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                                alertDialog.setTitle("Error posting thread");
                                alertDialog.setMessage("Something went wrong. Please exit and try again.");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }else{
                            AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                            alertDialog.setTitle("Error posting thread");
                            alertDialog.setMessage("Please enter a title.");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    } else {
                        //make new post using quick reply box
                        if (url.contains("showthread.php") || url.contains("newreply.php")) {
                            Toast.makeText(getApplicationContext(), "Posting reply...", Toast.LENGTH_LONG).show();
                            new initPost().execute();
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                            alertDialog.setTitle("Error posting reply");
                            alertDialog.setMessage("Something went wrong. Please exit and try again.");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                    if (action.equals("thread")) {
                        alertDialog.setTitle("Error posting thread");
                    } else {
                        alertDialog.setTitle("Error posting reply");
                    }
                    if(length == 0) {
                        alertDialog.setMessage("The message is too short. Please enter a message longer than 25 characters.");
                    }else{
                        alertDialog.setMessage("Please enter a title.");
                    }
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }else{
                if (length > 0 && subLength > 0 && recipLength > 0) {
                    //sending pm; any message > 1 char
                    //either post, forward, or reply
                    if (action.equals("pm")) {
                        Toast.makeText(getApplicationContext(), "Sending PM...", Toast.LENGTH_LONG).show();
                        new initPM().execute();
                    } else {
                        if (url.contains("action=send")) {
                            if (action.startsWith("pmfrom")) {
                                //reply
                                Toast.makeText(getApplicationContext(), "Replying...", Toast.LENGTH_LONG).show();
                            } else {
                                //forward
                                Toast.makeText(getApplicationContext(), "Forwarding...", Toast.LENGTH_LONG).show();
                            }
                            String sendTo = recipients.getText().toString();
                            String subject = title.getText().toString();
                            String message = editor.getText().toString();
                            //check to prevent multiple postings
                            if (browser.html != null) {
                                new sendPM(sendTo, subject, message).execute();
                            }
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                            alertDialog.setTitle("Error sending PM");
                            alertDialog.setMessage("Something went wrong. Please exit and try again.");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }
                }else{
                    AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                    alertDialog.setTitle("Error sending PM");
                    if(length == 0){
                        alertDialog.setMessage("Please enter a message.");
                    }else if(subLength == 0){
                        alertDialog.setMessage("Please enter a subject.");
                    }else{
                        alertDialog.setMessage("Please enter a recipient.");
                    }
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //thread functionality
    public class initThread extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            browser.loadUrl(browser.getUrl().replace("forumdisplay.php", "newthread.php"));
            browser.html = null;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String title = ((EditText) findViewById(R.id.title)).getText().toString();
            String message = ((EditText) findViewById(R.id.editor)).getText().toString();

            //check to prevent multiple postings
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null){
                new postThread(title, message).execute();
            }
        }
    }
    public class postThread extends AsyncTask<Void, Void, Void> {

        private final String mTitle, mMessage;
        postThread(String title, String message) {
            mTitle = title.replace("'", "\\'");
            //replace line breaks with literal \n for JavaScript
            message = message.replaceAll("\n", "\\\\n");
            message = message.replace("'", "\\'");
            mMessage = message;
            Log.e("asdf", "asdf:"+mMessage);
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null) {
                browser.loadUrl("javascript:document.getElementsByName('subject')[0].value = '" + mTitle + "';" +
                                "document.getElementsByName('message_new')[0].value='" + mMessage + "';" +
                                "document.getElementsByName('submit')[0].click();");
                browser.html = null;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);

            if(doc.select("title").text().contains(mTitle)){
                //success!
                Toast.makeText(getApplicationContext(), "Thread posted!", Toast.LENGTH_SHORT).show();

                String url = browser.getWebViewUrl();
//                String name = doc.select("#panel a[href^=\"https://hackforums.net/member.php?action=profile\"]").text();

                //add thread to top
//                ForumThread thread = new ForumThread(Integer.parseInt(url.split("tid=")[1]), "https://hackforums.net/images/modern_bl/newfolder.gif", mTitle, 0, name, new ArrayList<InboxDisplayFragment.ThreadDetails>());
//                THREADS.add(0, thread);
//                InboxDisplayFragment.mAdapter.notifyItemInserted(THREADS.size() - 1);

                //open thread
                Intent i = new Intent(EditorActivity.this, ThreadDisplayActivity.class);
                i.putExtra("THREAD_ID", Integer.parseInt(url.split("tid=")[1]));
                i.putExtra("THREAD_NAME", mTitle);
                i.putExtra("FORUM_PAGE", returnToPage);

                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                overridePendingTransition(0,0);
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }else{
                //fail
                AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                alertDialog.setTitle("Error posting thread");
                alertDialog.setMessage("Something went wrong. Please exit and try again.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    //post functionality
    public class initPost extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String url = browser.getUrl();
            if(url.contains("showthread.php")) {
                //normal reply
                url = url.replace("showthread.php", "newreply.php").split("&")[0];
            }else{
                //quoting user
                url = url.split("&")[0];
            }
            browser.loadUrl(url);
            Log.e("asdf", url);
            browser.html = null;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String message = ((EditText) findViewById(R.id.editor)).getText().toString();
            //check to prevent multiple postings
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null){
                new postReply(message).execute();
            }
        }
    }
    public class postReply extends AsyncTask<Void, Void, Void> {

        private final String mMessage;
        postReply(String message) {
            //replace line breaks with literal \n for JavaScript
            message = message.replaceAll("\n", "\\\\n");
            message = message.replace("'", "\\'");
            mMessage = message;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null) {
                browser.loadUrl("javascript:document.getElementsByName('message_new')[0].value = '" + mMessage + "';" +
                                "document.getElementsByName('submit')[0].click();");
                browser.html = null;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);
            if (browser.getWebViewUrl().contains("showthread.php")) {
                //success!
                Toast.makeText(getApplicationContext(), "Reply posted!", Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                //fail
                AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                alertDialog.setTitle("Error posting reply");
                alertDialog.setMessage("Something went wrong. Please exit and try again.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    //pm functionality
    public class initPM extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String url = "https://hackforums.net/private.php?action=send";
            browser.loadUrl(url);
            browser.html = null;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String recipients = ((EditText) findViewById(R.id.recipients)).getText().toString();
            String title = ((EditText) findViewById(R.id.title)).getText().toString();
            String message = ((EditText) findViewById(R.id.editor)).getText().toString();
            //check to prevent multiple postings
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null){
                new sendPM(recipients, title, message).execute();
            }
        }
    }

    public class sendPM extends AsyncTask<Void, Void, Void> {

        private final String mRecipients, mTitle, mMessage;
        sendPM(String recipients, String title, String message) {
            //replace line breaks with literal \n for JavaScript
            mRecipients = recipients;
            mTitle = title;
            message = message.replaceAll("\n", "\\\\n");
            message = message.replace("'", "\\'");
            mMessage = message;
        }

        @Override
        protected void onPreExecute(){
            HFBrowser browser = (HFBrowser) getApplicationContext();
            if (browser.html != null) {
                browser.loadUrl("javascript:document.getElementsByName('to')[0].value = '" + mRecipients + "';" +
                                "document.getElementsByName('subject')[0].value = '" + mTitle + "';" +
                                "document.getElementsByName('message_new')[0].value='" + mMessage + "';" +
                                "document.getElementsByName('submit')[0].click();");
                browser.html = null;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HFBrowser browser = (HFBrowser) getApplicationContext();
                while(browser.html == null){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            HFBrowser browser = (HFBrowser) getApplicationContext();
            String s = browser.html;
            Document doc = Jsoup.parse(s);
            if (doc.select(".error").size() == 0) {
                //success!
                Toast.makeText(getApplicationContext(), "PM sent!", Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                //fail
                String error = doc.select(".error li").first().text();
                AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                alertDialog.setTitle("Error sending PM");
                alertDialog.setMessage(error);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        final HFBrowser browser = (HFBrowser) getApplicationContext();
        new AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("Your changes will not be saved. Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("asdf", "editor -> " + returnToPage);
                        browser.setUrl(returnToPage);
                        if(action.equals("thread")){
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                        }else{
                            //reply
                            finish();
                        }
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compose, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}