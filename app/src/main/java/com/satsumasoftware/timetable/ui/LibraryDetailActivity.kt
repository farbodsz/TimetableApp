package com.satsumasoftware.timetable.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.licenses.Library
import com.satsumasoftware.timetable.util.UiUtils

class LibraryDetailActivity : AppCompatActivity() {

    internal companion object {
        private const val LOG_TAG = "LibraryDetailActivity"
        const val EXTRA_LIBRARY = "extra_library"
    }

    private var mLibrary: Library? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mLibrary = intent.extras.getParcelable<Library>(EXTRA_LIBRARY)

        if (mLibrary == null) {
            Log.w(LOG_TAG, "Intent is null")
            return
        }

        setupToolbar(toolbar)

        val container = findViewById(R.id.container) as LinearLayout
        container.addView(makeLicenseCard(container))
    }

    private fun setupToolbar(toolbar: Toolbar) {
        with(supportActionBar!!) {
            title = mLibrary!!.name
            subtitle = mLibrary!!.author
        }

        with(toolbar) {
            navigationIcon = UiUtils.tintDrawable(context, R.drawable.ic_close_black_24dp)
            setNavigationOnClickListener { finish() }
        }
    }

    private fun makeLicenseCard(container: LinearLayout): View {
        val card = layoutInflater.inflate(R.layout.card_general, container, false)

        with(card) {
            (findViewById(R.id.title) as TextView).text = "License"
            (findViewById(R.id.subtitle) as TextView).text = mLibrary!!.license.name
            (findViewById(R.id.content_text) as TextView).text =
                    mLibrary!!.license.getNotice(context)
        }

        return card
    }

}
