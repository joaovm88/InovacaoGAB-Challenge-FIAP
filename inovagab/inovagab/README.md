# InovaGAB API — Backend (Sprint 2)

Backend RESTful do Challenge FIAP — Grupo Águia Branca, construído com **Java 17 + Spring Boot 3**, persistência em **MongoDB**, autenticação **JWT** e integração com **IA (Google Gemini)** para pontuação automática de ideias de inovação.

## Stack

- Java 17
- Spring Boot 3.3.4 (Web, Security, Data MongoDB, Validation)
- MongoDB (Atlas ou local)
- JWT (java-jwt / Auth0)
- Lombok
- JUnit 5 + Mockito

## Pré-requisitos

- JDK 17+
- Maven 3.9+ (ou usar o wrapper `./mvnw`)
- Uma instância MongoDB acessível (Atlas ou local)

## Configuração

As configurações sensíveis são lidas de variáveis de ambiente, com valores padrão de desenvolvimento definidos em `src/main/resources/application.properties`:

| Variável | Descrição | Padrão (dev) |
|---|---|---|
| `MONGODB_URI` | String de conexão do MongoDB | cluster de exemplo (substitua pelo seu) |
| `JWT_SECRET` | Chave usada para assinar os tokens JWT | chave de desenvolvimento |
| `GEMINI_API_KEY` | Chave da API do Google Gemini (recurso de IA) | vazio (desativa a IA, aplica pontuação padrão) |
| `GEMINI_API_URL` | Endpoint do modelo Gemini | `gemini-1.5-flash:generateContent` |

Para rodar localmente, defina as variáveis antes de subir a aplicação:

```bash
export MONGODB_URI="mongodb+srv://usuario:senha@seu-cluster.mongodb.net/inovagab_db?retryWrites=true&w=majority"
export JWT_SECRET="uma-chave-secreta-bem-grande"
export GEMINI_API_KEY="sua-chave-da-api-gemini"   # opcional
```

> **Nunca** faça commit de credenciais reais. Os valores em `application.properties` são apenas placeholders de exemplo.

## Como executar

```bash
cd inovagab/inovagab

# Rodar os testes
./mvnw test

# Subir a aplicação
./mvnw spring-boot:run

# Ou gerar o executável e rodar o jar
./mvnw clean package
java -jar target/inovagab-0.0.1-SNAPSHOT.jar
```

A API sobe em `http://localhost:8080`.

## Perfis de acesso (roles)

| Perfil | Descrição |
|---|---|
| `OPERADOR` | Cadastra e consulta suas próprias ideias de inovação |
| `GESTOR` | Avalia/prioriza ideias e gerencia (CRUD) os projetos/iniciativas |
| `LIDER` | Gerencia (CRUD) as orientações estratégicas, consulta projetos e acessa o dashboard executivo |

A autorização é feita via JWT (header `Authorization: Bearer <token>`), com roles mapeadas para `ROLE_OPERADOR`, `ROLE_GESTOR` e `ROLE_LIDER`.

## Endpoints

### Autenticação (`/api/auth`) — públicos

| Método | Rota | Payload | Resposta |
|---|---|---|---|
| POST | `/api/auth/registro` | `{ "nome", "email", "senha", "nivelAcesso": "OPERADOR\|GESTOR\|LIDER" }` | `201` Usuário criado / `409` e-mail já existe |
| POST | `/api/auth/login` | `{ "email", "senha" }` | `200 { "token", "tipo": "Bearer", "nivelAcesso" }` |

### Estratégias (`/api/estrategias`) — `LIDER` gerencia (CRUD), demais perfis consultam

| Método | Rota | Acesso | Payload | Resposta |
|---|---|---|---|---|
| POST | `/api/estrategias` | LIDER | `{ "categoria", "campanha", "descricao" }` | `201` Estratégia criada |
| GET | `/api/estrategias` | Todos autenticados | — | `200` Lista das estratégias ativas |
| GET | `/api/estrategias/todas` | LIDER | — | `200` Histórico completo |
| PUT | `/api/estrategias/{id}` | LIDER | `{ "categoria", "campanha", "descricao" }` | `200` Estratégia atualizada / `404` |
| PATCH | `/api/estrategias/{id}/status?ativa=true\|false` | LIDER | — | `200` Estratégia atualizada / `404` |
| DELETE | `/api/estrategias/{id}` | LIDER | — | `204` Removida / `404` |

