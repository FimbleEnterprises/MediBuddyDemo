/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.fimbleenterprises.demobuddy;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Matt Weber, 21/Mar/22
 * Replacement for the old <a href="https://developer.android.com/reference/android/os/AsyncTask"> AsyncTask</a>
 * class that got deprecated in API 30.  Has onPreExecute(), onDoInBackground(), onProgress() and
 * onPostExecute() just like AsyncTask does/used to.<br/>
 * <br/>
 * <b>Example:</b>
 * <pre>
 * new MyAsyncTask() {
 *
 *     &#64Override
 *     public void onPreExecute() {
 *         // Runs (and finishes) before background task begins
 *
 *     }
 *
 *     &#64Override
 *     public void doInBackground() {
 *         // Do background, non-UI work here. Can optionally call "reportProgress(object)"
 *         // to access UI thread.
 *
 *     }
 *
 *     &#64Override
 *     public void onProgress(Object msg) {
 *         // Code here will run on the main (UI) thread.
 *
 *     }
 *
 *     &#64Override
 *     public void onPostExecute() {
 *         // Code here will run on the main thread.
 *
 *     }
 * }.execute();
 * </pre><br/>
 * <i>Inspired by:<br/>
 * https://stackoverflow.com/a/68859991/2097893</br></i>
 */
@SuppressWarnings("unused")
public abstract class MyAsyncTask {
    private final ExecutorService executors;
    private final Handler handler;
    private String bgThreadName;

    public String getBgThreadName() {
        return bgThreadName;
    }

    public void setBgThreadName(String bgThreadName) {
        this.bgThreadName = bgThreadName;
    }

    /**
     * Replacement for the old <a href="https://developer.android.com/reference/android/os/AsyncTask"> AsyncTask</a>
     * class that got deprecated in API 30.  Has onPreExecute(), onDoInBackground(), onProgress() and
     * onPostExecute() just like AsyncTask does/used to.<br/>
     * <br/>
     * <b>Example:</b>
     * <pre>
     * new MyAsyncTask() {
     *
     *     &#64Override
     *     public void onPreExecute() {
     *         // Runs (and finishes) before background task begins
     *
     *     }
     *
     *     &#64Override
     *     public void doInBackground() {
     *         // Do background, non-UI work here. Can optionally call "reportProgress(object)"
     *         // to access UI thread.
     *
     *     }
     *
     *     &#64Override
     *     public void onProgress(Object msg) {
     *         // Code here will run on the main (UI) thread.
     *
     *     }
     *
     *     &#64Override
     *     public void onPostExecute() {
     *         // Code here will run on the main thread.
     *
     *     }
     * }.execute();
     * </pre><br/>
     * <i>Inspired by:<br/>
     * <a href="https://stackoverflow.com/a/68859991/2097893">https://stackoverflow.com/a/68859991/2097893</a></br></i>
     */
    public MyAsyncTask() {
        this.executors = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper(), msg -> false);
    }

    /**
     * Replacement for the old <a href="https://developer.android.com/reference/android/os/AsyncTask"> AsyncTask</a>
     * class that got deprecated in API 30.  Has onPreExecute(), onDoInBackground(), onProgress() and
     * onPostExecute() just like AsyncTask does/used to.<br/>
     * <br/>
     * <b>Example:</b>
     * <pre>
     * new MyAsyncTask() {
     *
     *     &#64Override
     *     public void onPreExecute() {
     *         // Runs (and finishes) before background task begins
     *
     *     }
     *
     *     &#64Override
     *     public void doInBackground() {
     *         // Do background, non-UI work here. Can optionally call "reportProgress(object)"
     *         // to access UI thread.
     *
     *     }
     *
     *     &#64Override
     *     public void onProgress(Object msg) {
     *         // Code here will run on the main (UI) thread.
     *
     *     }
     *
     *     &#64Override
     *     public void onPostExecute() {
     *         // Code here will run on the main thread.
     *
     *     }
     * }.execute();
     * </pre><br/>
     * <i>Inspired by:<br/>
     * <a href="https://stackoverflow.com/a/68859991/2097893">https://stackoverflow.com/a/68859991/2097893</a></br></i>
     */
    public MyAsyncTask(String bgThreadName) {
        this.executors = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper(), msg -> false);
        this.bgThreadName = bgThreadName;
    }



    private void startBackground() {
        // Still on main thread - code in this block will finish before it moves on to the
        // background thread.
        onPreExecute();

        // Now we do the background stuff on a non-UI thread.
        // This lambda represents what would otherwise be an anonymous function e.g.:
        // executors.execute(new Runnable() {
        //     @Override
        //     public void run() {
        //          my code etc.
        //     }
        // });
        executors.execute(() -> {

            // Giving this thread a name because it seems like a cool idea.
            if (this.bgThreadName != null) {
                Thread.currentThread().setName(this.bgThreadName);
            }

            // Start doing the background work on a new thread.
            MyAsyncTask.this.doInBackground();

            // Now that the main work is done we call the onPostExecute method on the UI thread.
            // This is an anonymous class converted to a lambda and then to a method reference.  
            // This still looks so foreign to me!  I hope one day I will read this comment and laugh.
            handler.post(MyAsyncTask.this::onPostExecute);
        });
    }

    /**
     * Uses the class' shared final handler to call <i>handler.post(new Runnable() -> run() { <b>your code</b> })</i>
     * in order to get something executed on the main thread.
     * @param msg The object you want passed to the caller's anonymous "onProgress()" method.
     */
    public void reportProgress(Object msg) {
        // Using the handler created in the constructor we can post a new runnable on the UI thread.
        handler.post(() -> onProgress(msg));
    }

    /**
     * Starts the background task which in turn first executes all code in "onPreExecute()".
     */
    public void execute() {
        startBackground();
    }

    /**
     * Will return any object that was passed using "<code>reportProgress(object)</code> within the
     * <code>doInBackground()</code>.
     * @param msg Runs and returns the object passed using <code>reportProgress()</code> from doInBackground()
     */
    public abstract void onProgress(Object msg);

    /**
     * This code will be executed in toto before any code in <code>doInBackground()</code> is executed.
     */
    public abstract void onPreExecute();

    /**
     * Put code you want executed on a separate thread here.
     */
    public abstract void doInBackground();

    /**
     * This code will be executed upon completion of all code in <code>doInBackground()</code>
     */
    public abstract void onPostExecute();
}
