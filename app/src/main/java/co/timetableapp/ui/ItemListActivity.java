package co.timetableapp.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.db.handler.TimetableItemHandler;
import co.timetableapp.framework.TimetableItem;

/**
 * An activity providing default behavior for displaying a list of items.
 *
 * Subclasses must implement abstract methods to define behavior specific to the type of item being
 * displayed, such as how the list is sorted, and what placeholder view is used when there are no
 * items in the list.
 *
 * @param <T> the type of list items to be displayed
 */
abstract class ItemListActivity<T extends TimetableItem> extends NavigationDrawerActivity {

    /**
     * A data handler relevant to the type of items being displayed.
     *
     * @see #instantiateDataHandler()
     */
    TimetableItemHandler<T> mDataHandler;

    /**
     * @return a new data handler instance relevant to the type of items being displayed
     */
    abstract TimetableItemHandler<T> instantiateDataHandler();

    /**
     * A list of the data items being displayed of the generic type {@link T}
     */
    ArrayList<T> mItems;

    /**
     * The RecyclerView adapter used when displaying items to the list.
     */
    RecyclerView.Adapter mAdapter;

    /**
     * The RecyclerView used to list items in the UI.
     */
    RecyclerView mRecyclerView;

    private FrameLayout mPlaceholderLayout;

    /**
     * Defines the default actions when the activity is created: assigning the data handler and
     * setting up the toolbar and remainder of the layout.
     *
     * @see #instantiateDataHandler()
     * @see #setupToolbar()
     * @see #setupLayout()
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        mDataHandler = instantiateDataHandler();

        setupToolbar();
        setupLayout();
    }

    /**
     * Gets the toolbar from the layout and sets it as the activity's action bar.
     */
    void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Invokes {@link #setupList()} before setting up the floating action button and placeholder
     * layout.
     *
     * @see #onFabButtonClick()
     * @see #refreshPlaceholderStatus()
     */
    void setupLayout() {
        setupList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabButtonClick();
            }
        });

        mPlaceholderLayout = (FrameLayout) findViewById(R.id.placeholder);
        refreshPlaceholderStatus();
    }

    /**
     * This should be overridden to define the actions when the floating action button is clicked.
     */
    abstract void onFabButtonClick();

    /**
     * Populates the items list with data from the database table for the current timetable, before
     * sorting this and displaying it.
     *
     * @see #sortList()
     * @see #setupAdapter()
     */
    void setupList() {
        mItems = mDataHandler.getItems(getApplication());
        sortList();

        mAdapter = setupAdapter();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Instantiates, sets up, and returns the relevant RecyclerView adapter.
     * The setup process may involve setting properties, adding listeners, etc.
     *
     * @return the setup RecyclerView adapter, relevant to the subclass' items type.
     */
    abstract RecyclerView.Adapter setupAdapter();

    /**
     * Sorts the list of items being displayed.
     */
    abstract void sortList();

    /**
     * Updates the list with any modified or removed data. If there is none to display, the
     * placeholder layout is shown instead.
     *
     * @see #sortList()
     * @see #refreshPlaceholderStatus()
     */
    void refreshList() {
        mItems.clear();
        mItems.addAll(mDataHandler.getItems(getApplication()));
        sortList();
        mAdapter.notifyDataSetChanged();
        refreshPlaceholderStatus();
    }

    /**
     * Displays the placeholder layout if the list of items is empty, otherwise the list is
     * displayed and the placeholder is not shown.
     *
     * @see #getPlaceholderView()
     */
    void refreshPlaceholderStatus() {
        if (mItems.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mPlaceholderLayout.setVisibility(View.VISIBLE);

            mPlaceholderLayout.removeAllViews();
            mPlaceholderLayout.addView(getPlaceholderView());

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mPlaceholderLayout.setVisibility(View.GONE);
        }
    }

    /**
     * @return the view used as the placeholder layout for the subclass activity.
     *
     * @see #refreshPlaceholderStatus()
     */
    abstract View getPlaceholderView();


    @Override
    protected Toolbar getSelfToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected DrawerLayout getSelfDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
