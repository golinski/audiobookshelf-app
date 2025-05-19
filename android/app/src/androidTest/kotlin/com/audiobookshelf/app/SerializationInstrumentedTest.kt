package com.audiobookshelf.app

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

interface Id {
  val id: String?
}


@Serializable
@JsonClass(generateAdapter = true)
data class LibraryItems(
  val libraryItems: List<LibraryItem> = listOf()
)


@Serializable
@JsonClass(generateAdapter = true)
data class Metadata(
  val title: String? = null,
  val titleIgnorePrefix: String? = null,
  val subtitle: String? = null,
  val authorName: String? = null,
  val authorNameLF: String? = null,
  val narratorName: String? = null,
  val seriesName: String? = null,
  val genres: List<String> = listOf(),
  val publishedYear: String? = null,
  val publishedDate: String? = null,
  val publisher: String? = null,
  val description: String? = null,
  val isbn: String? = null,
  val asin: String? = null,
  val language: String? = null,
  val explicit: Boolean? = null,
  val abridged: Boolean? = null

)


@Serializable
@JsonClass(generateAdapter = true)
data class Media(
  val id: String? = null,
  val metadata: Metadata? = Metadata(),
  val coverPath: String? = null,
  val tags: List<String> = listOf(),
  val numTracks: Int? = null,
  val numAudioFiles: Int? = null,
  val numChapters: Int? = null,
  val duration: Double? = null,
  val size: Long? = null
)


@Serializable
@JsonClass(generateAdapter = true)
data class LibraryItem(
  override val id: String? = null,
  val ino: String? = null,
  val oldLibraryItemId: String? = null,
  val libraryId: String? = null,
  val folderId: String? = null,
  val path: String? = null,
  val relPath: String? = null,
  val isFile: Boolean? = null,
  val mtimeMs: Long? = null,
  val ctimeMs: Long? = null,
  val birthtimeMs: Long? = null,
  val addedAt: Long? = null,
  val updatedAt: Long? = null,
  val isMissing: Boolean? = null,
  val isInvalid: Boolean? = null,
  val mediaType: String? = null,
  val media: Media? = Media(),
  val numFiles: Int? = null,
  val size: Long? = null,
  val progressLastUpdate: Long? = null

): Id


@Serializable
@JsonClass(generateAdapter = true)
data class SeriesItem(
  override val id: String? = null,
  val name: String? = null,
  val nameIgnorePrefix: String? = null,
  val description: String? = null,
  val addedAt: Long? = null,
  val updatedAt: Long? = null,
  val libraryId: String? = null,
  val books: List<LibraryItem> = listOf()
): Id

@Serializable
@JsonClass(generateAdapter = true)
data class AuthorsItem(
  override val id: String? = null,
  val asin: String? = null,
  val name: String? = null,
  val description: String? = null,
  val imagePath: String? = null,
  val libraryId: String? = null,
  val addedAt: Long? = null,
  val updatedAt: Long? = null,
  val numBooks: Int? = null
): Id


// Moshi
val polymorphicAdapterFactory = PolymorphicJsonAdapterFactory.of(LibraryShelf::class.java, "type")
  .withSubtype(LibraryShelfBooks::class.java, "book")
  .withSubtype(LibraryShelfSeries::class.java, "series")
  .withSubtype(LibraryShelfAuthors::class.java, "authors")

// Jackson
@JsonTypeInfo(
  use=JsonTypeInfo.Id.NAME,
  property = "type",
  include = JsonTypeInfo.As.PROPERTY,
  visible = true
)
@JsonSubTypes(
  JsonSubTypes.Type(LibraryShelfBooks::class, name = "book"),
  JsonSubTypes.Type(LibraryShelfSeries::class, name = "series"),
  JsonSubTypes.Type(LibraryShelfAuthors::class, name = "authors"),
)

// kotlinx.serialization
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")

