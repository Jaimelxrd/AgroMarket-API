# AgroMarket API

Para conhecer o problema que o sistema aborda, os utilizadores, os fluxos de negócio, a arquitetura e o âmbito atual, consulte a [documentação completa do projeto](docs/PROJETO.md).

API REST para intermediar a comercialização de produtos agrícolas entre produtores e compradores em Moçambique. Inclui autenticação JWT, catálogo, encomendas, mensagens, moderação e dashboards.

## Requisitos

- Java 21
- Maven
- MySQL (a configuração local padrão é compatível com XAMPP)

## Configuração local

1. Inicie o MySQL e confirme que a porta e as credenciais correspondem às variáveis abaixo. A aplicação cria `agromarket_db` se necessário.
2. No PowerShell, configure uma chave JWT aleatória e inicie a aplicação:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:DB_URL = 'jdbc:mysql://localhost:3306/agromarket_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Africa/Maputo'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = ''
mvn spring-boot:run
```

`DB_URL` deve usar MySQL, que é o driver configurado pelo projeto. Se o ambiente já definir uma URL para outro banco, substitua-a pelo endereço MySQL acima. A chave `JWT_SECRET` deve conter pelo menos 32 bytes aleatórios codificados em Base64; não use uma chave de produção no desenvolvimento.

Variáveis de conexão: `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. O token expira após 15 minutos por padrão; `JWT_EXPIRATION_MINUTES` altera esse prazo. As fotografias são guardadas em `uploads` por padrão (`UPLOAD_DIR` pode alterar o diretório); o limite é 5 MB por ficheiro e 25 MB por pedido.

### Criar o primeiro administrador

O bootstrap administrativo está desativado por padrão. Para a primeira inicialização, defina `ADMIN_BOOTSTRAP_ENABLED=true`, `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PHONE` e `ADMIN_PASSWORD`. A palavra-passe deve ter entre 12 e 72 caracteres. O bootstrap só cria a conta se ainda não existir um administrador. Desative-o depois de criar a conta e proteja as credenciais.

## Autenticação e formato das respostas

Registo e login são públicos. Para rotas protegidas, envie o token do login no cabeçalho `Authorization: Bearer <accessToken>`. O papel da conta determina as operações disponíveis.

Exemplo de registo:

```json
{
  "nomeCompleto": "Nome do Utilizador",
  "email": "utilizador@example.com",
  "numeroTelefone": "+258841234567",
  "palavraPasse": "uma-palavra-passe-segura",
  "tipo": "COMPRADOR"
}
```

As respostas JSON de sucesso usam o envelope:

```json
{
  "success": true,
  "message": "Operação realizada com sucesso.",
  "data": {},
  "timestamp": "2026-11-03T12:00:00+02:00"
}
```

Erros usam `success: false`, `message`, uma lista `errors` e `timestamp`. Respostas `204 No Content` e conteúdo binário das fotografias não têm envelope.

## Documentação interativa

Com a aplicação iniciada:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

A lista das rotas e permissões para desenvolver o frontend está em [rotas.md](rotas.md). A lista de endpoints por módulo está em [docs/ENDPOINTS.md](docs/ENDPOINTS.md).
