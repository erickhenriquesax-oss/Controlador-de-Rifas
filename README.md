# 🎟️ Controlador de Rifas API

Uma API REST completa para **gestão e controle de rifas e sorteios**. Projetada para atuar como backoffice administrativo, onde os administradores e operadores do sistema gerenciam rifas, cadastram compradores (clientes) e registram a aquisição de cotas/números com cálculo automático de valores e cache em memória de alta performance.

---

## 📌 Objetivo da API

O sistema é voltado para a **operação interna e gestão de rifas**:
- **Usuários (Operadores/Admins):** Autenticam-se via JWT para operar o sistema.
- **Clientes (Compradores):** Pessoas físicas cadastradas no sistema que adquirem cotas de sorteios.
- **Rifas:** Cadastro do sorteio, descrição e preço unitário por cota/número.
- **Controle de Compras (`ClienteRifa`):** O operador registra qual cliente adquiriu quantas cotas de determinada rifa. O valor total é calculado e gravado automaticamente com data e hora.
- **Cache com Redis:** Consultas frequentes a rifas ativas são armazenadas em cache no Redis, acelerando o tempo de resposta e reduzindo a carga no PostgreSQL.

---

## 🛠️ Tecnologias Utilizadas

- **Java 25** (Oracle OpenJDK)
- **Spring Boot 4.1.1**
  - Spring Data JPA & Hibernate
  - Spring Security (Autenticação Stateless com JWT)
  - Spring Cache com **Redis**
  - Spring Boot Docker Compose (Gerenciamento automático de contêineres)
  - Bean Validation (`jakarta.validation`)
- **PostgreSQL 18** (Banco de dados relacional em contêiner Docker)
- **Redis 7** (Armazenamento em cache em contêiner Docker)
- **Auth0 Java JWT (v4.4.0)** (Geração e validação de tokens JWT)
- **Lombok** (Produtividade no código boilerplate)
- **Maven 3.9+**

---

## ⚙️ Pré-requisitos

1. **Docker Desktop** instalado e em execução (necessário para subir os contêineres de PostgreSQL e Redis).
2. **Java 25** instalado e configurado no `PATH` (ou configurado na sua IDE, como IntelliJ IDEA).
3. **Maven 3.9+** (ou Maven Wrapper/Bundled compatível com JDK 25).

---

## 🚀 Como Executar o Projeto

### 1. Configurar variáveis de ambiente e propriedades

Copie os arquivos de exemplo para os seus respectivos arquivos reais:

```bash
# Na raiz do projeto:
cp .env.example .env

# Em src/main/resources:
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Ajuste as senhas e credenciais no seu `.env` e `application.properties`:

```env
POSTGRES_DB=Controlador
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha_postgres
REDIS_PASSWORD=sua_senha_redis
API_SECURITY_TOKEN_SECRET=sua_chave_jwt_secreta
```

### 2. Iniciar a Aplicação

Com o **Docker Desktop aberto**, inicie a aplicação pelo terminal ou pela sua IDE (`ControladorApplication.java`):

```bash
mvn spring-boot:run
```

O **Spring Boot Docker Compose** detectará o arquivo `compose.yaml` e subirá automaticamente:
- O contêiner do **PostgreSQL** na porta `5433` (mapeada para a `5432` interna).
- O contêiner do **Redis** na porta `6379`.
- A inicialização automática via `data.sql` garantirá que o cargo padrão `ADMIN` (ID 1) exista na tabela `tb_tipo_usuario`.

A API estará disponível em: `http://localhost:8080`

---

## 🔐 Autenticação e Segurança

A API adota o modelo **Bearer Token (JWT)**:
1. As únicas rotas públicas são:
   - `POST /controlador/usuario` (Cadastro do primeiro operador/admin)
   - `POST /controlador/usuario/login` (Autenticação para obter o Token)
2. Todas as demais rotas requerem o envio do token no cabeçalho HTTP:
   ```http
   Authorization: Bearer <seu_token_jwt_aqui>
   ```

---

## 📖 Rotas e Endpoints da API

### 1. Usuários e Autenticação (`/controlador/usuario`)

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/controlador/usuario` | **Público** | Cadastra um novo usuário/operador |
| `POST` | `/controlador/usuario/login` | **Público** | Realiza login e retorna o Token JWT |
| `GET` | `/controlador/usuario` | Autenticado | Lista todos os usuários cadastrados |
| `GET` | `/controlador/usuario/{id}` | Autenticado | Busca usuário por ID |
| `GET` | `/controlador/usuario/email/{email}` | Autenticado | Busca usuário por email |
| `PUT` | `/controlador/usuario/{id}` | Autenticado | Atualiza os dados de um usuário |
| `DELETE`| `/controlador/usuario/{id}` | Autenticado | Remove um usuário |

#### 📥 Exemplo de Cadastro de Usuário (`POST /controlador/usuario`):
```json
{
  "nome": "Administrador",
  "email": "admin@sorteio.com",
  "senha": "senhaSegura123",
  "idTipoUsuario": 1
}
```

#### 📥 Exemplo de Login (`POST /controlador/usuario/login`):
```json
{
  "email": "admin@sorteio.com",
  "senha": "senhaSegura123"
}
```

#### 📤 Resposta de Sucesso:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 2. Tipos de Usuário / Roles (`/controlador/tipo-usuario`)

> *Nota: O tipo `ADMIN` (ID 1) é inserido automaticamente no banco de dados na inicialização do sistema.*

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/controlador/tipo-usuario` | Autenticado | Lista os tipos de usuário existentes |
| `GET` | `/controlador/tipo-usuario/{id}` | Autenticado | Detalha um tipo de usuário por ID |
| `POST` | `/controlador/tipo-usuario` | Autenticado | Cria um novo tipo de usuário |
| `PUT` | `/controlador/tipo-usuario/{id}` | Autenticado | Atualiza a descrição de um tipo |
| `DELETE`| `/controlador/tipo-usuario/{id}` | Autenticado | Remove um tipo de usuário |

