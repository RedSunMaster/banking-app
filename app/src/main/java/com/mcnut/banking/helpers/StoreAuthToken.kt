package com.mcnut.banking.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreAuthToken(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStoree: DataStore<Preferences> by preferencesDataStore("authToken")
        val USER_AUTH_TOKEN = stringPreferencesKey("authToken")
    }

    //get the saved email
    val getAuthToken: Flow<String?> = context.dataStoree.data
        .map { preferences ->
            preferences[USER_AUTH_TOKEN]
        }

    //save email into datastore
    suspend fun saveAuthToken(token: String) {
        context.dataStoree.edit { preferences ->
            preferences[USER_AUTH_TOKEN] = token
        }
    }
}

class StoreCheckedCategories(private val context: Context) {
    companion object {
        private val Context.dataStoree: DataStore<Preferences> by preferencesDataStore("checkedCategories")
        val CHECKED_CATEGORIES = stringSetPreferencesKey("checkedCategories")
    }
    //get the saved checked categories
    val getCheckedCategories: Flow<Set<String>> = context.dataStoree.data
        .map { preferences ->
            preferences[CHECKED_CATEGORIES] ?: setOf()
        }

    //save checked categories into datastore
    suspend fun saveCheckedCategories(categories: Set<String>) {
        context.dataStoree.edit { preferences ->
            preferences[CHECKED_CATEGORIES] = categories
        }
    }

}

class StoreDarkMode(private val context: Context) {
    companion object {
        private val Context.dataStoree: DataStore<Preferences> by preferencesDataStore("darkMode")
        val DARK_MODE = intPreferencesKey("darkMode")
    }
    //get the saved dark mode state
    val getDarkMode: Flow<Int> = context.dataStoree.data
        .map { preferences ->
            preferences[DARK_MODE] ?: 2 // default to follow system
        }

    //save dark mode state into datastore
    suspend fun saveDarkMode(modeIndex: Int) {
        context.dataStoree.edit { preferences ->
            preferences[DARK_MODE] = modeIndex
        }
    }
}
