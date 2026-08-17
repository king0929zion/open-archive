package com.king0929zion.openarchive

import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveLabelsTest {
    @Test fun weatherLabelsStayCompatibleWithPrototype() {
        assertEquals("晴朗", ArchiveViewModel.weatherLabel("sunny"))
        assertEquals("多云", ArchiveViewModel.weatherLabel("cloudy"))
        assertEquals("阴天", ArchiveViewModel.weatherLabel("overcast"))
        assertEquals("小雨", ArchiveViewModel.weatherLabel("rain"))
        assertEquals("下雪", ArchiveViewModel.weatherLabel("snow"))
    }

    @Test fun moodSliderUsesFiveTwoCharacterLabels() {
        val labels = listOf("low", "calm", "cozy", "happy", "energy").map(ArchiveViewModel::moodLabel)
        assertEquals(listOf("低落", "平静", "悠闲", "开心", "活力"), labels)
        labels.forEach { assertEquals(2, it.length) }
    }
}
