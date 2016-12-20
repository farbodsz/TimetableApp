package com.satsumasoftware.timetable.db;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class DriveDbHandler {

    private static final String LOG_TAG = "DriveDbHandler";

    private static final String PACKAGE_NAME = "com.satsumasoftware.timetable";

    private static final String DATABASE_PATH =
            "/data/data/" + PACKAGE_NAME + "/databases/" + TimetableDbHelper.DATABASE_NAME;

    private static final String FILE_NAME = TimetableDbHelper.DATABASE_NAME;
    private static final String MIME_TYPE = "application/x-sqlite-3";

    private DriveDbHandler() {
    }

    public static void tryCreatingDbOnDrive(final GoogleApiClient googleApiClient) {
        // We need to check if the database already exists on Google Drive. If so, we won't create
        // it again.

        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.TITLE, FILE_NAME),
                        Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE)))
                .build();
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);

        appFolder.queryChildren(googleApiClient, query).setResultCallback(
                new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        if (!metadataBufferResult.getStatus().isSuccess()) {
                            Log.e(LOG_TAG, "Query for " + FILE_NAME + " unsuccessful!");
                            return;
                        }

                        int count = metadataBufferResult.getMetadataBuffer().getCount();

                        Log.d(LOG_TAG, "Successfully ran query for " + FILE_NAME + " and found " +
                                count + " results");

                        if (count > 1) {
                            Log.e(LOG_TAG, "App folder contains more than one database file! " +
                                    "Found " + count + " matching results.");
                            return;
                        }

                        // Create the database on Google Drive if it doesn't exist already
                        if (count == 0) {
                            Log.d(LOG_TAG, "No existing database found on Google Drive");
                            saveToDrive(googleApiClient);
                        }
                    }
                });
    }

    private static void saveToDrive(final GoogleApiClient googleApiClient) {
        Log.d(LOG_TAG, "Starting to save to drive...");

        // Create content from file
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            Log.w(LOG_TAG, "Drive contents result not a success! " +
                                    "Not saving data to drive.");
                            return;
                        }

                        Log.d(LOG_TAG, "Created drive contents for file");
                        createNewFile(googleApiClient, driveContentsResult.getDriveContents());
                    }
                });
    }

    private static void createNewFile(GoogleApiClient googleApiClient, DriveContents driveContents) {
        // Write file to contents (see http://stackoverflow.com/a/33610727/4230345)
        File file = new File(DATABASE_PATH);
        OutputStream outputStream = driveContents.getOutputStream();
        try {
            InputStream inputStream = new FileInputStream(file);
            byte[] buf = new byte[4096];
            int c;
            while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
                outputStream.write(buf, 0, c);
                outputStream.flush();
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Written file to output stream of drive contents");

        // Create metadata
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(FILE_NAME)
                .setMimeType(MIME_TYPE)
                .build();

        // Create the file on Google Drive
        DriveFolder folder = Drive.DriveApi.getAppFolder(googleApiClient);
        folder.createFile(googleApiClient, metadataChangeSet, driveContents).setResultCallback(
                new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                if (!driveFileResult.getStatus().isSuccess()) {
                    Log.w(LOG_TAG, "File did not get created in Google Drive!");
                    return;
                }

                Log.i(LOG_TAG, "Successfully created file in Google Drive");
            }
        });
    }

}
