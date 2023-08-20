package com.example.phl.data

import java.time.LocalDateTime
import java.time.ZonedDateTime

interface AbstractData {
    val sessionId: String
    val time: LocalDateTime // TODO: Change to ZonedDateTime
}