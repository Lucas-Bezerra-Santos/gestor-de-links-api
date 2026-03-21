package com.gestordelinks.api.repository

// Importa a entidade Markdown — o tipo que este repository gerencia
import com.gestordelinks.api.model.Markdown
// JpaRepository é a interface do Spring Data que fornece operações CRUD prontas
import org.springframework.data.jpa.repository.JpaRepository

// Interface — não precisa de implementação! O Spring detecta automaticamente via Component Scan,
// cria uma implementação em runtime e a disponibiliza para injeção de dependência.
// É como se o Spring gerasse automaticamente um service de acesso a dados baseado apenas na tipagem.
//
// JpaRepository<Markdown, Long>:
//   - Markdown = a entidade que este repository gerencia
//   - Long = o tipo do campo @Id da entidade (chave primária)
//
// Métodos herdados gratuitamente (sem escrever nenhuma linha de código):
//   - findAll()        → busca todos os registros da tabela markdowns
//   - findById(id)     → busca por ID (retorna Optional<Markdown>)
//   - save(entity)     → insere um novo registro ou atualiza um existente
//   - deleteById(id)   → deleta um registro pelo ID
//   - existsById(id)   → verifica se um registro existe (retorna Boolean)
//   - count()          → conta o total de registros
//   - Além de paginação e ordenação via PagingAndSortingRepository
interface MarkdownRepository : JpaRepository<Markdown, Long>
