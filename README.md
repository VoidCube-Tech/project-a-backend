# Project A — Backend

Backend de uma plataforma multitenant para lojas gerenciarem produtos, promoções e landing pages. A aplicação também oferece autenticação, redirecionamento para WhatsApp, analytics, auditoria e registro manual de vendas com controle de estoque.

Este README contém somente o necessário para instalar, executar e testar o projeto localmente.

## Tecnologias

- Java 21;
- Spring Boot;
- Gradle Wrapper;
- PostgreSQL 16;
- Flyway;
- Docker e Docker Compose.

## O que instalar

Instale:

1. [Git](https://git-scm.com/);
2. [JDK 21](https://adoptium.net/);
3. [Docker Desktop](https://www.docker.com/products/docker-desktop/).

Para trabalhar no código, recomenda-se IntelliJ IDEA. Para testar a API, pode ser usado Insomnia ou Postman.

Não é necessário instalar Gradle: o repositório já possui o Gradle Wrapper.

Confirme as instalações:

```bash
java -version
git --version
docker --version
docker compose version
```

O Java deve estar na versão 21.

## Clonar o projeto

```bash
git clone <URL_DO_REPOSITORIO>
cd <PASTA_DO_PROJETO>
```

Substitua os valores entre `<...>` pelos dados reais do repositório.

## 1. Iniciar o PostgreSQL

Abra o Docker Desktop. Na raiz do projeto, execute:

```bash
docker compose up -d postgres
```

Verifique o container:

```bash
docker compose ps
```

Para consultar os logs:

```bash
docker compose logs -f postgres
```

No ambiente de desenvolvimento atual, o PostgreSQL normalmente utiliza a porta `5433`. Confira o valor final no `compose.yaml` e no profile `dev`.

## 2. Executar o backend

No Windows:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

No Linux ou macOS:

```bash
./gradlew bootRun --args="--spring.profiles.active=dev"
```

Quando a inicialização terminar, a API estará normalmente em:

```text
http://localhost:8080
```

O Flyway cria ou atualiza as tabelas automaticamente durante a inicialização.

### Executar backend e banco pelo Docker

Se o `compose.yaml` também possuir um serviço para o backend:

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f
```

Se existir somente o serviço `postgres`, execute o banco pelo Docker e o backend pelo Gradle Wrapper.

## Configuração local

Os arquivos de configuração ficam em:

```text
src/main/resources/
```

O projeto possui profiles para desenvolvimento e produção. Para executar localmente, utilize `dev`.

Confira principalmente:

- conexão com PostgreSQL;
- URL permitida do frontend;
- configurações de e-mail;
- diretório de imagens.

Não salve senhas ou secrets de produção no Git.

## Primeiro teste da API

A autenticação usa sessão e CSRF. Cookies precisam permanecer habilitados no Insomnia, Postman ou frontend.

### Obter o token CSRF

```http
GET http://localhost:8080/api/v1/auth/csrf
```

Resposta aproximada:

```json
{
  "headerName": "X-XSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "token-gerado"
}
```

Para requisições `POST`, `PUT`, `PATCH` e `DELETE`, envie o token no header indicado:

```http
X-XSRF-TOKEN: token-gerado
```

### Fazer login

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json
X-XSRF-TOKEN: token-gerado
```

```json
{
  "email": "admin@example.com",
  "password": "Senha123!"
}
```

Depois do login ou logout, busque um novo token CSRF porque ele pode ser rotacionado.

## Funcionalidades principais

O backend possui rotas para:

- cadastro, verificação de e-mail, login e logout;
- produtos, imagens, tags e variações;
- promoções;
- landing pages privadas e públicas;
- redirecionamento para WhatsApp;
- analytics;
- dashboard e exportação;
- administração de tenants e auditoria;
- vendas e cancelamento com controle de estoque.

Rotas administrativas exigem sessão autenticada. Operações que alteram dados também exigem CSRF.

## Executar os testes

Todos os testes:

```bash
./gradlew clean test
```

No Windows, também pode ser usado:

```powershell
.\gradlew.bat clean test
```

Uma classe específica:

```bash
./gradlew test --tests "*SaleServiceTest"
```

O relatório HTML é gerado em:

```text
build/reports/tests/test/index.html
```

## Gerar o `.jar`

```bash
./gradlew clean bootJar
```

No Windows:

```powershell
.\gradlew.bat clean bootJar
```

O arquivo será criado em `build/libs/`.

## Acessar o PostgreSQL

```bash
docker compose exec postgres psql -U postgres -d voidcube_dev
```

Comandos úteis dentro do `psql`:

```text
\dt  -> listar tabelas
\q   -> sair
```

## Parar os containers

```bash
docker compose down
```

Esse comando preserva os volumes. Não use `docker compose down -v` se quiser manter os dados, pois `-v` remove os volumes.

## Problemas comuns

### Docker não inicia

Confirme que o Docker Desktop está aberto e execute:

```bash
docker compose ps
docker compose logs postgres
```

### Backend não conecta ao banco

Confira container, porta, nome do banco, usuário, senha e configurações do profile `dev`.

### Porta ocupada

Verifique se outro processo utiliza `8080`, `5432` ou `5433`.

### Resposta `403 Forbidden`

Confira:

- login realizado;
- cookies preservados;
- token CSRF atualizado;
- header `X-XSRF-TOKEN`;
- papel correto do usuário.

### Erro do Flyway

Não edite migrations que já foram aplicadas. Confira versão, nome e checksum antes de reparar o banco.

### `No tests found`

Confira o nome usado no filtro e tente:

```bash
./gradlew clean test
```

## Fluxo recomendado

```text
1. Abrir Docker Desktop
2. Subir PostgreSQL
3. Executar o backend com profile dev
4. Implementar a alteração
5. Executar testes direcionados
6. Executar todos os testes
7. Revisar git diff
8. Criar o commit da task
```

## Produção

As configurações locais não devem ser copiadas diretamente para produção. O ambiente produtivo precisa de HTTPS, cookies seguros, secrets externos, backup, volume persistente para imagens, health checks e CORS configurado para o frontend real.

