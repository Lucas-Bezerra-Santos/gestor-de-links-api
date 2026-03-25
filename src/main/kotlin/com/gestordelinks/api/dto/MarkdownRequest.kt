package com.gestordelinks.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MarkdownRequest(
    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    val name: String,

    @field:NotBlank(message = "Descrição é obrigatória")
    @field:Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    val description: String,

    @field:NotBlank(message = "Conteúdo é obrigatório")
    val content: String,

    val categoryId: Long? = null
)
