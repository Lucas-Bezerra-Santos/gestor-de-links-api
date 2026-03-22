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

---

# Parte 2: Categorias de Markdown

## Contexto

Agora que o CRUD básico está funcionando, vamos evoluir a API adicionando **categorias** aos markdowns. Cada markdown poderá ter uma categoria opcional (ex: BACK, FRONT, INFRA...), e categorias são dados fixos pré-populados no banco. Isso vai te ensinar conceitos novos: **migrations com Flyway**, **relacionamentos entre entidades** e **dados seed**.

---

## Etapa 11: Introdução ao Flyway e Migrations

### O que você vai aprender
- O que são migrations e por que são essenciais em projetos reais
- Como o Flyway versiona o schema do banco de dados
- Por que trocar `ddl-auto=update` por migrations controladas
- Como configurar o Flyway manualmente no Spring Boot 4.x
- Diferença entre Flyway e o DDL auto do Hibernate

### Passo a passo

1. **Adicione as dependências do Flyway** no `pom.xml`, dentro do bloco `<dependencies>`:

   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
   </dependency>
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-database-postgresql</artifactId>
   </dependency>
   ```

   > O `flyway-core` é o motor de migrations. O `flyway-database-postgresql` é o módulo específico para PostgreSQL. A versão é gerenciada pelo Spring Boot (BOM), então não precisa declarar `<version>`.

2. **Altere o `application.properties`** — troque o `ddl-auto` de `update` para `validate`:

   ```properties
   # JPA / Hibernate
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

   > Com `validate`, o Hibernate apenas **verifica** se as entidades batem com o schema do banco, mas **não altera** nada. Quem gerencia o schema agora é o Flyway.

3. **Crie o diretório de migrations**: `src/main/resources/db/migration/`

   > O Flyway procura migrations neste caminho por padrão. A convenção de nomes é: `V{número}__{descrição}.sql` (note os dois underscores `__`).

4. **Crie a primeira migration** `V1__create_markdowns_table.sql`:

   ```sql
   CREATE TABLE IF NOT EXISTS markdowns (
       id BIGSERIAL PRIMARY KEY,
       name VARCHAR(255) NOT NULL,
       description VARCHAR(500) NOT NULL,
       content TEXT NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP NOT NULL DEFAULT NOW()
   );
   ```

   > Essa migration recria a tabela `markdowns` que o Hibernate criava automaticamente. O `IF NOT EXISTS` garante que, se a tabela já existir no seu banco de desenvolvimento, a migration não vai falhar.

5. **Importante — se você já tem dados no banco**: como o Flyway está sendo adicionado a um projeto existente, ele precisa saber que o schema atual já está no estado da V1. Adicione essa config no `application.properties`:

   ```properties
   # Flyway
   spring.flyway.baseline-on-migrate=true
   ```

   > `baseline-on-migrate=true` diz ao Flyway: "se o banco já existe mas nunca teve migrations, considere o estado atual como a baseline (V1)". Sem isso, ele tentaria rodar V1 em um banco que já tem a tabela e falharia.

6. **Crie a configuração manual do Flyway** — no Spring Boot 4.x, a auto-configuração do Flyway foi removida. É necessário criar um `@Configuration` bean manualmente.

   Crie o pacote `config` em `src/main/kotlin/com/gestordelinks/api/config/` e o arquivo `FlywayConfig.kt`:

   ```kotlin
   package com.gestordelinks.api.config

   import org.flywaydb.core.Flyway
   import org.springframework.beans.factory.annotation.Value
   import org.springframework.context.annotation.Bean
   import org.springframework.context.annotation.Configuration
   import javax.sql.DataSource

   @Configuration
   class FlywayConfig {

       @Bean(initMethod = "migrate")
       fun flyway(
           dataSource: DataSource,
           @Value("\${spring.flyway.baseline-on-migrate:false}") baselineOnMigrate: Boolean
       ): Flyway {
           return Flyway.configure()
               .dataSource(dataSource)
               .baselineOnMigrate(baselineOnMigrate)
               .locations("classpath:db/migration")
               .load()
       }
   }
   ```

   > **Por que isso é necessário?** Em versões anteriores do Spring Boot (3.x), bastava adicionar `flyway-core` no classpath e o Spring configurava tudo automaticamente via `FlywayAutoConfiguration`. No Spring Boot 4.x, essa auto-configuração foi removida — precisamos criar o bean manualmente.
   >
   > **Como funciona:**
   > - `@Configuration` marca a classe como uma fonte de beans gerenciados pelo Spring
   > - `@Bean(initMethod = "migrate")` cria o bean Flyway e chama `.migrate()` automaticamente ao inicializar — executando todas as migrations pendentes
   > - `dataSource: DataSource` é injetado pelo Spring — reutiliza a mesma conexão de banco configurada no `application.properties`
   > - `@Value("\${spring.flyway.baseline-on-migrate:false}")` lê a propriedade do `application.properties`. O `:false` é o valor padrão caso a propriedade não exista
   > - `"classpath:db/migration"` diz ao Flyway onde encontrar os arquivos `.sql`

