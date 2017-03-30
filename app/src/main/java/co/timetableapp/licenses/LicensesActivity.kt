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

        val toolbar = findViewById(R.id.toolbar) as Toolbar
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
        val container = findViewById(R.id.container) as LinearLayout

        Collections.sort(mLibraries)

        for (library in mLibraries) {
            container.addView(makeCard(library, container))
        }
    }

    private fun makeCard(library: Library, container: LinearLayout): View {
        val card = layoutInflater.inflate(R.layout.item_license_card, container, false)

        with(card) {
            (findViewById(R.id.title) as TextView).text = library.name
            (findViewById(R.id.subtitle) as TextView).text = library.author

            (findViewById(R.id.content_text) as TextView).text = library.license.getNotice(context)

            setOnClickListener {
                val intent = Intent(context, LibraryDetailActivity::class.java)
                intent.putExtra(LibraryDetailActivity.EXTRA_LIBRARY, library)
                startActivity(intent)
            }
        }

        return card
    }

}
