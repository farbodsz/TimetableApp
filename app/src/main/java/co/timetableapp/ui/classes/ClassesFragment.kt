package co.timetableapp.ui.classes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.model.Class
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemListFragment
import co.timetableapp.util.UiUtils
import java.util.*

/**
 * A fragment for displaying a list of classes to the user.
 *
 * @see Class
 * @see ClassesActivity
 * @see ClassDetailActivity
 * @see ClassEditActivity
 */
class ClassesFragment : ItemListFragment<Class>() {

    companion object {
        private const val REQUEST_CODE_CLASS_DETAIL = 1
    }

    private var mShowAll = false

    override fun instantiateDataHandler() = ClassHandler(activity)

    override fun setupLayout() {
        super.setupLayout()
        setupFab()
    }

    private fun setupFab() {
        activity.findViewById(R.id.fab).setOnClickListener {
            val intent = Intent(activity, ClassEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL)
        }
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = ClassesAdapter(activity, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(activity, ClassDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM, mItems!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL, bundle)
        }

        return adapter
    }

    override fun fetchItems() =
            (mDataHandler as ClassHandler).getCurrentClasses(activity.application, mShowAll)

    override fun sortList() {
        Collections.sort(mItems) { c1, c2 ->
            val s1 = Subject.create(activity, c1.subjectId)!!
            val s2 = Subject.create(activity, c2.subjectId)!!
            s1.name.compareTo(s2.name)
        }
    }

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_class_black_24dp,
            R.string.placeholder_classes,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_classes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_show_all -> {
                item.isChecked = !mShowAll
                mShowAll = !mShowAll
                updateList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
