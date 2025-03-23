package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.ContactEntity

@Dao
interface ContactDao {
    @Query("""
        SELECT * FROM contacts 
        ORDER BY frequency DESC, last_interaction DESC 
        LIMIT :limit
    """)
    suspend fun getMostFrequentContacts(limit: Int): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET frequency = frequency + 1, last_interaction = :timestamp WHERE id = :contactId")
    suspend fun incrementContactFrequency(contactId: String, timestamp: Long = System.currentTimeMillis())
}