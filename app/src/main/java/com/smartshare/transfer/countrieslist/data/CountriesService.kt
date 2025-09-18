/*
 CountriesService: Fetches and parses the countries JSON feed using OkHttp.
 - Performs network I/O on Dispatchers.IO with timeouts.
 - Parses with org.json into Country list while preserving source order.
 - Wraps results in kotlin.Result and propagates failures.
 @Murugesan Sagadevan
*/
package com.smartshare.transfer.countrieslist.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class CountriesService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchCountries(url: String): Result<List<Country>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Unexpected HTTP ${'$'}{response.code}"))
                }
                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty body"))
                val countries = parseCountries(body)
                Result.success(countries)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCountries(json: String): List<Country> {
        val array = JSONArray(json)
        val list = ArrayList<Country>(array.length())
        for (i in 0 until array.length()) {
            val obj: JSONObject = array.getJSONObject(i)
            val name = obj.optString("name")
            val region = obj.optString("region")
            val code = obj.optString("code")
            val capital = obj.optString("capital")
            list.add(Country(name = name, region = region, code = code, capital = capital))
        }
        return list
    }
}


