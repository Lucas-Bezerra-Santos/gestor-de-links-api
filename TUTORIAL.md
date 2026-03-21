# Plano Tutorial: API Gestor de Markdowns

## Contexto

Você é um desenvolvedor senior frontend em transição para full-stack. Este projeto é uma API REST em **Kotlin + Spring Boot + PostgreSQL + Maven** para CRUD de markdowns. O objetivo principal é **aprender a stack backend** construindo do zero, entendendo cada camada e conceito.

---

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:
- **JDK 21** — `java -version` (recomendo instalar via `brew install openjdk@21`)
- **Maven** — `mvn -version` (instalar via `brew install maven`)
- **PostgreSQL** — `psql --version` (instalar via `brew install postgresql@16` ou usar Docker)
Comando util para setar a senha do bd: docker run --name postgres -e POSTGRES_PASSWORD=28031998 -p 5432:5432 -d     postgres                                                                      
- **Docker (opcional)** — para rodar o Postgres em container, evitando instalação local
- **IntelliJ IDEA** ou **VS Code com extensão Kotlin** — IDE para desenvolvimento

---

## Etapa 1: Criação do Projeto com Spring Initializr

### O que você vai aprender
- Como o Spring Boot estrutura um projeto
- O que é o `pom.xml` e como o Maven gerencia dependências
- A estrutura de pastas padrão de um projeto Kotlin/Spring

### Passo a passo

