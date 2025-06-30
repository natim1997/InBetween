package com.example.inbetween

import java.io.Serializable
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

data class GoalItem(
    val id: Int,
    val title: String,
    val sessionsPerWeek: Int,
    val durationMin: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
) : Serializable {
    companion object {
        private val counter = AtomicInteger(0)
        fun nextId(): Int = counter.incrementAndGet()
    }
}
