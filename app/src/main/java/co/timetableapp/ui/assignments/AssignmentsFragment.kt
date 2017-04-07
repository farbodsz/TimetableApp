package co.timetableapp.ui.assignments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.timetableapp.R
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.model.Assignment
import co.timetableapp.ui.assignments.AssignmentsFragment.Companion.DISPLAY_ALL_UPCOMING
import co.timetableapp.ui.assignments.AssignmentsFragment.Companion.DISPLAY_TODO
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemListFragment
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import com.github.clans.fab.FloatingActionMenu
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * A fragment for displaying a list of assignments to the user.
 *
 * Assignments can be displayed in two formats: [DISPLAY_TODO] and [DISPLAY_ALL_UPCOMING].
 * In the former, only incomplete assignments will be displayed; in the latter, only assignments
 * that are due in the future (regardless of completion) and overdue assignments will be shown.
 *
 * @see Assignment
 * @see AgendaActivity
 * @see AssignmentDetailActivity
 * @see AssignmentEditActivity
 */
class AssignmentsFragment : ItemListFragment<Assignment>(), AgendaActivity.OnFilterChangeListener {

    companion object {

        private const val REQUEST_CODE_ASSIGNMENT_DETAIL = 1

        /**
         * The intent extra key for the display mode of the assignments.
         *
         * This should be either [DISPLAY_TODO] or [DISPLAY_ALL_UPCOMING]. If the data passed with
         * this key is null, [DISPLAY_ALL_UPCOMING] will be used by default.
         */
        const val ARGUMENT_MODE = "extra_mode"

        /**
         * Suggests that only incomplete assignments will be shown in the list.
         *
         * It is specified by passing it through an intent extra with the [ARGUMENT_MODE] key.
         *
         * @see DISPLAY_ALL_UPCOMING
         */
        const val DISPLAY_TODO = 1

        /**
         * Suggests that only assignments due in the future and overdue assignments will be shown in
         * the list.
         *
         * It is specified by passing it through an intent extra with the [ARGUMENT_MODE] key.
         *
         * @see DISPLAY_TODO
         */
        const val DISPLAY_ALL_UPCOMING = 2
    }

    private var mHeaders: ArrayList<String?>? = null

    private var mShowCompleted = AgendaActivity.DEFAULT_SHOW_COMPLETED
    private var mShowPast = AgendaActivity.DEFAULT_SHOW_PAST

    override fun instantiateDataHandler() = AssignmentHandler(activity)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        determineDisplayMode()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun determineDisplayMode() {
        val extras = arguments
        val mode = extras?.getInt(ARGUMENT_MODE) ?: DISPLAY_ALL_UPCOMING

        mShowCompleted = mode == DISPLAY_ALL_UPCOMING
    }

    override fun setupLayout() {
        super.setupLayout()
        setupFab()
    }

    private fun setupFab() {
        activity.findViewById(R.id.fab_assignment).setOnClickListener {
            val intent = Intent(activity, AssignmentDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL)

            (activity.findViewById(R.id.fabMenu) as FloatingActionMenu).close(false)
        }
    }

    override fun setupList() {
        mHeaders = ArrayList<String?>()
        super.setupList()
        makeItemTouchHelper().attachToRecyclerView(mRecyclerView)
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = AssignmentsAdapter(activity, mHeaders, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(activity, AssignmentDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM, mItems!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL, bundle)
        }

        return adapter
    }

    private fun makeItemTouchHelper() = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        private var mRemovedHeader: String? = null
        private var mRemovedAssignment: Assignment? = null
        private var mRemovedAssignmentPos: Int = 0
        private var mRemovedCompletionProgress: Int = 0

