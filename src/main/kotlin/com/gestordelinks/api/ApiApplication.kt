package com.gestordelinks.api

// Anotação que combina 3 configurações em uma só:
//   1. @Configuration — marca a classe como fonte de configuração do Spring (como um arquivo de config)
//   2. @EnableAutoConfiguration — ativa a auto-configuração do Spring Boot.
//      O Spring analisa as dependências no pom.xml e configura automaticamente:
//      - Achou spring-web? Configura servidor HTTP (Tomcat)
//      - Achou spring-data-jpa + postgresql? Configura conexão com banco
//      - Achou jackson? Configura serialização JSON
//      É como se o framework "adivinhasse" o que você precisa pelas dependências instaladas.
//   3. @ComponentScan — escaneia todos os pacotes a partir deste (com.gestordelinks.api)
//      procurando classes anotadas com @Service, @RestController, @Repository, etc.
//      e as registra como beans gerenciados (singletons que podem ser injetados).
//      Em JS seria como um auto-import que registra todos os módulos automaticamente.
import org.springframework.boot.autoconfigure.SpringBootApplication
// Função helper do Spring Boot para Kotlin — inicia a aplicação.
// É uma versão Kotlin-friendly de SpringApplication.run() do Java.
import org.springframework.boot.runApplication

// @SpringBootApplication marca esta classe como o ponto de entrada da aplicação Spring Boot.
// É a "raiz" do projeto — tudo começa aqui.
// Em Node.js seria como o arquivo app.js/index.js onde você faz:
//   const app = express()
//   app.use(cors())
//   app.use(express.json())
//   app.use('/api', routes)
//   app.listen(8080)
// Mas no Spring, tudo isso é feito automaticamente pela auto-configuração.
@SpringBootApplication
// Classe vazia — serve apenas como âncora para o @SpringBootApplication.
// O Spring usa o pacote desta classe (com.gestordelinks.api) como base para o @ComponentScan.
// Não precisa de body {} porque não tem lógica — apenas existe para carregar a anotação.
// Em Kotlin, classes sem body não precisam de chaves (diferente do JS: class ApiApplication {})
class ApiApplication

// Ponto de entrada da aplicação — equivalente ao main() de qualquer linguagem.
// Em Kotlin, funções podem existir fora de classes (top-level functions).
// Em JS não existe esse conceito — tudo roda no escopo global ou dentro de módulos.
// Em Java, o main() OBRIGATORIAMENTE fica dentro de uma classe.
//
// "args: Array<String>" = argumentos da linha de comando.
// Em JS seria: process.argv (array de strings)
// Em Node: node app.js --port 3000 → args = ["--port", "3000"]
fun main(args: Array<String>) {
    // runApplication<ApiApplication>(*args) inicia toda a aplicação Spring Boot:
    //   1. Cria o contexto Spring (container de beans/dependências)
    //   2. Executa o @ComponentScan — descobre e registra todos os beans (@Service, @RestController, etc.)
    //   3. Configura automaticamente tudo baseado nas dependências (auto-configuration)
    //   4. Sobe o servidor Tomcat embutido na porta 8080 (padrão)
    //   5. A aplicação fica escutando requisições HTTP
    //
    // O "*args" é o spread operator do Kotlin — desempacota o Array em argumentos individuais.
    // É IDÊNTICO ao spread do JS: ...args
    // Kotlin: funcao(*array) = JS: funcao(...array)
    //
    // "<ApiApplication>" é um generic (tipo entre <>) — diz ao Spring qual classe é a raiz.
    // Em TypeScript seria: runApplication<ApiApplication>(args)
    runApplication<ApiApplication>(*args)
}
