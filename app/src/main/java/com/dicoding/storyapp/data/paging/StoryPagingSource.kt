package com.dicoding.storyapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.storyapp.data.local.database.StoryEntity
import com.dicoding.storyapp.data.retrofit.ApiService

class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, StoryEntity>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStories(position, params.loadSize)

            LoadResult.Page(
                data = responseData.listStory.map { story ->
                    StoryEntity(
                        story.id.toString(),
                        story.name.toString(),
                        story.description.toString(),
                        story.photoUrl.toString(),
                        story.createdAt.toString(),
                        story.lat?.toDouble(),
                        story.lon?.toDouble()
                    )
                },
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStory.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}