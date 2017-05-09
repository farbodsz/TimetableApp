package co.timetableapp.data

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.TimetableHandler
import co.timetableapp.model.BaseItem

/**
 * A worker fragment to handle import and export actions. It displays warning dialogs, verifies the
 * relevant permissions, and is generally concerned with the UI portion of the porting process.
 *
 * The file I/O is managed by [DataPorting].
 */
class PortingFragment : Fragment() {

    companion object {

        private const val LOG_TAG = "PortingFragment"

        const val ARGUMENT_PORT_TYPE = "extra_port_type"
        const val TYPE_IMPORT = 0
        const val TYPE_EXPORT = 1

        /**
         * A fragment construction argument key used to specify whether the database being imported
         * will be the first database in the app. This would be true if the user has installed the
         * app for the first time, and there is no database in the app.
         *
         * The value passed with this argument key is only used if the argument key
         * [ARGUMENT_PORT_TYPE] is passed with a value [TYPE_IMPORT] (i.e. if this fragment is being
         * used to import).
         * By default, a value of 'false' will be used for this key if this argument is not
         * specified when creating the fragment.
         *
         * @see mImportingFirstDb
         * @see startImport
         */
        const val ARGUMENT_IMPORT_FIRST_DB = "extra_import_first_database"

        private const val REQUEST_CODE_EXPORT_PERM = 1
        private const val REQUEST_CODE_IMPORT_PERM = 2
        private const val REQUEST_CODE_PICK_IMPORTING_DB = 3

        private val STORAGE_PERMISSIONS = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    var onPortingCompleteListener: OnPortingCompleteListener? = null

    /**
     * Whether the database being imported will be the first database in the app (i.e. true if the
     * app doesn't have an existing database).
     *
     * @see ARGUMENT_IMPORT_FIRST_DB
     */
    private var mImportingFirstDb: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mImportingFirstDb = arguments.getBoolean(ARGUMENT_IMPORT_FIRST_DB, false)

        val portType = arguments.getInt(ARGUMENT_PORT_TYPE)
        when (portType) {
            TYPE_IMPORT -> startImport()
            TYPE_EXPORT -> startExport()
            else -> throw IllegalArgumentException("invalid port type: $portType")
        }
    }

    /**
     * Starts the import process.
     *
     * It first displays a dialog warning the user of the consequences of importing. If they choose
     * to continue with the import, then we check to see if we've been granted storage permissions.
     *
     * The import process will be started [onRequestPermissionsResult] after the permissions have
     * been verified.
     */
    private fun startImport() {
        if (mImportingFirstDb!!) {
            // Don't show the warning dialog since there is no data that would be overwritten.
            verifyStoragePermissions(true)
            return
        }

        AlertDialog.Builder(context)
                .setTitle(R.string.dialog_import_warning_title)
                .setMessage(R.string.dialog_import_warning_message)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    verifyStoragePermissions(true)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    /**
     * Starts the export process.
     *
     * First, we need to check if the storage permissions have been granted. The export process
     * will be invoked [onRequestPermissionsResult] after verifying permissions.
     */
    private fun startExport() {
        verifyStoragePermissions(false)
    }

    /**
     * Checks to see if the user has granted the storage permissions, which are needed for porting.
     *
     * If the permissions haven't been granted, it will request for the permissions. If they have,
     * a porting process (import/export) will be started depending on the [importing] parameter.
     */
    private fun verifyStoragePermissions(importing: Boolean) {
        Log.v(LOG_TAG, "Verifying storage permissions")

        val storagePermission = if (importing) {
            Manifest.permission.READ_EXTERNAL_STORAGE
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        // Check if we have the 'write' permission
        val permission = ActivityCompat.checkSelfPermission(context, storagePermission)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            Log.v(LOG_TAG, "No permission - prompting user")

            val requestCode = if (importing) REQUEST_CODE_IMPORT_PERM else REQUEST_CODE_EXPORT_PERM
            requestPermissions(STORAGE_PERMISSIONS, requestCode)

        } else {
            if (importing) handleDbImport() else handleDbExport()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_EXPORT_PERM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "Permission to write to storage granted.")
                handleDbExport()
            } else {
                Log.w(LOG_TAG, "Permission to write to storage denied.")
            }

        } else if (requestCode == REQUEST_CODE_IMPORT_PERM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "Permission to write to storage granted.")
                handleDbImport()
            } else {
                Log.w(LOG_TAG, "Permission to write to storage denied.")
            }
        }
    }

    private fun handleDbExport() {
        val successful = DataPorting.exportDatabase(activity)

        onPortingCompleteListener?.onPortingComplete(TYPE_EXPORT, successful)

        val toastTextRes = if (successful) {
            R.string.data_export_success
        } else {
            R.string.data_export_fail
        }
        Toast.makeText(context, toastTextRes, Toast.LENGTH_SHORT).show()
    }

    private fun handleDbImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMPORTING_DB)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMPORTING_DB) {
            if (resultCode == RESULT_OK) {
                val exportBackup = !mImportingFirstDb!! // export backup if there is an existing db
                completeDbImport(data!!.data, exportBackup)
            }
        }
    }

    private fun completeDbImport(importData: Uri, exportBackup: Boolean) {
        if (!DataPorting.isDatabaseValid(importData)) {
            showImportInvalidDialog()
            return
        }

        if (exportBackup) {
            Log.e(LOG_TAG, "Preparing to export backup of data before completing import.")
            startExport()
        }

        val successful = DataPorting.importDatabase(activity, importData)

        setNewCurrentTimetable()

        onPortingCompleteListener?.onPortingComplete(TYPE_IMPORT, successful)

        val toastTextRes = if (successful) {
            R.string.data_import_success
        } else {
            R.string.data_import_fail
        }
        Toast.makeText(context, toastTextRes, Toast.LENGTH_SHORT).show()
    }

    private fun showImportInvalidDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.dialog_import_invalid_title)
                .setMessage(R.string.dialog_import_invalid_message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
    }

    private fun setNewCurrentTimetable() {
        val timetables = TimetableHandler(context).getAllItems()
        timetables.sortWith(BaseItem.ItemIdComparator())

        val newCurrent = timetables[0]
        (activity.application as TimetableApplication).setCurrentTimetable(context, newCurrent)
    }

    interface OnPortingCompleteListener {

        /**
         * Callback invoked when import/export has been completed.
         *
         * @param portingType 0 for import, 1 for export
         * @param successful whether the porting operation completed successfully
         */
        fun onPortingComplete(portingType: Int, successful: Boolean)

    }

}
