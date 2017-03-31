package co.timetableapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import co.timetableapp.R
import co.timetableapp.data.handler.SubjectHandler
import co.timetableapp.framework.Subject
import co.timetableapp.ui.adapter.SubjectsAdapter
import co.timetableapp.util.UiUtils

/**
 * An activity for displaying a list of subjects to the user.
 *
 * @see Subject
 * @see SubjectEditActivity
 */
class SubjectsActivity : ItemListActivity<Subject>() {

    companion object {
        private val REQUEST_CODE_SUBJECT_DETAIL = 1
    }

    override fun instantiateDataHandler() = SubjectHandler(this)

    override fun onFabButtonClick() {
        val intent = Intent(this, SubjectEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL)
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = SubjectsAdapter(this, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(this, SubjectEditActivity::class.java)
            intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItems[position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_SUBJECT_DETAIL, bundle)
        }

        return adapter
    }

    override fun sortList() = mItems!!.sort()

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            this,
            R.drawable.ic_list_black_24dp,
            R.string.placeholder_subjects,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == RESULT_OK) {
                updateList()
            }
        }
    }

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_SUBJECTS

}
