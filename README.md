# Controlador de Rifas

[![Java 25](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4.1.1](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

**Stack:** Java 25 · Spring Boot 4.1.1 · Spring Security e JWT · Spring Data JPA · PostgreSQL · Redis · springdoc-openapi (Swagger)

API REST para controlar rifas e a venda de cotas. Um operador cadastra as rifas, com preço e quantidade de cotas, cadastra os compradores (clientes) e registra quantas cotas cada cliente comprou. A API calcula o valor total de cada compra, impede vendas acima das cotas disponíveis e bloqueia novas compras quando a rifa é encerrada.

A API também gerencia os usuários que operam o sistema e seus tipos, e oferece autenticação por token.

## Sumário

- [Fluxo principal de uma rifa](#fluxo-principal-de-uma-rifa)
  - [Estados da rifa](#estados-da-rifa)
  - [Regras de negócio](#regras-de-negócio)
- [Requisitos](#requisitos)
- [Configuração](#configuração)
- [Executar](#executar)
  - [Integração do Spring com o Docker Compose](#integração-do-spring-com-o-docker-compose)
- [Diagnóstico rápido](#diagnóstico-rápido)
- [Tipos de usuário](#tipos-de-usuário)
- [Autenticação](#autenticação)
- [Endpoints principais](#endpoints-principais)
  - [Usuários](#usuários)
  - [Tipos de usuário](#tipos-de-usuário-1)
  - [Clientes](#clientes)
  - [Rifas](#rifas)
  - [Compras](#compras)
  - [Exemplos de requisição e resposta](#exemplos-de-requisição-e-resposta)
- [Campos obrigatórios dos requests](#campos-obrigatórios-dos-requests)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Erros HTTP](#erros-http)
- [Cache](#cache)
- [Testes](#testes)
- [Estrutura do código](#estrutura-do-código)
- [Limitações e documentação](#limitações-e-documentação)

## Fluxo principal de uma rifa

1. **Cadastro da rifa:** o operador cria a rifa com descrição, preço da cota e quantidade total de cotas. A rifa começa `ABERTA`.
2. **Cadastro dos clientes:** o operador cadastra os compradores com nome, e-mail e telefone.
3. **Venda de cotas:** o operador registra a compra informando o cliente, a rifa e a quantidade. A API confere se a rifa está aberta e se ainda há cotas disponíveis, e calcula o total com o preço da cota naquele momento.
4. **Ajustes:** enquanto a rifa está aberta, uma compra pode ser alterada (outra quantidade, outro cliente ou outra rifa) ou cancelada. As cotas canceladas voltam a ficar disponíveis.
5. **Encerramento:** o operador encerra a rifa. A partir daí, as compras ficam congeladas: não é possível comprar, alterar nem cancelar. Se necessário, a rifa pode ser reaberta.

### Estados da rifa

- `ABERTA`: aceita compras, alterações e cancelamentos.
- `ENCERRADA`: compras congeladas; novas compras, alterações e cancelamentos retornam `409`.

As transições são feitas por `PATCH /controlador/rifa/{id}/encerrar` e `PATCH /controlador/rifa/{id}/reabrir`. Encerrar uma rifa já encerrada, ou reabrir uma já aberta, retorna `409`. O status também pode ser enviado no `PUT /controlador/rifa/{id}`. Se o campo for omitido, o status atual é mantido.

### Regras de negócio

| Regra | Comportamento |
|---|---|
| Rifa encerrada não vende | Comprar, alterar ou cancelar compra de rifa `ENCERRADA` retorna `409` |
| Limite de cotas | Uma compra acima das cotas disponíveis retorna `409` com a quantidade disponível na mensagem (ex.: `Cotas insuficientes. Disponíveis: 4, solicitadas: 5.`) |
| Cotas não diminuem abaixo do vendido | `PUT` na rifa com `quantidadeCotas` menor que o total já vendido retorna `409` |
| Total calculado no servidor | `total = quantidade × preço da cota`. Campos de valor enviados no corpo são ignorados |
| Preço histórico | A compra guarda o `precoUnitario` do momento da venda. Mudar o preço da rifa depois não altera compras já feitas |
| Exclusão protegida | Excluir rifa ou cliente que tenha compras retorna `409`. Cancele as compras antes |

A compra trava a linha da rifa no banco (lock pessimista) enquanto confere as cotas. Assim, compras simultâneas não vendem acima do limite: em um teste com 30 compras paralelas de 1 cota numa rifa de 20 cotas, exatamente 20 foram aceitas e 10 recusadas com `409`.

## Requisitos

- Java 25
- Docker e Docker Compose, para iniciar PostgreSQL e Redis
- Maven Wrapper incluído no projeto

## Configuração

Todos os comandos deste README devem ser executados dentro da pasta `Controlador/`, onde ficam `pom.xml`, `mvnw`, `compose.yaml` e `.env.example`.

Copie os arquivos de exemplo. No PowerShell:

```powershell
Copy-Item .env.example .env
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
```

Em macOS/Linux:

```shell
cp .env.example .env
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

O `.env` e o `application.properties` estão no `.gitignore`. Mantenha neles apenas as credenciais locais e não os versione. Preencha as senhas no `.env`:

```env
POSTGRES_DB=Controlador
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<senha do PostgreSQL>
REDIS_PASSWORD=<senha do Redis>
API_SECURITY_TOKEN_SECRET=<segredo do JWT>
```

Gere um segredo aleatório para o JWT com OpenSSL:

```shell
openssl rand -base64 32
```

O `application.properties` lê esses valores das variáveis de ambiente (`${POSTGRES_PASSWORD:...}`, `${REDIS_PASSWORD:...}`, `${API_SECURITY_TOKEN_SECRET:...}`). Se elas não estiverem definidas no ambiente em que a API roda, coloque os mesmos valores diretamente no `application.properties`, no lugar dos padrões de exemplo. O Docker Compose lê o `.env` para criar os containers.

Os containers leem o `.env` apenas quando são criados. Se você alterar `POSTGRES_*` ou `REDIS_PASSWORD` depois disso:

- Redis: recrie o container com `docker compose up -d --force-recreate redis`. O cache é descartável.
- PostgreSQL: o volume `postgres_data` mantém o banco, o usuário e a senha da primeira criação. Volte o `.env` para os valores originais ou recrie o volume com `docker compose down -v`. Esse comando **apaga os dados**.

## Executar

Inicie os serviços de infraestrutura:

```shell
docker compose up -d
```

Inicie a API pelo Maven Wrapper:

```shell
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O PostgreSQL é publicado na porta `5434`, e o Redis na `6380`. Essas portas foram escolhidas para não conflitar com outros projetos que usam as portas `5433` e `6379`. Os containers se chamam `controlador-rifas-postgres` e `controlador-rifas-redis`.

Na inicialização, o `data.sql` insere o tipo de usuário `ADMIN` (ID `1`) se ele não existir e ajusta a sequence de `tb_tipo_usuario`.

O Hibernate está configurado com `ddl-auto=update`, que atualiza o schema durante o desenvolvimento. Esse modo cria tabelas e colunas novas, mas não altera colunas que já existem e não remove tabelas de entidades que deixaram de existir. Rifas criadas antes da coluna `quantidade_cotas` ficam com esse campo vazio e são tratadas como **sem limite de cotas** até serem editadas. Não use essa estratégia em produção; para ambientes controlados, adote migrações versionadas com Flyway ou Liquibase.

### Integração do Spring com o Docker Compose

O `pom.xml` inclui `spring-boot-docker-compose`. Com isso, `./mvnw spring-boot:run` lê o `compose.yaml` e sobe os serviços sozinho quando eles não estão rodando (`spring.docker.compose.lifecycle-management=start-and-stop`). O `docker compose up -d` acima continua válido e é útil para subir a infraestrutura antes da API.

Essa integração também muda a origem das credenciais:

- **PostgreSQL:** a conexão é montada a partir do container, com host, porta, banco, usuário e senha tirados das variáveis do serviço `postgres`.
- **Redis:** o serviço `redis` do `compose.yaml` tem a label `org.springframework.boot.ignore: true`. A conexão Redis vinda do Docker Compose traz apenas host e porta, sem senha, porque a imagem oficial define a senha no `--requirepass`. Sem essa label, a aplicação conectava sem autenticar e a listagem de rifas falhava com `NOAUTH`. Com a label, o Redis usa `spring.data.redis.*`, inclusive a senha.

## Diagnóstico rápido

- Se a inicialização falhar ao conectar ao PostgreSQL ou Redis, confirme que os serviços estão ativos com `docker compose ps` e que as portas e credenciais do `.env` correspondem às dos containers (veja a seção [Configuração](#configuração)).
- Se `GET /controlador/rifa` retornar `500` com `RedisConnectionFailureException` / `NOAUTH` no log, confira que a label `org.springframework.boot.ignore` continua no serviço `redis` do `compose.yaml`. Depois de adicionar ou alterar a label, recrie o container com `docker compose up -d --force-recreate redis`; o Spring não recria um container que já está rodando.
- Se o Docker Compose acusar porta em uso, outro projeto pode estar usando `5434` ou `6380`. Veja com `docker ps` e ajuste as portas no `compose.yaml` e no `application.properties`.
- Se o banco for recriado (por exemplo, com `docker compose down -v`), limpe também o cache do Redis. Caso contrário, a listagem de rifas pode devolver dados antigos por até uma hora. Para limpar: `docker compose exec redis sh -c 'REDISCLI_AUTH=$REDIS_PASSWORD redis-cli FLUSHALL'`.
- Limpe o cache também depois de alterar os campos de `RifaModel`. O Redis guarda os objetos serializados no formato antigo.
- O `pom.xml` usa Spring Boot 4.1.1 e springdoc 3.2.0; o projeto springdoc documenta a linha 3.x para Spring Boot 4 ([matriz oficial de compatibilidade](https://github.com/springdoc/springdoc-openapi)).

## Tipos de usuário

Os tipos ficam em `tb_tipo_usuario` e são associados às autoridades pelo enum `UserRoles`, por ID:

| ID | Tipo | Autoridade |
|---:|---|---|
| 1 | ADMIN | `ROLE_ADMIN` |

Hoje existe um único papel. É possível cadastrar novos tipos em `/controlador/tipo-usuario`, mas eles só podem ser usados por usuários depois de mapeados no enum `UserRoles`. Cadastrar um usuário com um tipo sem papel mapeado retorna `400` e nada é gravado.

O cadastro de usuários (`POST /controlador/usuario`) é público. Para criar o primeiro operador, basta cadastrá-lo com `idTipoUsuario: 1` e fazer login.

## Autenticação

Faça login com e-mail e senha:

```http
POST /controlador/usuario/login
Content-Type: application/json

{
  "email": "operador@exemplo.com",
  "senha": "sua-senha"
}
```

O login retorna somente o token:

```json
{
  "token": "<jwt>"
}
```

Envie o token no cabeçalho das próximas requisições protegidas:

```http
Authorization: Bearer <jwt>
```

O token é um JWT assinado com HS256 e contém estas informações:

| Claim | Conteúdo |
|---|---|
| `sub` | e-mail do usuário |
| `idUsuario` | ID do usuário |
| `role` | autoridade (`ROLE_ADMIN`) |
| `exp` | expiração, 88.888 segundos (cerca de 24 horas e 40 minutos) após o login |

O papel é lido do token a cada requisição. Por isso, depois de excluir um usuário ou trocar a senha, os tokens já emitidos continuam válidos até expirar.

## Endpoints principais

Todas as rotas usam o prefixo `/controlador`. Exceto o cadastro e o login de usuário, todas exigem token.

### Usuários

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `POST /usuario` | Público | `200 OK` |
| `POST /usuario/login` | Público | `200 OK` |
| `GET /usuario` | Autenticado | `200 OK` |
| `GET /usuario/{id}` | Autenticado | `200 OK` |
| `GET /usuario/email/{email}` | Autenticado | `200 OK` |
| `PUT /usuario/{id}` | Autenticado | `200 OK` |
| `DELETE /usuario/{id}` | Autenticado | `200 OK` |

Observações sobre usuários:

- E-mail e nome são únicos. A comparação de e-mail não diferencia maiúsculas de minúsculas: `Ana@Exemplo.com` e `ana@exemplo.com` contam como duplicados (`409`).
- A senha é gravada com BCrypt e não aparece em nenhuma resposta.
- As respostas trazem `id`, `nome`, `email`, `idTipoUsuario`, `tipoUsuarioDescricao` e `role`.

### Tipos de usuário

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `GET /tipo-usuario` | Autenticado | `200 OK` |
| `GET /tipo-usuario/{id}` | Autenticado | `200 OK` |
| `POST /tipo-usuario` | Autenticado | `200 OK` |
| `PUT /tipo-usuario/{id}` | Autenticado | `200 OK` |
| `DELETE /tipo-usuario/{id}` | Autenticado | `200 OK` |

A descrição é única (`409` se repetida). Excluir um tipo em uso por algum usuário retorna `409`. As respostas trazem `idTipoUsuario` e `descricao`.

### Clientes

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `POST /cliente` | Autenticado | `200 OK` |
| `GET /cliente` | Autenticado | `200 OK` |
| `GET /cliente/{id}` | Autenticado | `200 OK` |
| `GET /cliente/email/{email}` | Autenticado | `200 OK` |
| `GET /cliente/nome/{nome}` | Autenticado | `200 OK` |
| `PUT /cliente/{id}` | Autenticado | `200 OK` |
| `DELETE /cliente/{id}` | Autenticado | `200 OK` |

Observações sobre clientes:

- E-mail e telefone são únicos (`409`). O e-mail é comparado sem diferenciar maiúsculas de minúsculas, inclusive na busca por e-mail.
- `GET /cliente/nome/{nome}` busca pelo nome exato e retorna lista vazia quando não encontra. `GET /cliente/email/{email}` retorna `404` quando não encontra.
- Um cliente com compras não pode ser excluído (`409`).
- As respostas trazem `id`, `nome`, `email`, `telefone` e `endereco`.

### Rifas

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `POST /rifa` | Autenticado | `200 OK` |
| `GET /rifa` | Autenticado; resultado em cache no Redis | `200 OK` |
| `GET /rifa/{id}` | Autenticado | `200 OK` |
| `GET /rifa/descricao/{descricao}` | Autenticado; busca parcial, sem diferenciar maiúsculas | `200 OK` |
| `PUT /rifa/{id}` | Autenticado | `200 OK` |
| `PATCH /rifa/{id}/encerrar` | Autenticado | `200 OK` |
| `PATCH /rifa/{id}/reabrir` | Autenticado | `200 OK` |
| `DELETE /rifa/{id}` | Autenticado | `200 OK` |

As respostas trazem `id`, `descricao`, `preco`, `quantidadeCotas` e `status`. Uma rifa com compras não pode ser excluída (`409`).

### Compras

| Método e rota | Acesso | Resposta de sucesso |
|---|---|---|
| `POST /cliente-rifa` | Autenticado | `200 OK` |
| `GET /cliente-rifa` | Autenticado | `200 OK` |
| `GET /cliente-rifa/{id}` | Autenticado | `200 OK` |
| `GET /cliente-rifa/cliente/{idCliente}` | Autenticado; `404` se o cliente não existe | `200 OK` |
| `GET /cliente-rifa/rifa/{idRifa}` | Autenticado; `404` se a rifa não existe | `200 OK` |
| `PUT /cliente-rifa/{id}` | Autenticado; só com a rifa aberta | `200 OK` |
| `DELETE /cliente-rifa/{id}` | Autenticado; só com a rifa aberta (cancela a compra) | `200 OK` |

No `PUT`, as cotas da própria compra não contam como vendidas. Assim, numa rifa de 10 cotas com uma compra de 6, essa compra pode ir até 10. Se a compra continuar na mesma rifa, mantém o preço original da cota; se mudar de rifa, passa a usar o preço atual da nova rifa.

As respostas trazem `id`, `idCliente`, `nomeCliente`, `telefoneCliente`, `idRifa`, `descricaoRifa`, `precoUnitario`, `quantidade`, `total` e `dataCompra`.

### Exemplos de requisição e resposta

Os exemplos abaixo assumem que `TOKEN` foi obtido pelo login e que os IDs correspondem a registros existentes. Substitua os valores pelos dados do seu banco.

Cadastrar o primeiro operador (rota pública):

```http
POST /controlador/usuario
Content-Type: application/json

{
  "nome": "Operador inicial",
  "email": "operador@exemplo.com",
  "senha": "troque-por-uma-senha-forte",
  "idTipoUsuario": 1
}
```

Criar uma rifa:

```http
POST /controlador/rifa
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "descricao": "Rifa beneficente - cesta de café da manhã",
  "preco": 2.50,
  "quantidadeCotas": 10
}
```

Exemplo de resposta `200 OK`:

```json
{
  "id": 7,
  "descricao": "Rifa beneficente - cesta de café da manhã",
  "preco": 2.50,
  "quantidadeCotas": 10,
  "status": "ABERTA"
}
```

Cadastrar um cliente:

```http
POST /controlador/cliente
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "nome": "João da Silva",
  "email": "joao.silva@exemplo.com",
  "telefone": "11988887777",
  "endereco": "Rua das Flores, 123 - Centro"
}
```

Registrar uma compra de 6 cotas:

```http
POST /controlador/cliente-rifa
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "idCliente": 3,
  "idRifa": 7,
  "quantidade": 6
}
```

Exemplo de resposta `200 OK` (total calculado pelo servidor):

```json
{
  "id": 27,
  "idCliente": 3,
  "nomeCliente": "João da Silva",
  "telefoneCliente": "11988887777",
  "idRifa": 7,
  "descricaoRifa": "Rifa beneficente - cesta de café da manhã",
  "precoUnitario": 2.50,
  "quantidade": 6,
  "total": 15.00,
  "dataCompra": "2026-11-11T15:38:52.670417"
}
```

Tentar comprar 5 cotas quando restam 4 retorna `409 Conflict`:

```json
{
  "erro": "Cotas insuficientes. Disponíveis: 4, solicitadas: 5."
}
```

Encerrar a rifa:

```http
PATCH /controlador/rifa/7/encerrar
Authorization: Bearer TOKEN
```

Depois disso, uma nova compra na rifa retorna `409 Conflict`:

```json
{
  "erro": "A rifa \"Rifa beneficente - cesta de café da manhã\" está encerrada e não aceita novas compras ou alterações."
}
```

## Campos obrigatórios dos requests

Os campos abaixo são obrigatórios conforme os DTOs de entrada; `@Valid` também aplica as restrições indicadas:

| Operação | Campos obrigatórios e validações principais |
|---|---|
| `POST /usuario/login` | `email` válido e `senha` não vazios |
| `POST /usuario`, `PUT /usuario/{id}` | `nome`, `email` válido, `senha` (6 a 72 caracteres) e `idTipoUsuario` |
| `POST /tipo-usuario`, `PUT /tipo-usuario/{id}` | `descricao` não vazia (até 50 caracteres) |
| `POST /cliente`, `PUT /cliente/{id}` | `nome` (até 200), `email` válido (até 200) e `telefone` (até 50); `endereco` é opcional (até 200) |
| `POST /rifa`, `PUT /rifa/{id}` | `descricao` (até 150), `preco` (mínimo 0,01, até 8 dígitos inteiros e 2 casas decimais) e `quantidadeCotas` (inteiro, mínimo 1); `status` é opcional (`ABERTA` ou `ENCERRADA`) |
| `POST /cliente-rifa`, `PUT /cliente-rifa/{id}` | `idCliente`, `idRifa` e `quantidade` (inteiro, mínimo 1) |

Os limites de tamanho seguem o tamanho das colunas no banco, então o texto é recusado na validação (`400`) antes de chegar ao banco. Quando vários campos são inválidos, a mensagem lista todos, separados por `;`. Números decimais em campos inteiros (como `"quantidade": 1.5`) também retornam `400`, em vez de serem truncados.

## Documentação interativa (Swagger)

Com a aplicação em execução, acesse:

- Interface Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificação OpenAPI em JSON: <http://localhost:8080/v3/api-docs>

As duas rotas são públicas. Os endpoints aparecem agrupados (Usuários, Tipos de usuário, Clientes, Rifas e Compras), e as respostas de erro `400`, `403`, `404` e `409` estão documentadas com o formato `{"erro": "..."}`.

Para autenticar e executar operações protegidas:

1. Cadastre um usuário em `POST /controlador/usuario` com `idTipoUsuario: 1`, se ainda não tiver um.
2. Faça login em `POST /controlador/usuario/login`. Essa rota é pública e não exige token.
3. Copie o valor de `token` retornado no JSON.
4. Na interface Swagger UI, clique em **Authorize**.
5. Cole o token JWT no campo exibido e confirme em **Authorize**. Como o esquema é HTTP Bearer, informe somente o token; o Swagger adiciona o prefixo `Bearer` nas requisições.
6. Feche a janela de autorização e execute as operações.

## Erros HTTP

| Status | Quando ocorre |
|---|---|
| `400 Bad Request` | Falha de validação, JSON malformado, ID não numérico na URL, status de rifa inválido, número decimal em campo inteiro ou tipo de usuário sem papel mapeado |
| `401 Unauthorized` | Login com e-mail ou senha inválidos |
| `403 Forbidden` | Token ausente ou inválido em rota protegida |
| `404 Not Found` | Registro inexistente em consultas, alterações e exclusões, ou rota inexistente |
| `405 Method Not Allowed` | Método HTTP não suportado no endpoint |
| `409 Conflict` | Regra de negócio violada (rifa encerrada, cotas insuficientes, cotas abaixo do vendido, encerrar/reabrir repetido), dado duplicado ou exclusão de registro com vínculos |
| `500 Internal Server Error` | Erro inesperado; o detalhe fica só no log do servidor |

As exceções tratadas pelo `ApiExceptionHandler` retornam JSON no formato `{"erro":"mensagem"}`. A exceção é o `403` de token ausente ou inválido: ele é respondido pelo Spring Security antes de chegar aos controllers e usa o corpo padrão do Spring (`timestamp`, `status`, `error`, `path`).

## Cache

A listagem geral de rifas (`GET /controlador/rifa`) usa `@Cacheable` com Redis e TTL de uma hora (`spring.cache.redis.time-to-live=3600000`).

| Cache | Chave | Invalidado por |
|---|---|---|
| `rifas` | `SimpleKey []` | cadastro, `PUT`, encerramento, reabertura e exclusão de rifa |

As demais consultas vão direto ao PostgreSQL.

## Testes

Execute a suíte com:

```shell
./mvnw test
```

No Windows PowerShell:

```powershell
.\mvnw.cmd test
```

A suíte tem um único teste, `ControladorApplicationTests.contextLoads`, que verifica se o contexto do Spring sobe. Ele usa a configuração normal da aplicação, então precisa do PostgreSQL e do Redis rodando (`docker compose up -d`) e do `application.properties` configurado.

As regras de negócio e os erros foram validados manualmente pela API em execução: cadastro e login, CRUD de todos os recursos, rifa encerrada, limite de cotas, compras simultâneas, consistência do cache e os códigos de erro de cada cenário. Esses cenários ainda não estão automatizados.

## Estrutura do código

- `controller`: endpoints HTTP
- `service`: regras de negócio e transações
- `repository`: persistência com Spring Data JPA (inclui o lock pessimista da compra e a soma de cotas vendidas)
- `model`: entidades JPA
- `dto`: formatos de entrada (`request`) e saída (`response`) da API
- `enums`: `StatusRifa` e `UserRoles`
- `config`: segurança, JWT, Swagger (`OpenApiConfig`) e tratamento global de erros (`ApiExceptionHandler`)

## Limitações e documentação

- Os testes automatizados cobrem só a subida do contexto; as regras de negócio ainda não têm testes de unidade ou integração.
- O projeto ainda não inclui uma coleção Postman/Insomnia.
- Existe um único papel (`ADMIN`), e o cadastro de usuários é público: qualquer pessoa pode criar um operador com acesso total.
- Token ausente ou inválido responde `403` em vez de `401`, com o corpo padrão do Spring.
- Tokens emitidos continuam válidos até expirar, mesmo depois de troca de senha ou exclusão do usuário.
- Criação e exclusão respondem `200 OK`, e não `201 Created` / `204 No Content`.
- Ainda não há migrações versionadas; para produção, adote Flyway ou Liquibase em vez de `ddl-auto=update`.
- As listagens não têm paginação nem filtros (por status de rifa, por exemplo).
- A busca de clientes por nome só encontra o nome exato.

## Sobre mim

Sou Erick Henrique Pereira e estou em busca da minha primeira oportunidade como desenvolvedor backend Java júnior. Neste projeto, venho trabalhando com Java, Spring Boot, criação de APIs REST, Spring Security e JWT, Spring Data JPA, PostgreSQL, Redis, Maven, Docker Compose e documentação com Swagger/OpenAPI.

- LinkedIn: [linkedin.com/in/erickhenriquepereira](https://www.linkedin.com/in/erickhenriquepereira/)
- E-mail: [erickhenriquesax@gmail.com](mailto:erickhenriquesax@gmail.com)
