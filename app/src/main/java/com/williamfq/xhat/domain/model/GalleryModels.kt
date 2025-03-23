/*
 * Updated: 2025-01-21 20:39:04
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

import android.net.Uri
import java.util.Date

data class GalleryImage(
    val id: String,
    val uri: Uri,
    val name: String,
    val path: String,
    val size: Long,
    val dateCreated: Date,
    val dateModified: Date,
    val mimeType: String,
    val width: Int,
    val height: Int
)

data class GalleryFolder(
    val id: String,
    val name: String,
    val path: String,
    val coverImage: Uri?,
    val imageCount: Int
)

enum class GallerySortOrder {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}