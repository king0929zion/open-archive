package com.king0929zion.openarchive.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class ArchiveRepository(private val dao: ArchiveDao) {
    val entries: Flow<List<ArchiveEntry>> = combine(
        dao.observeEntries(),
        dao.observeImages(),
    ) { entries, images ->
        val byEntry = images.groupBy { it.entryId }
        entries.map { e ->
            ArchiveEntry(
                id = e.id,
                createdAt = e.createdAt,
                text = e.text,
                images = byEntry[e.id].orEmpty().sortedBy { it.sortOrder }.map { it.uri },
                location = e.location,
                weather = e.weather,
                mood = e.mood,
            )
        }
    }

    val comments: Flow<List<ArchiveComment>> = dao.observeComments().map { list ->
        list.map {
            ArchiveComment(
                id = it.id,
                entryId = it.entryId,
                parentId = it.parentId,
                authorName = it.authorName,
                avatarSeed = it.avatarSeed,
                text = it.text,
                createdAt = it.createdAt,
            )
        }
    }

    suspend fun createEntry(draft: DraftSnapshot): String {
        val id = System.currentTimeMillis().toString()
        val entry = EntryEntity(
            id = id,
            createdAt = System.currentTimeMillis(),
            text = draft.text.trim(),
            location = draft.location,
            weather = draft.weather,
            mood = draft.mood,
        )
        val images = draft.images.take(9).mapIndexed { index, uri ->
            EntryImageEntity(
                id = UUID.randomUUID().toString(),
                entryId = id,
                uri = uri,
                sortOrder = index,
            )
        }
        dao.insertEntryWithImages(entry, images)
        return id
    }

    suspend fun deleteEntry(id: String) = dao.deleteEntryById(id)

    suspend fun addComment(
        entryId: String,
        text: String,
        authorName: String,
        avatarSeed: String,
        parentId: String? = null,
    ) {
        dao.insertComment(
            CommentEntity(
                id = UUID.randomUUID().toString(),
                entryId = entryId,
                parentId = parentId,
                authorName = authorName,
                avatarSeed = avatarSeed,
                text = text.trim(),
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun seedDemoIfEmpty() {
        if (dao.entryCount() > 0) return
        val now = System.currentTimeMillis()
        val demo = listOf(
            EntryEntity(
                id = "demo-3",
                createdAt = now - 2 * 60 * 60 * 1000L,
                text = "周末的下午在街角咖啡馆。\n没什么特别的事，只是看着窗外发呆，感觉时间变得很慢。阳光刚好打在桌面的杯子上，形成好看的几何阴影。",
                location = "上海 · 武康路",
                weather = "sunny",
                mood = "calm",
            ),
            EntryEntity(
                id = "demo-2",
                createdAt = now - 2 * 24 * 60 * 60 * 1000L,
                text = "整理房间时翻出了一些旧物。这些不曾想起的片段，构成了现在的我。",
                location = "",
                weather = "overcast",
                mood = "low",
            ),
            EntryEntity(
                id = "demo-1",
                createdAt = now - 5 * 24 * 60 * 60 * 1000L,
                text = "决定开始记录。",
                location = "",
                weather = "",
                mood = "",
            ),
        )
        demo.forEach { dao.insertEntry(it) }
        dao.insertImages(
            listOf(
                EntryImageEntity("demo-img-cafe", "demo-3", "demo://cafe", 0),
                EntryImageEntity("demo-img-box", "demo-2", "demo://box", 0),
                EntryImageEntity("demo-img-book", "demo-2", "demo://book", 1),
                EntryImageEntity("demo-img-polaroid", "demo-2", "demo://polaroid", 2),
            )
        )
    }
}
