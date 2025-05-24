package com.example.pharmacyl3;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportImportUtils {
    private static final String TAG = "ExportImportUtils";
    private static final String EXPORT_DIR = "exports";
    public static final String IMAGES_DIR = "images";

    // Create a temporary directory for export
    public static File createTempExportDir(Context context) throws IOException {
        File exportDir = new File(context.getExternalFilesDir(null), "temp_export_" + System.currentTimeMillis());
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                throw new IOException("Failed to create export directory");
            }
        }
        return exportDir;
    }

    // Create images directory inside export directory
    public static File createImagesDir(File exportDir) throws IOException {
        File imagesDir = new File(exportDir, IMAGES_DIR);
        if (!imagesDir.exists()) {
            if (!imagesDir.mkdirs()) {
                throw new IOException("Failed to create images directory");
            }
        }
        return imagesDir;
    }

    // Copy file to destination
    public static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new java.io.FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    // Copy content URI to file
    public static void copyUriToFile(Context context, Uri uri, File dstFile) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dstFile)) {
            if (in == null) throw new IOException("Cannot open input stream from URI");
            
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    // Create a zip file from directory
    public static File createZipFromDirectory(File sourceDir, String zipName) throws IOException {
        File zipFile = new File(sourceDir.getParentFile(), zipName + ".zip");
        try (ZipFile zip = new ZipFile(zipFile)) {
            // Add files to zip
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        zip.addFolder(file);
                    } else {
                        zip.addFile(file);
                    }
                }
            }
        }
        return zipFile;
    }

    // Get export filename with timestamp
    public static String getExportFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "pharmacy_export_" + timeStamp;
    }

    // Clean up temporary files
    public static void cleanupTempFiles(File... files) {
        for (File file : files) {
            if (file != null && file.exists()) {
                if (file.isDirectory()) {
                    deleteRecursive(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    // Extract zip file to destination directory
    public static File extractZip(Context context, Uri zipUri, String destinationFolderName) throws IOException {
        // Create destination directory
        File destDir = new File(context.getExternalFilesDir(null), destinationFolderName);
        if (destDir.exists()) {
            deleteRecursive(destDir);
        }
        destDir.mkdirs();

        // Copy zip to internal storage first
        File tempZip = new File(context.getCacheDir(), "temp_import.zip");
        try (InputStream in = context.getContentResolver().openInputStream(zipUri);
             OutputStream out = new FileOutputStream(tempZip)) {
            if (in == null) throw new IOException("Cannot open input stream from URI");
            
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        // Extract zip
        try (ZipFile zipFile = new ZipFile(tempZip)) {
            zipFile.extractAll(destDir.getAbsolutePath());
        } finally {
            tempZip.delete(); // Clean up temp zip file
        }

        return destDir;
    }
}
