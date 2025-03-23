/*
 * Updated: 2025-01-22 01:29:38
 * Author: William8677
 */

package com.williamfq.xhat.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.williamfq.xhat.domain.model.CallRecord
import com.williamfq.xhat.domain.model.CallStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.Filter

@Singleton
class CallRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getCallHistory(userId: String): Flow<List<CallRecord>> = callbackFlow {
        val subscription = db.collection("calls")
            .where(
                Filter.or(
                    Filter.equalTo("callerUserId", userId),
                    Filter.equalTo("receiverUserId", userId)
                )
            )
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val calls = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CallRecord::class.java)
                } ?: emptyList()

                trySend(calls)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun saveCallRecord(call: CallRecord) {
        db.collection("calls")
            .add(call)
    }

    suspend fun updateCallStatus(callId: String, status: CallStatus) {
        db.collection("calls")
            .document(callId)
            .update("status", status)
    }

    suspend fun updateCallDuration(callId: String, endTime: Long, duration: Long) {
        db.collection("calls")
            .document(callId)
            .update(
                mapOf(
                    "endTime" to endTime,
                    "duration" to duration
                )
            )
    }
}