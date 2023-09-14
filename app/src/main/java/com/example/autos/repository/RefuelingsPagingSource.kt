package com.example.autos.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.autos.domain.DomainRefueling

private const val TAG = "xxRps"

class RefuelingsPagingSource (
    private val repository: AutosRepository,
    private val autoId: Int
): PagingSource<Int, DomainRefueling>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DomainRefueling> {
        return try {
            val page = params.key ?: 1
            val offset = (page - 1) * 10
            val response: List<DomainRefueling> = repository.getRepostajes(autoId, offset)

            LoadResult.Page(
                data = response,
                prevKey = null ,
                nextKey =
                if (response.isEmpty()) null
                else                    page.plus(1)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DomainRefueling>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let {  anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}