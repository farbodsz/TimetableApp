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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import co.timetableapp.R
import co.timetableapp.data.handler.SubjectHandler
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.base.ItemListFragment
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

    override fun setupLayout() {
        super.setupLayout()
        setupFab()
    }

    private fun setupFab() {
        activity.findViewById(R.id.fab).setOnClickListener {
            val intent = Intent(activity, SubjectEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL)
        }
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = SubjectsAdapter(activity, mItems!!)
        adapter.setOnItemClickListener { view, position ->
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

            startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL, bundle)
        }

        return adapter
    }

    override fun sortList() = mItems!!.sort()

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_school_black_24dp,
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
