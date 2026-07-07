package com.dmariani.streamkit.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class MuxApiClient @Inject constructor(
    private val httpClient: HttpClient,
) {

    suspend fun listAssets(): Result<List<MuxAssetDto>> = try {
        val allAssets = mutableListOf<MuxAssetDto>()
        var cursor: String? = null
        do {
            val response: MuxListAssetsResponse = httpClient.get(ASSETS_URL) {
                cursor?.let { parameter(PAGE_TOKEN_PARAM, it) }
            }.body()
            allAssets.addAll(response.data)
            cursor = response.nextCursor
        } while (cursor != null)
        Result.success(allAssets)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        const val ASSETS_URL = "https://api.mux.com/video/v1/assets"
        const val PAGE_TOKEN_PARAM = "page_token"
    }
}
