
package com.xhat.core.utils

object WalkieTalkieManager {
    private var isWalkieTalkieActive = false

    fun toggleWalkieTalkie(): Boolean {
        isWalkieTalkieActive = !isWalkieTalkieActive
        return isWalkieTalkieActive
    }

    fun isActive(): Boolean = isWalkieTalkieActive
}
