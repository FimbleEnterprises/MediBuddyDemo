package com.fimbleenterprises.medimileage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RestoreDbActivity extends AppCompatActivity {

    private static final String TAG = "RestoreDbActivity";
    Context context;
    MySettingsHelper options;
    final ArrayList<File> backups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_db);
        context = this;
        populate();
        options = new MySettingsHelper(context);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.restore_db, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_delete_backups :
                MyYesNoDialog.showDialog(context, "Are you sure you want to delete all backups?", new MyYesNoDialog.YesNoListener() {
                    @Override
                    public void onYes() {
                        int count = 0;
                        for (File file : Environment.getExternalStorageDirectory().listFiles()) {
                            if (file.getName().endsWith(".db")) {
                                if (file.delete()) {
                                    count++;
                                }
                            }
                        }
                        Toast.makeText(context, "Deleted " + count + " backups.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNo() {

                    }
                });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void populate() {
        final ArrayList<String> filenames = new ArrayList<>();

        for (File file : Helpers.Files.getBackupDirectory().listFiles()) {
            if (file.getName().endsWith(".db")) {
                filenames.add(file.getName());
                backups.add(file);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.backup_list);
        AdapterDbBackups adapter;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterDbBackups(this, filenames);
        adapter.setClickListener(new AdapterDbBackups.ItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.restore_options);
                Button btnDelete = dialog.findViewById(R.id.btnDelete);
                Button btnRestore = dialog.findViewById(R.id.btnSubmit);
                dialog.setTitle("");
                dialog.setCancelable(true);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File file = backups.get(position);
                        if (file.exists()) {
                            file.delete();
                            populate();
                        }
                        dialog.dismiss();
                    }
                });
                btnRestore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyYesNoDialog.showDialog(context, "Are you sure you want to restore this database?",
                                new MyYesNoDialog.YesNoListener() {
                            @Override
                            public void onYes() {
                                try {
                                    restore(backups.get(position));
                                    Toast.makeText(context, "Database was restored.", Toast.LENGTH_SHORT).show();
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onNo() {

                            }
                        });
                    }
                });
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                dialog.show();

            }
        });
        recyclerView.setAdapter(adapter);
    }

    void restore(File file) throws FileNotFoundException {
        if (file.exists()) {

            SQLiteDatabase database = new MySqlDatasource().getDatabase();
            database.close();

            File source = file;
            File destination = new File(database.getPath());

            try {
                if (destination.canWrite()) {
                    Log.d("TAG", "DatabaseHandler: can write in sd");
                    FileChannel src = new FileInputStream(source).getChannel();
                    @SuppressWarnings("resource")
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    long bytes = dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    if (bytes > 0) {
                        Toast.makeText(context, "Restored", Toast.LENGTH_SHORT).show();

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }























































}
