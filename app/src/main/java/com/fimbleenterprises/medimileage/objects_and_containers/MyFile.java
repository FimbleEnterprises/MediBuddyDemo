package com.fimbleenterprises.medimileage.objects_and_containers;

import java.io.File;
import java.net.URI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class MyFile extends File {

    public MyFile(@NonNull String pathname) {
        super(pathname);
    }

    public MyFile(@Nullable String parent, @NonNull String child) {
        super(parent, child);
    }

    public MyFile(@Nullable File parent, @NonNull String child) {
        super(parent, child);
    }

    public MyFile(@NonNull URI uri) {
        super(uri);
    }

}