1. Acesse [start.spring.io](https://start.spring.io) e configure: (Feito)
   - **Project:** Maven
   - **Language:** Kotlin
   - **Spring Boot:** 3.4.x (versão estável mais recente)
   - **Group:** `com.gestordelinks`
   - **Artifact:** `api`
   - **Name:** `api`
   - **Package name:** `com.gestordelinks.api`
   - **Packaging:** Jar
   - **Java:** 21
   - **Dependencies a adicionar:**
     - `Spring Web` — fornece o framework MVC para criar endpoints REST
     - `Spring Data JPA` — abstração para acesso a banco de dados via ORM (Hibernate por baixo)
     - `PostgreSQL Driver` — driver JDBC para conectar ao PostgreSQL
     - `Spring Boot DevTools` — hot-reload durante desenvolvimento
     - `Validation` — Bean Validation para validar dados de entrada

2. Clique em **Generate**, baixe o `.zip` e extraia o conteúdo para este diretório (Feito)

3. Abra o projeto na IDE e explore a estrutura gerada: (Feito)

```
gestor-de-links-api/
├── pom.xml                          ← Configuração do Maven (dependências, plugins, versão Java)
├── src/
│   ├── main/
│   │   ├── kotlin/com/gestordelinks/api/
│   │   │   └── ApiApplication.kt   ← Ponto de entrada da aplicação (equivalente ao main())
│   │   └── resources/
│   │       └── application.properties  ← Configurações da aplicação (porta, banco, etc.)
│   └── test/
│       └── kotlin/com/gestordelinks/api/
│           └── ApiApplicationTests.kt  ← Testes padrão
└── mvnw / mvnw.cmd                  ← Maven Wrapper (roda Maven sem instalação global)
```

### Conceitos-chave para entender

- **`pom.xml`**: pense nele como o `package.json` do mundo Java/Kotlin. Define dependências, plugins de build e metadados do projeto
- **`@SpringBootApplication`**: anotação no `ApiApplication.kt` que configura auto-configuração, scan de componentes e configuração
- **Maven Wrapper (`mvnw`)**: equivalente ao `npx` — garante que todos usem a mesma versão do Maven

### Commit sugerido
```
git init
git add .
git commit -m "chore: inicializa projeto Spring Boot com Kotlin e dependências base"
```

---

## Etapa 2: Configuração do Banco de Dados PostgreSQL

### O que você vai aprender
- Como configurar conexão com banco no Spring Boot
- O que é o `application.properties` / `application.yml`
- Como o Spring Data JPA se conecta ao PostgreSQL

### Passo a passo

1. **Suba o PostgreSQL** (escolha uma opção):

   **Opção A — Docker (recomendado):**
   Crie um arquivo `docker-compose.yml` na raiz do projeto:
   ```yaml
   services:
     postgres:
       image: postgres:16
       environment:
         POSTGRES_DB: postgres
         POSTGRES_USER: gestor_user
         POSTGRES_PASSWORD: gestor_pass
       ports:
         - "5432:5432"
       volumes:
         - pgdata:/var/lib/postgresql/data

   volumes:
     pgdata:
   ```
   Rode: `docker compose up -d`

    Comandos úteis para gerenciar:
  - docker compose ps — ver containers rodando
  - docker compose stop — parar
  - docker compose down — parar e remover containers
  - docker compose down -v — parar, remover containers e os volumes (apaga dados do banco)

   **Opção B — PostgreSQL local:**
   ```bash
   createdb gestor_markdowns
   ```

2. **Configure o `application.properties`** em `src/main/resources/`:

   ```properties
   # Conexão com o PostgreSQL
   spring.datasource.url=jdbc:postgresql://localhost:5432/gestor_markdowns
   spring.datasource.username=gestor_user
   spring.datasource.password=gestor_pass

   # JPA / Hibernate
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

### Conceitos-chave

- **`spring.datasource.*`**: configurações de conexão JDBC — URL, usuário e senha do banco
- **`ddl-auto=update`**: o Hibernate analisa suas entidades e atualiza o schema do banco automaticamente. Perfeito para desenvolvimento. Em produção, usaríamos **migrations** (Flyway/Liquibase)
- **`show-sql=true`**: imprime as queries SQL no console — excelente para aprender o que o JPA gera por baixo

3. **Teste a conexão**: rode `source .env && export POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD && ./mvnw spring-boot:run` e verifique se a aplicação sobe sem erros de conexão

### Commit sugerido
```
git add .
git commit -m "feat: configura conexão com PostgreSQL e docker-compose"
```

---

## Etapa 3: Criação da Entidade (Model)

### O que você vai aprender
- O que é uma Entity JPA e como ela mapeia para uma tabela no banco
- Anotações JPA: `@Entity`, `@Id`, `@GeneratedValue`, `@Column`
- Como o Kotlin lida com data classes vs classes regulares no JPA

### Passo a passo

1. Crie o pacote `model` em `src/main/kotlin/com/gestordelinks/api/model/`

2. Crie o arquivo `Markdown.kt`:

   ```kotlin
   package com.gestordelinks.api.model

   import jakarta.persistence.*
   import java.time.LocalDateTime

   @Entity
   @Table(name = "markdowns")
   class Markdown(
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       val id: Long = 0,

       @Column(nullable = false, length = 255)
       var name: String,

       @Column(nullable = false, length = 500)
       var description: String,

       @Column(nullable = false, columnDefinition = "TEXT")
       var content: String,

       @Column(name = "created_at", nullable = false, updatable = false)
       val createdAt: LocalDateTime = LocalDateTime.now(),

       @Column(name = "updated_at", nullable = false)
       var updatedAt: LocalDateTime = LocalDateTime.now()
   )
   ```

### Conceitos-chave

- **`@Entity`**: diz ao JPA que esta classe representa uma tabela no banco. Cada instância = uma linha
- **`@Table(name = "markdowns")`**: define o nome da tabela. Sem isso, usaria o nome da classe
- **`@Id` + `@GeneratedValue`**: chave primária auto-incrementada. `IDENTITY` usa a sequência nativa do PostgreSQL
- **`@Column`**: configura a coluna — `nullable`, `length`, `columnDefinition` para tipos especiais como `TEXT`
- **`class` vs `data class`**: JPA precisa de entidades mutáveis e com construtor sem argumentos. `class` regular funciona melhor que `data class` aqui — o Hibernate manipula proxies internamente
- **`val` vs `var`**: campos imutáveis (`val`) para `id` e `createdAt`, mutáveis (`var`) para campos editáveis
- **`LocalDateTime`**: tipo do Java para datas — o Hibernate converte automaticamente para `TIMESTAMP` no PostgreSQL

3. Rode a aplicação novamente — o Hibernate deve criar a tabela `markdowns` automaticamente. Verifique no console as queries DDL

### Commit sugerido
```
git add .
git commit -m "feat: cria entidade Markdown com mapeamento JPA"
```

---

## Etapa 4: Criação do Repository

### O que você vai aprender
- O padrão Repository e como o Spring Data JPA elimina boilerplate
- O que o `JpaRepository` oferece gratuitamente
- Como o Spring cria implementações automaticamente a partir de interfaces

### Passo a passo

1. Crie o pacote `repository` em `src/main/kotlin/com/gestordelinks/api/repository/`

2. Crie o arquivo `MarkdownRepository.kt`:

   ```kotlin
   package com.gestordelinks.api.repository

   import com.gestordelinks.api.model.Markdown
   import org.springframework.data.jpa.repository.JpaRepository

   interface MarkdownRepository : JpaRepository<Markdown, Long>
   ```

   Sim, é só isso! Sem implementação.

### Conceitos-chave

- **`JpaRepository<Markdown, Long>`**: o primeiro tipo é a entidade, o segundo é o tipo do ID. Ao estender essa interface, você ganha de graça:
  - `findAll()` — busca todos
  - `findById(id)` — busca por ID (retorna `Optional<Markdown>`)
  - `save(entity)` — insere ou atualiza
  - `deleteById(id)` — deleta por ID
  - `count()`, `existsById()`, paginação, ordenação...
- **Inversão de Controle (IoC)**: o Spring detecta essa interface automaticamente (Component Scan), cria uma implementação em runtime e a disponibiliza para injeção de dependência
- Comparando com frontend: é como se o Spring fosse um framework que gera automaticamente os services/hooks de acesso a dados baseado apenas na tipagem

### Commit sugerido
```
git add .
git commit -m "feat: cria repository para entidade Markdown"
```

---

## Etapa 5: Criação dos DTOs (Data Transfer Objects)

### O que você vai aprender
- O que são DTOs e por que separar da entidade
- Validação de dados com Bean Validation
- `data class` do Kotlin para DTOs

### Passo a passo

1. Crie o pacote `dto` em `src/main/kotlin/com/gestordelinks/api/dto/`

2. Crie `MarkdownRequest.kt` — dados que o cliente envia:

   ```kotlin
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
       val content: String
   )
   ```

3. Crie `MarkdownResponse.kt` — dados que a API retorna:

   ```kotlin
   package com.gestordelinks.api.dto

   import com.gestordelinks.api.model.Markdown
   import java.time.LocalDateTime

   data class MarkdownResponse(
       val id: Long,
       val name: String,
       val description: String,
       val content: String,
       val createdAt: LocalDateTime,
       val updatedAt: LocalDateTime
   ) {
       companion object {
           fun from(markdown: Markdown) = MarkdownResponse(
               id = markdown.id,
               name = markdown.name,
               description = markdown.description,
               content = markdown.content,
               createdAt = markdown.createdAt,
               updatedAt = markdown.updatedAt
           )
       }
   }
   ```

### Conceitos-chave

- **DTO vs Entity**: a entidade é o mapeamento direto do banco. O DTO é o "contrato" da API. Separar ambos evita expor detalhes internos e permite evoluir banco e API independentemente. Pense como a diferença entre o schema do banco e o schema da sua API GraphQL/REST
- **`@field:NotBlank`**: o `field:` é necessário em Kotlin porque as anotações podem ir para o getter, setter ou campo — `field:` garante que vai para o campo, onde o Bean Validation procura
- **`data class`**: perfeito para DTOs — gera `equals()`, `hashCode()`, `toString()`, `copy()` automaticamente. Imutável por usar `val`
- **`companion object`**: similar a métodos `static` em Java. O `from()` é um factory method para converter Entity → Response

### Commit sugerido
```
git add .
git commit -m "feat: cria DTOs de request e response para Markdown"
```

---

## Etapa 6: Criação do Service

### O que você vai aprender
- A camada de serviço e regras de negócio
- Injeção de dependência no Spring
- Tratamento de erros com exceções

### Passo a passo

1. Crie o pacote `service` em `src/main/kotlin/com/gestordelinks/api/service/`

2. Crie `MarkdownService.kt`:

   ```kotlin
   package com.gestordelinks.api.service

   import com.gestordelinks.api.dto.MarkdownRequest
   import com.gestordelinks.api.dto.MarkdownResponse
   import com.gestordelinks.api.model.Markdown
   import com.gestordelinks.api.repository.MarkdownRepository
   import org.springframework.stereotype.Service
   import java.time.LocalDateTime

   @Service
   class MarkdownService(
       private val markdownRepository: MarkdownRepository
   ) {

       fun findAll(): List<MarkdownResponse> =
           markdownRepository.findAll().map { MarkdownResponse.from(it) }

       fun findById(id: Long): MarkdownResponse {
           val markdown = markdownRepository.findById(id)
               .orElseThrow { NoSuchElementException("Markdown com id $id não encontrado") }
           return MarkdownResponse.from(markdown)
       }

       fun create(request: MarkdownRequest): MarkdownResponse {
           val markdown = Markdown(
               name = request.name,
               description = request.description,
               content = request.content
           )
           return MarkdownResponse.from(markdownRepository.save(markdown))
       }

       fun update(id: Long, request: MarkdownRequest): MarkdownResponse {
           val markdown = markdownRepository.findById(id)
               .orElseThrow { NoSuchElementException("Markdown com id $id não encontrado") }

           markdown.name = request.name
           markdown.description = request.description
           markdown.content = request.content
           markdown.updatedAt = LocalDateTime.now()

           return MarkdownResponse.from(markdownRepository.save(markdown))
       }

       fun delete(id: Long) {
           if (!markdownRepository.existsById(id)) {
               throw NoSuchElementException("Markdown com id $id não encontrado")
           }
           markdownRepository.deleteById(id)
       }
   }
   ```

### Conceitos-chave

- **`@Service`**: marca a classe como um bean gerenciado pelo Spring. O container Spring cria uma instância e a injeta onde necessário
- **Constructor injection**: `private val markdownRepository: MarkdownRepository` no construtor. O Spring injeta automaticamente. É o padrão recomendado (vs `@Autowired` em campos) — mais testável e explícito
- **Camadas**: `Controller → Service → Repository → Database`. O Service contém a lógica de negócio, o Controller lida com HTTP, o Repository com persistência. Comparando com frontend: é como separar componente (view) → hook/store (lógica) → API client (dados)
- **`orElseThrow`**: `findById` retorna `Optional<T>`. Se vazio, lançamos exceção que será tratada globalmente depois

### Commit sugerido
```
git add .
git commit -m "feat: cria service com lógica de negócio do CRUD de Markdown"
```

---

## Etapa 7: Criação do Controller (endpoints REST)

### O que você vai aprender
- Como criar endpoints REST no Spring
- Anotações HTTP: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- Respostas HTTP com `ResponseEntity`
- Validação de entrada com `@Valid`

### Passo a passo

1. Crie o pacote `controller` em `src/main/kotlin/com/gestordelinks/api/controller/`

2. Crie `MarkdownController.kt`:

   ```kotlin
   package com.gestordelinks.api.controller

   import com.gestordelinks.api.dto.MarkdownRequest
   import com.gestordelinks.api.dto.MarkdownResponse
   import com.gestordelinks.api.service.MarkdownService
   import jakarta.validation.Valid
   import org.springframework.http.HttpStatus
   import org.springframework.http.ResponseEntity
   import org.springframework.web.bind.annotation.*

   @RestController
   @RequestMapping("/api/markdowns")
   class MarkdownController(
       private val markdownService: MarkdownService
   ) {

       @GetMapping
       fun findAll(): ResponseEntity<List<MarkdownResponse>> =
           ResponseEntity.ok(markdownService.findAll())

       @GetMapping("/{id}")
       fun findById(@PathVariable id: Long): ResponseEntity<MarkdownResponse> =
           ResponseEntity.ok(markdownService.findById(id))

       @PostMapping
       fun create(@Valid @RequestBody request: MarkdownRequest): ResponseEntity<MarkdownResponse> =
           ResponseEntity.status(HttpStatus.CREATED).body(markdownService.create(request))

       @PutMapping("/{id}")
       fun update(
           @PathVariable id: Long,
           @Valid @RequestBody request: MarkdownRequest
       ): ResponseEntity<MarkdownResponse> =
           ResponseEntity.ok(markdownService.update(id, request))

       @DeleteMapping("/{id}")
       fun delete(@PathVariable id: Long): ResponseEntity<Void> {
           markdownService.delete(id)
           return ResponseEntity.noContent().build()
       }
   }
   ```

### Conceitos-chave

- **`@RestController`**: combina `@Controller` + `@ResponseBody`. Todo retorno é serializado automaticamente para JSON (via Jackson)
- **`@RequestMapping("/api/markdowns")`**: prefixo de URL para todos os endpoints desta classe
- **`@GetMapping`, `@PostMapping`, etc.**: mapeiam métodos HTTP para funções Kotlin. Equivalente a definir rotas em Express.js ou Next.js API routes
- **`@PathVariable`**: extrai parâmetros da URL (`/api/markdowns/{id}` → `id: Long`)
- **`@RequestBody`**: deserializa o body JSON para o objeto Kotlin automaticamente
- **`@Valid`**: aciona a validação do Bean Validation definida no DTO. Se falhar, lança `MethodArgumentNotValidException`
- **`ResponseEntity`**: wrapper para controlar status HTTP, headers e body da resposta

### Endpoints resultantes

| Método | URL                    | Ação                  | Status de sucesso |
|--------|------------------------|-----------------------|-------------------|
| GET    | `/api/markdowns`       | Listar todos          | 200 OK            |
| GET    | `/api/markdowns/{id}`  | Buscar por ID         | 200 OK            |
| POST   | `/api/markdowns`       | Criar novo            | 201 Created       |
| PUT    | `/api/markdowns/{id}`  | Atualizar existente   | 200 OK            |
| DELETE | `/api/markdowns/{id}`  | Deletar               | 204 No Content    |

### Commit sugerido
```
git add .
git commit -m "feat: cria controller REST com endpoints CRUD de Markdown"
```

---

## Etapa 8: Tratamento Global de Erros

### O que você vai aprender
- `@RestControllerAdvice` para tratamento centralizado de exceções
- Como retornar respostas de erro padronizadas
- Mapeamento de exceções para status HTTP corretos

### Passo a passo

1. Crie o pacote `exception` em `src/main/kotlin/com/gestordelinks/api/exception/`

2. Crie `ApiExceptionHandler.kt`:

   ```kotlin
   package com.gestordelinks.api.exception

   import org.springframework.http.HttpStatus
   import org.springframework.http.ResponseEntity
   import org.springframework.web.bind.MethodArgumentNotValidException
   import org.springframework.web.bind.annotation.ExceptionHandler
   import org.springframework.web.bind.annotation.RestControllerAdvice

   data class ApiError(
       val status: Int,
       val message: String,
       val errors: List<String> = emptyList()
   )

   @RestControllerAdvice
   class ApiExceptionHandler {

       @ExceptionHandler(NoSuchElementException::class)
       fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ApiError> =
           ResponseEntity.status(HttpStatus.NOT_FOUND).body(
               ApiError(status = 404, message = ex.message ?: "Recurso não encontrado")
           )

       @ExceptionHandler(MethodArgumentNotValidException::class)
       fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
           val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
               ApiError(status = 400, message = "Erro de validação", errors = errors)
           )
       }
   }
   ```

### Conceitos-chave

- **`@RestControllerAdvice`**: intercepta exceções lançadas por qualquer controller. É como um middleware de erro global (pense no `app.use((err, req, res, next) => ...)` do Express)
- **`@ExceptionHandler`**: define qual exceção aquele método trata
- Com isso, ao buscar um ID inexistente, o cliente recebe `404` com uma mensagem clara em vez de um stack trace genérico `500`

### Commit sugerido
```
git add .
git commit -m "feat: adiciona tratamento global de erros com @RestControllerAdvice"
```

---

## Etapa 9: Testando a API

### O que você vai aprender
- Como rodar e testar a aplicação
- Usar curl ou ferramentas HTTP para validar os endpoints

### Passo a passo

1. **Suba a aplicação**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Teste cada endpoint** (com curl, Postman, Insomnia ou extensão REST Client do VS Code):

   ```bash
   # Criar
   curl -X POST http://localhost:8080/api/markdowns \
     -H "Content-Type: application/json" \
     -d '{"name":"Meu primeiro markdown","description":"Teste inicial","content":"# Hello World\n\nEste é meu primeiro markdown!"}'

   # Listar todos
   curl http://localhost:8080/api/markdowns

   # Buscar por ID
   curl http://localhost:8080/api/markdowns/1

   # Atualizar
   curl -X PUT http://localhost:8080/api/markdowns/1 \
     -H "Content-Type: application/json" \
     -d '{"name":"Markdown atualizado","description":"Descrição nova","content":"# Atualizado\n\nConteúdo modificado"}'

   # Deletar
   curl -X DELETE http://localhost:8080/api/markdowns/1

   # Testar validação (body vazio)
   curl -X POST http://localhost:8080/api/markdowns \
     -H "Content-Type: application/json" \
     -d '{}'

   # Testar 404
   curl http://localhost:8080/api/markdowns/999
   ```

3. Verifique as respostas e os status HTTP de cada chamada

---

## Etapa 10: Escrevendo Testes Automatizados

### O que você vai aprender
- Testes de integração com `@SpringBootTest`
- `TestRestTemplate` para testar endpoints
- Configuração de banco de teste com H2 (in-memory)

### Passo a passo

1. Adicione a dependência do H2 no `pom.xml` (apenas para testes):
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>test</scope>
   </dependency>
   ```

