package com.satsumasoftware.timetable.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.satsumasoftware.timetable.TimetableApplication;

public class SignOutActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String LOG_TAG = "SignOutActivity";

    public static final String EXTRA_REVOKE_ACCESS = "extra_revoke_access";

    private GoogleApiClient mGoogleApiClient;

    private boolean mRevokeAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRevokeAccess = getIntent().hasExtra(EXTRA_REVOKE_ACCESS) &&
                getIntent().getExtras().getBoolean(EXTRA_REVOKE_ACCESS);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRevokeAccess) {
            Log.i(LOG_TAG, "Signing out the current user and revoking access");
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(this);
            Toast.makeText(this, "Revoked access and signed out", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(LOG_TAG, "Signing out the current user");
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(this);
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Unable to connect to Google's services", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Unable to connect to Google's services", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onResult(@NonNull Status status) {
        // Invoked after signing out or revoking access
        ((TimetableApplication) getApplication()).setSignInAccount(null);

        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