7. **Rode a aplicação** para verificar que o Flyway executa a migration:
   ```bash
   ./mvnw spring-boot:run
   ```
   Você deve ver no console algo como:
   ```
   Flyway Community Edition ...
   Successfully validated 1 migration
   Successfully applied 1 migration to schema "public"
   ```

### Conceitos-chave

- **Migration**: um script SQL versionado que altera o schema do banco. Pense como um "commit" do banco de dados — cada migration é uma mudança incremental e irreversível (em produção)
- **Flyway**: ferramenta que gerencia a execução de migrations na ordem correta. Ele mantém uma tabela `flyway_schema_history` no banco para rastrear quais migrations já foram aplicadas
- **`V1__`**: o prefixo `V` + número da versão + `__` (dois underscores) é a convenção obrigatória do Flyway. Ele executa em ordem numérica crescente
- **Por que trocar DDL auto por Flyway?**: `ddl-auto=update` é conveniente para prototipar, mas perigoso em produção:
  - Não tem rollback
  - Não versiona mudanças
  - Pode apagar colunas/dados silenciosamente
  - Impossível reproduzir o schema exato em outro ambiente
  - Flyway resolve tudo isso — é o padrão da indústria
- **Configuração manual no Spring Boot 4.x**: diferente do Spring Boot 3.x que auto-configurava o Flyway, no 4.x você precisa criar o bean explicitamente. Isso dá mais controle mas exige que você entenda como o Spring gerencia beans — algo que vale aprender
- **Comparando com frontend**: migrations são como arquivos de migração do Prisma ou Drizzle — mudanças incrementais e rastreáveis no schema. A config manual do Flyway é como quando você precisa criar um `drizzle.config.ts` — a ferramenta não se configura sozinha

### Commit sugerido
```
git add .
git commit -m "feat: adiciona Flyway e cria migration inicial da tabela markdowns"
```

---

## Etapa 12: Criação da Tabela Categories e Relacionamento via Migrations

### O que você vai aprender
- Como criar tabelas relacionadas com chaves estrangeiras via migrations
- Como popular dados fixos (seed) no banco via migration
- O conceito de dados de referência (lookup tables)

### Passo a passo

1. **Crie a migration** `V2__create_categories_table.sql`:

   ```sql
   CREATE TABLE categories (
       id BIGSERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL UNIQUE
   );
   ```

   > Tabela simples com `id` auto-incrementado e `name` único. O `UNIQUE` garante que não haverá categorias duplicadas.

2. **Crie a migration** `V3__add_category_to_markdowns.sql`:

   ```sql
   ALTER TABLE markdowns
       ADD COLUMN category_id BIGINT,
       ADD CONSTRAINT fk_markdowns_category
           FOREIGN KEY (category_id) REFERENCES categories(id);
   ```

   > Adiciona a coluna `category_id` na tabela `markdowns`. Como a categoria é **opcional**, a coluna é `BIGINT` sem `NOT NULL` (permite `NULL`). A constraint `FOREIGN KEY` garante integridade referencial — não dá para inserir um `category_id` que não existe na tabela `categories`.