sealed class LibraryShelf {
  abstract val id: String?
  abstract val label: String?
  abstract val labelStringKey: String?
  abstract val total: Int?
  abstract val type: String?
  abstract val entities: List<Id>
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is LibraryShelf) return false

    if (total != other.total) return false
    if (id != other.id) return false
    if (label != other.label) return false
    if (labelStringKey != other.labelStringKey) return false
    if (type != other.type) return false
    if (entities.size != other.entities.size) return false
    for ((our, their) in entities.zip(other.entities)) {
      if (our.id != their.id) return false
    }
    return true
  }
}


@Serializable
@SerialName("book")

@JsonClass(generateAdapter = true)
class LibraryShelfBooks(
  override val id: String?,
  override val label: String?,
  override val labelStringKey: String?,
  override val total: Int?,
  override val type: String?,
  override val entities: List<LibraryItem>,
) : LibraryShelf()


@Serializable
@SerialName("series")

@JsonClass(generateAdapter = true)
class LibraryShelfSeries(
  override val id: String?,
  override val label: String?,
  override val labelStringKey: String?,
  override val total: Int?,
  override val type: String?,
  override val entities: List<SeriesItem>,

  ): LibraryShelf()


@Serializable
@SerialName("authors")

@JsonClass(generateAdapter = true)
class LibraryShelfAuthors(
  override val id: String?,
  override val label: String?,
  override val labelStringKey: String?,
  override val total: Int?,
  override val type: String?,
  override val entities: List<AuthorsItem>,
): LibraryShelf()


val TAG = "serialization"

fun timeIt(s: String, f: () -> Unit) {
  val start = System.currentTimeMillis()
  f()
  Log.w(TAG, "$s ${System.currentTimeMillis() - start}")
}


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class SerializationInstrumentedTest {



  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun `1personalizedTest`() {
    val content1 = object {}.javaClass.getResourceAsStream("/personalized.json")
      ?.bufferedReader()
      ?.readLines()
      ?.joinToString(separator = "")!!
    val content2 = object {}.javaClass.getResourceAsStream("/personalized2.json")
      ?.bufferedReader()
      ?.readLines()
      ?.joinToString(separator = "")!!
    lateinit var x: List<LibraryShelf>
    lateinit var y: List<LibraryShelf>
    lateinit var z: List<LibraryShelf>

    val json = Json { ignoreUnknownKeys = true }
    timeIt("kotlinx.serialization") {
      x = json.decodeFromString<List<LibraryShelf>>(content1)
    }
    timeIt("kotlinx.serialization") {
      json.decodeFromString<List<LibraryShelf>>(content2)
    }


    val jom = jacksonObjectMapper()
    timeIt("Jackson") {
      y = jom.readValue<List<LibraryShelf>>(content1)
    }
    timeIt("Jackson") {
      jom.readValue<List<LibraryShelf>>(content2)
    }


    val moshi = Moshi.Builder().add(polymorphicAdapterFactory).build()
    val adapter = moshi.adapter<List<LibraryShelf>>()


    timeIt("Moshi") {
      z = adapter.fromJson(content1)!!
    }
    timeIt("Moshi") {
      adapter.fromJson(content2)
    }

    assertEquals(x, y)
    assertEquals(x, z)
    assertEquals(y, z)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun `2itemsInProgressTest`() {
    val content = object {}.javaClass.getResourceAsStream("/items-in-progress.json")
      ?.bufferedReader()
      ?.readLines()
      ?.joinToString(separator = "")!!

    lateinit var x: LibraryItems
    lateinit var y: LibraryItems
    lateinit var z: LibraryItems
    val json = Json { ignoreUnknownKeys = true }

    timeIt("kotlinx.serialization") {
      x = json.decodeFromString<LibraryItems>(content)
    }

    val jom = jacksonObjectMapper()
    timeIt("Jackson") {
      y = jom.readValue<LibraryItems>(content)
    }

    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter<LibraryItems>()
    timeIt("Moshi") {
      z = adapter.fromJson(content)!!
    }

    assertEquals(x, y)
    assertEquals(x, z)
    assertEquals(y, z)
  }

}


