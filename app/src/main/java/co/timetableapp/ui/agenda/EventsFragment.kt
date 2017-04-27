package co.timetableapp.ui.agenda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import co.timetableapp.R
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.model.Event
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.base.ItemListFragment
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.events.EventsAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import com.github.clans.fab.FloatingActionMenu
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * A fragment for displaying a list of events to the user.
 *
 * @see Event
 * @see AgendaActivity
 * @see EventDetailActivity
 */
class EventsFragment : ItemListFragment<Event>(), AgendaActivity.OnFilterChangeListener {

    companion object {
        private const val REQUEST_CODE_EVENT_EDIT = 1
    }

    private var mHeaders: ArrayList<String?>? = null

    private var mShowPast = false

    override fun instantiateDataHandler() = EventHandler(activity)

    override fun setupLayout() {
        super.setupLayout()
        setupFab()
    }

    private fun setupFab() {
        activity.findViewById(R.id.fab_event).setOnClickListener {
            val intent = Intent(activity, EventDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_EVENT_EDIT)

            (activity.findViewById(R.id.fabMenu) as FloatingActionMenu).close(false)
        }
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = EventsAdapter(activity, mHeaders, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(activity, EventDetailActivity::class.java)
            intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItems!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            startActivityForResult(intent, REQUEST_CODE_EVENT_EDIT, bundle)
        }

        return adapter
    }

    override fun setupList() {
        mHeaders = ArrayList<String?>()
        super.setupList()
    }

    override fun sortList() {
        if (mShowPast) {
            mItems!!.sortWith(Event.COMPARATOR_REVERSE_DATE_TIME)
        } else {
            mItems!!.sort()
        }

        val headers = ArrayList<String?>()
        val events = ArrayList<Event?>()

        var currentTimePeriod = -1

        for (i in mItems!!.indices) {
            val event = mItems!![i]

            val eventDate = event.startTime
            val timePeriodId: Int

            if (eventDate.isBefore(LocalDateTime.now())) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(eventDate.year.toString() + eventDate.monthValue.toString())

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(eventDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")))
                        events.add(null)
                    }

                    headers.add(null)
                    events.add(event)

                    currentTimePeriod = timePeriodId
                }

            } else {

                if (!mShowPast) {
                    timePeriodId = DateUtils.getDatePeriodId(eventDate.toLocalDate())

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtils.makeHeaderName(activity, timePeriodId))
                        events.add(null)
                    }

                    headers.add(null)
                    events.add(event)

                    currentTimePeriod = timePeriodId
                }
            }
        }

        mHeaders!!.clear()
        mHeaders!!.addAll(headers)

        mItems!!.clear()
        mItems!!.addAll(events)
    }

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_event_black_24dp,
            if (mShowPast) R.string.placeholder_events_past else R.string.placeholder_events,
            subtitleRes = if (mShowPast)
                R.string.placeholder_events_past_subtitle
            else
                R.string.placeholder_events_subtitle)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EVENT_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onFilterChange(showCompleted: Boolean, showPast: Boolean) {
        mShowPast = showPast
        updateList()
    }

}
