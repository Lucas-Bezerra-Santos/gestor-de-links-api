package com.gestordelinks.api.repository

import com.gestordelinks.api.model.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>
