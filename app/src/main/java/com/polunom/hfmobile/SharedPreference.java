package com.polunom.hfmobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;

public class SharedPreference {

    public static final String PREFS_NAME = "HFMobile";
    public static final String THREADS = "HIDDEN_THREADS";

    public SharedPreference() {
        super();
    }

    public void saveThreads(Context context, List<ForumThread> threads) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();

        String jsonThreads = gson.toJson(threads);
        editor.putString(THREADS, jsonThreads);
        editor.commit();
    }

    public void addThread(Context context, ForumThread product) {
        List<ForumThread> threads = getHiddenThreads(context);
        if (threads == null)
            threads = new ArrayList<ForumThread>();
        threads.add(product);
        saveThreads(context, threads);
    }

    public void removeThread(Context context, ForumThread product) {
        ArrayList<ForumThread> threads = getHiddenThreads(context);
        if (threads != null) {
            for(int i = 0; i < threads.size(); i++){
                ForumThread ft = threads.get(i);
                if(ft.id == product.id){
                    threads.remove(i);
                    i--;
                }
            }
            saveThreads(context, threads);
        }
    }

    public ArrayList<ForumThread> getHiddenThreads(Context context) {
        SharedPreferences settings;
        List<ForumThread> threads;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(THREADS)) {
            String jsonThreads = settings.getString(THREADS, null);
            Gson gson = new Gson();
            ForumThread[] threadItems = gson.fromJson(jsonThreads,
                    ForumThread[].class);

            threads = Arrays.asList(threadItems);
            threads = new ArrayList<>(threads);
        } else
            return null;

        return (ArrayList<ForumThread>) threads;
    }
}