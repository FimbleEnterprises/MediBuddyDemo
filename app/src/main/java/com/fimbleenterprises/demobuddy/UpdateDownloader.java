package com.fimbleenterprises.demobuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediBuddyUpdate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import androidx.core.content.FileProvider;

public class UpdateDownloader extends AsyncTask<String, String, String> {

    private static final String TAG = "UpdateDownloader";
    /*
    public final static String FILE_NAME = "MileBuddy.apk";
    public final static String FILE_BASE_PATH = "file://";
    public final static String MIME_TYPE = "application/vnd.android.package-archive";
    */
    public final static String PROVIDER_PATH = ".provider";
    public final static String APP_INSTALL_PATH = "\"application/vnd.android.package-archive\"";

    // Get references to our dialog itself, its progress bar and the
    // textview showing status
    MyProgressDialog myProgressDialog;
    Activity activity;

    public boolean DOWNLOADING;

    // For use on our progress bar
    private int progPct;
    private int progTotal;
    private long total = 0;
    private ArrayList<Long> samples;
    private long lastTotal = 0;
    private long thisTotal = 0;
    private final int SPEED_CHECK_MAX_SAMPLES = 10;
    private final int SPEED_CHECK_INTERVAL = 500;
    private long downloadSpeed = 0;
    private String strDownloaded = "";
    private String strDownloadSpeed = "";
    private URL url;
    private URLConnection connection;
    private OutputStream output = null;
    private InputStream input = null;
    private String downloadFailureReason = "";
    private boolean cancelRequested = false;
    MediBuddyUpdate update;
    MyPreferencesHelper options;
    boolean silently;

    public UpdateDownloader(Activity activity, MediBuddyUpdate update, boolean silently) {
        this.activity = activity;
        this.options = new MyPreferencesHelper(activity);
        this.silently = silently;
        this.update = update;
        this.myProgressDialog = new MyProgressDialog(activity, MyProgressDialog.PROGRESS_TYPE);

        try {
            this.url = new URL(update.downloadLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this.execute("", "", "");
    }

    private Handler handler = new Handler();

    private final Runnable speedometer = new Runnable() {
        @Override
        public void run() {
            // Log.d(TAG, "Calculating download speed...");
            thisTotal = total;
            long difference = Math.abs(thisTotal - lastTotal);

            if (thisTotal > 0) {
                samples.add(difference);

                if (samples.size() > SPEED_CHECK_MAX_SAMPLES) {
                    samples.remove(0);
                    Log.d(TAG, "Removed a sample from the array");
                }
                long sum = 0;

                for (long sample : samples) {
                    sum += sample;
                }
                long avg = sum / samples.size();

                downloadSpeed = (avg * 2);
                String spd = "";

                // Download speed
                strDownloadSpeed = Formatter.formatShortFileSize(activity,
                        downloadSpeed);
                strDownloadSpeed = strDownloadSpeed.replace("KB", " KB").replace("MB", " MB") + "/sec";

                Log.d(TAG, "Speed is: " + spd + " /second");
                lastTotal = thisTotal;


            } else {
                Log.d(TAG, "Speedo is waiting for the download to begin.");
            }
            Log.d(TAG, "run: Preparing to download");
            handler.postDelayed(this, SPEED_CHECK_INTERVAL);
        }
    };

    @Override
    protected void onPreExecute() {

        cancelRequested = false;
        DOWNLOADING = true;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        alertDialog.setView(inflater.inflate(R.layout.update_app_dialog, null))
                // .setCancelable(false)
                .setIcon(R.drawable.ms64).setTitle("Retrieving update...");
        alertDialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(activity, "Cancel requested.", Toast.LENGTH_SHORT).show();
                cancelRequested = true;
            }
        });

        myProgressDialog.setTitleText("Downloading New Version");
        if (! this.silently) {
            myProgressDialog.show();
        }

