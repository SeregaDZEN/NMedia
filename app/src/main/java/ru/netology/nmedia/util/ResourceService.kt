package ru.netology.nmedia.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

}