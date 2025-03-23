package com.williamfq.xhat.repositories

import com.williamfq.data.dao.PollDao
import com.williamfq.data.entities.Poll
import com.williamfq.data.entities.PollComment
import com.williamfq.data.entities.PollReward

class PollRepository(private val pollDao: PollDao) {

    suspend fun createPoll(poll: Poll): Long {
        return pollDao.insertPoll(poll)
    }

    suspend fun getPoll(pollId: Int): Poll? {
        return pollDao.getPollById(pollId)
    }

    suspend fun addComment(comment: PollComment) {
        pollDao.insertComment(comment)
    }

    suspend fun getComments(pollId: Int): List<PollComment> {
        return pollDao.getCommentsByPollId(pollId)
    }

    suspend fun addReward(reward: PollReward) {
        pollDao.insertReward(reward)
    }

    suspend fun getRewards(pollId: Int): List<PollReward> {
        return pollDao.getRewardsByPollId(pollId)
    }
}
