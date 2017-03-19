package com.satsumasoftware.timetable.db

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
import com.satsumasoftware.timetable.R

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

        private const val REQUEST_CODE_EXPORT_PERM = 1
        private const val REQUEST_CODE_IMPORT_PERM = 2
        private const val REQUEST_CODE_PICK_IMPORTING_DB = 3

        private val STORAGE_PERMISSIONS = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    var onPortingCompleteListener: OnPortingCompleteListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val portType = arguments.getInt(ARGUMENT_PORT_TYPE)
        when (portType) {
            TYPE_IMPORT -> startImport()
            TYPE_EXPORT -> startExport()
            else -> throw IllegalArgumentException("invalid port type: $portType")
        }
    }

    /**
     * Starts the import process.
     * Displays a dialog warning the user of the consequences of importing. If they choose to
     * continue with the import, then we check to see if we've been granted storage permissions.
     */
    private fun startImport() {
        AlertDialog.Builder(context)
                .setTitle(R.string.dialog_import_warning_title)
                .setMessage(R.string.dialog_import_warning_message)
                .setPositiveButton(android.R.string.yes) { _, _ -> verifyStoragePermissions(true) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    /**
     * Starts the export process.
     * First, we need to check if the storage permissions have been granted.
     */
    private fun startExport() {
        verifyStoragePermissions(false)
    }

    /**
     * Checks to see if the user has granted the storage permissions, which are needed for porting.
     * If the permissions haven't been granted, it will request for the permissions.
     */
    private fun verifyStoragePermissions(importing: Boolean) {
        Log.v(LOG_TAG, "Verifying storage permissions")

        val storagePermission = if (importing)
            Manifest.permission.READ_EXTERNAL_STORAGE
        else
            Manifest.permission.WRITE_EXTERNAL_STORAGE

        // Check if we have the 'write' permission
        val permission = ActivityCompat.checkSelfPermission(context, storagePermission)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            Log.v(LOG_TAG, "No permission - prompting user")

            val requestCode = if (importing) REQUEST_CODE_IMPORT_PERM else REQUEST_CODE_EXPORT_PERM

            ActivityCompat.requestPermissions(
                    activity,
                    STORAGE_PERMISSIONS,
                    requestCode)

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
                completeDbImport(data!!.data)
            }
        }
    }

    private fun completeDbImport(importData: Uri) {
        if (!DataPorting.isDatabaseValid(importData)) {
            showImportInvalidDialog()
            return
        }

        Log.e(LOG_TAG, "Preparing to export backup of data before completing import.")
        startExport()

        val successful = DataPorting.importDatabase(activity, importData)

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
