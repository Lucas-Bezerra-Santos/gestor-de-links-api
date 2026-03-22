package com.gestordelinks.api.service

// Importa o DTO de entrada (dados que o cliente envia)
import com.gestordelinks.api.dto.MarkdownRequest
// Importa o DTO de saída (dados que a API retorna)
import com.gestordelinks.api.dto.MarkdownResponse
// Importa a entidade JPA (representação da tabela no banco)
import com.gestordelinks.api.model.Markdown
// Importa o repository (acesso ao banco de dados)
import com.gestordelinks.api.repository.MarkdownRepository
// Anotação do Spring que marca esta classe como um bean de serviço
import org.springframework.stereotype.Service
// Tipo do Java para data e hora
import java.time.LocalDateTime

// @Service marca esta classe como um bean gerenciado pelo Spring.
// O container Spring cria UMA instância (singleton) e a injeta onde for necessário.
// É como registrar um service no Angular: @Injectable({ providedIn: 'root' })
//
// Arquitetura em camadas: Controller → Service → Repository → Database
// O Service contém a lógica de negócio. Comparando com frontend:
//   Controller = componente React (lida com HTTP/view)
//   Service = custom hook/store (lógica)
//   Repository = API client (acesso a dados)
@Service
// Constructor injection — o Spring injeta automaticamente o MarkdownRepository no construtor.
// É o padrão recomendado (vs @Autowired em campos) — mais testável e explícito.
// Em JS seria como receber uma dependência no construtor:
//   class MarkdownService {
//     constructor(private markdownRepository) {}
//   }
// "private val" = a propriedade é privada (só acessível dentro da classe) e imutável
class MarkdownService(
    private val markdownRepository: MarkdownRepository
) {

    // --- LISTAR TODOS ---
    // Retorna List<MarkdownResponse> — uma lista tipada (em JS seria MarkdownResponse[])
    //
    // markdownRepository.findAll() → busca todos os registros do banco (herdado do JpaRepository)
    // .map { MarkdownResponse.from(it) } → converte cada Entity em DTO
    //
    // O "it" é o parâmetro implícito de lambdas com um argumento no Kotlin.
    // Em JS seria: .map((markdown) => MarkdownResponse.from(markdown))
    // No Kotlin, quando a lambda tem só 1 parâmetro, pode usar "it" em vez de nomeá-lo.
    //
    // O "=" é a forma curta de função (single expression):
    //   fun findAll(): List<MarkdownResponse> {
    //       return markdownRepository.findAll().map { MarkdownResponse.from(it) }
    //   }
    fun findAll(): List<MarkdownResponse> = markdownRepository.findAll().map { MarkdownResponse.from(it) }
    // --- BUSCAR POR ID ---
    // Recebe o id (Long) e retorna um MarkdownResponse
    fun findById(id: Long): MarkdownResponse {
        // findById(id) retorna Optional<Markdown> — um wrapper que pode conter o valor ou estar vazio.
        // Optional é o jeito do Java de lidar com nulidade. Em JS não existe — usaríamos null/undefined.
        //
        // .orElseThrow { ... } → se o Optional está vazio (registro não existe), lança a exceção.
        // É como: const markdown = await db.findById(id) ?? throw new Error("não encontrado")
        //
        // NoSuchElementException será capturada pelo @RestControllerAdvice (handler global de erros)
        // e convertida em uma resposta HTTP 404.
        //
        // "$id" é string template do Kotlin — equivale a `${id}` no JS template literal
        val markdown = markdownRepository.findById(id).orElseThrow { NoSuchElementException("Markdown com id $id nao encontrado") }
        // Converte a entidade do banco para o DTO de resposta usando o factory method estático
        return MarkdownResponse.from(markdown)
    }

    // --- CRIAR ---
    // Recebe MarkdownRequest (já validado pelo @Valid no Controller) e retorna MarkdownResponse
    fun create(request: MarkdownRequest): MarkdownResponse {
        // Cria uma nova instância da entidade Markdown usando named arguments.
        // Em JS seria: new Markdown({ name: request.name, description: request.description, ... })
        // Os campos id, createdAt e updatedAt usam seus valores padrão (definidos na entidade)
        val markdown = Markdown(
            name = request.name,
            description = request.description,
            content = request.content
        )

        // markdownRepository.save(markdown):
        //   - Como o id é 0 (novo registro), o Hibernate faz INSERT no banco
        //   - Retorna a entidade com o id gerado pelo PostgreSQL (IDENTITY)
        // MarkdownResponse.from(...) converte o resultado em DTO para retornar ao cliente
        return MarkdownResponse.from(markdownRepository.save(markdown))
    }

    // --- ATUALIZAR ---
    // Recebe o id do registro e os novos dados via MarkdownRequest
    fun update(id: Long, request: MarkdownRequest): MarkdownResponse {
        // Primeiro busca o registro existente — se não existir, lança 404
        val markdown = markdownRepository.findById(id)
            .orElseThrow { NoSuchElementException("Markdown com id $id não encontrado") }

        // Atualiza os campos mutáveis (var) da entidade.
        // Como a entidade veio do banco via findById, o Hibernate está "rastreando" ela (managed state).
        // Qualquer alteração nos campos será persistida quando chamar save().
        // Em JS seria: Object.assign(markdown, { name: request.name, ... })
        markdown.name = request.name
        markdown.description = request.description
        markdown.content = request.content
        // Atualiza manualmente o timestamp — marca quando o registro foi modificado
        markdown.updatedAt = LocalDateTime.now()

        // markdownRepository.save(markdown):
        //   - Como o id já existe (não é 0), o Hibernate faz UPDATE no banco (não INSERT)
        //   - O Hibernate diferencia INSERT vs UPDATE pelo estado do id
        return MarkdownResponse.from(markdownRepository.save(markdown))
    }

    // --- DELETAR ---
    // Retorna Unit (equivale a void no JS/Java — função que não retorna nada)
    fun delete(id: Long) {
        // Verifica se existe antes de deletar.
        // existsById() retorna Boolean — executa um SELECT COUNT rápido.
        // O "!" é negação, igual ao JS: if (!exists)
        if (!markdownRepository.existsById(id)) {
            // Se não existe, lança exceção → o handler global converte em 404
            throw NoSuchElementException("Markdown com id $id não encontrado")
        }
        // deleteById() executa DELETE FROM markdowns WHERE id = ?
        markdownRepository.deleteById(id)
    }
}
