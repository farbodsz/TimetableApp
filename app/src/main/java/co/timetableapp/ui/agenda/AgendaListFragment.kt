package co.timetableapp.ui.agenda

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import co.timetableapp.R
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Assignment
import co.timetableapp.model.Event
import co.timetableapp.model.Exam
import co.timetableapp.model.agenda.AgendaHeader
import co.timetableapp.model.agenda.AgendaItem
import co.timetableapp.model.agenda.AgendaListItem
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.util.PrefUtils
import co.timetableapp.util.UiUtils

/**
 * A fragment for displaying a list of Agenda items to the user.
 */
class AgendaListFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_ITEM_DETAIL = 2
    }

    private lateinit var mRootView: View
    private lateinit var mAdapter: AgendaListItemAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mPlaceholderLayout: FrameLayout

    private val mItems by lazy {
        sortAndFilterItems(fetchAgendaListItems()) as ArrayList<AgendaListItem>
    }

    private var mShowCompleted = true
    private var mShowPast = AgendaActivity.DEFAULT_SHOW_PAST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater!!.inflate(R.layout.fragment_content_list, container, false)

        setupLayout()

        mShowCompleted = PrefUtils.showCompletedAgendaItems(activity)

        return mRootView
    }

    private fun setupLayout() {
        setupList()

        mPlaceholderLayout = mRootView.findViewById(R.id.placeholder) as FrameLayout
        refreshPlaceholderStatus()
    }

    private fun setupList() {
        setupAdapter()
        mRecyclerView = mRootView.findViewById(R.id.recyclerView) as RecyclerView
        with(mRecyclerView) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        val itemTouchHelper = ItemTouchHelper(AgendaItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(mRecyclerView)
    }

    private fun setupAdapter() {
        mAdapter = AgendaListItemAdapter(activity, mItems)
        mAdapter.setOnEntryClickListener(object : AgendaListItemAdapter.OnEntryClickListener {
            override fun onEntryClick(view: View, position: Int) {
                val item = mItems[position] as AgendaItem
                val detailActivity = when (item) {
                    is Assignment -> AssignmentDetailActivity::class.java
                    is Exam -> ExamDetailActivity::class.java
                    is Event -> EventDetailActivity::class.java
                    else -> throw IllegalArgumentException("invalid item type at position: $position")
                }

                val intent = Intent(activity, detailActivity)
                        .putExtra(ItemDetailActivity.EXTRA_ITEM, item)

                var bundle: Bundle? = null
                if (UiUtils.isApi21()) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            view,
                            getString(R.string.transition_1))
                    bundle = options.toBundle()
                }

                startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL, bundle)
            }
        })
    }

    /**
     * @return an unsorted list of all agenda list items - assignments, exams, events, and headers
     */
    private fun fetchAgendaListItems(): List<AgendaListItem> {
        val arrayList = ArrayList<AgendaListItem>()

        val assignmentManager = AssignmentHandler(activity)
        val assignments = assignmentManager.getItems(activity.application)
        if (!mShowCompleted) {
            // If we're not showing completed items, then only show the incomplete items
            // N.B: this is different from only showing complete items if mShowCompleted = true
            // which is not what we want.
            assignments.filter { !it.isComplete() }
        }
        arrayList.addAll(assignments)

        val examManager = ExamHandler(activity)
        arrayList.addAll(examManager.getItems(activity.application))

        val eventManager = EventHandler(activity)
        arrayList.addAll(eventManager.getItems(activity.application))

        arrayList.addAll(AgendaHeader.getAllHeaderTypes())

        return arrayList
    }

    /**
     * @return a sorted list of agenda list items with redundant headers removed
     */
    private fun sortAndFilterItems(items: List<AgendaListItem>): List<AgendaListItem> {
        val sortedItems = ArrayList(items.sorted())

        // Removing items whilst iterating causes a ConcurrentModificationException
        // We will go through the list and store which items need to be removed and do that later
        val itemsToRemove = ArrayList<AgendaListItem>()

        var previousItem: AgendaListItem? = null
        for (item in sortedItems) {
            if (previousItem == null) {
                previousItem = item
                continue
            }

            if (previousItem.isHeader() && item.isHeader()) {
                // Two consecutive headers found - remove redundant header
                itemsToRemove.add(previousItem)
            }

            previousItem = item
        }

        sortedItems.removeAll(itemsToRemove)

        return sortedItems
    }

    private fun refreshPlaceholderStatus() {
        if (mItems.isEmpty()) {
            mRecyclerView.visibility = View.GONE
            mPlaceholderLayout.visibility = View.VISIBLE

            mPlaceholderLayout.removeAllViews()
            mPlaceholderLayout.addView(getPlaceholderView())

        } else {
            mRecyclerView.visibility = View.VISIBLE
            mPlaceholderLayout.visibility = View.GONE
        }
    }

    private fun getPlaceholderView(): View {
        val titleRes = if (mShowPast)
            R.string.placeholder_assignments_past_title
        else
            R.string.placeholder_assignments_title

        // TODO subtitle

        return UiUtils.makePlaceholderView(
                activity,
                R.drawable.ic_homework_black_24dp, // TODO better icon
                titleRes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AgendaActivity.REQUEST_CODE_CREATE_ITEM
                || requestCode == REQUEST_CODE_ITEM_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                // updateList() // TODO
            }
        }
    }

    inner class AgendaItemTouchHelperCallback : ItemTouchHelper.Callback() {

        private lateinit var mCachedAssignment: Assignment

        override fun getMovementFlags(recyclerView: RecyclerView?,
                                      viewHolder: RecyclerView.ViewHolder?): Int {
            val position = viewHolder!!.adapterPosition
            val item = mItems[position]

            val swipeFlags = if (item is Assignment) {
                // Items can be swiped from left to right, but not the other way so to avoid
                // confusion with the tab swiping gesture.
                ItemTouchHelper.END
            } else {
                // Don't allow swiping for headers or items other than assignments:
                // Only assignments can be swiped to complete; other items can't be "completed"
                0
            }

            return makeMovementFlags(0, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView?,
                            viewHolder: RecyclerView.ViewHolder?,
                            target: RecyclerView.ViewHolder?) = false // not supporting drag & drop

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            // Only assignments can be swiped according to getMovementFlags
            val position = viewHolder!!.adapterPosition
            val assignment = mItems[position] as Assignment

            // Keep a copy of this assignment in cache so we can undo any actions
            mCachedAssignment = assignment

            writeAssignmentCompletion(assignment)

            // Don't remove the assignment from the list if we're showing completed items
            // But otherwise, overdue assignment should get removed from the list
            if (mShowCompleted && assignment.isUpcoming()) {
                updateCompletedAssignment(position, assignment)
                return
            }

            removeCompletedAssignment(position)

            // No need to refresh the list now, but check if it's empty and needs a placeholder
            refreshPlaceholderStatus()
        }

        /**
         * Updates the database with the new completed progress level of the assignment (100).
         */
        private fun writeAssignmentCompletion(assignment: Assignment) {
            assignment.completionProgress = 100
            AssignmentHandler(activity).replaceItem(assignment.id, assignment)
        }

        /**
         * Removes and adds back the list item so that the 'done' background goes away and the item
         * content gets updated.
         *
         * To the user, the end result is that the item does *not* get removed from the list, but
         * its progress text updates.
         *
         * @param position      the adapter position of the item represented by the ViewHolder
         * @param assignment    the assignment to update (remove and add back)
         */
        private fun updateCompletedAssignment(position: Int, assignment: Assignment) {
            // Remove and add back assignment
            mItems.removeAt(position)
            mAdapter.notifyItemRemoved(position)
            mItems.add(position, assignment)
            mAdapter.notifyItemInserted(position)

            // Add Snackbar UI for undo action
            val undoListener = View.OnClickListener {
                // Update database
                assignment.completionProgress = mCachedAssignment.completionProgress
                AssignmentHandler(activity).replaceItem(assignment.id, assignment)

                // Update list
                mItems[position] = assignment
                mAdapter.notifyItemChanged(position)
            }

            Snackbar.make(
                    activity.findViewById(R.id.coordinatorLayout),
                    R.string.message_assignment_completed,
                    Snackbar.LENGTH_SHORT
            ).setAction(R.string.action_undo, undoListener).show()
        }

        /**
         * Removes the list item after it has been swiped off the screen by the user.
         *
         * @param posParameter  the adapter position of the item represented by the ViewHolder.
         *                      Note that this is only used to assign a `position` variable, as
         *                      parameters in Kotlin are effectively `final`.
         */
        private fun removeCompletedAssignment(posParameter: Int) {
            var pos = posParameter
            var cachedHeader: AgendaHeader? = null

            // Check for removing headers - if assignment is the only one in its date group
            // I.e. if the list item is surrounded by headers
            if (mItems[pos - 1] is AgendaHeader
                    && (mItems.size - 1 == pos || mItems[pos + 1] is AgendaHeader)) {
                val headerPosition = pos - 1
                cachedHeader = mItems[headerPosition] as AgendaHeader

                mItems.removeAt(headerPosition)
                mAdapter.notifyItemRemoved(pos)

                // Update position of the assignment since we just removed an item
                pos--
            }

            // Now remove the assignment
            mItems.removeAt(pos)
            mAdapter.notifyItemRemoved(pos)

            val undoListener = View.OnClickListener {
                if (cachedHeader != null) {
                    // A header was removed, we need to add it back in
                    mItems.add(pos, cachedHeader!!)
                    mAdapter.notifyItemInserted(pos)

                    // Update position since we just inserted an item
                    pos++
                }

                // Add back the assignment to the UI and database
                mItems.add(pos, mCachedAssignment)
                mAdapter.notifyItemInserted(pos)

                AssignmentHandler(activity).replaceItem(mCachedAssignment.id, mCachedAssignment)

                refreshPlaceholderStatus()
            }

            // Show the Snackbar with undo action
            Snackbar.make(
                    activity.findViewById(R.id.coordinatorLayout),
                    R.string.message_assignment_completed,
                    Snackbar.LENGTH_SHORT
            ).setAction(R.string.action_undo, undoListener).show()
        }

        override fun onChildDraw(c: Canvas?,
                                 recyclerView: RecyclerView?,
                                 viewHolder: RecyclerView.ViewHolder?,
                                 dX: Float,
                                 dY: Float,
                                 actionState: Int,
                                 isCurrentlyActive: Boolean) {
            val itemView = viewHolder!!.itemView

            val background = ColorDrawable(
                    ContextCompat.getColor(activity, R.color.item_done_background))
            background.setBounds(itemView.left, itemView.top, itemView.right, itemView.bottom)
            background.draw(c)

            val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_done_white_24dp)

            val left = (if (dX > 0) {
                itemView.left + UiUtils.dpToPixels(activity, 16)
            } else {
                itemView.right - UiUtils.dpToPixels(activity, 16) - icon.width
            }).toFloat()

            val top = (itemView.top + (itemView.bottom - itemView.top - icon.height) / 2).toFloat()

            c!!.drawBitmap(icon, left, top, null)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

}