2. Crie `src/test/resources/application-test.properties`:
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driver-class-name=org.h2.Driver
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

3. Crie `MarkdownControllerTest.kt` em `src/test/kotlin/com/gestordelinks/api/controller/`:

   ```kotlin
   package com.gestordelinks.api.controller

   import com.gestordelinks.api.dto.MarkdownRequest
   import com.gestordelinks.api.dto.MarkdownResponse
   import org.junit.jupiter.api.Assertions.*
   import org.junit.jupiter.api.Test
   import org.springframework.beans.factory.annotation.Autowired
   import org.springframework.boot.test.context.SpringBootTest
   import org.springframework.boot.test.web.client.TestRestTemplate
   import org.springframework.http.HttpEntity
   import org.springframework.http.HttpMethod
   import org.springframework.http.HttpStatus
   import org.springframework.test.context.ActiveProfiles

   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
   @ActiveProfiles("test")
   class MarkdownControllerTest {

       @Autowired
       lateinit var restTemplate: TestRestTemplate

       @Test
       fun `deve criar e buscar um markdown`() {
           val request = MarkdownRequest(
               name = "Teste",
               description = "Descrição teste",
               content = "# Conteúdo"
           )

           val createResponse = restTemplate.postForEntity(
               "/api/markdowns", request, MarkdownResponse::class.java
           )

           assertEquals(HttpStatus.CREATED, createResponse.statusCode)
           assertNotNull(createResponse.body?.id)
           assertEquals("Teste", createResponse.body?.name)

           val getResponse = restTemplate.getForEntity(
               "/api/markdowns/${createResponse.body?.id}", MarkdownResponse::class.java
           )

           assertEquals(HttpStatus.OK, getResponse.statusCode)
           assertEquals("Teste", getResponse.body?.name)
       }

       @Test
       fun `deve retornar 404 para markdown inexistente`() {
           val response = restTemplate.getForEntity(
               "/api/markdowns/99999", String::class.java
           )

           assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
       }
   }
   ```

