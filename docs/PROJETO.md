# AgroMarket: visão e documentação do projeto

## 1. Resumo

AgroMarket é uma API REST para apoiar a ligação digital entre produtores agrícolas e compradores em Moçambique. A aplicação oferece um catálogo de produtos agrícolas, perfis de produtor, pedidos de compra, mensagens entre utilizadores, moderação administrativa e painéis com indicadores.

Este repositório contém o backend. Um frontend pode consumir os endpoints documentados em [rotas.md](../rotas.md) e usar a autenticação JWT para adaptar a interface ao papel da conta.

## 2. Problema que o projeto pretende resolver

Produtores e compradores podem ter dificuldade em encontrar-se, conhecer a oferta disponível e iniciar uma negociação com informação suficiente sobre o produto e a sua origem. Quando a divulgação e o contacto dependem apenas de canais dispersos, a descoberta de produtos, a comparação de ofertas e o acompanhamento de pedidos tornam-se menos organizados.

O AgroMarket responde a esse problema com um ponto digital comum onde:

- produtores publicam os produtos que têm disponíveis, com preço, quantidade, unidade de medida e localização;
- compradores pesquisam a oferta por nome, categoria, localidade e intervalo de preço;
- pedidos de compra são registados com quantidade, preço acordado e estado;
- comprador e produtor podem trocar mensagens dentro da plataforma;
- administradores gerem categorias, contas e aprovação de anúncios.

O objetivo do sistema é facilitar a descoberta e a comunicação comercial e organizar os pedidos. A aplicação não processa pagamentos, não gere transporte ou entrega e não substitui os acordos feitos entre comprador e produtor.

## 3. Objetivos

1. Disponibilizar um catálogo pesquisável de produtos agrícolas.
2. Permitir que produtores mantenham um perfil e publiquem a sua oferta.
3. Dar aos compradores uma forma estruturada de solicitar produtos.
4. Acompanhar os estados dos pedidos e atualizar a quantidade disponível quando um pedido é aceite.
5. Permitir comunicação direta através de mensagens associadas às contas.
6. Dar aos administradores ferramentas de moderação e gestão da plataforma.
7. Proteger operações e dados através de autenticação, autorização por papel e validação de entradas.

## 4. Utilizadores e permissões

### Visitante

Sem iniciar sessão, pode consultar categorias ativas, pesquisar produtos disponíveis, consultar detalhes públicos e fotografias de produtos disponíveis e aceder à documentação da API.

### Comprador (`COMPRADOR`)

Pode gerir os próprios dados, procurar produtos, criar e consultar os seus pedidos, cancelar pedidos pendentes, conversar com outros utilizadores e consultar o dashboard de comprador.

### Produtor (`PRODUTOR`)

Pode gerir o perfil de produtor, criar e atualizar os próprios produtos, enviar fotografias, consultar os pedidos que recebeu, aceitar ou rejeitar pedidos pendentes, concluir pedidos aceites, comunicar com outros utilizadores e consultar o dashboard de produtor.

Produtos criados ou editados ficam pendentes de aprovação administrativa antes de serem apresentados como disponíveis no catálogo público.

### Administrador (`ADMIN`)

Pode moderar produtos, gerir categorias, ativar ou desativar contas, consultar pedidos e aceder ao dashboard administrativo. O primeiro administrador pode ser criado pelo bootstrap opcional da aplicação.

As rotas completas e a autorização de cada operação estão em [rotas.md](../rotas.md). As permissões também dependem da propriedade do recurso: por exemplo, um produtor só altera os próprios produtos, e apenas os participantes consultam os detalhes de um pedido.

## 5. Fluxos principais

### 5.1 Registo e acesso

1. O utilizador cria uma conta como `COMPRADOR` ou `PRODUTOR`.
2. Envia email e palavra-passe para o endpoint de login.
3. A API devolve um token JWT e os dados básicos da conta.
4. O frontend envia o token como `Authorization: Bearer <accessToken>` nas chamadas protegidas.

As palavras-passe são guardadas com hash BCrypt. O token tem validade configurável, de 15 minutos por padrão.

### 5.2 Publicação e descoberta de produtos

1. O produtor pode preencher o perfil da exploração e publica um produto com categoria, preço por unidade, quantidade, localização e descrição.
2. O anúncio entra no estado `PENDENTE`.
3. Um administrador aprova ou rejeita o anúncio.
4. Produtos aprovados com quantidade positiva passam a `DISPONIVEL` e podem aparecer nas pesquisas públicas.
5. O produtor pode enviar até cinco fotografias JPEG, PNG ou WebP por produto. Cada ficheiro pode ter até 5 MB.
6. Quando um produtor edita um produto, ele volta a `PENDENTE` para nova moderação.

