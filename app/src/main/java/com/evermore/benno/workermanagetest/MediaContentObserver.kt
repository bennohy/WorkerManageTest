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
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d("ContentChange", "change selfChange > $selfChange, uri > $uri")
    }

    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        super.onChange(selfChange, uri, flags)
        Log.d("ContentChange", "change selfChange > $selfChange, uri > $uri, flags > $flags")
        val flag = when (flags) {
            NOTIFY_INSERT -> "NOTIFY_INSERT"
            NOTIFY_UPDATE -> "NOTIFY_UPDATE"
            NOTIFY_DELETE -> "NOTIFY_DELETE"
            else -> flags
        }
        Log.d("ContentChange", "->change flags > $flag")
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
        uri?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.query(this, projection, Bundle().apply {
                    putInt(QUERY_ARG_LIMIT, 1)
                    putStringArray(QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media._ID))
                    putInt(QUERY_ARG_SORT_DIRECTION, QUERY_SORT_DIRECTION_DESCENDING)
//                    putString(QUERY_ARG_SQL_SELECTION, selection)
//                    putStringArray(QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                }, null)
            } else {
                context.contentResolver.query(this, projection, null, null, "${MediaStore.Images.Media._ID} DESC LIMIT 1")
            }?.use { cursor ->
                Log.d("ContentChange", "-->change uri query cursor.count > ${cursor.count}")
                if (cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                            val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                            val displayNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
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
}