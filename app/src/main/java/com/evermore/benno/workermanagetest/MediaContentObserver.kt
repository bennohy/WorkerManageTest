package com.evermore.benno.workermanagetest

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log

class MediaContentObserver(private val context: Context, private val handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        super.onChange(selfChange, uri, flags)
        Log.d("ContentChange", "change uri > $uri")
        Log.d("ContentChange", "change flags > $flags")
        val projection = arrayOf(
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.RELATIVE_PATH
        )
        uri?.run {
            context.contentResolver.query(this, projection, null, null, null)?.let { cursor ->
                while (cursor.moveToFirst()) {
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
                }
                cursor.close()
            }
        }
    }
}