3. **Crie a migration** `V4__seed_categories.sql`:

   ```sql
   INSERT INTO categories (name) VALUES
       ('BACK'),
       ('FRONT'),
       ('BFF'),
       ('INFRA'),
       ('PERFORMANCE'),
       ('QUALIDADE'),
       ('DIA_A_DIA');
   ```

   > Dados seed — categorias fixas que a aplicação precisa para funcionar. Usar uma migration para seed garante que qualquer novo ambiente já terá esses dados.

4. **Rode a aplicação** e verifique no console que as 3 novas migrations foram aplicadas:
   ```bash
   ./mvnw spring-boot:run
   ```

   A aplicação vai falhar! Isso é esperado — o Hibernate está com `ddl-auto=validate` e a entidade `Markdown` ainda não tem o campo `category_id`. Vamos resolver na próxima etapa.

### Conceitos-chave

- **Chave estrangeira (Foreign Key)**: uma constraint que liga uma coluna de uma tabela à chave primária de outra. Garante que `category_id` em `markdowns` sempre aponte para um `id` válido em `categories`
- **Dados seed**: dados essenciais que a aplicação precisa para funcionar. Diferente de dados de usuário — são dados de referência/configuração
- **`BIGSERIAL`**: tipo do PostgreSQL que cria um `BIGINT` com auto-incremento. Equivalente ao `GenerationType.IDENTITY` do JPA
- **Nullable FK**: como `category_id` pode ser `NULL`, o markdown pode existir sem categoria — é um relacionamento **opcional** (0..1 para N)
- **Migrations incrementais**: note que cada migration faz **uma** coisa — criar tabela, adicionar coluna, popular dados. Isso facilita debugging e rollback

### Commit sugerido
```
git add .
git commit -m "feat: cria migrations para tabela categories, FK em markdowns e dados seed"
```

---

## Etapa 13: Criação da Entidade Category e Atualização do Markdown

### O que você vai aprender
- Como criar uma nova entidade JPA
- Relacionamento `@ManyToOne` (muitos-para-um) entre entidades
- Como mapear relacionamentos opcionais no Kotlin/JPA

### Passo a passo

1. **Crie o arquivo** `Category.kt` em `src/main/kotlin/com/gestordelinks/api/model/`:

   ```kotlin
   package com.gestordelinks.api.model

   import jakarta.persistence.*

   @Entity
   @Table(name = "categories")
   class Category(
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       val id: Long = 0,

       @Column(nullable = false, unique = true, length = 100)
       val name: String
   )
   ```

   > Entidade simples — apenas `id` e `name`. Ambos são `val` (imutáveis) pois categorias são dados fixos e não devem ser editados pela API.

2. **Crie o arquivo** `CategoryRepository.kt` em `src/main/kotlin/com/gestordelinks/api/repository/`:

   ```kotlin
   package com.gestordelinks.api.repository

   import com.gestordelinks.api.model.Category
   import org.springframework.data.jpa.repository.JpaRepository

   interface CategoryRepository : JpaRepository<Category, Long>
   ```

3. **Atualize a entidade** `Markdown.kt` adicionando o relacionamento com Category:

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

       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "category_id")
       var category: Category? = null,

       @Column(name = "created_at", nullable = false, updatable = false)
       val createdAt: LocalDateTime = LocalDateTime.now(),

       @Column(name = "updated_at", nullable = false)
       var updatedAt: LocalDateTime = LocalDateTime.now()
   )
   ```

   > O campo `category` foi adicionado entre `content` e `createdAt`.

4. **Rode a aplicação** — agora o Hibernate deve validar com sucesso que as entidades batem com o schema do banco:
   ```bash
   ./mvnw spring-boot:run
   ```

### Conceitos-chave

- **`@ManyToOne`**: define um relacionamento onde **muitos** markdowns podem ter **uma** categoria. É o lado "muitos" da relação N:1
- **`fetch = FetchType.LAZY`**: a categoria só é carregada do banco quando você realmente acessa `markdown.category`. Sem isso, o JPA carregaria a categoria junto com cada markdown (EAGER), gerando queries desnecessárias. Regra de ouro: **sempre use LAZY** em `@ManyToOne`
- **`@JoinColumn(name = "category_id")`**: diz ao JPA qual coluna no banco é a FK. Deve bater com o nome que usamos na migration
- **`Category? = null`**: o `?` torna o tipo nullable em Kotlin e o `= null` define o valor padrão. Isso reflete que a FK é nullable no banco — a categoria é opcional
- **`var` no category**: mutável porque o markdown pode mudar de categoria via update
- **Comparando com frontend**: `@ManyToOne` é como uma referência entre entidades — similar a um `userId` em um objeto `Post` que referencia a tabela `users`. A diferença é que o JPA resolve essa referência automaticamente, carregando o objeto inteiro

### Commit sugerido
```
git add .
git commit -m "feat: cria entidade Category e adiciona relacionamento ManyToOne no Markdown"
```

---

## Etapa 14: Atualização dos DTOs do Markdown

### O que você vai aprender
- Como representar relacionamentos nos DTOs
- Validação de campos opcionais
- Por que expor IDs e nomes (não objetos inteiros) nos DTOs

### Passo a passo

1. **Atualize o `MarkdownRequest.kt`** adicionando o campo `categoryId` opcional:

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
       val content: String,

       val categoryId: Long? = null
   )
   ```

   > `categoryId: Long? = null` — campo opcional. Se o cliente não enviar, o valor padrão é `null` (sem categoria). O `?` indica tipo nullable em Kotlin.

