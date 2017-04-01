package co.timetableapp.ui

import android.app.Activity
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
 * A fragment for displaying a list of subjects to the user.
 *
 * @see Subject
 * @see SubjectsActivity
 * @see SubjectEditActivity
 */
class SubjectsFragment : ItemListFragment<Subject>() {

    companion object {
        private const val REQUEST_CODE_SUBJECT_DETAIL = 1
    }

    override fun instantiateDataHandler() = SubjectHandler(activity)

    override fun onFabButtonClick() {
        val intent = Intent(activity, SubjectEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL)
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = SubjectsAdapter(activity, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(activity, SubjectEditActivity::class.java)
            intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItems!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(activity, intent, REQUEST_CODE_SUBJECT_DETAIL, bundle)
        }

        return adapter
    }

    override fun sortList() = mItems!!.sort()

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_list_black_24dp,
            R.string.placeholder_subjects,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

}
