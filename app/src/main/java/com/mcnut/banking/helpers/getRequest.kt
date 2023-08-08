package com.mcnut.banking.helpers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


suspend fun getRequest(
    client: OkHttpClient,
    url: String,
    bearerToken: String? = null,
    params: List<Pair<String, Any>>
): Pair<Boolean, Any> = withContext(Dispatchers.IO) {
    val paramString = params.joinToString("&") { "${it.first}=${it.second}" }
    val request = Request.Builder()
        .url("$url?$paramString")
        .addHeader("Authorization", "Bearer $bearerToken")
        .build()
    try {
        val response = client.newCall(request).execute()
        val code = response.code
        if (code == 200) {
            val jsonString = response.body.string()
            return@withContext Pair(true, jsonString)
        } else {
            val errorJson = response.body.string()
            return@withContext Pair(false, errorJson)
        }
    } catch (e: Exception){
        return@withContext  Pair(false, "Couldn't Connect To Server")
    }
}



suspend fun postRequest(
    client: OkHttpClient,
    url: String,
    bearerToken: String? = null,
    variables: List<Pair<String, Any>>): Pair<Boolean, Any?> = withContext(Dispatchers.IO)
{
    val jsonBuilder = StringBuilder()
    jsonBuilder.append("{")
    for ((name, value) in variables) {
        when (value) {
            is String -> jsonBuilder.append("\"$name\":\"$value\",")
            is Double -> jsonBuilder.append("\"$name\":$value,")
        }
    }
    if (variables.isNotEmpty()) {
        jsonBuilder.deleteCharAt(jsonBuilder.lastIndex)
    }
    jsonBuilder.append("}")
    val json = jsonBuilder.toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    Log.d("POST", json)
    val request: Request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $bearerToken")
        .post(requestBody)
        .build()
    try {
        val response = client.newCall(request).execute()
        val code = response.code
        if (code == 200) {
            val jsonString = response.body.string()
            return@withContext Pair(true, jsonString)
        } else {
            val errorJson = response.body.string()
            return@withContext Pair(false, errorJson)
        }
    } catch (e: Exception) {
        return@withContext Pair(false, "Couldn't Connect To Server")
    }
}


suspend fun accountPostRequest(
    url: String,
    bearerToken: String? = null,
    variables: List<Pair<String, Any>>): Pair<Boolean, Any?> = withContext(Dispatchers.IO)
{
    val jsonBuilder = StringBuilder()
    jsonBuilder.append("{")
    for ((name, value) in variables) {
        when (value) {
            is String -> jsonBuilder.append("\"$name\":\"$value\",")
            is Double -> jsonBuilder.append("\"$name\":$value,")
        }
    }
    if (variables.isNotEmpty()) {
        jsonBuilder.deleteCharAt(jsonBuilder.lastIndex)
    }
    jsonBuilder.append("}")
    val json = jsonBuilder.toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    Log.d("POST", json)
    val request: Request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $bearerToken")
        .post(requestBody)
        .build()
    val client = OkHttpClient()
    try {
        val response = client.newCall(request).execute()
        val code = response.code
        if (code == 200) {
            val jsonString = response.body.string()
            return@withContext Pair(true, jsonString)
        } else {
            val errorJson = response.body.string()
            return@withContext Pair(false, errorJson)
        }
    } catch (e: Exception) {
        return@withContext Pair(false, "Couldn't Connect To Server")
    }
}



suspend fun patchRequest(
    client: OkHttpClient,
    url: String,
    bearerToken: String?,
    variables: List<Pair<String, Any>>): Pair<Boolean, Any?> = withContext(Dispatchers.IO)
{
    val jsonBuilder = StringBuilder()
    jsonBuilder.append("{")
    for ((name, value) in variables) {
        when (value) {
            is String -> jsonBuilder.append("\"$name\":\"$value\",")
            is Double -> jsonBuilder.append("\"$name\":$value,")
            is Int -> jsonBuilder.append("\"$name\":$value,")
        }
    }
    if (variables.isNotEmpty()) {
        jsonBuilder.deleteCharAt(jsonBuilder.lastIndex)
    }
    jsonBuilder.append("}")
    val json = jsonBuilder.toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    Log.d("PATCH", json)
    val request: Request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $bearerToken")
        .patch(requestBody)
        .build()
    try {
        val response = client.newCall(request).execute()
        val code = response.code
        if (code == 200) {
            val jsonString = response.body.string()
            return@withContext Pair(true, jsonString)
        } else {
            val errorJson = response.body.string()
            return@withContext Pair(false, errorJson)
        }
    } catch (e: Exception) {
        return@withContext Pair(false, "Couldn't Connect To Server")
    }


}


suspend fun deleteRequest(
    client: OkHttpClient,
    url: String,
    bearerToken: String?,
    variables: List<Pair<String, Any>>): Pair<Boolean, Any?> = withContext(Dispatchers.IO)
{
    val jsonBuilder = StringBuilder()
    jsonBuilder.append("{")
    for ((name, value) in variables) {
        when (value) {
            is String -> jsonBuilder.append("\"$name\":\"$value\",")
            is Double -> jsonBuilder.append("\"$name\":$value,")
            is Int -> jsonBuilder.append("\"$name\":$value,")
        }
    }
    if (variables.isNotEmpty()) {
        jsonBuilder.deleteCharAt(jsonBuilder.lastIndex)
    }
    jsonBuilder.append("}")
    val json = jsonBuilder.toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toRequestBody(mediaType)
    Log.d("DELETE", json)
    val request: Request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $bearerToken")
        .delete(requestBody)
        .build()
    try {
        val response = client.newCall(request).execute()
        val code = response.code
        if (code == 200) {
            val jsonString = response.body.string()
            return@withContext Pair(true, jsonString)
        } else {
            val errorJson = response.body.string()
            return@withContext Pair(false, errorJson)
        }
    } catch (e: Exception) {
        return@withContext Pair(false, "Couldn't Connect To Server")
    }
}
