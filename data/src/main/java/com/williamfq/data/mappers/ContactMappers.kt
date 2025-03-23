/*
 * Updated: 2025-01-25 01:48:55
 * Author: William8677
 */

package com.williamfq.data.mappers

import com.williamfq.data.entities.ContactEntity
import com.williamfq.domain.models.Contact

fun ContactEntity.toDomain(): Contact {
    return Contact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        frequency = frequency,
        lastInteraction = lastInteraction
    )
}

fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        frequency = frequency,
        lastInteraction = lastInteraction
    )
}