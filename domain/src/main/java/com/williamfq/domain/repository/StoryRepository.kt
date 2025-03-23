/*
 * Updated: 2025-02-16 14:39:42
 * Author: William8677
 */

package com.williamfq.domain.repository

import com.williamfq.domain.model.*
import kotlinx.coroutines.flow.Flow

interface StoryRepository {

    val stories: Flow<List<Story>>

    suspend fun getStories(): List<Story>

    suspend fun getStoryById(storyId: Int): Story?

    suspend fun addStory(story: Story)

    suspend fun updateStory(updatedStory: Story)

    suspend fun deleteStory(storyId: Int)

    suspend fun markStoryAsViewed(storyId: Int)

    suspend fun addReaction(storyId: Int, reaction: Reaction)

    suspend fun addComment(storyId: Int, commentId: String, comment: String)

    suspend fun getMoreStories(offset: Int): List<Story>

    suspend fun getStoriesByUserId(userId: String): List<Story>

    suspend fun searchStories(query: String): List<Story>

    suspend fun addMention(storyId: Int, mention: StoryMention)

    suspend fun addHashtag(storyId: Int, hashtag: StoryHashtag)

    suspend fun addInteraction(storyId: Int, interaction: StoryInteraction)

    suspend fun createHighlight(highlight: StoryHighlight)

    suspend fun shareStory(storyId: Int, platform: String)

    suspend fun updateStories(storiesToUpdate: List<Story>)
}