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

package co.timetableapp.ui.start

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.PortingFragment
import co.timetableapp.ui.home.MainActivity

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

    private fun hasExistingData() = (application as TimetableApplication).currentTimetable != null

    private fun setupLayout() {
        findViewById<View>(R.id.button_import).setOnClickListener {
            startImportFragment()
        }

        findViewById<View>(R.id.button_next).setOnClickListener {
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
