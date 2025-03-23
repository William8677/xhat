package com.williamfq.domain.repository

import com.williamfq.domain.models.Contact

interface ContactRepository {
    suspend fun getMostFrequentContacts(limit: Int): List<Contact>
    suspend fun getContact(contactId: String): Contact?
    suspend fun saveContact(contact: Contact): Long
    suspend fun deleteContact(contactId: String)
    suspend fun updateContactFrequency(contactId: String)
}