/*
 * Updated: 2025-01-25 01:48:55
 * Author: William8677
 */

package com.williamfq.domain.models

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val frequency: Int = 0,
    val lastInteraction: Long = System.currentTimeMillis()
)