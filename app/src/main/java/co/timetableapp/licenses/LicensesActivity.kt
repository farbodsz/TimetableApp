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

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.util.UiUtils
import java.util.*

class LicensesActivity : AppCompatActivity() {

    private val mLibraries: ArrayList<Library> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { finish() }

        addLicenses()

        displayCards()
    }

    private fun addLicenses() {
        // TODO parse JSON, YML, or XML instead of adding these all programmatically

        mLibraries.add(Library("Android v7 AppCompat Support Library", null,
                "The Android Open Source Project",
                ApacheLicense()))
        mLibraries.add(Library("Android v7 CardView Support Library", null,
                "The Android Open Source Project",
                ApacheLicense()))
        mLibraries.add(Library("Android v7 RecyclerView Support Library", null,
                "The Android Open Source Project",
                ApacheLicense()))
        mLibraries.add(Library("Android Design Support Library", null,
                "The Android Open Source Project",
                ApacheLicense()))

        mLibraries.add(Library("FloatingActionButton",
                "https://github.com/Clans/FloatingActionButton",
                "Dmytro Tarianyk",
                ApacheLicense()))
        mLibraries.add(Library("ThreeTenABP",
                "https://github.com/JakeWharton/ThreeTenABP",
                "JakeWharton",
                ApacheLicense()))
        mLibraries.add(Library("UsefulViews",
                "https://github.com/FarbodSalamat-Zadeh/UsefulViews",
                "Farbod Salamat-Zadeh",
                ApacheLicense()))
        mLibraries.add(Library("MaterialDesignUtils",
                "https://github.com/FarbodSalamat-Zadeh/MaterialDesignUtils",
                "Farbod Salamat-Zadeh",
                ApacheLicense()))
        mLibraries.add(Library("CircleImageView",
                "https://github.com/hdodenhof/CircleImageView",
                "Henning Dodenhof",
                ApacheLicense()))
    }

    private fun displayCards() {
        val container = findViewById<LinearLayout>(R.id.container)

        Collections.sort(mLibraries)

        for (library in mLibraries) {
            container.addView(makeCard(library, container))
        }
    }

    private fun makeCard(library: Library, container: LinearLayout): View {
        val card = layoutInflater.inflate(R.layout.item_license_card, container, false)

        with(card) {
            findViewById<TextView>(R.id.title).text = library.name
            findViewById<TextView>(R.id.subtitle).text = library.author

            findViewById<TextView>(R.id.content_text).text = library.license.getNotice(context)

            setOnClickListener {
                val intent = Intent(context, LibraryDetailActivity::class.java)
                intent.putExtra(LibraryDetailActivity.EXTRA_LIBRARY, library)
                startActivity(intent)
            }
        }

        return card
    }

}
