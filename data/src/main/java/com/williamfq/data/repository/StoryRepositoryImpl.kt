package com.williamfq.data.repository

import com.williamfq.data.dao.StoryDao
import com.williamfq.data.mapper.StoryMapper.toDomain
import com.williamfq.data.mapper.StoryMapper.toEntity
import com.williamfq.domain.model.*
import com.williamfq.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao
) : StoryRepository {

    private val mutex = Mutex()
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    override val stories: Flow<List<Story>> = _stories.asStateFlow()

    companion object {
        private const val STORIES_PAGE_SIZE = 10
    }

    override suspend fun getStories(): List<Story> = mutex.withLock {
        val stories = storyDao.getAllStories().map { it.toDomain() }
        _stories.value = stories
        stories
    }

    override suspend fun getStoryById(storyId: Int): Story? = mutex.withLock {
        storyDao.getStoryById(storyId)?.toDomain()
    }

    override suspend fun addStory(story: Story) {
        mutex.withLock {
            storyDao.insertStory(story.toEntity())
            _stories.value = _stories.value + story
        }
    }

    override suspend fun updateStory(updatedStory: Story) {
        mutex.withLock {
            storyDao.updateStory(updatedStory.toEntity())
            _stories.value = _stories.value.map { story ->
                if (story.id == updatedStory.id) updatedStory else story
            }
        }
    }

    override suspend fun deleteStory(storyId: Int) {
        mutex.withLock {
            storyDao.deleteStoryById(storyId)
            _stories.value = _stories.value.filter { it.id != storyId }
        }
    }

    override suspend fun markStoryAsViewed(storyId: Int) {
        mutex.withLock {
            storyDao.incrementStoryViews(storyId)
            val story = _stories.value.find { it.id == storyId } ?: return
            val updatedStory = story.copy(views = story.views + 1)
            updateStory(updatedStory)
        }
    }

    override suspend fun addReaction(storyId: Int, reaction: Reaction) {
        mutex.withLock {
            val story = getStoryById(storyId) ?: return
            val updatedStory = story.copy(reactions = story.reactions + reaction)
            updateStory(updatedStory)
        }
    }

    override suspend fun addComment(storyId: Int, commentId: String, comment: String) {
        mutex.withLock {
            val story = getStoryById(storyId) ?: return
            val newComment = Comment(
                id = commentId,
                userId = "currentUser",
                content = comment,
                timestamp = System.currentTimeMillis()
            )
            val updatedStory = story.copy(comments = story.comments + newComment)
            updateStory(updatedStory)
        }
    }

    override suspend fun getMoreStories(offset: Int): List<Story> {
        return storyDao.getStoriesWithOffset(offset, STORIES_PAGE_SIZE).map { it.toDomain() }
    }

    override suspend fun getStoriesByUserId(userId: String): List<Story> = mutex.withLock {
        storyDao.getStoriesByUser(userId).map { it.toDomain() }
    }

    override suspend fun searchStories(query: String): List<Story> = mutex.withLock {
        storyDao.searchStories(query).map { it.toDomain() }
    }

    override suspend fun addMention(storyId: Int, mention: StoryMention) {
        mutex.withLock {
            val story = getStoryById(storyId) ?: return
            val updatedStory = story.copy(mentions = story.mentions + mention)
            updateStory(updatedStory)
        }
    }

    override suspend fun addHashtag(storyId: Int, hashtag: StoryHashtag) {
        mutex.withLock {
            val story = getStoryById(storyId) ?: return
            val updatedStory = story.copy(hashtags = story.hashtags + hashtag)
            updateStory(updatedStory)
        }
    }

    override suspend fun addInteraction(storyId: Int, interaction: StoryInteraction) {
        mutex.withLock {
            val story = getStoryById(storyId) ?: return
            val updatedStory = story.copy(interactions = story.interactions + interaction)
            updateStory(updatedStory)
        }
    }

    override suspend fun createHighlight(highlight: StoryHighlight) {
        println("StoryRepository: Destacado creado: $highlight")
    }

    override suspend fun shareStory(storyId: Int, platform: String) {
        println("StoryRepository: Compartiendo la historia $storyId en la plataforma $platform")
    }

    override suspend fun updateStories(storiesToUpdate: List<Story>) {
        mutex.withLock {
            storiesToUpdate.forEach { story ->
                storyDao.updateStory(story.toEntity())
            }
            val storiesMap = _stories.value.associateBy { it.id }.toMutableMap()
            storiesToUpdate.forEach { updatedStory ->
                storiesMap[updatedStory.id] = updatedStory
            }
            _stories.value = storiesMap.values.toList()
        }
    }
}