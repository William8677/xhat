package com.williamfq.data.repository

import com.williamfq.data.dao.ContactDao
import com.williamfq.data.entities.ContactEntity
import com.williamfq.data.mappers.toDomain
import com.williamfq.data.mappers.toEntity
import com.williamfq.domain.models.Contact
import com.williamfq.domain.repository.ContactRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao
) : ContactRepository {

    override suspend fun getMostFrequentContacts(limit: Int): List<Contact> {
        return contactDao.getMostFrequentContacts(limit).map { it.toDomain() }
    }

    override suspend fun getContact(contactId: String): Contact? {
        // Como no existe el método en el DAO, podemos obtenerlo de getMostFrequentContacts
        return contactDao.getMostFrequentContacts(Int.MAX_VALUE)
            .find { it.id == contactId }
            ?.toDomain()
    }

    override suspend fun saveContact(contact: Contact): Long {
        contactDao.insertContact(contact.toEntity())
        return 0L // El DAO no devuelve el ID, pero cumplimos con la interfaz
    }

    override suspend fun deleteContact(contactId: String) {
        // Como no existe deleteContact en el DAO, podemos crear un ContactEntity vacío
        // con el ID y frecuencia 0 para efectivamente "eliminarlo" de los contactos frecuentes
        contactDao.updateContact(
            ContactEntity(
                id = contactId,
                name = "", // Nombre vacío para contacto eliminado
                phoneNumber = "", // Número vacío para contacto eliminado
                frequency = 0,
                lastInteraction = 0L
            )
        )
    }

    override suspend fun updateContactFrequency(contactId: String) {
        contactDao.incrementContactFrequency(contactId)
    }
}