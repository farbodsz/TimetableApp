package co.timetableapp.ui.start

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.timetableapp.BuildConfig
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.PortingFragment
import co.timetableapp.ui.MainActivity

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

    private fun hasExistingData(): Boolean {
        return if (BuildConfig.DEBUG) {
            // If debugging, we may (or may not) want to see the welcome page
            false
        } else {
            (application as TimetableApplication).currentTimetable != null
        }
    }

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
        fragmentArgs.putBoolean(PortingFragment.ARGUMENT_IMPORT_FIRST_DB, true)

        val fragment = PortingFragment()
        fragment.arguments = fragmentArgs
        fragment.onPortingCompleteListener = object : PortingFragment.OnPortingCompleteListener {
            override fun onPortingComplete(portingType: Int, successful: Boolean) {
                if (successful && portingType == PortingFragment.TYPE_IMPORT) {
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                    finish()
                }
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(fragment, null)
        transaction.commit()
    }

}
