package com.williamfq.data.manager

import com.williamfq.domain.model.User

interface UserManager {
    fun getCurrentUserId(): String
    fun getCurrentUser(): User
}