2. **Atualize o `MarkdownResponse.kt`** adicionando a categoria como objeto aninhado:

   ```kotlin
   package com.gestordelinks.api.dto

   import com.gestordelinks.api.model.Markdown
   import java.time.LocalDateTime

   data class MarkdownResponse(
       val id: Long,
       val name: String,
       val description: String,
       val content: String,
       val category: CategoryResponse?,
       val createdAt: LocalDateTime,
       val updatedAt: LocalDateTime
   ) {
       companion object {
           fun from(markdown: Markdown) = MarkdownResponse(
               id = markdown.id,
               name = markdown.name,
               description = markdown.description,
               content = markdown.content,
               category = markdown.category?.let { CategoryResponse.from(it) },
               createdAt = markdown.createdAt,
               updatedAt = markdown.updatedAt
           )
       }
   }
   ```

   > Note o uso do **`?.let { ... }`** do Kotlin: se `markdown.category` for `null`, retorna `null` direto. Se existir, converte para `CategoryResponse` usando o factory method que já criamos. Isso significa que o `CategoryResponse` precisa ser criado **antes** deste passo (Etapa 15), ou você pode adiantar a criação dele aqui.

   > **Importante**: como o `MarkdownResponse` agora depende do `CategoryResponse`, a Etapa 15 (criação do `CategoryResponse`) deve ser feita **antes** desta etapa, ou você pode criar o `CategoryResponse.kt` agora e reutilizá-lo na Etapa 15.

### Conceitos-chave

- **Objeto aninhado no response**: em vez de campos planos (`categoryId`, `categoryName`), retornamos `category: { id, name }` como objeto. Isso é mais expressivo e facilita a tipagem no frontend — o cliente recebe `category: CategoryResponse | null`
- **Reutilização de DTOs**: o `CategoryResponse` é usado tanto no endpoint `/api/categories` quanto dentro do `MarkdownResponse`. Um DTO bem feito serve para múltiplos contextos
- **`?.let { ... }`**: padrão Kotlin idiomático para transformar valores nullable. Se `category` for `null`, o resultado é `null`. Se existir, aplica a transformação dentro do `let`
- **`Long? = null`**: em Kotlin, `Long` não pode ser `null`, mas `Long?` pode. O `= null` como valor padrão significa que o campo é opcional no JSON — se omitido, o Jackson (deserializador) usa `null`
- **Comparando com frontend**: é como retornar `category: { id: number, name: string } | null` em TypeScript — o frontend recebe um objeto tipado ou `null`

### Commit sugerido
```
git add .
git commit -m "feat: adiciona categoryId opcional nos DTOs de Markdown"
```

---

## Etapa 15: Criação do Endpoint de Categories

### O que você vai aprender
- Como criar uma nova feature completa (DTO + Service + Controller)
- Endpoint de leitura simples para dados de referência
- Reutilização de padrões já aprendidos

### Passo a passo

