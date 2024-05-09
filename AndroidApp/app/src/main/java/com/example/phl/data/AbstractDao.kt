package com.example.phl.data

import java.time.LocalDate

interface AbstractDao<T> {
    fun insert(data: T)
    fun getAll(): List<T>
    fun getById(id: String): T
    fun getBySessionId(sessionId: String): List<T>
    fun getByDay(day: LocalDate): List<T>
    fun deleteById(id: String)
    fun deleteBySessionId(sessionId: String)
}