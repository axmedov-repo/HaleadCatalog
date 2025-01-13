package com.halead.catalog.repository.main

interface MainRepository {
    fun getMaterials(): Map<String, Int>
}