        override fun getMovementFlags(recyclerView: RecyclerView,
                                      viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.adapterPosition
            val isHeader = mItems!![position] == null

            val swipeFlags = if (isHeader) {
                0  // don't allow swiping for headers
            } else {
                ItemTouchHelper.START or ItemTouchHelper.END  // list items can be swiped left/right
            }

            return makeMovementFlags(0, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            val assignment = mItems!![position]

            // Store the assignment we're about to remove for the undo action
            mRemovedHeader = null
            mRemovedAssignmentPos = position
            mRemovedAssignment = assignment
            mRemovedCompletionProgress = assignment.completionProgress

            assignment.completionProgress = 100
            mDataHandler!!.replaceItem(assignment.id, assignment)

            // Don't remove the item from the list if we're showing completed items
            // But overdue items should get removed from the list
            if (mShowCompleted && assignment.isUpcoming()) {
                updateCompletedAssignment(position)
                return
            }

            removeCompletedAssignment(position)

            // No need to refresh the list now, but check if it's empty and needs a placeholder
            refreshPlaceholderStatus()
        }

        /**
         * Removes and adds back the list item so that the 'done' background goes away and the item
         * content gets updated, after the user swipes the item off the screen.
         *
         * To the user, it seems that the item does *not* get removed from the list.
         *
         * @param position the adapter position of the item represented by the ViewHolder
         */
        private fun updateCompletedAssignment(position: Int) {
            mItems!!.removeAt(position)
            mAdapter!!.notifyItemRemoved(position)
            mItems!!.add(position, mRemovedAssignment!!)
            mAdapter!!.notifyItemInserted(position)

            val finalPos = position

            val undoListener = View.OnClickListener {
                val removedAssignment = mRemovedAssignment
                removedAssignment!!.completionProgress = mRemovedCompletionProgress

                mDataHandler!!.replaceItem(removedAssignment.id, removedAssignment)

                mItems!![finalPos] = removedAssignment
                mAdapter!!.notifyItemChanged(finalPos)
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
         * @param positionParam the adapter position of the item represented by the ViewHolder. Note
         *          that this is only used to assign a `position` variable, as parameters in Kotlin
         *          are effectively `final`.
         */
        private fun removeCompletedAssignment(positionParam: Int) {
            var position = positionParam

            // Check if assignment is only one in date group
            if (mItems!![position - 1] == null
                    && (mItems!!.size == position + 1 || mItems!![position + 1] == null)) {
                // Positions either side of the assignment are empty (i.e. headers)
                val headerPosition = position - 1

                // Store the header we're about to remove for the undo action
                mRemovedHeader = mHeaders!![headerPosition]

                // Remove the header from both lists
                mHeaders!!.removeAt(headerPosition)
                mItems!!.removeAt(headerPosition)
                mAdapter!!.notifyItemRemoved(position)

                // Update the position of the assignment because we just removed an item
                position -= 1
            }

            // Remove the assignment from both lists
            mHeaders!!.removeAt(position)
            mItems!!.removeAt(position)
            mAdapter!!.notifyItemRemoved(position)

            val undoListener = View.OnClickListener {
                if (mRemovedHeader != null) {
                    mHeaders!!.add(mRemovedAssignmentPos - 1, mRemovedHeader!!)
                    mItems!!.add(mRemovedAssignmentPos - 1, null)
                    mAdapter!!.notifyItemInserted(mRemovedAssignmentPos - 1)
                }

                val removedAssignment = mRemovedAssignment
                removedAssignment!!.completionProgress = mRemovedCompletionProgress

                mHeaders!!.add(mRemovedAssignmentPos, null)
                mItems!!.add(mRemovedAssignmentPos, removedAssignment)
                mAdapter!!.notifyItemInserted(mRemovedAssignmentPos)

                mDataHandler!!.replaceItem(removedAssignment.id, removedAssignment)

                refreshPlaceholderStatus()
            }

            // Show a Snackbar with the undo action
            Snackbar.make(
                    activity.findViewById(R.id.coordinatorLayout),
                    R.string.message_assignment_completed,
                    Snackbar.LENGTH_SHORT
            ).setAction(R.string.action_undo, undoListener).show()
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                                 viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                 actionState: Int, isCurrentlyActive: Boolean) {
            val itemView = viewHolder.itemView

            val background = ColorDrawable(
                    ContextCompat.getColor(activity, R.color.item_done_background))

            background.setBounds(itemView.left, itemView.top, itemView.right,
                    itemView.bottom)

            background.draw(c)

            val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_done_white_24dp)

            val left = (if (dX > 0) {
                itemView.left + UiUtils.dpToPixels(activity, 16)
            } else {
                itemView.right - UiUtils.dpToPixels(activity, 16) - icon.width
            }).toFloat()

            val top = (itemView.top + (itemView.bottom - itemView.top - icon.height) / 2).toFloat()

            c.drawBitmap(icon, left, top, null)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    })

    override fun sortList() {
        if (mShowPast) {
            mItems!!.sortWith(Assignment.COMPARATOR_REVERSE_DUE_DATE)
        } else {
            mItems!!.sort()
        }

        val headers = ArrayList<String?>()
        val assignments = ArrayList<Assignment?>()

        var currentTimePeriod = -1

        for (i in mItems!!.indices) {
            val assignment = mItems!![i]

            val dueDate = assignment.dueDate
            val timePeriodId: Int

            if (mShowPast && assignment.isPastAndDone()) {
                // Show everything in the past and completed (mShowCompleted is irrelevant here)

                timePeriodId =
                        Integer.parseInt(dueDate.year.toString() + dueDate.monthValue.toString())

                if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                    headers.add(dueDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")))
                    assignments.add(null)
                }

                headers.add(null)
                assignments.add(assignment)

                currentTimePeriod = timePeriodId

            } else if (!mShowPast && (assignment.isOverdue() || assignment.isUpcoming())) {

                // If we're showing completed items, show everything.
                // Otherwise, only show incomplete items.
                if (mShowCompleted || !assignment.isComplete()) {

                    timePeriodId = DateUtils.getDatePeriodId(dueDate)

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtils.makeHeaderName(activity, timePeriodId))
                        assignments.add(null)
                    }

                    headers.add(null)
                    assignments.add(assignment)

                    currentTimePeriod = timePeriodId
                }
            }
        }

        mHeaders!!.clear()
        mHeaders!!.addAll(headers)

        mItems!!.clear()
        mItems!!.addAll(assignments)
    }

    override fun getPlaceholderView(): View {
        val titleRes = if (mShowPast)
            R.string.placeholder_assignments_past_title
        else
            R.string.placeholder_assignments_title

        val subtitleRes = if (mShowCompleted) {
            if (mShowPast) {
                R.string.placeholder_assignments_past_subtitle
            } else {
                R.string.placeholder_assignments_upcoming_subtitle
            }
        } else {
            R.string.placeholder_assignments_todo_subtitle
        }

        return UiUtils.makePlaceholderView(
                activity,
                R.drawable.ic_homework_black_24dp,
                titleRes,
                subtitleRes = subtitleRes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ASSIGNMENT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onFilterChange(showCompleted: Boolean, showPast: Boolean) {
        mShowCompleted = showCompleted
        mShowPast = showPast
        updateList()
    }

}
