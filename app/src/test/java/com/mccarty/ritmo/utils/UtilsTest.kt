package com.mccarty.ritmo.utils

import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.domain.mainItem
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class UtilsTest {

    private val fakeAlbum = "Good Music"
    private val fakeTrackName = "trackName"

    @Test
    fun `assert size is 1 if initial list is empty`() {
        val mainItems = listOf(mainItem)
        val createdDetails = emptyList<Details>().createListFromDetails(mainItems)
        assertEquals(1, createdDetails.size)
    }

    @Test
    fun `assert size is 0 if both lists are empty`() {
        val createdDetails = emptyList<Details>().createListFromDetails(emptyList())
        assertEquals(0, createdDetails.size)
    }

    @Test
    fun `assert album name if details not empty`() {
        val mainItems = listOf(mainItem)
        val createdDetails = details.createListFromDetails(mainItems)
        assertEquals(fakeAlbum, createdDetails[0].albumName)
    }

    @Test
    fun `assert track name when initial list is empty and mainItems is not`() {
        val mainItems = listOf(mainItem)
        val createdDetails = emptyList<Details>().createListFromDetails(mainItems)
        val track = createdDetails[0].trackName
        assertEquals(fakeTrackName, track)
    }

    private val details = listOf(
        Details(
            albumName = fakeAlbum,
            trackName = "Good Track",
            explicit = false,
            artists = emptyList(),
            images = emptyList(),
            trackId = "fake-id",
            uri = "fake-uri",
            type = "Track",
        ),
        Details(
            albumName = "Another One",
            trackName = "Just as Good",
            explicit = true,
            artists = emptyList(),
            images = emptyList(),
            trackId = "fake-id-2",
            uri = "fake-uri-2",
            type = "Track",
        )
    )
}