1. **Crie o `CategoryResponse.kt`** em `src/main/kotlin/com/gestordelinks/api/dto/`:

   ```kotlin
   package com.gestordelinks.api.dto

   import com.gestordelinks.api.model.Category

   data class CategoryResponse(
       val id: Long,
       val name: String
   ) {
       companion object {
           fun from(category: Category) = CategoryResponse(
               id = category.id,
               name = category.name
           )
       }
   }
   ```

2. **Crie o `CategoryService.kt`** em `src/main/kotlin/com/gestordelinks/api/service/`:

   ```kotlin
   package com.gestordelinks.api.service

   import com.gestordelinks.api.dto.CategoryResponse
   import com.gestordelinks.api.repository.CategoryRepository
   import org.springframework.data.domain.Sort
   import org.springframework.stereotype.Service

   @Service
   class CategoryService(
       private val categoryRepository: CategoryRepository
   ) {

       fun findAll(): List<CategoryResponse> =
           categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
               .map { CategoryResponse.from(it) }
   }
   ```

   > Service simples com apenas um método — categorias são dados de leitura, não precisam de CRUD completo.

3. **Crie o `CategoryController.kt`** em `src/main/kotlin/com/gestordelinks/api/controller/`:

   ```kotlin
   package com.gestordelinks.api.controller

   import com.gestordelinks.api.dto.CategoryResponse
   import com.gestordelinks.api.service.CategoryService
   import org.springframework.http.ResponseEntity
   import org.springframework.web.bind.annotation.GetMapping
   import org.springframework.web.bind.annotation.RequestMapping
   import org.springframework.web.bind.annotation.RestController

   @RestController
   @RequestMapping("/api/categories")
   class CategoryController(
       private val categoryService: CategoryService
   ) {

       @GetMapping
       fun findAll(): ResponseEntity<List<CategoryResponse>> =
           ResponseEntity.ok(categoryService.findAll())
   }
   ```

### Conceitos-chave

- **Feature completa em 3 arquivos**: `CategoryResponse` (DTO) + `CategoryService` (lógica) + `CategoryController` (HTTP). Mesmo padrão do Markdown, mas mais enxuto pois só precisamos de leitura
- **Dados de referência (lookup data)**: categorias são dados fixos que servem de referência para outros recursos. O padrão comum é expor um endpoint GET para que o frontend consuma e popule dropdowns/selects
- **Sem POST/PUT/DELETE**: como categorias são gerenciadas via migrations (dados seed), não precisam de endpoints de escrita. Se no futuro for necessário gerenciar categorias dinamicamente, é fácil adicionar

### Novo endpoint

| Método | URL                | Ação               | Status de sucesso |
|--------|--------------------|---------------------|-------------------|
| GET    | `/api/categories`  | Listar categorias   | 200 OK            |

### Commit sugerido
```
git add .
git commit -m "feat: cria endpoint GET /api/categories com DTO, service e controller"
```

---

## Etapa 16: Atualização do MarkdownService para Categorias

### O que você vai aprender
- Como resolver relacionamentos no service
- Validação de referências entre entidades
- Tratamento de campos opcionais na lógica de negócio

### Passo a passo