### Ideias de inovação (`/api/ideias`) — CRUD do próprio autor; avaliação por Gestor/Líder

| Método | Rota | Acesso | Payload | Resposta |
|---|---|---|---|---|
| POST | `/api/ideias` | OPERADOR, GESTOR, LIDER | `{ "titulo", "descricao", "setor", "autorId", "estrategiaId" }` | `201` Ideia criada (já com pontuação de IA) |
| GET | `/api/ideias/autor/{autorId}` | Autenticado | — | `200` Ideias do autor |
| GET | `/api/ideias/pendentes` | Autenticado | — | `200` Ideias pendentes de avaliação |
| PUT | `/api/ideias/{id}` | Autor da ideia, GESTOR ou LIDER | `{ "titulo", "descricao", "setor" }` | `200` Ideia atualizada / `403` / `404` |
| DELETE | `/api/ideias/{id}` | Autor da ideia, GESTOR ou LIDER | — | `204` Removida / `403` / `404` |
| PATCH | `/api/ideias/{id}/avaliacao?status=APROVADA\|ARQUIVADA&parecer=...` | GESTOR, LIDER | — | `200` Ideia avaliada / `404` |

### Projetos (`/api/projetos`) — `GESTOR` gerencia (CRUD), `LIDER` consulta

| Método | Rota | Acesso | Payload | Resposta |
|---|---|---|---|---|
| POST | `/api/projetos` | GESTOR | `{ "nome", "descricao", "etapa", "divisao", "ideiaOrigemId", "estrategiaId", "investimento", ... }` | `201` Projeto criado / `400` investimento inválido |
| GET | `/api/projetos` | GESTOR, LIDER | — | `200` Lista de projetos |
| GET | `/api/projetos/{id}` | GESTOR, LIDER | — | `200` Projeto / `404` |
| PUT | `/api/projetos/{id}` | GESTOR | `{ "nome", "descricao", "divisao", "estrategiaId", "ideiaOrigemId", "investimento", ... }` | `200` Projeto atualizado / `404` |
| PUT | `/api/projetos/{id}/progresso?fase=...&retornoFinanceiro=...&finalizado=...` | GESTOR | — | `200` Projeto atualizado / `404` |
| DELETE | `/api/projetos/{id}` | GESTOR | — | `204` Removido / `404` |

### Dashboard (`/api/dashboard`) — apenas `LIDER`

| Método | Rota | Resposta |
|---|---|---|
| GET | `/api/dashboard/resultados` | `200` Resumo executivo: investimento total, retorno total, lucro líquido, ROI %, redução de custos, CO₂ evitado e água poupada |

Os totais são calculados no próprio MongoDB via **Aggregation Pipeline** (`$group`
com `$sum`/`$count` sobre a coleção `projetos`), em vez de carregar todos os
documentos para a memória do backend — ver `ProjetoService.consolidarResultadosGlobais()`.

## Diferencial de IA

O cadastro de ideias (`POST /api/ideias`) é enriquecido automaticamente pelo `GeminiService`, que envia título, setor, descrição e a estratégia vigente para a **Google Gemini API** e recebe de volta uma pontuação (0–100) e uma justificativa textual, auxiliando gestores a priorizar as melhores propostas. Caso a chave `GEMINI_API_KEY` não esteja configurada ou a API externa esteja indisponível, o serviço aplica uma pontuação padrão (fallback), sem bloquear o cadastro da ideia.

## Índices do MongoDB

- `usuarios.email` — índice único (`@Indexed(unique = true)`), usado no login.
- `ideias.{status, autorId}` — índice composto (`@CompoundIndex`), acelera as
  consultas mais frequentes da aplicação: fila de pendentes do Gestor
  (`findByStatus`) e histórico de ideias do próprio operador (`findByAutorId`).

## Estrutura do projeto

```
src/main/java/br/com/fiap/inovagab/
├── controller/   # Endpoints REST
├── service/      # Regras de negócio e integração com IA
├── security/     # JWT, filtro de autenticação e configuração de acesso por role
├── repository/   # Interfaces Spring Data MongoDB
├── model/        # Documentos MongoDB (entidades)
└── dto/          # Objetos de transferência de dados
```

## Testes

```bash
./mvnw test
```

Cobre autenticação, geração de token, regras de negócio de projetos (validação de investimento) e o serviço de IA (fallback sem chave configurada).
