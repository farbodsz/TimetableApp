package com.satsumasoftware.timetable.ui.start

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.db.PortingFragment
import com.satsumasoftware.timetable.ui.MainActivity

/**
 * This activity is the launcher activity. It redirects to [MainActivity] if the user already has
 * timetable data, otherwise displays a welcome screen.
 *
 * In the welcome screen, the user has options to import their data, or to go through a setup guide.
 *
 * @see InitialSetupActivity
 */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasExistingData()) {
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)
        setupLayout()
    }

    private fun hasExistingData() = false // TODO: (application as TimetableApplication).currentTimetable != null

    private fun setupLayout() {
        findViewById(R.id.button_import).setOnClickListener {
            startImportFragment()
        }

        findViewById(R.id.button_next).setOnClickListener {
            startActivity(Intent(this, InitialSetupActivity::class.java))
            finish()
        }
    }

    private fun startImportFragment() {
        val fragmentArgs = Bundle()
        fragmentArgs.putInt(PortingFragment.ARGUMENT_PORT_TYPE, PortingFragment.TYPE_IMPORT)

        val fragment = PortingFragment()
        fragment.arguments = fragmentArgs

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(fragment, null)
        transaction.commit()
    }

}
