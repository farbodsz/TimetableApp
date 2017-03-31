package co.timetableapp.ui

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import co.timetableapp.R
import co.timetableapp.data.handler.TimetableItemHandler
import co.timetableapp.framework.TimetableItem

/**
 * A fragment providing default behavior for displaying a list of items.
 *
 * Subclasses must implement abstract methods to define behavior specific to the type of item being
 * displayed, such as how the list is sorted, and what placeholder view is used when there are no
 * items in the list.
 *
 * @param <T> the type of list items to be displayed
 *
 * @see ItemListActivity
 */
abstract class ItemListFragment<T : TimetableItem> : Fragment(), ItemListImpl<T> {

    /**
     * A data handler relevant to the type of items being displayed.
     *
     * @see instantiateDataHandler
     */
    protected var mDataHandler: TimetableItemHandler<T>? = null

    /**
     * A list of the data items being displayed of the generic type [T]
     */
    protected var mItems: ArrayList<T>? = null

    /**
     * The RecyclerView adapter used when displaying items to the list.
     */
    protected var mAdapter: RecyclerView.Adapter<*>? = null

    /**
     * The RecyclerView used to list items in the UI.
     */
    protected var mRecyclerView: RecyclerView? = null

    private var mPlaceholderLayout: FrameLayout? = null

    private var mRootView: View? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater!!.inflate(R.layout.fragment_content_list, container, false)

        mDataHandler = instantiateDataHandler()
        setupLayout()

        return mRootView
    }

    override fun setupLayout() {
        setupList()

        val fab = activity.findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            onFabButtonClick()
        }

        mPlaceholderLayout = mRootView!!.findViewById(R.id.placeholder) as FrameLayout
        refreshPlaceholderStatus()
    }

    override fun setupList() {
        mItems = mDataHandler!!.getItems(activity.application)
        sortList()

        mAdapter = setupAdapter()

        mRecyclerView = mRootView!!.findViewById(R.id.recyclerView) as RecyclerView
        with(mRecyclerView!!) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = mAdapter
        }
    }

    override fun fetchItems() = mDataHandler!!.getItems(activity.application)

    override fun updateList() {
        mItems!!.clear()
        mItems!!.addAll(fetchItems())
        sortList()
        mAdapter!!.notifyDataSetChanged()
        refreshPlaceholderStatus()
    }

    override fun refreshPlaceholderStatus() {
        if (mItems!!.isEmpty()) {
            mRecyclerView!!.visibility = View.GONE
            mPlaceholderLayout!!.visibility = View.VISIBLE

            mPlaceholderLayout!!.removeAllViews()
            mPlaceholderLayout!!.addView(getPlaceholderView())

        } else {
            mRecyclerView!!.visibility = View.VISIBLE
            mPlaceholderLayout!!.visibility = View.GONE
        }
    }

}
