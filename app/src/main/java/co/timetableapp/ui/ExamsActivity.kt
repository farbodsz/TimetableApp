package co.timetableapp.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import co.timetableapp.R
import co.timetableapp.framework.Exam

/**
 * An activity for displaying a list of exams to the user.
 *
 * @see Exam
 * @see ExamsFragment
 * @see ExamDetailActivity
 * @see ExamEditActivity
 */
class ExamsActivity : NavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        setupToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, ExamsFragment())
                    .commit()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_EXAMS

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
