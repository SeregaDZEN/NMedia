package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeCheck
import ru.netology.nmedia.dto.TimeType

class TimeSeparatorFactory(
    private val now: Long,
) {
    private companion object {
        const val ONE_DAY_IN_SECONDS = 24 * 60 * 60
    }

    private val twoDayInMillis = ONE_DAY_IN_SECONDS * 2

    fun create(before: Post?, after: Post?): TimeCheck? =
        when {
            before == null && after is Post -> {
                // Для первого элемента в списке проверяем, нужно ли вставить разделитель
                when {
                    now - after.published <= ONE_DAY_IN_SECONDS -> TimeCheck(TimeType.TODAY)

                    now - after.published <= twoDayInMillis -> TimeCheck(TimeType.YESTERDAY)

                    else -> TimeCheck(TimeType.WEEK_AGO)
                }
            }

            before is Post && after is Post -> {
                // Для элементов в середине списка вставляем разделитель, если дата изменилась
                when {
                    !before.isToday() && after.isToday() -> TimeCheck(TimeType.TODAY)
                    !before.isYesterday() && after.isYesterday() -> TimeCheck(TimeType.YESTERDAY)
                    !before.isWeekAgo() && after.isWeekAgo() -> TimeCheck(TimeType.WEEK_AGO)
                    else -> null
                }
            }

            else -> null
        }

    private fun Post.isToday(): Boolean = published in now..now - ONE_DAY_IN_SECONDS

    private fun Post.isYesterday(): Boolean =
        published in now - twoDayInMillis..now - ONE_DAY_IN_SECONDS

    private fun Post.isWeekAgo(): Boolean = published < now - twoDayInMillis
}