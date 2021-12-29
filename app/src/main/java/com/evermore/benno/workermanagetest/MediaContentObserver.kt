package com.evermore.benno.workermanagetest

import android.content.ContentResolver
import android.content.ContentResolver.*
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log

class MediaContentObserver(private val context: Context, private val contentUri: Uri, private val handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        Log.d("ContentChange", "change selfChange > $selfChange")
        queryChangeUri(contentUri)
    }

    private fun queryChangeUri(uri: Uri) {
        Log.d("ContentChange", "->queryChangeUri > $uri")
        val queryLimit = 1
        val querySortBy = MediaStore.Images.Media._ID
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.RELATIVE_PATH
            )
        } else {
            arrayOf(
                MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME
            )
        }
        val selection = "${MediaStore.Images.Media.TITLE} NOT LIKE ?"
        val selectionArgs = arrayOf(".pending%")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.query(uri, projection, Bundle().apply {
                putInt(QUERY_ARG_LIMIT, queryLimit)
                putStringArray(QUERY_ARG_SORT_COLUMNS, arrayOf(querySortBy))
                putInt(QUERY_ARG_SORT_DIRECTION, QUERY_SORT_DIRECTION_DESCENDING)
                    putString(QUERY_ARG_SQL_SELECTION, selection)
                    putStringArray(QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            }, null)
        } else {
            context.contentResolver.query(uri, projection, null, null, "$querySortBy DESC LIMIT $queryLimit")
        }?.use { cursor ->
            Log.d("ContentChange", "-->change uri query cursor.count > ${cursor.count}")
            if (cursor.count > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                        val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                        val titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                        val displayNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                        val dateModifiedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                        val dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                        val id = cursor.getString(idColumn)
                        val data = cursor.getString(dataColumn)
                        val relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val relativePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                            cursor.getString(relativePathColumn)
                        } else {
                            ""
                        }
                        val title = cursor.getString(titleColumn)
                        val displayName = cursor.getString(displayNameColumn)
                        val dateModified = cursor.getString(dateModifiedColumn)
                        val dateAdded = cursor.getString(dateAddedColumn)
                        Log.d("ContentChange", "--->uri cursor of id > $id")
                        Log.d("ContentChange", "--->uri cursor of data > $data")
                        Log.d("ContentChange", "--->uri cursor of relativePath > $relativePath")
                        Log.d("ContentChange", "--->uri cursor of title > $title")
                        Log.d("ContentChange", "--->uri cursor of displayName > $displayName")
                    } while (cursor.moveToNext())
                }
            }
            cursor.close()
            Log.d("ContentChange", "uri cursor close")
        }
    }
}