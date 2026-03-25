package com.gestordelinks.api.controller

import com.gestordelinks.api.dto.CategoryResponse
import com.gestordelinks.api.service.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
class CategoryController (
    private val categoryService: CategoryService
){

    @GetMapping
    fun findAll(): ResponseEntity<List<CategoryResponse>> = ResponseEntity.ok((categoryService.findAll()))
}