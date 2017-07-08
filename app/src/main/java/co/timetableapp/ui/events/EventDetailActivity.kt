package co.timetableapp.ui.events

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.model.Event
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import org.threeten.bp.format.DateTimeFormatter

/**
 * Shows the details of an event.
 *
 * @see Event
 * @see EventEditActivity
 * @see ItemDetailActivity
 */
class EventDetailActivity : ItemDetailActivity<Event>() {

    override fun initializeDataHandler() = EventHandler(this)

    override fun getLayoutResource() = R.layout.activity_event_detail

    override fun onNullExtras() {
        val intent = Intent(this, EventEditActivity::class.java)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun setupLayout() {
        setupToolbar()

        setupDateText()
        setupTimeText()
        setupNotesText()
        setupLocationText()
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        (findViewById(R.id.title) as TextView).text = mItem.title

        UiUtils.setBarColors(Event.DEFAULT_COLOR, this, toolbar)
    }

    private fun setupDateText() {
        val dateFormatter = DateUtils.FORMATTER_FULL_DATE

        val textView = findViewById(R.id.textView_date) as TextView
        textView.text = if (mItem.hasDifferentStartEndDates()) {
            "${mItem.startDateTime.format(dateFormatter)} - ${mItem.endDateTime.format(dateFormatter)}"
        } else {
            mItem.startDateTime.format(dateFormatter)
        }
    }

    private fun setupTimeText() {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val timeText = mItem.startDateTime.format(timeFormatter) + " - " +
                mItem.endDateTime.format(timeFormatter)
        (findViewById(R.id.textView_times) as TextView).text = timeText
    }

    private fun setupNotesText() {
        val notesText = findViewById(R.id.textView_notes) as TextView
        with(notesText) {
            if (mItem.hasNotes()) {
                text = mItem.notes

                setTypeface(null, Typeface.NORMAL)
                setTextColor(ContextCompat.getColor(context, R.color.mdu_text_black))
            } else {
                text = getString(R.string.placeholder_notes_empty)

                setTypeface(null, Typeface.ITALIC)
                setTextColor(ContextCompat.getColor(context, R.color.mdu_text_black_secondary))
            }
        }
    }

    private fun setupLocationText() {
        if (!mItem.hasLocation()) {
            findViewById(R.id.divider_location).visibility = View.GONE
            findViewById(R.id.viewGroup_location).visibility = View.GONE
            return
        }

        (findViewById(R.id.textView_location) as TextView).text = mItem.location
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, EventEditActivity::class.java)
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun saveEditsAndClose() {
        val intent = Intent().putExtra(EXTRA_ITEM, mItem)
        setResult(Activity.RESULT_OK, intent) // to reload any changes in EventsActivity
        supportFinishAfterTransition()
    }

    override fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in EventsActivity
        finish()
    }

}
