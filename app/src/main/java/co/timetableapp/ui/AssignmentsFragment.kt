package co.timetableapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.framework.Assignment
import co.timetableapp.ui.adapter.AssignmentsAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * A fragment for displaying a list of assignments to the user.
 *
 * @see Assignment
 * @see AssignmentsActivity
 * @see AssignmentDetailActivity
 * @see AssignmentEditActivity
 */
class AssignmentsFragment : ItemListFragment<Assignment>() {

    companion object {

        private const val REQUEST_CODE_ASSIGNMENT_DETAIL = 1

        /**
         * @see AssignmentsActivity.EXTRA_MODE
         */
        const val ARGUMENT_MODE = AssignmentsActivity.EXTRA_MODE

        /**
         * @see AssignmentsActivity.DISPLAY_TODO
         */
        const val DISPLAY_TODO = AssignmentsActivity.DISPLAY_TODO

        /**
         * @see AssignmentsActivity.DISPLAY_ALL_UPCOMING
         */
        const val DISPLAY_ALL_UPCOMING = AssignmentsActivity.DISPLAY_ALL_UPCOMING
    }

    private var mMode: Int = DISPLAY_ALL_UPCOMING

    private var mHeaders: ArrayList<String?>? = null

    private var mShowPast = false

    override fun instantiateDataHandler() = AssignmentHandler(activity)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        determineDisplayMode()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun determineDisplayMode() {
        if (mMode != 0) {
            return
        }

        val extras = arguments
        mMode = extras?.getInt(ARGUMENT_MODE) ?: DISPLAY_ALL_UPCOMING
    }

    override fun onFabButtonClick() {
        val intent = Intent(activity, AssignmentDetailActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL)
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

            ActivityCompat.startActivityForResult(
                    activity,
                    intent,
                    REQUEST_CODE_ASSIGNMENT_DETAIL,
                    bundle)
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

            val swipeFlags = if (isHeader)
                0
            else
                ItemTouchHelper.START or ItemTouchHelper.END

            return ItemTouchHelper.Callback.makeMovementFlags(0, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            var position = viewHolder.adapterPosition

            val assignment = mItems!![position]

            // Store the assignment we're about to remove for the undo action
            mRemovedHeader = null
            mRemovedAssignmentPos = position
            mRemovedAssignment = assignment
            mRemovedCompletionProgress = assignment.completionProgress

            assignment.completionProgress = 100
            mDataHandler!!.replaceItem(assignment.id, assignment)

            // Do not completely remove the item if we're not in DISPLAY_TODO mode
            if (mMode != DISPLAY_TODO) {
                // We should remove and add back the item so the 'done' background goes away
                // and the item gets updated
                mItems!!.removeAt(position)
                mAdapter!!.notifyItemRemoved(position)
                mItems!!.add(position, mRemovedAssignment!!)
                mAdapter!!.notifyItemInserted(position)

                val finalPos = position

                Snackbar.make(activity.findViewById(R.id.coordinatorLayout),
                        R.string.message_assignment_completed,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_undo) {
                            val removedAssignment = mRemovedAssignment
                            removedAssignment!!.completionProgress = mRemovedCompletionProgress

                            mDataHandler!!.replaceItem(removedAssignment.id, removedAssignment)

                            mItems!![finalPos] = removedAssignment
                            mAdapter!!.notifyItemChanged(finalPos)
                        }
                        .show()
                return
            }

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

            // Show a Snackbar with the undo action
            Snackbar.make(activity.findViewById(R.id.coordinatorLayout),
                    R.string.message_assignment_completed,
                    Snackbar.LENGTH_SHORT)
                    .setAction(R.string.action_undo) {
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
                    .show()

            // No need to refresh the list now, but check if it's empty and needs a placeholder
            refreshPlaceholderStatus()
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
        Collections.sort(mItems) { a1, a2 ->
            val dueDate1 = a1.dueDate
            val dueDate2 = a2.dueDate
            if (mShowPast) {
                dueDate2.compareTo(dueDate1)
            } else {
                dueDate1.compareTo(dueDate2)
            }
        }

        val headers = ArrayList<String?>()
        val assignments = ArrayList<Assignment?>()

        var currentTimePeriod = -1

        for (i in mItems!!.indices) {
            val assignment = mItems!![i]

            val dueDate = assignment.dueDate
            val timePeriodId: Int

            if (mMode == DISPLAY_ALL_UPCOMING && assignment.isPastAndDone() && mShowPast) {
                timePeriodId =
                        Integer.parseInt(dueDate.year.toString() + dueDate.monthValue.toString())

                if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                    headers.add(dueDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")))
                    assignments.add(null)
                }

                headers.add(null)
                assignments.add(assignment)

                currentTimePeriod = timePeriodId

            } else if (mMode == DISPLAY_ALL_UPCOMING && !assignment.isPastAndDone()
                    && !mShowPast || mMode == DISPLAY_TODO && !assignment.isComplete()) {
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

        val subtitleRes: Int
        if (mMode == DISPLAY_TODO) {
            subtitleRes = R.string.placeholder_assignments_todo_subtitle
        } else {
            subtitleRes = if (mShowPast)
                R.string.placeholder_assignments_past_subtitle
            else
                R.string.placeholder_assignments_upcoming_subtitle
        }

        val drawableRes = if (mShowPast)
            R.drawable.ic_assignment_black_24dp
        else
            R.drawable.ic_assignment_turned_in_black_24dp

        return UiUtils.makePlaceholderView(
                activity,
                drawableRes,
                titleRes,
                R.color.mdu_blue_400,
                R.color.mdu_white,
                R.color.mdu_white,
                true,
                subtitleRes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ASSIGNMENT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        // Do not show the menu in DISPLAY_TODO mode
        if (mMode != DISPLAY_TODO) {
            inflater!!.inflate(R.menu.menu_assignments, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_show_past -> {
                mShowPast = !mShowPast
                item.isChecked = mShowPast

                val textView = activity.findViewById(R.id.text_infoBar) as TextView
                if (mShowPast) {
                    textView.visibility = View.VISIBLE
                    textView.text = getString(R.string.showing_past_assignments)
                } else {
                    textView.visibility = View.GONE
                }
                updateList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
