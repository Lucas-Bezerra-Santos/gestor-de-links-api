package com.gestordelinks.api.dto

// Importa a entidade Markdown para poder converter Entity → DTO
import com.gestordelinks.api.model.Category
import com.gestordelinks.api.model.Markdown
// Tipo do Java para representar data e hora
import java.time.LocalDateTime

// DTO de saída — representa o "contrato" da API, o formato JSON que o cliente recebe.
// Separar do Entity evita expor detalhes internos do banco (ex: campos sensíveis, relações JPA).
//
// data class gera automaticamente: toString(), equals(), hashCode(), copy()
// Todos os campos são val (imutáveis) — depois de criado, o response não muda.
//
// Os parâmetros no construtor (...) são ao mesmo tempo o construtor E as propriedades.
// Em JS seria:
//   constructor(id, name, ...) {
//     this.id = id
//     this.name = name
//   }
data class MarkdownResponse(
    val id: Long,
    val category: CategoryResponse?,
    val name: String,
    val description: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    // companion object = bloco de métodos estáticos (static em JS/Java).
    //
    // --- O que é um método estático? (POO) ---
    // Um método estático pertence à CLASSE, não à instância.
    // Isso significa que você chama direto na classe: MarkdownResponse.from(markdown)
    // Sem precisar criar um objeto antes: NÃO precisa de "val response = MarkdownResponse(...)" para chamar .from()
    //
    // Em JavaScript seria:
    //   class MarkdownResponse {
    //     static from(markdown) { return new MarkdownResponse(...) }
    //   }
    //   MarkdownResponse.from(markdown) // chamada direto na classe
    //
    // --- Quando usar métodos estáticos? ---
    // 1. Factory methods — criar instâncias a partir de outros objetos (exatamente o caso aqui: Entity → DTO)
    // 2. Utilitários — funções que não dependem do estado de uma instância (ex: Math.random(), Date.now())
    // 3. Constantes — valores fixos compartilhados por todas as instâncias
    //
    // --- Por que companion object e não só "static"? ---
    // Kotlin não tem a keyword "static". Em vez disso, agrupa todos os métodos estáticos
    // dentro de um bloco companion object { }. Funciona igual, só a sintaxe que muda.
    companion object {
        // Factory method que converte uma entidade Markdown (vinda do banco) em MarkdownResponse (enviado ao cliente).
        //
        // "fun from(markdown: Markdown) = ..." é a forma curta de:
        //   fun from(markdown: Markdown): MarkdownResponse {
        //       return MarkdownResponse(...)
        //   }
        // Quando a função tem só uma expressão, o "=" substitui "{ return ... }"
        //
        // Uso no Service:
        //   val markdown = markdownRepository.findById(id)  // busca do banco (Entity)
        //   return MarkdownResponse.from(markdown)           // converte para DTO
        //
        // Named arguments (id = markdown.id) deixam explícito qual valor vai para qual campo.
        // Em JS seria por posição: new MarkdownResponse(markdown.id, markdown.name, ...)
        fun from(markdown: Markdown) = MarkdownResponse(
            id = markdown.id,
            category = markdown.category?.let { CategoryResponse.from(it) },
            name = markdown.name,
            description = markdown.description,
            content = markdown.content,
            createdAt = markdown.createdAt,
            updatedAt = markdown.updatedAt
        )
    }
}
