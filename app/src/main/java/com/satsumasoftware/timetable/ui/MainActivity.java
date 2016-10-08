package com.satsumasoftware.timetable.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.util.AssignmentUtils;
import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.db.util.ExamUtils;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.HomeCardsAdapter;
import com.satsumasoftware.timetable.ui.card.AssignmentsCard;
import com.satsumasoftware.timetable.ui.card.ClassesCard;
import com.satsumasoftware.timetable.ui.card.ExamsCard;
import com.satsumasoftware.timetable.ui.card.HomeCard;
import com.satsumasoftware.timetable.util.DateUtils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Timetable currentTimetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert currentTimetable != null;

        assert getSupportActionBar() != null;
        getSupportActionBar().setSubtitle(currentTimetable.getDisplayedName());

        configureSignIn();

        setupLayout();
    }

    private void configureSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic profile.
        // ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Setup the button
        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Setup the other buttons
        Button signOutButton = (Button) findViewById(R.id.btn_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        Button revokeAccessButton = (Button) findViewById(R.id.btn_revoke_access);
        revokeAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeAccess();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateSignInUI(false);
                    }
                }
        );
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateSignInUI(false);
                    }
                }
        );
    }

    private void updateSignInUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.btn_sign_in).setVisibility(View.GONE);
            findViewById(R.id.btn_sign_out).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_revoke_access).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btn_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_sign_out).setVisibility(View.GONE);
            findViewById(R.id.btn_revoke_access).setVisibility(View.GONE);
        }
    }

    private void setupLayout() {
        TextView infoBar = (TextView) findViewById(R.id.text_infoBar);
        infoBar.setVisibility(View.VISIBLE);

        String todayText = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy"));
        infoBar.setText(todayText);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<HomeCard> cards = new ArrayList<>();

        cards.add(new ClassesCard(this, getClassesToday()));
        cards.add(new AssignmentsCard(this, getAssignments()));

        ArrayList<Exam> exams = getExams();
        if (!exams.isEmpty()) {
            cards.add(new ExamsCard(this, getExams()));
        }

        recyclerView.setAdapter(new HomeCardsAdapter(this, cards));
    }

    private ArrayList<ClassTime> getClassesToday() {
        LocalDate now = LocalDate.now();
        DayOfWeek today = now.getDayOfWeek();
        int weekNumber = DateUtils.findWeekNumber(getApplication());

        ArrayList<ClassTime> classTimes =
                ClassUtils.getClassTimesForDay(this, today, weekNumber, now);

        Collections.sort(classTimes, new Comparator<ClassTime>() {
            @Override
            public int compare(ClassTime ct1, ClassTime ct2) {
                return ct1.getStartTime().compareTo(ct2.getStartTime());
            }
        });

        return classTimes;
    }

    private ArrayList<Assignment> getAssignments() {
        ArrayList<Assignment> assignments = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (Assignment assignment : AssignmentUtils.getAssignments(this, getApplication())) {
            LocalDate dueDate = assignment.getDueDate();

            boolean dueInNextThreeDays = dueDate.isAfter(now) && dueDate.isBefore(now.plusDays(4));

            if (assignment.isOverdue() || dueDate.isEqual(now) || dueInNextThreeDays) {
                assignments.add(assignment);  // overdue (incomplete) assignment
            }
        }

        Collections.sort(assignments, new Comparator<Assignment>() {
            @Override
            public int compare(Assignment a1, Assignment a2) {
                LocalDate date1 = a1.getDueDate();
                LocalDate date2 = a2.getDueDate();
                return date1.compareTo(date2);
            }
        });

        return assignments;
    }

    private ArrayList<Exam> getExams() {
        ArrayList<Exam> exams = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Exam exam : ExamUtils.getExams(this, getApplication())) {
            LocalDateTime examDateTime = exam.makeDateTimeObject();

            if (!examDateTime.isBefore(now) && examDateTime.isBefore(now.plusWeeks(6))) {
                exams.add(exam);
            }
        }

        Collections.sort(exams, new Comparator<Exam>() {
            @Override
            public int compare(Exam exam1, Exam exam2) {
                LocalDateTime dateTime1 = exam1.makeDateTimeObject();
                LocalDateTime dateTime2 = exam2.makeDateTimeObject();
                return dateTime1.compareTo(dateTime2);
            }
        });

        return exams;
    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(LOG_TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI
            GoogleSignInAccount account = result.getSignInAccount();
            Toast.makeText(this, "Account " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            updateSignInUI(true);
        } else {
            // Signed out, show unauthenticated UI
            updateSignInUI(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed: " + connectionResult);
    }

    @Override
    protected Toolbar getSelfToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected DrawerLayout getSelfDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_HOME;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
