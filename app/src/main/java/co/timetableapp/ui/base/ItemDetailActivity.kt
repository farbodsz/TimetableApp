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

package co.timetableapp.ui.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.handler.TimetableItemHandler
import co.timetableapp.model.TimetableItem
import co.timetableapp.ui.base.ItemDetailActivity.Companion.EXTRA_ITEM
import co.timetableapp.util.UiUtils

/**
 * An activity for showing the details of an item (e.g. assignment, class, exam, etc.).
 *
 * The details are displayed to the user but they cannot be edited here, and must instead be done in
 * the corresponding 'edit' activity.
 *
 * This activity may be started to create a new item, passing no intent data so that [EXTRA_ITEM] is
 * null.
 *
 * @param T the type of the item (e.g. assignment, class, exam)
 *
 * @see ItemEditActivity
 * @see ItemListFragment
 */
abstract class ItemDetailActivity<T : TimetableItem> : AppCompatActivity() {

    companion object {

        private const val LOG_TAG = "ItemDetailActivity"

        /**
         * The key for the item of type [T] passed through an intent extra.

         * It should be null if we're creating a new item.
         */
        internal const val EXTRA_ITEM = "extra_item"

        /**
         * Request code to use when starting an activity to edit item [T] with a result.
         *
         * @see onMenuEditClick
         */
        internal const val REQUEST_CODE_ITEM_EDIT = 1
    }

    /**
     * The item whose details will be displayed in this activity's UI.
     */
    @field:JvmSynthetic  // prevent backing methods being visible in Java activities
    protected lateinit var mItem: T

    /**
     * Whether or not we are creating a new item.
     */
    @JvmField protected var mIsNew: Boolean = false

    protected abstract fun initializeDataHandler(): TimetableItemHandler<T>

    protected val mDataHandler: TimetableItemHandler<T> by lazy { initializeDataHandler() }

    /**
     * The layout resource for the activity layout.
     *
     * @see setContentView
     */
    @LayoutRes
    protected abstract fun getLayoutResource(): Int

    /**
     * When a subclass activity is started, by default, the following will occur when the activity
     * gets created:
     *  - Sets the activity content using a layout resource
     *  - Initializes the data handler
     *  - Handles passed intent extras
     *  - Invokes a function to setup the UI
     *
     *  @see getLayoutResource
     *  @see initializeDataHandler
     *  @see onNullExtras
     *  @see setupLayout
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResource())

        val extras = intent.extras
        if (extras == null) {
            // No item to display - assume we mean to create a new one
            mIsNew = true
            onNullExtras()
            return
        }

        mItem = extras.getParcelable(EXTRA_ITEM)

        setupLayout()
    }

    /**
     * Callback for when intent extras are null.
     * This would be invoked when there is no item to display, so we assume to create a new one.
     *
     * Implementations of this function should start the 'edit' activity passing no intent extras.
     *
     * @see onMenuEditClick
     */
    protected abstract fun onNullExtras()

    protected abstract fun setupLayout()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ITEM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the edited item (if new, it would have the highest id)
                val editedItemId = if (mIsNew) {
                    mDataHandler.getHighestItemId()
                } else {
                    mItem.id
                }

                try {
                    mItem = mDataHandler.createFromId(editedItemId)
                } catch (e: DataNotFoundException) {
                    Log.d(LOG_TAG, "Item cannot be found - assume it must have been deleted")
                    saveDeleteAndClose()
                }

                if (mIsNew) {
                    saveEditsAndClose()
                } else {
                    setupLayout()
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (mIsNew) {
                    cancelAndClose()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_detail, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_edit)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_edit -> onMenuEditClick()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Callback for when the 'edit' menu item is clicked.
     * Implementations of this function should start the 'edit' activity, sending [mItem] as an
     * intent extra.
     *
     * @see onNullExtras
     */
    protected abstract fun onMenuEditClick()

    override fun onBackPressed() = saveEditsAndClose()

    /**
     * Exits this activity without saving changes.
     */
    protected abstract fun cancelAndClose()

    /**
     * Exits this activity after saving changes.
     */
    protected abstract fun saveEditsAndClose()

    /**
     * Exits this activity after saving the item deletion.
     */
    protected abstract fun saveDeleteAndClose()

}
