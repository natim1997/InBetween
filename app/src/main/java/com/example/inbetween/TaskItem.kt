package com.example.inbetween

import java.io.Serializable
import java.time.LocalDate

data class TaskItem(
    val title             : String,
    val date              : LocalDate,
    val startTime         : String,
    val endTime           : String,
    val note              : String?      = null,
    var isDaily           : Boolean      = false,
    val isWeekly          : Boolean      = false,
    val recurrenceEndDate : LocalDate?   = null,
    val excludedDates: MutableList<LocalDate> = mutableListOf(),
    val isPermanent: Boolean = false
) : Serializable
