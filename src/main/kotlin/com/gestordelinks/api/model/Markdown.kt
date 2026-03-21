package com.gestordelinks.api.model

// Importa todas as anotações JPA (Jakarta Persistence API) — @Entity, @Table, @Id, @Column, etc.
import jakarta.persistence.*
// Tipo do Java para representar data e hora — o Hibernate converte para TIMESTAMP no PostgreSQL
import java.time.LocalDateTime

// Marca esta classe como uma entidade JPA — cada instância representa uma linha na tabela do banco
@Entity
// Define o nome da tabela no banco. Sem isso, o JPA usaria o nome da classe ("Markdown") como nome da tabela
@Table(name = "markdowns")
// Construtor primário com todos os campos — o JPA/Hibernate precisa disso para criar instâncias
class Markdown(
    // Marca este campo como a chave primária da tabela
    @Id
    // Auto-incrementa o ID usando a estratégia nativa do PostgreSQL (SERIAL/IDENTITY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // val (imutável) porque o ID nunca deve ser alterado após criação. Valor padrão 0 para novos registros
    val id: Long = 0,

    // Configura a coluna: não aceita null e limita a 255 caracteres
    @Column(nullable = false, length = 255)
    // var (mutável) porque o nome pode ser atualizado via PUT
    var name: String,

    // Configura a coluna: não aceita null e limita a 500 caracteres
    @Column(nullable = false, length = 500)
    // var (mutável) porque a descrição pode ser atualizada via PUT
    var description: String,

    // columnDefinition = "TEXT" cria uma coluna do tipo TEXT no PostgreSQL (sem limite de tamanho)
    @Column(nullable = false, columnDefinition = "TEXT")
    // var (mutável) porque o conteúdo pode ser atualizado via PUT
    var content: String,

    // name = "created_at" define o nome da coluna no banco (snake_case, convenção SQL)
    // updatable = false impede que o Hibernate altere este campo em UPDATEs — data de criação nunca muda
    @Column(name = "created_at", nullable = false, updatable = false)
    // val (imutável) + valor padrão = momento da criação. LocalDateTime.now() captura a data/hora atual
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // Coluna de controle para saber quando o registro foi modificado pela última vez
    @Column(name = "updated_at", nullable = false)
    // var (mutável) porque é atualizado manualmente no Service a cada PUT
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
