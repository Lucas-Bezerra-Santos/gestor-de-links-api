package com.gestordelinks.api.exception

// Enum com os códigos de status HTTP (200, 400, 404, 500, etc.)
import org.springframework.http.HttpStatus
// Wrapper para construir respostas HTTP com status, headers e body
import org.springframework.http.ResponseEntity
// Exceção lançada automaticamente pelo Spring quando a validação @Valid falha no Controller.
// Contém os detalhes de quais campos falharam e as mensagens de erro.
import org.springframework.web.bind.MethodArgumentNotValidException
// Anotação que marca um método como handler de um tipo específico de exceção
import org.springframework.web.bind.annotation.ExceptionHandler
// Anotação que marca a classe como interceptador global de exceções de TODOS os controllers
import org.springframework.web.bind.annotation.RestControllerAdvice

// data class para padronizar o formato de erro retornado pela API.
// Toda resposta de erro terá este formato JSON:
// {
//   "status": 404,
//   "message": "Markdown com id 99 não encontrado",
//   "errors": []
// }
//
// Em Express seria o objeto que você passa no res.json():
//   res.status(404).json({ status: 404, message: "...", errors: [] })
//
// "emptyList()" é o valor padrão — lista vazia quando não há erros de validação detalhados.
// Em JS seria: errors = [] (valor padrão no destructuring ou parâmetro)
data class ApiError(
    val status: Int,
    val message: String,
    val errors: List<String> = emptyList()
)

// @RestControllerAdvice = interceptador global de exceções para TODOS os controllers.
// Captura exceções lançadas em qualquer controller e converte em respostas HTTP padronizadas.
//
// Em Express seria o middleware de erro global (o último middleware da cadeia):
//   app.use((err, req, res, next) => {
//     if (err instanceof NotFoundError) res.status(404).json({ message: err.message })
//     if (err instanceof ValidationError) res.status(400).json({ message: "Erro de validação" })
//   })
//
// Sem isso, exceções não tratadas retornariam um stack trace genérico com status 500 —
// expondo detalhes internos da aplicação ao cliente (inseguro e pouco amigável).
@RestControllerAdvice
class ApiExceptionHandler {

    // --- Handler para NoSuchElementException → 404 Not Found ---
    //
    // @ExceptionHandler(NoSuchElementException::class) = "quando qualquer controller lançar
    // NoSuchElementException, execute este método em vez de deixar o erro estourar"
    //
    // "::class" é a sintaxe do Kotlin para referenciar o tipo da classe (equivale a .class no Java).
    // Em JS seria: instanceof NoSuchElementException (mas aqui é declarativo, não um if/else)
    //
    // O parâmetro "ex" recebe a exceção lançada — o Spring injeta automaticamente.
    //
    // "ex.message ?: 'Recurso não encontrado'" usa o operador Elvis (?:) do Kotlin.
    // Se ex.message for null, usa o valor à direita como fallback.
    // Em JS seria: ex.message ?? "Recurso não encontrado" (nullish coalescing)
    // Ou: ex.message || "Recurso não encontrado"
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError(status = 404, message = ex.message ?: "Recurso não encontrado")
        )

    // --- Handler para MethodArgumentNotValidException → 400 Bad Request ---
    //
    // Esta exceção é lançada AUTOMATICAMENTE pelo Spring quando @Valid falha no Controller.
    // Por exemplo, se o cliente enviar um POST com { "name": "" }, o @NotBlank falha
    // e o Spring lança MethodArgumentNotValidException antes mesmo de chegar no Service.
    //
    // Em Express com Zod seria como:
    //   const result = schema.safeParse(req.body)
    //   if (!result.success) res.status(400).json({ errors: result.error.issues })
    // Mas aqui o Spring faz esse check automaticamente — você só trata o erro.
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        // ex.bindingResult.fieldErrors = lista de erros de validação por campo.
        // Cada fieldError tem: field (nome do campo) e defaultMessage (mensagem definida na anotação).
        //
        // .map { "${it.field} : ${it.defaultMessage}" } = transforma cada erro em uma string legível.
        // "it" é o parâmetro implícito da lambda (como vimos no Service).
        // "${...}" é string template do Kotlin (equivale a `${...}` no JS template literal).
        //
        // Resultado exemplo: ["name : Nome é obrigatório", "content : Conteúdo é obrigatório"]
        // Em JS seria: errors.map(err => `${err.field} : ${err.message}`)
        val errors = ex.bindingResult.fieldErrors.map { "${it.field} : ${it.defaultMessage}" }
        // Retorna 400 com a lista de erros de validação no campo "errors" do ApiError
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(status = 400, message = "Erro de validação", errors = errors)
        )
    }
}