        samples = new ArrayList<Long>();
        speedometer.run();

    }

    @Override
    protected String doInBackground(String... params) {
        // instantiate a remote connection using the static varialbe
        // representing the url

        try {

            connection = url.openConnection();
            connection.connect();
            int fileLength = connection.getContentLength();

            output = new FileOutputStream(new File(Helpers.Files.AppUpdates.getDirectory().getPath(),
                    update.version + ".apk"));
            // download the file using the passed url to a filestream object
            input = new BufferedInputStream(url.openStream());
            // prepare a filestream that we'll use to write the file to the
            // filesystem

            byte data[] = new byte[8000];
            int count = 0;
            total = 0;

            // Zero out our progress total and percentage
            progTotal = 0;
            progPct = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                progTotal += count;
                progPct = Math.abs(progTotal * 100 / fileLength);

                // Log.d("UpdateDownloader.doInBackground",
                // "Downloaded: " + (total / 1000 ) +
                // " KB of the buddy_version.xml file...");
                output.write(data, 0, count);

                if (cancelRequested) resetConnection();

                boolean shouldReport = (count % 3 == 0);
                if (shouldReport) {
                    // Total downloaded
                    String downloaded = Long.toString(Helpers.Files.convertBytesToMb(total));
                    strDownloaded = "Downloaded " + downloaded.replace("MB", " MB").replace("KB", " KB" + " (" + progPct + ")");
                    publishProgress(strDownloaded);
                }

                // SystemClock.sleep(1);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloadFailureReason = e.toString();
            options.clearMileBuddyUpdate();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            downloadFailureReason = e.toString();
            options.clearMileBuddyUpdate();
        } catch (IOException e) {
            e.printStackTrace();
            downloadFailureReason = e.toString();
            options.clearMileBuddyUpdate();
        } catch (IllegalStateException e) {
            options.clearMileBuddyUpdate();
            try {
                output.flush();
                output.close();
                input.close();
            } catch (FileNotFoundException fof) {
                // Nothing
            } catch (IOException io) {
                options.clearMileBuddyUpdate();
            }
            downloadFailureReason = "";
            options.clearMileBuddyUpdate();
            return null;
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file = new File(Helpers.Files.AppUpdates.getDirectory().getPath(),
                update.version + ".apk");
        if (file.exists()) {
            options.clearMileBuddyUpdate();
            options.setMediBuddyUpdate(update.json);
        }

        DOWNLOADING = false;
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        DOWNLOADING = false;

        if (downloadFailureReason != "") {
            myProgressDialog.setTitle("Download failed");
            myProgressDialog.setContentText(downloadFailureReason);
            myProgressDialog.dismiss();
        } else {
            myProgressDialog.setContentText("A new version is available!");
            Log.i(TAG, "onPostExecute | Downloaded!  Will update preferences that a download is available for the next restart!");
            options.setMediBuddyUpdate(update.json);
            install(!silently, activity, update);
            myProgressDialog.dismiss();
        }
        resetConnection();
    }

    @Override
    protected void onProgressUpdate(String... progress) {

        Log.d("onProgressUpdate", Helpers.Files.convertBytesToKb(progTotal) + " MB completed - Speed: " + downloadSpeed + " bytes/sec");

        if (cancelRequested) {
            Log.w(TAG, "onCancelled: Cancel requested!");
            this.cancel(true);
        }

        myProgressDialog.setContentText(strDownloaded + " MB (" + strDownloadSpeed + ")");

    }

    @Override
    protected void onCancelled(String s) {
        DOWNLOADING = false;
        Log.w(TAG, "Operation was cancelled, yo!");
        Toast.makeText(activity, "Update cancelled", Toast.LENGTH_LONG).show();
        resetConnection();
        super.onCancelled(s);
    }

    public boolean resetConnection() {

        try {
            output.flush();
            output.close();
            input.close();
            output = null;
            input = null;
            connection = null;
            url = null;
            handler.removeCallbacks(speedometer);
        } catch (Exception e) {
            Log.d(TAG, "");
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "Connection was reset");
        return true;
    }

    public static void install(boolean show, final Activity activity, final MediBuddyUpdate update) {

        final MyPreferencesHelper options = new MyPreferencesHelper(activity);

        if (show) {

            final Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.update_app_dialog);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    try {
                        MediBuddyUpdate.deleteAllLocallyAvailableUpdates();
                    } catch (Exception e) {
                        Toast.makeText(activity, "Failed to remove local updates.  Call Matt!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            TextView txtChangeLog = dialog.findViewById(R.id.txtChangelog);
            txtChangeLog.setText("Ver: " + options.getMediBuddyUpdate().version + "\n"
                    + options.getMediBuddyUpdate().changelog);
            Button btnInstall = dialog.findViewById(R.id.btnInstall);
            btnInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    final File originalFile = new File(Helpers.Files.AppUpdates.getDirectory().getPath(), update.version + ".apk");
                    // Creates a staging directory to install from.
                    final File tempDir = Helpers.Files.AppUpdates.createStagingDir();

                    // Prepare the update file for copy to the staging directory.
                    final File tempApp = new File(tempDir.getAbsolutePath(),update.version + ".apk");
                    if (tempApp.exists()) {
                        tempApp.delete();
                    }

                    // Copy the update to the staging directory as a ready to execute file.
                    if (!Helpers.Files.copy(originalFile, tempApp)) {
                        Toast.makeText(activity, "Failed to install (the copy part failed, dawg).", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "install: Failed to copy the update file to a temp location for installation");
                        MediBuddyUpdate.deleteAllLocallyAvailableUpdates();
                        return;
                    } else {
                        Log.i(TAG, "install Ready to install!");
                    }

                    // delete the downloaded, original file since it should now have been copied to
                    // a temporary location.
                    MediBuddyUpdate.deleteAllLocallyAvailableUpdates();

                    // Install the app according to the OS' best practices.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(activity,
                                BuildConfig.APPLICATION_ID + PROVIDER_PATH, tempApp);
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                        install.setData(contentUri);
                        activity.startActivity(install);
                    } else {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        install.setDataAndType(Uri.fromFile(tempApp), APP_INSTALL_PATH);
                        activity.startActivity(install);
                    }
                }
            });
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        MediBuddyUpdate.deleteAllLocallyAvailableUpdates();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            if (show) {
                dialog.show();
            }
        }
    }
    
}

















































































