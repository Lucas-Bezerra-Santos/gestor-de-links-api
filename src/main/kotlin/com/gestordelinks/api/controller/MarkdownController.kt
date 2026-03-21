package com.gestordelinks.api.controller

// Importa o DTO de entrada (dados que o cliente envia no body)
import com.gestordelinks.api.dto.MarkdownRequest
// Importa o DTO de saída (dados que a API retorna ao cliente)
import com.gestordelinks.api.dto.MarkdownResponse
// Importa o Service (camada de lógica de negócio)
import com.gestordelinks.api.service.MarkdownService
// Anotação de validação — aciona o Bean Validation nos campos do DTO
import jakarta.validation.Valid
// Enum com os códigos de status HTTP (200, 201, 204, 404, etc.)
import org.springframework.http.HttpStatus
// Wrapper para construir respostas HTTP com status, headers e body
import org.springframework.http.ResponseEntity
// Importa todas as anotações REST do Spring Web de uma vez (*)
// Inclui: @RestController, @RequestMapping, @GetMapping, @PostMapping, @PutMapping,
//         @DeleteMapping, @PathVariable, @RequestBody
import org.springframework.web.bind.annotation.*

// @RestController = @Controller + @ResponseBody
// - @Controller: marca como bean Spring que recebe requisições HTTP (como um router no Express)
// - @ResponseBody: todo retorno é serializado automaticamente para JSON (via Jackson)
// Em Express.js seria: const router = express.Router() + res.json() automático em toda rota
//
// No Next.js API Routes seria como: export default function handler(req, res) { res.json(...) }
// Mas aqui o Spring faz o res.json() automaticamente — você só retorna o objeto.
@RestController
// @RequestMapping define o prefixo de URL para TODOS os endpoints desta classe.
// Todas as rotas começam com /api/markdowns
// Em Express seria: router.use('/api/markdowns', ...)
@RequestMapping("/api/markdowns")
// Constructor injection — o Spring injeta automaticamente o MarkdownService.
// É o mesmo padrão usado no Service com o Repository.
// Em JS seria: class MarkdownController { constructor(private markdownService) {} }
class MarkdownController(
    private val markdownService: MarkdownService
) {

    // --- GET /api/markdowns → Listar todos ---
    // @GetMapping sem parâmetro = responde a GET na rota base (/api/markdowns)
    // Em Express seria: router.get('/', (req, res) => ...)
    //
    // ResponseEntity<List<MarkdownResponse>> = resposta HTTP contendo uma lista de MarkdownResponse
    // Em JS o tipo seria: Response<MarkdownResponse[]>
    //
    // ResponseEntity.ok(...) = cria resposta com status 200 e o body passado como argumento
    // Em Express seria: res.status(200).json(markdowns)
    @GetMapping
    fun findAll(): ResponseEntity<List<MarkdownResponse>> =
        ResponseEntity.ok(markdownService.findAll())

    // --- GET /api/markdowns/{id} → Buscar por ID ---
    // @GetMapping("/{id}") = rota com parâmetro dinâmico na URL
    // Em Express seria: router.get('/:id', (req, res) => ...)
    //
    // @PathVariable extrai o {id} da URL e injeta no parâmetro da função.
    // É o equivalente ao req.params.id no Express.
    // O Spring converte automaticamente a string da URL para Long (tipagem forte).
    // Em JS você faria: const id = parseInt(req.params.id)
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<MarkdownResponse> =
        ResponseEntity.ok(markdownService.findById(id))

    // --- POST /api/markdowns → Criar novo ---
    // @PostMapping = responde a POST na rota base
    // Em Express seria: router.post('/', (req, res) => ...)
    //
    // @Valid aciona a validação do Bean Validation definida no MarkdownRequest.
    // Valida @NotBlank, @Size, etc. Se falhar, lança MethodArgumentNotValidException
    // que o @RestControllerAdvice captura e converte em resposta 400.
    // Em Express você usaria um middleware de validação (Joi, Zod, express-validator).
    //
    // @RequestBody deserializa o body JSON da requisição para o objeto MarkdownRequest.
    // O Jackson (biblioteca de serialização) faz isso automaticamente.
    // Em Express seria: const request = req.body (com express.json() middleware)
    //
    // ResponseEntity.status(HttpStatus.CREATED).body(...) = resposta com status 201 + body
    // Em Express seria: res.status(201).json(created)
    @PostMapping
    fun create(@Valid @RequestBody request: MarkdownRequest): ResponseEntity<MarkdownResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(markdownService.create(request))

    // --- PUT /api/markdowns/{id} → Atualizar existente ---
    // @PutMapping("/{id}") = responde a PUT com parâmetro dinâmico
    // Em Express seria: router.put('/:id', (req, res) => ...)
    //
    // Combina @PathVariable (id da URL) + @Valid @RequestBody (body validado)
    // É como ter acesso ao req.params.id E req.body ao mesmo tempo no Express
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: MarkdownRequest
    ): ResponseEntity<MarkdownResponse> =
        ResponseEntity.ok(markdownService.update(id, request))

    // --- DELETE /api/markdowns/{id} → Deletar ---
    // @DeleteMapping("/{id}") = responde a DELETE com parâmetro dinâmico
    // Em Express seria: router.delete('/:id', (req, res) => ...)
    //
    // ResponseEntity<Void> = resposta sem body (Void = nenhum conteúdo)
    // Em JS, Void não existe — seria Response<void> no TypeScript
    //
    // Esta função usa a forma longa (com chaves { }) em vez da forma curta (=)
    // porque tem duas instruções: chamar delete + retornar response.
    //
    // ResponseEntity.noContent().build() = resposta com status 204 (No Content) sem body
    // Em Express seria: res.status(204).send()
    // 204 é o status padrão para DELETE bem-sucedido — "deu certo, mas não tenho nada para retornar"
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        markdownService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