### 5.3 Pedido de compra

1. Um comprador solicita uma quantidade de um produto disponível.
2. A API regista o preço unitário e o valor total no momento do pedido, mantendo um registo do preço usado nessa solicitação.
3. O pedido inicia em `PENDENTE` e fica visível ao produtor associado.
4. O produtor aceita ou rejeita o pedido. Só pode concluir um pedido aceite.
5. Ao aceitar, a API reduz a quantidade disponível do produto; se chegar a zero, o produto passa a `ESGOTADO`.
6. O comprador pode cancelar um pedido enquanto ainda estiver pendente.

Estados implementados: `PENDENTE`, `ACEITE`, `REJEITADO`, `CANCELADO` e `CONCLUIDO`.

### 5.4 Mensagens

Utilizadores autenticados podem enviar mensagens. A API organiza mensagens em conversas entre remetente e destinatário, apresenta interlocutores e permite ao destinatário marcar uma mensagem como lida.

## 6. Funcionalidades incluídas

- Registo, login e gestão da própria conta.
- Autorização por papel e validação de propriedade dos recursos.
- Perfil do produtor com localização e dados da exploração.
- Gestão de categorias por administradores.
- Catálogo público com filtros e paginação.
- Anúncios moderados e gestão de fotografias.
- Criação e acompanhamento de pedidos de compra.
- Mensagens e conversas entre utilizadores.
- Dashboards para administrador, produtor e comprador.
- Envelope comum para respostas JSON, erros tratados centralmente, Swagger/OpenAPI e health check.

## 7. Fora do âmbito implementado

O backend atual não integra pagamentos, carteira ou transferência de dinheiro; logística, cálculo de transporte ou confirmação de entrega; avaliações e reputação; notificações por SMS, email ou push; nem chat em tempo real. O fluxo de mensagens é baseado em pedidos HTTP. Estas funcionalidades podem ser consideradas em fases futuras se fizerem parte dos requisitos do produto.

## 8. Modelo de domínio

| Entidade | Responsabilidade |
|---|---|
| `Utilizador` | Identidade, email, telefone, hash da palavra-passe, papel e estado da conta. |
| `PerfilProdutor` | Informações da exploração, produção e localização associadas a um produtor. |
| `Categoria` | Agrupamento de produtos, com estado ativo ou inativo. |
| `Produto` | Anúncio, preço, unidade, quantidade, localização, categoria, produtor e estado de moderação/disponibilidade. |
| `FotografiaProduto` | Metadados e caminho do ficheiro de imagem de um produto. |
| `PedidoCompra` | Comprador, produtor, produto, quantidade, preço registado, total, estado e datas. |
| `Mensagem` | Remetente, destinatário, conteúdo, data de envio e estado de leitura. |

As entidades persistentes são geridas por JPA/Hibernate e armazenadas no MySQL. Fotografias são guardadas no sistema de ficheiros local, por padrão no diretório `uploads`, enquanto a base de dados mantém os seus metadados e caminho.

## 9. Arquitetura do backend

A aplicação segue uma organização por camadas dentro do pacote `mz.co.agromarket`:

| Pacote | Função |
|---|---|
| `controller` | Rotas HTTP, leitura dos parâmetros e delegação das operações. |
| `dto.request` | Formatos recebidos pela API e respetivas validações. |
| `dto.response` | Formatos devolvidos ao frontend, incluindo páginas, envelopes e dashboards. |
| `service` | Regras de negócio, validação de permissões sobre recursos e transações. |
| `repository` | Acesso e consultas à base de dados através do Spring Data JPA. |
| `model.entity` | Entidades persistidas. |
| `model.enums` | Papéis e estados usados no domínio. |
| `security` | Geração/validação de JWT, carregamento de utilizadores e erros de segurança. |
| `config` | Configuração do Spring Security, CORS, envelope HTTP e criação opcional do administrador. |
| `exception` | Conversão de exceções em respostas de erro consistentes. |

Fluxo típico: `HTTP → Controller → Service → Repository/armazenamento → Service → DTO de resposta`.

## 10. Tecnologias

- Java 21 como versão alvo definida no Maven.
- Spring Boot 4 para arranque, configuração e ciclo de vida da aplicação.
- Spring MVC para API REST.
- Spring Security e JWT para autenticação e autorização.
- Jakarta Bean Validation para validação de pedidos.
- Spring Data JPA e Hibernate para persistência relacional.
- MySQL como base de dados.
- Springdoc OpenAPI para Swagger UI e especificação OpenAPI.
- Spring Boot Actuator para health check.
- Maven para dependências e ciclo de build.

## 11. Contrato HTTP

Respostas JSON bem-sucedidas usam o formato `{ success, message, data, timestamp }`. Respostas de erro usam `{ success: false, message, errors, timestamp }`. O timestamp é produzido com o fuso `Africa/Maputo`.

