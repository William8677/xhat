/*
 * Updated: 2025-01-25 23:10:33
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.domain.model

import com.williamfq.domain.model.CommunityRule
import com.williamfq.domain.model.CommunitySettings

// Se define ModerationAction como sealed class para poder adjuntar datos a cada acción
sealed class ModerationAction {
    data class UpdateRules(val updatedRules: List<CommunityRule>) : ModerationAction()
    data class UpdateSettings(val newSettings: CommunitySettings) : ModerationAction()
    data class ManageUser(val userId: String, val action: UserManagementAction, val duration: Long?) : ModerationAction()
    data class HandleReport(val reportId: String, val action: ReportAction) : ModerationAction()
}

enum class UserManagementAction {
    Ban,
    Mute,
    Promote
}

enum class ReportAction {
    Approve,
    Reject
}
