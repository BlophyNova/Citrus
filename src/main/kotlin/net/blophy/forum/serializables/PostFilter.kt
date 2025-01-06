package net.blophy.forum.serializables

import kotlinx.serialization.Serializable
import net.blophy.forum.enums.Partitions
import net.blophy.forum.services.PostsService


enum class PostFilterDependsOn(val id: Int) {
    DATE(0),
    POPULAR(1);

    companion object {
        private val map = PostFilterDependsOn.entries.associateBy(PostFilterDependsOn::id)
        fun fromId(typeId: Int) = map[typeId] ?: POPULAR
    }
}

data class PostFilter(
    val sortByDescending: Boolean = false,
    val userId: Int? = null,
    val dependsOn: PostFilterDependsOn = PostFilterDependsOn.POPULAR,
    val hasContent: String? = null,
    val partitions: List<Partitions>? = null,
)

@Serializable
data class SearchFilter(
    val sortByDescending: Boolean = false,
    val userId: Int? = null,
    val dependsOn: Int = 1,
    val content: String? = null,
    val partitions: List<Partitions>? = null
)

fun SearchFilter.toPostFilter() = PostFilter(
    sortByDescending = sortByDescending,
    userId = userId,
    dependsOn = PostFilterDependsOn.fromId(dependsOn),
    hasContent = content,
    partitions = partitions
)

suspend fun PostFilter.run() = PostsService.getFilteredPosts(this)

suspend fun SearchFilter.getFilteredPosts() = this.toPostFilter().run()
