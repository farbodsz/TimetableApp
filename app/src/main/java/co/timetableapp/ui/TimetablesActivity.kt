package co.timetableapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.PortingFragment
import co.timetableapp.data.handler.TimetableHandler
import co.timetableapp.framework.Timetable
import co.timetableapp.ui.adapter.TimetablesAdapter
import co.timetableapp.util.UiUtils
import java.util.*

/**
 * An activity for displaying a list of timetables to the user.
 *
 * Note that unlike other activities, there cannot be a placeholder background since there is always
 * at least one existing timetable in the app's database.
 *
 * @see Timetable
 * @see TimetableEditActivity
 */
class TimetablesActivity : NavigationDrawerActivity() {

    companion object {
        private val REQUEST_CODE_TIMETABLE_EDIT = 1
    }

    private var mTimetables: ArrayList<Timetable>? = null

    private var mAdapter: TimetablesAdapter? = null
    private val mDataHandler = TimetableHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        setupLayout()
    }

    private fun setupLayout() {
        setupList()

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val intent = Intent(this, TimetableEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT)
        }
    }

    private fun setupList() {
        mTimetables = mDataHandler.getAllItems()
        sortList()

        mAdapter = TimetablesAdapter(this, mTimetables, findViewById(R.id.coordinatorLayout))
        mAdapter!!.setOnEntryClickListener { view, position ->
            val intent = Intent(this, TimetableEditActivity::class.java)
            intent.putExtra(ItemEditActivity.EXTRA_ITEM, mTimetables!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_TIMETABLE_EDIT, bundle)
        }

        val recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = mAdapter
    }

    private fun sortList() = mTimetables!!.sort()

    private fun refreshList() {
        mTimetables!!.clear()
        mTimetables!!.addAll(mDataHandler.getAllItems())
        sortList()
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TIMETABLE_EDIT) {
            if (resultCode == RESULT_OK) {
                refreshList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_timetables, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handles import/export options.
        var portType = -1
        when (item.itemId) {
            R.id.action_export -> portType = PortingFragment.TYPE_EXPORT
            R.id.action_import -> portType = PortingFragment.TYPE_IMPORT
        }
        startPortingFragment(portType)

        return super.onOptionsItemSelected(item)
    }

    private fun startPortingFragment(portType: Int) {
        val fragmentArgs = Bundle()
        fragmentArgs.putInt(PortingFragment.ARGUMENT_PORT_TYPE, portType)

        val portFragment = PortingFragment()
        portFragment.onPortingCompleteListener = object : PortingFragment.OnPortingCompleteListener {
            override fun onPortingComplete(portingType: Int, successful: Boolean) {
                refreshList()

                val currentTimetable = (application as TimetableApplication).currentTimetable!!
                val message = getString(R.string.message_set_current_timetable,
                        currentTimetable.displayedName)

                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        message,
                        Snackbar.LENGTH_LONG).show()
            }
        }
        portFragment.arguments = fragmentArgs

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(portFragment, null)
        transaction.commit()
    }

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_MANAGE_TIMETABLES

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