---

### 3. Clientes / Compradores (`/controlador/cliente`)

Gerencia as informações dos participantes das rifas.

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/controlador/cliente` | Autenticado | Cadastra um novo comprador |
| `GET` | `/controlador/cliente` | Autenticado | Lista todos os compradores |
| `GET` | `/controlador/cliente/{id}` | Autenticado | Busca comprador por ID |
| `GET` | `/controlador/cliente/email/{email}` | Autenticado | Busca comprador por e-mail |
| `GET` | `/controlador/cliente/nome/{nome}` | Autenticado | Busca compradores pelo nome |
| `PUT` | `/controlador/cliente/{id}` | Autenticado | Atualiza os dados do comprador |
| `DELETE`| `/controlador/cliente/{id}` | Autenticado | Remove o comprador |

#### 📥 Exemplo de Cadastro de Cliente:
```json
{
  "nome": "João da Silva",
  "email": "joao.silva@email.com",
  "telefone": "11988887777",
  "endereco": "Rua das Flores, 123 - Centro"
}
```

---

### 4. Rifas / Sorteios (`/controlador/rifa`)

Gerencia os sorteios disponíveis no sistema. As consultas são otimizadas via **Redis Cache**.

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/controlador/rifa` | Autenticado | Cria uma nova rifa *(invalida o cache)* |
| `GET` | `/controlador/rifa` | Autenticado | Lista todas as rifas *(cacheado no Redis)* |
| `GET` | `/controlador/rifa/{id}` | Autenticado | Busca rifa por ID |
| `GET` | `/controlador/rifa/descricao/{descricao}`| Autenticado | Filtra rifas por texto da descrição |
| `PUT` | `/controlador/rifa/{id}` | Autenticado | Atualiza uma rifa *(invalida o cache)* |
| `DELETE`| `/controlador/rifa/{id}` | Autenticado | Remove uma rifa *(invalida o cache)* |

#### 📥 Exemplo de Cadastro de Rifa:
```json
{
  "descricao": "Rifa Beneficente - Smartphone de Última Geração",
  "preco": 25.00
}
```

---

### 5. Registro de Compras (`/controlador/cliente-rifa`)

Registra e controla a compra de cotas por parte dos clientes.

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/controlador/cliente-rifa` | Autenticado | Registra a compra de cotas para um cliente |
| `GET` | `/controlador/cliente-rifa` | Autenticado | Lista todos os registros de compra |
| `GET` | `/controlador/cliente-rifa/{id}` | Autenticado | Detalha um registro de compra por ID |
| `GET` | `/controlador/cliente-rifa/cliente/{idCliente}` | Autenticado | Lista todas as compras de um cliente |
| `GET` | `/controlador/cliente-rifa/rifa/{idRifa}` | Autenticado | Lista todas as compras de uma rifa |
| `DELETE`| `/controlador/cliente-rifa/{id}` | Autenticado | Cancela/exclui um registro de compra |

#### 📥 Exemplo de Registro de Compra (`POST /controlador/cliente-rifa`):
```json
{
  "clienteId": 1,
  "rifaId": 1,
  "quantidade": 4
}
```

#### 📤 Exemplo de Resposta (Total calculado automaticamente):
```json
{
  "id": 1,
  "clienteNome": "João da Silva",
  "rifaDescricao": "Rifa Beneficente - Smartphone de Última Geração",
  "precoUnitario": 25.00,
  "quantidade": 4,
  "total": 100.00,
  "dataCompra": "2026-11-02T18:00:00"
}
```

---

## 🗄️ Estrutura do Banco de Dados

- **`tb_tipo_usuario`**: Tabela de perfis (`ADMIN`, etc.).
- **`tb_usuario`**: Tabela de operadores do sistema com senhas criptografadas (BCrypt).
- **`tb_cliente`**: Tabela de compradores dos bilhetes/cotas.
- **`tb_rifa`**: Tabela de rifas ativas com valor unitário.
- **`tb_cliente_rifa`**: Tabela relacional de compras contendo `cliente_id`, `rifa_id`, quantidade, valor total e carimbo de data/hora.
# Controlador-de-Rifas
