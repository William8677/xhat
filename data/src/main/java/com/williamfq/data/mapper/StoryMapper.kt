package com.williamfq.data.mapper

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.williamfq.data.entities.StoryEntity
import com.williamfq.domain.model.*

object StoryMapper {
    private val gson = Gson()

    private inline fun <reified T> fromJson(json: String, defaultValue: T): T = try {
        if (json.isBlank()) defaultValue
        else gson.fromJson(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        defaultValue
    }

    private fun String?.toColor(): Color? {
        return try {
            this?.let { Color(AndroidColor.parseColor(it)) }
        } catch (e: Exception) {
            null
        }
    }

    private fun Color.toHexString(): String {
        return String.format("#%08X", this.value.toLong())
    }

    fun Story.toEntity() = StoryEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        content = content,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        timestamp = timestamp,
        isActive = isActive,
        views = views,
        durationHours = durationHours,
        durationSeconds = durationSeconds,
        isEncrypted = isEncrypted,
        encryptionType = encryptionType,
        tags = gson.toJson(tags),
        comments = gson.toJson(comments),
        reactions = gson.toJson(reactions),
        poll = poll?.let { gson.toJson(it) },
        mentions = gson.toJson(mentions),
        hashtags = gson.toJson(hashtags),
        interactions = gson.toJson(interactions),
        highlights = gson.toJson(highlights),
        hasEffects = hasEffects,
        hasInteractiveElements = hasInteractiveElements,
        privacy = privacy,
        expirationTime = expirationTime,
        allowedViewers = gson.toJson(allowedViewers),
        blockedViewers = gson.toJson(blockedViewers),
        category = category,
        metadata = gson.toJson(metadata),
        analytics = gson.toJson(metadata.analytics),
        processingStatus = metadata.processingStatus,
        viewerSettings = gson.toJson(metadata.viewerSettings),
        question = question,
        options = options?.let { gson.toJson(it) },
        correctAnswer = correctAnswer,
        explanation = explanation,
        votes = votes?.let { gson.toJson(it) },
        quizQuestion = quizQuestion,
        quizOptions = quizOptions?.let { gson.toJson(it) },
        pollQuestion = pollQuestion,
        pollOptions = pollOptions?.let { gson.toJson(it) },
        backgroundColor = backgroundColor?.toHexString(),
        textColor = textColor?.toHexString(),
        interactiveElements = interactiveElements?.let { gson.toJson(it) },
        arInstructions = arInstructions
    )

    fun StoryEntity.toDomain() = Story(
        id = id,
        userId = userId,
        title = title,
        description = description,
        content = content,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        timestamp = timestamp,
        isActive = isActive,
        views = views,
        durationHours = durationHours,
        durationSeconds = durationSeconds,
        isEncrypted = isEncrypted,
        encryptionType = encryptionType,
        tags = fromJson(tags, emptyList()),
        comments = fromJson(comments, emptyList()),
        reactions = fromJson(reactions, emptyList()),
        poll = poll?.let { fromJson(it, null as Poll?) },
        mentions = fromJson(mentions, emptyList()),
        hashtags = fromJson(hashtags, emptyList()),
        interactions = fromJson(interactions, emptyList()),
        highlights = fromJson(highlights, emptyList()),
        hasEffects = hasEffects,
        hasInteractiveElements = hasInteractiveElements,
        privacy = privacy,
        expirationTime = expirationTime,
        allowedViewers = fromJson(allowedViewers, emptyList()),
        blockedViewers = fromJson(blockedViewers, emptyList()),
        category = category,
        metadata = fromJson(metadata, StoryMetadata()),
        question = question,
        options = options?.let { fromJson(it, null as List<String>?) },
        correctAnswer = correctAnswer,
        explanation = explanation,
        votes = votes?.let { fromJson(it, null as List<Int>?) },
        quizQuestion = quizQuestion,
        quizOptions = quizOptions?.let { fromJson(it, null as List<String>?) },
        pollQuestion = pollQuestion,
        pollOptions = pollOptions?.let { fromJson(it, null as List<String>?) },
        backgroundColor = backgroundColor?.toColor(),
        textColor = textColor?.toColor(),
        interactiveElements = interactiveElements?.let { fromJson(it, null as List<InteractiveElement>?) },
        arInstructions = arInstructions
    )

    fun List<Story>.toEntityList() = map { it.toEntity() }
    fun List<StoryEntity>.toDomainList() = map { it.toDomain() }
}