1. **Atualize o `MarkdownService.kt`** para injetar o `CategoryRepository` e tratar a categoria no create/update:

   ```kotlin
   package com.gestordelinks.api.service

   import com.gestordelinks.api.dto.MarkdownRequest
   import com.gestordelinks.api.dto.MarkdownResponse
   import com.gestordelinks.api.model.Markdown
   import com.gestordelinks.api.repository.CategoryRepository
   import com.gestordelinks.api.repository.MarkdownRepository
   import org.springframework.data.domain.Sort
   import org.springframework.stereotype.Service
   import java.time.LocalDateTime

   @Service
   class MarkdownService(
       private val markdownRepository: MarkdownRepository,
       private val categoryRepository: CategoryRepository
   ) {

       fun findAll(): List<MarkdownResponse> =
           markdownRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
               .map { MarkdownResponse.from(it) }

       fun findById(id: Long): MarkdownResponse {
           val markdown = markdownRepository.findById(id)
               .orElseThrow { NoSuchElementException("Markdown com id $id nao encontrado") }
           return MarkdownResponse.from(markdown)
       }

       fun create(request: MarkdownRequest): MarkdownResponse {
           val category = request.categoryId?.let {
               categoryRepository.findById(it)
                   .orElseThrow { NoSuchElementException("Categoria com id $it não encontrada") }
           }

           val markdown = Markdown(
               name = request.name,
               description = request.description,
               content = request.content,
               category = category
           )
           return MarkdownResponse.from(markdownRepository.save(markdown))
       }

       fun update(id: Long, request: MarkdownRequest): MarkdownResponse {
           val markdown = markdownRepository.findById(id)
               .orElseThrow { NoSuchElementException("Markdown com id $id não encontrado") }

           val category = request.categoryId?.let {
               categoryRepository.findById(it)
                   .orElseThrow { NoSuchElementException("Categoria com id $it não encontrada") }
           }

           markdown.name = request.name
           markdown.description = request.description
           markdown.content = request.content
           markdown.category = category
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

- **Injeção de múltiplos repositories**: o `MarkdownService` agora recebe dois repositories no construtor. O Spring injeta ambos automaticamente — basta adicionar no construtor
- **`request.categoryId?.let { ... }`**: padrão Kotlin idiomático. Se `categoryId` não for `null`, executa o bloco `let` (busca a categoria). Se for `null`, retorna `null` (sem categoria). É mais elegante que `if (categoryId != null) { ... }`
- **Validação de referência**: ao criar/atualizar, verificamos se o `categoryId` realmente existe na tabela `categories`. Se não existir, lançamos `NoSuchElementException` (que o `ApiExceptionHandler` já trata como 404). Isso evita erros de FK no banco
- **`category = category`**: ao passar `null`, o markdown fica sem categoria. Ao passar uma `Category`, cria o vínculo. O JPA cuida de salvar o `category_id` correto na coluna FK
- **Comparando com frontend**: é como resolver um `userId` em uma chamada — antes de criar um `Post` com `userId`, você verifica se o usuário existe. Mesma lógica, mas no backend

### Commit sugerido
```
git add .
git commit -m "feat: atualiza MarkdownService para suportar categoria opcional"
```

---

## Etapa 17: Testando a Funcionalidade de Categorias

### O que você vai aprender
- Como testar a nova funcionalidade end-to-end
- Verificar que as migrations rodaram corretamente
- Testar relacionamentos opcionais

### Passo a passo

1. **Suba a aplicação**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Teste o endpoint de categorias**:

   ```bash
   # Listar todas as categorias
   curl http://localhost:8080/api/categories
   ```

   Resposta esperada:
   ```json
   [
     {"id": 1, "name": "BACK"},
     {"id": 2, "name": "BFF"},
     {"id": 3, "name": "DIA_A_DIA"},
     {"id": 4, "name": "FRONT"},
     {"id": 5, "name": "INFRA"},
     {"id": 6, "name": "PERFORMANCE"},
     {"id": 7, "name": "QUALIDADE"}
   ]
   ```

3. **Teste criar markdown SEM categoria** (compatibilidade):

   ```bash
   curl -X POST http://localhost:8080/api/markdowns \
     -H "Content-Type: application/json" \
     -d '{"name":"Sem categoria","description":"Markdown sem categoria","content":"# Teste"}'
   ```

   Resposta esperada: `category` deve ser `null`.

4. **Teste criar markdown COM categoria**:

   ```bash
   curl -X POST http://localhost:8080/api/markdowns \
     -H "Content-Type: application/json" \
     -d '{"name":"Guia de Deploy","description":"Deploy na AWS","content":"# Deploy","categoryId":5}'
   ```

   Resposta esperada: `category: {"id": 5, "name": "INFRA"}`.

5. **Teste atualizar categoria de um markdown existente**:

   ```bash
   curl -X PUT http://localhost:8080/api/markdowns/1 \
     -H "Content-Type: application/json" \
     -d '{"name":"Sem categoria","description":"Agora com categoria","content":"# Teste","categoryId":1}'
   ```

6. **Teste remover categoria** (enviar sem categoryId):

   ```bash
   curl -X PUT http://localhost:8080/api/markdowns/1 \
     -H "Content-Type: application/json" \
     -d '{"name":"Sem categoria","description":"Sem categoria de novo","content":"# Teste"}'
   ```

7. **Teste categoria inválida** (deve retornar 404):

   ```bash
   curl -X POST http://localhost:8080/api/markdowns \
     -H "Content-Type: application/json" \
     -d '{"name":"Teste","description":"Teste","content":"# Teste","categoryId":999}'
   ```

8. **Atualize o teste de integração** em `MarkdownControllerTest.kt` — adicione a dependência H2 do Flyway no `pom.xml` para testes:

   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-database-h2</artifactId>
       <scope>test</scope>
   </dependency>
   ```

   E adicione novos testes no `MarkdownControllerTest.kt`:

   ```kotlin
   @Test
   fun `deve listar categorias`() {
       val response = restTemplate.getForEntity(
           "/api/categories", Array<Map<String, Any>>::class.java
       )

       assertEquals(HttpStatus.OK, response.statusCode)
       assertTrue(response.body!!.isNotEmpty())
   }

   @Test
   fun `deve criar markdown com categoria`() {
       // Primeiro, busca as categorias para pegar um ID válido
       val categories = restTemplate.getForEntity(
           "/api/categories", Array<Map<String, Any>>::class.java
       )
       val categoryId = (categories.body!!.first()["id"] as Number).toLong()

       val request = mapOf(
           "name" to "Com Categoria",
           "description" to "Teste com categoria",
           "content" to "# Conteúdo",
           "categoryId" to categoryId
       )

       val response = restTemplate.postForEntity(
           "/api/markdowns", request, MarkdownResponse::class.java
       )

       assertEquals(HttpStatus.CREATED, response.statusCode)
       assertNotNull(response.body?.category)
       assertNotNull(response.body?.category?.id)
       assertNotNull(response.body?.category?.name)
   }

   @Test
   fun `deve criar markdown sem categoria`() {
       val request = MarkdownRequest(
           name = "Sem Categoria",
           description = "Teste sem categoria",
           content = "# Conteúdo"
       )

       val response = restTemplate.postForEntity(
           "/api/markdowns", request, MarkdownResponse::class.java
       )

       assertEquals(HttpStatus.CREATED, response.statusCode)
       assertNull(response.body?.category)
   }
   ```

