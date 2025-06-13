package com.example.inbetween

import java.io.Serializable
import java.time.LocalDate

data class TaskItem(
    val title             : String,
    val date              : LocalDate,
    val startTime         : String,
    val endTime           : String,
    val note              : String?      = null,
    val bring             : String?      = null,
    val remindBeforeMinutes: Int?        = null,
    val travelMinutes     : Int?         = null,
    val isWeekly          : Boolean      = false,
    val recurrenceEndDate : LocalDate?   = null
) : Serializable
