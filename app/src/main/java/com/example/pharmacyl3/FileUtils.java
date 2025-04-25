package com.example.pharmacyl3;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    // Copies the content URI to a file in the app's internal storage and returns the absolute path
    public static String copyUriToInternalStorage(Context context, Uri uri, String fileName) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("Unable to open input stream from URI");
        File file = new File(context.getFilesDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return file.getAbsolutePath();
    }
}