9. **Rode os testes**:
   ```bash
   ./mvnw test
   ```

### Conceitos-chave

- **Testes de integração com Flyway**: quando a aplicação sobe para testes, o Flyway executa as mesmas migrations no H2. Isso garante que o schema de teste é idêntico ao de desenvolvimento
- **`flyway-database-h2`**: módulo do Flyway específico para H2, necessário para rodar as migrations no banco in-memory dos testes
- **Teste de compatibilidade**: é essencial testar que markdowns **sem** categoria continuam funcionando. Isso valida que a mudança é backwards-compatible
- **Teste de validação**: testar `categoryId: 999` confirma que a API retorna 404 para categorias inválidas, não um erro 500

### Commit sugerido
```
git add .
git commit -m "test: adiciona testes de integração para funcionalidade de categorias"
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
Flyway (migrations)              ← gerencia schema e dados seed
     ↓
PostgreSQL                       ← persistência dos dados
```

```
markdowns (N) ──── category_id (FK, nullable) ────→ (1) categories
"Muitos markdowns podem ter uma categoria, categoria é opcional"
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
- [ ] GET `/api/categories` retorna as 7 categorias
- [ ] POST `/api/markdowns` com `categoryId` válido associa a categoria
- [ ] POST `/api/markdowns` sem `categoryId` cria sem categoria (null)
- [ ] POST `/api/markdowns` com `categoryId` inválido retorna 404
- [ ] PUT `/api/markdowns/{id}` permite trocar ou remover categoria
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
10. `feat: adiciona Flyway e cria migration inicial da tabela markdowns`
11. `feat: cria migrations para tabela categories, FK em markdowns e dados seed`
12. `feat: cria entidade Category e adiciona relacionamento ManyToOne no Markdown`
13. `feat: adiciona categoryId opcional nos DTOs de Markdown`
14. `feat: cria endpoint GET /api/categories com DTO, service e controller`
15. `feat: atualiza MarkdownService para suportar categoria opcional`
16. `test: adiciona testes de integração para funcionalidade de categorias`
