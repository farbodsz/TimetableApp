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

package co.timetableapp.ui.subjects

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import co.timetableapp.R
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.NavigationDrawerActivity

/**
 * An activity for displaying a list of subjects to the user.
 *
 * @see Subject
 * @see SubjectsFragment
 * @see SubjectEditActivity
 */
class SubjectsActivity : NavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        setupToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, SubjectsFragment())
                    .commit()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_SUBJECTS

    override fun getSelfToolbar(): Toolbar = findViewById(R.id.toolbar)

    override fun getSelfDrawerLayout(): DrawerLayout = findViewById(R.id.drawerLayout)

    override fun getSelfNavigationView(): NavigationView = findViewById(R.id.navigationView)

}
