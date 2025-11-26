package com.lynk.mediaservice.domain.model

data class MediaFile(
    val fileName: String,
    val content: ByteArray? = null,
    val contentType: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaFile

        if (fileName != other.fileName) return false
        if (content != null) {
            if (other.content == null) return false
            if (!content.contentEquals(other.content)) return false
        } else if (other.content != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + (content?.contentHashCode() ?: 0)
        return result
    }
}