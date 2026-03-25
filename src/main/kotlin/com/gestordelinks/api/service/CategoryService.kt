package com.gestordelinks.api.service

import com.gestordelinks.api.dto.CategoryResponse
import com.gestordelinks.api.repository.CategoryRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    fun findAll(): List<CategoryResponse> =
        categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
            .map { CategoryResponse.from(it) }
}