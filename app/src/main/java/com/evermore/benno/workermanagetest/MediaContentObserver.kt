package com.evermore.benno.workermanagetest

import android.content.ContentResolver.*
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log

class MediaContentObserver(private val context: Context, private val contentUri: Uri, private val handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        Log.d("ContentChange", "change uri > $uri")
        val flag = when (flags) {
            NOTIFY_INSERT -> "NOTIFY_INSERT"
            NOTIFY_UPDATE -> "NOTIFY_UPDATE"
            NOTIFY_DELETE -> "NOTIFY_DELETE"
            else -> flags
        }
        Log.d("ContentChange", "change flags > $flag")
        val projection = arrayOf(
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection =
            "${MediaStore.Images.Media.TITLE} NOT LIKE ?"
        val selectionArgs = arrayOf(".pending%")
        uri?.run {
            context.contentResolver.query(this, projection, selection, selectionArgs, "_id DESC")?.let { cursor ->
                Log.d("ContentChange", "change uri query cursor.count > ${cursor.count}")
                if (cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                            val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val relativePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                            val titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                            val displayNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                            val id = cursor.getString(idColumn)
                            val data = cursor.getString(dataColumn)
                            val relativePath = cursor.getString(relativePathColumn)
                            val title = cursor.getString(titleColumn)
                            val displayName = cursor.getString(displayNameColumn)
                            Log.d("ContentChange", "uri cursor of id > $id")
                            Log.d("ContentChange", "uri cursor of data > $data")
                            Log.d("ContentChange", "uri cursor of relativePath > $relativePath")
                            Log.d("ContentChange", "uri cursor of title > $title")
                            Log.d("ContentChange", "uri cursor of displayName > $displayName")
                        } while (cursor.moveToNext())
                    }
                }
                cursor.close()
            }
        }
    }
}