package com.fimbleenterprises.demobuddy;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;

/***************************************************************************************************

            THIS IS NOT USED AS I CONVERTED IT FROM KOTLIN AND INCORPORATED IT
            INTO THE UpdateDownloader CLASS (THE INSTALL PORTION ANYWAY).  BUT
            FUCK IF IT DOESN'T WORK ON ANDROID 10!

 **************************************************************************************************/

class DownloadController {
    public final static String FILE_NAME = "MileBuddy.apk";
    public final static String FILE_BASE_PATH = "file://";
    public final static String MIME_TYPE = "application/vnd.android.package-archive";
    public final static String PROVIDER_PATH = ".provider";
    public final static String APP_INSTALL_PATH = "\"application/vnd.android.package-archive\"";
    public String url = null;
    public Activity activity;

    public DownloadController(Activity activity, String url) {
        this.url = url;
        this.activity = activity;
    }

    void enqueueDownload() {
        String  destination =
                activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
        destination += FILE_NAME;
        Uri uri = Uri.parse(FILE_BASE_PATH + destination);
        File file = new File(destination);
        if (file.exists()) file.delete();
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setMimeType(MIME_TYPE);
        request.setTitle("MileBuddy");
        request.setDescription("downloading...");
        // set destination
        request.setDestinationUri(uri);
        showInstallOption(destination, uri);
        // Enqueue a new download and same the referenceId
        downloadManager.enqueue(request);
        Toast.makeText(activity, "downloading...", Toast.LENGTH_LONG)
                .show();
    }
    private void showInstallOption(final String destination, final Uri uri) {

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri contentUri = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                            new File(destination)
                    );
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.setData(contentUri);
                    context.startActivity(install);
                    context.unregisterReceiver(this);
                    // finish()
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri, APP_INSTALL_PATH);
                    context.startActivity(install);
                    context.unregisterReceiver(this);
                    // finish()
                }
            }
        };
        activity.registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}