/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.licenses

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.util.UiUtils

/**
 * An activity to display the detail of a library, such as its name, author and license.
 *
 * @see Library
 */
class LibraryDetailActivity : AppCompatActivity() {

    companion object {

        private const val LOG_TAG = "LibraryDetailActivity"

        /**
         * The key for the [Library] being passed through the intent extra to this activity.
         */
        internal const val EXTRA_LIBRARY = "extra_library"
    }

    private var mLibrary: Library? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        mLibrary = intent.extras.getParcelable<Library>(EXTRA_LIBRARY)

        if (mLibrary == null) {
            Log.w(LOG_TAG, "Intent is null")
            return
        }

        setupLayout()
    }

    private fun setupLayout() {
        setupToolbar()

        val container = findViewById(R.id.container) as LinearLayout
        container.addView(makeLicenseCard(container))
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

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
        val card = layoutInflater.inflate(R.layout.item_license_card, container, false)

        with(card) {
            (findViewById(R.id.title) as TextView).text = getString(R.string.title_license)
            (findViewById(R.id.subtitle) as TextView).text = mLibrary!!.license.name
            (findViewById(R.id.content_text) as TextView).text =
                    mLibrary!!.license.getNotice(context)
        }

        return card
    }

}
