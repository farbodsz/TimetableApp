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
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.model.BaseItem
import co.timetableapp.ui.base.ItemEditActivity.Companion.EXTRA_ITEM
import co.timetableapp.util.UiUtils

/**
 * Invoked and displayed to the user for editing the details of an item.
 *
 * This activity can also be started to create a new item. If so, there will be no intent extra data
 * supplied to this activity (i.e. [EXTRA_ITEM] will be null).
 *
 * @see ItemDetailActivity
 * @see ItemListFragment
 */
abstract class ItemEditActivity<T : BaseItem> : AppCompatActivity() {

    companion object {

        /**
         * The key for the item passed through an intent extra.
         *
         * It should be null if we're creating a new item.
         */
        internal const val EXTRA_ITEM = "extra_item"
    }

    /**
     * The item whose details will be displayed in this activity's UI.
     */
    @JvmField protected var mItem: T? = null

    /**
     * Whether or not we are creating a new item.
     */
    @JvmField protected var mIsNew = true

    @JvmField protected var mToolbar: Toolbar? = null

    /**
     * The layout resource for the activity layout.
     *
     * @see setContentView
     */
    @LayoutRes
    protected abstract fun getLayoutResource(): Int

    /**
     * When a subclass activity is created, by default, the following will happen:
     *  - Sets the activity content using a layout resource
     *  - Handles passed intent extras
     *  - Invokes a function to setup the user interface
     *
     * @see getLayoutResource
     * @see handleExtras
     * @see setupLayout
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResource())

        handleExtras()

        mToolbar = setupToolbar()
        setupLayout()
    }

    /**
     * Receives the intent extras and assigns the value for [EXTRA_ITEM] to [mItem].
     *
     * The value for [mIsNew] is set depending on whether extras have been passed or not.
     */
    protected open fun handleExtras() {
        val extras = intent.extras
        if (extras != null) {
            mItem = extras.getParcelable(EXTRA_ITEM)
            mIsNew = false
        }
    }

    private fun setupToolbar(): Toolbar {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setTitle(getTitleRes(mIsNew))

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { handleCloseAction() }

        return toolbar
    }

    @StringRes
    protected abstract fun getTitleRes(isNewItem: Boolean): Int

    protected abstract fun setupLayout()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_edit, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_done)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu!!.findItem(R.id.action_delete).isVisible = !mIsNew
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_done -> handleDoneAction()
            R.id.action_delete -> handleDeleteAction()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        handleCloseAction()
    }

    protected open fun handleCloseAction() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    protected abstract fun handleDoneAction()

    protected abstract fun handleDeleteAction()

}
