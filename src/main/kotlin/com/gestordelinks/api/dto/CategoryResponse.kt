package com.gestordelinks.api.dto

import com.gestordelinks.api.model.Category

data class CategoryResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(category: Category) = CategoryResponse(
            id = category.id,
            name = category.name
        )
    }
}