Respostas `204 No Content` não têm corpo. O endpoint de fotografia devolve conteúdo binário, não um envelope JSON. Listas paginadas usam o DTO `PaginaResponse`.

Consulte [rotas.md](../rotas.md) para métodos, parâmetros e permissões. Com a aplicação em execução, a especificação também pode ser consultada em `/v3/api-docs` e a interface em `/swagger-ui.html`.

## 12. Configuração e execução local

### Requisitos

- JDK 21 ou superior compatível com a configuração do projeto.
- Maven.
- MySQL local, por exemplo XAMPP com MySQL iniciado.

### Variáveis principais

| Variável | Uso | Padrão |
|---|---|---|
| `DB_URL` | URL JDBC do MySQL | `jdbc:mysql://localhost:3306/agromarket_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Africa/Maputo` |
| `DB_USERNAME` | Utilizador MySQL | `root` |
| `DB_PASSWORD` | Palavra-passe MySQL | vazia |
| `JWT_SECRET` | Chave Base64 com pelo menos 32 bytes aleatórios | sem valor; deve ser definida |
| `JWT_EXPIRATION_MINUTES` | Duração do JWT em minutos | `15` |
| `UPLOAD_DIR` | Diretório das fotografias | `uploads` |
| `ADMIN_BOOTSTRAP_ENABLED` | Ativa criação opcional do primeiro administrador | `false` |
| `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PHONE`, `ADMIN_PASSWORD` | Dados necessários para o bootstrap administrativo | vazios |

### Iniciar no PowerShell

Na raiz do projeto, inicie o MySQL e execute:

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

O Hibernate está configurado com `ddl-auto=update`, que atualiza o esquema durante o desenvolvimento. Para produção, planeie migrações de base de dados explícitas e configure credenciais próprias, uma chave JWT persistente e segura, armazenamento durável de fotografias e HTTPS.

### Bootstrap administrativo

O bootstrap está desativado por padrão. Para criar o primeiro administrador, defina `ADMIN_BOOTSTRAP_ENABLED=true` e os campos `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PHONE` e `ADMIN_PASSWORD`. A palavra-passe deve ter entre 12 e 72 caracteres e o telefone deve obedecer ao formato moçambicano validado pela aplicação. Se já existir um administrador, o bootstrap não cria outro. Desative a opção depois da criação.

## 13. Upload e armazenamento de fotografias

São aceites ficheiros JPEG, PNG e WebP, validados pelo tipo e pela assinatura do conteúdo. O limite configurado no serviço é de cinco fotografias por produto, 5 MB por fotografia e 25 MB por pedido multipart. O diretório de armazenamento é `UPLOAD_DIR` (padrão `uploads`). Em produção, o diretório precisa de persistência, cópias de segurança e permissões apropriadas.

## 14. Segurança e validação

- As rotas públicas estão limitadas ao registo, login, consulta pública do catálogo/categorias, documentação e health check.
- As rotas restantes exigem JWT válido.
- `@PreAuthorize` e as regras dos serviços limitam operações por papel e propriedade do recurso.
- Contas desativadas não devem continuar a usar tokens; a validação do JWT consulta o estado da conta.
- A API valida os DTOs e devolve erros uniformes para dados inválidos, falta de autenticação, falta de permissão e conflitos.
- O CORS está limitado às origens locais atualmente configuradas no `SecurityConfig`; a origem do frontend precisa constar nessa lista.

## 15. Documentos relacionados

- [Rotas e permissões para o frontend](../rotas.md)
- [Lista de endpoints por módulo](ENDPOINTS.md)
- [Diagramas visíveis no GitHub (Mermaid)](DIAGRAMAS.md)
- [Diagrama de classes (PlantUML)](diagrama-classes.puml)
- [Diagrama de casos de uso (PlantUML)](diagrama-casos-de-uso.puml)
- [README e arranque rápido](../README.md)

Os diagramas `.puml` podem ser visualizados num editor com suporte PlantUML ou convertidos para SVG/PNG com PlantUML. Foram modelados a partir das entidades e permissões implementadas no backend.

## 16. Testes e validação

Execute a suite com `mvn test`. Os testes unitários cobrem registo e autenticação, publicação e moderação de produtos, filtros e limite de paginação, criação de pedidos, cálculo do preço registado, permissões sobre pedidos, alterações de estado e consistência do stock. O teste `AgroMarketApplicationTests` verifica o arranque integrado da aplicação.

O teste de contexto usa a configuração normal da aplicação: precisa de MySQL acessível e de `JWT_SECRET` configurada. Os testes unitários de serviço usam mocks e não precisam de conexão à base de dados.
