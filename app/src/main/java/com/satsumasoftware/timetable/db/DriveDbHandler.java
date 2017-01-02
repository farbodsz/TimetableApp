package com.satsumasoftware.timetable.db;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
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

    public static void saveToDrive(final GoogleApiClient googleApiClient) {
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

                        Log.v(LOG_TAG, "Successfully ran query for " + FILE_NAME + " and found " +
                                count + " results");

                        switch (count) {
                            case 0:
                                // Create the database on Google Drive if it doesn't exist already
                                Log.d(LOG_TAG, "No existing database found on Google Drive");
                                createDatabaseOnDrive(googleApiClient);
                                break;

                            case 1:
                                // Update the database if it exists on Drive
                                Log.d(LOG_TAG, "Found database on Google Drive");

                                Metadata metadata = metadataBufferResult.getMetadataBuffer().get(0);
                                DriveId driveId = metadata.getDriveId();

                                updateDatabase(googleApiClient, driveId.asDriveFile());
                                break;

                            default:
                                Log.e(LOG_TAG, "App folder contains more than one database file! " +
                                        "Found " + count + " matching results.");
                                break;
                        }
                    }
                });
    }

    private static void createDatabaseOnDrive(final GoogleApiClient googleApiClient) {
        Log.d(LOG_TAG, "Creating the database on Google Drive...");

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
        // Write file to contents
        driveContents = writeDatabaseContents(driveContents);

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

    private static void updateDatabase(final GoogleApiClient googleApiClient, DriveFile file) {
        Log.d(LOG_TAG, "Updating Google Drive database...");

        file.open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(LOG_TAG, "Couldn't open file");
                    return;
                }

                DriveContents driveContents = result.getDriveContents();
                writeDatabaseContents(driveContents);
                driveContents.commit(googleApiClient, null);

                Log.i(LOG_TAG, "Updated database on Drive");
            }
        });
    }

    private static DriveContents writeDatabaseContents(DriveContents driveContents) {
        // See http://stackoverflow.com/a/33610727/4230345
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

        return driveContents;
    }

}
