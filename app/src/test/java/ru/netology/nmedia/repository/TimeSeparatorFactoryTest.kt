package ru.netology.nmedia.repository

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeCheck
import ru.netology.nmedia.dto.TimeType
import java.time.LocalDate
import java.time.ZoneOffset

class TimeSeparatorFactoryTest {

    private val now = LocalDate.parse("1994-02-27")
        .atTime(0, 0)
        .atOffset(ZoneOffset.UTC)
    private val factory = TimeSeparatorFactory(now.toInstant().epochSecond)
    private val todayPost = createPost(now.toInstant().epochSecond)
    private val yesterdayPost = createPost(now.minusDays(2).toInstant().epochSecond)
    private val weekAgoPost = createPost(now.minusWeeks(1).toInstant().epochSecond)
    private val twoWeeksAgoPost = createPost(now.minusWeeks(2).toInstant().epochSecond)

    @Test
    fun `when previous post is null and next post is today then TODAY`() {
        val expected = TimeCheck(TimeType.TODAY)

        val actual = factory.create(null, todayPost)

        assertEquals(expected, actual)
    }

    @Test
    fun `when previous post is null and next post is yesterday then YESTERDAY`() {
        val expected = TimeCheck(TimeType.YESTERDAY)

        val actual = factory.create(null, yesterdayPost)

        assertEquals(expected, actual)
    }

    @Test
    fun `when previous post is null and next post is week ago then WEEK_AGO`() {
        val expected = TimeCheck(TimeType.WEEK_AGO)

        val actual = factory.create(null, weekAgoPost)

        assertEquals(expected, actual)
    }

    @Test
    fun `when previous post is today and next post is yesterday then YESTERDAY`() {
        val expected = TimeCheck(TimeType.YESTERDAY)

        val actual = factory.create(todayPost, yesterdayPost)

        assertEquals(expected, actual)
    }

    @Test
    fun `when previous post is yesterday and next post is week ago then WEEK_AGO`() {
        val expected = TimeCheck(TimeType.WEEK_AGO)

        val actual = factory.create(yesterdayPost, weekAgoPost)

        assertEquals(expected, actual)
    }

    @Test
    fun `when previous post is yesterday and next post is yesterday then null`() {
        val actual = factory.create(yesterdayPost, yesterdayPost)

        assertNull(actual)
    }

    @Test
    fun `when previous post is week ago and next post is week ago then null`() {
        val actual = factory.create(weekAgoPost, weekAgoPost)

        assertNull(actual)
    }

    @Test
    fun `when previous post is week ago and next post is two weeks ago then null`() {
        val actual = factory.create(weekAgoPost, twoWeeksAgoPost)

        assertNull(actual)
    }

    private fun createPost(published: Long): Post = Post(
        id = 0,
        authorId = 0,
        content = "",
        author = "",
        published = published,
        likedByMe = false,
        hide = false,
        likes = 0,
    )
}