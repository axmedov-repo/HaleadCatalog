package com.halead.catalog.repository

interface MainRepository {
    fun getMaterials(): Map<String, Int>
}