4. Rode os testes:
   ```bash
   ./mvnw test
   ```

### Conceitos-chave

- **`@SpringBootTest`**: sobe o contexto completo da aplicação para testes de integração
- **`RANDOM_PORT`**: usa uma porta aleatória para evitar conflitos
- **`@ActiveProfiles("test")`**: usa o `application-test.properties` com H2 em memória
- **H2**: banco in-memory que imita o PostgreSQL. Perfeito para testes — rápido e descartável
- **Backticks em nomes de função**: feature do Kotlin que permite nomes legíveis em testes

### Commit sugerido
```
git add .
git commit -m "test: adiciona testes de integração para MarkdownController"
```

---

## Resumo da Arquitetura Final

```
Cliente HTTP
     ↓
Controller (@RestController)     ← recebe requests, valida, retorna responses
     ↓
Service (@Service)               ← lógica de negócio, conversões DTO ↔ Entity
     ↓
Repository (JpaRepository)       ← acesso ao banco (queries geradas automaticamente)
     ↓
PostgreSQL                       ← persistência dos dados
```

## Checklist de Verificação Final

- [ ] `./mvnw spring-boot:run` sobe sem erros
- [ ] POST `/api/markdowns` cria um registro e retorna 201
- [ ] GET `/api/markdowns` lista os registros
- [ ] GET `/api/markdowns/{id}` retorna um registro específico
- [ ] PUT `/api/markdowns/{id}` atualiza e retorna 200
- [ ] DELETE `/api/markdowns/{id}` retorna 204
- [ ] POST com body inválido retorna 400 com erros de validação
- [ ] GET com ID inexistente retorna 404
- [ ] `./mvnw test` passa todos os testes

## Ordem dos Commits

1. `chore: inicializa projeto Spring Boot com Kotlin e dependências base`
2. `feat: configura conexão com PostgreSQL e docker-compose`
3. `feat: cria entidade Markdown com mapeamento JPA`
4. `feat: cria repository para entidade Markdown`
5. `feat: cria DTOs de request e response para Markdown`
6. `feat: cria service com lógica de negócio do CRUD de Markdown`
7. `feat: cria controller REST com endpoints CRUD de Markdown`
8. `feat: adiciona tratamento global de erros com @RestControllerAdvice`
9. `test: adiciona testes de integração para MarkdownController`
