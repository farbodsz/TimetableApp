package co.timetableapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.framework.Class
import co.timetableapp.framework.Subject
import co.timetableapp.ui.adapter.ClassesAdapter
import co.timetableapp.util.UiUtils
import java.util.*

/**
 * An activity for displaying a list of classes to the user.
 *
 * @see Class
 * @see ClassDetailActivity
 * @see ClassEditActivity
 */
class ClassesActivity : ItemListActivity<Class>() {

    companion object {
        private val REQUEST_CODE_CLASS_DETAIL = 1
    }

    private var mShowAll = false

    override fun instantiateDataHandler() = ClassHandler(this)

    override fun onFabButtonClick() {
        val intent = Intent(this, ClassEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL)
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = ClassesAdapter(this, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(this, ClassDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM, mItems[position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_CLASS_DETAIL, bundle)
        }

        return adapter
    }

    override fun getItems() =
            (mDataHandler as ClassHandler).getCurrentClasses(application, mShowAll)

    override fun sortList() {
        Collections.sort(mItems) { c1, c2 ->
            val s1 = Subject.create(baseContext, c1.subjectId)!!
            val s2 = Subject.create(baseContext, c2.subjectId)!!
            s1.name.compareTo(s2.name)
        }
    }

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            this,
            R.drawable.ic_class_black_24dp,
            R.string.placeholder_classes,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == RESULT_OK) {
                refreshList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_classes, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_manage_subjects)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_manage_subjects -> startActivity(Intent(this, SubjectsActivity::class.java))

            R.id.action_show_all -> {
                item.isChecked = !mShowAll
                mShowAll = !mShowAll
                refreshList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_CLASSES

}
