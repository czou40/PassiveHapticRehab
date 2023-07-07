package com.example.phl.data

import java.time.LocalDateTime

interface AbstractData {
    val sessionId: String
    val time: LocalDateTime
}