# Endpoints da API AgroMarket

Base URL local: `http://localhost:8080`. As rotas protegidas requerem `Authorization: Bearer <accessToken>`. Salvo indicação, respostas de sucesso contêm o resultado em `data`; `204` não contém corpo. Os nomes de papéis são `COMPRADOR`, `PRODUTOR` e `ADMIN`.

## Autenticação e conta

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/registo` | Público | Regista comprador ou produtor (`tipo`: `COMPRADOR` ou `PRODUTOR`). |
| POST | `/api/auth/login` | Público | Autentica por email e palavra-passe; devolve token JWT. |
| GET | `/api/utilizadores/me` | Autenticado | Consulta o perfil da conta atual. |
| PUT | `/api/utilizadores/me` | Autenticado | Atualiza os dados pessoais permitidos. |
| PUT | `/api/utilizadores/me/palavra-passe` | Autenticado | Altera a palavra-passe; resposta `204`. |
| DELETE | `/api/utilizadores/me` | Autenticado | Desativa a conta atual; resposta `204`. |

## Perfil de produtor

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/produtores/me` | PRODUTOR | Consulta o perfil de produtor. |
| PUT | `/api/produtores/me` | PRODUTOR | Cria ou atualiza o perfil de produtor. |
| GET | `/api/produtores/me/dashboard` | PRODUTOR | Consulta indicadores do produtor autenticado. |

## Categorias

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/categorias` | Público | Lista categorias ativas. |
| GET | `/api/admin/categorias` | ADMIN | Lista todas as categorias. |
| POST | `/api/admin/categorias` | ADMIN | Cria uma categoria. |
| PUT | `/api/admin/categorias/{id}` | ADMIN | Atualiza uma categoria. |
| DELETE | `/api/admin/categorias/{id}` | ADMIN | Desativa uma categoria; resposta `204`. |

## Produtos e fotografias

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/produtos` | Público | Pesquisa produtos públicos. |
| GET | `/api/produtos/{id}` | Público | Consulta um produto disponível. |
| GET | `/api/produtos/meus` | PRODUTOR | Lista os produtos do produtor autenticado. |
| POST | `/api/produtos` | PRODUTOR | Cria um produto. |
| PUT | `/api/produtos/{id}` | PRODUTOR | Atualiza um produto próprio. |
| DELETE | `/api/produtos/{id}` | PRODUTOR | Elimina um produto próprio; resposta `204`. |
| POST | `/api/produtos/{id}/fotografias` | PRODUTOR | Envia fotografias multipart no campo `ficheiros`. |
| GET | `/api/produtos/{id}/fotografias/{fotografiaId}` | Público ou proprietário, conforme estado do produto | Obtém o conteúdo binário da fotografia. |
| DELETE | `/api/produtos/{id}/fotografias/{fotografiaId}` | PRODUTOR proprietário | Elimina uma fotografia; resposta `204`. |
| GET | `/api/admin/produtos?estado=PENDENTE` | ADMIN | Lista produtos para moderação; o estado padrão é `PENDENTE`. |
| POST | `/api/admin/produtos/{id}/aprovar` | ADMIN | Aprova um produto. |
| POST | `/api/admin/produtos/{id}/rejeitar` | ADMIN | Rejeita um produto. |

Parâmetros opcionais de pesquisa: `nome`, `categoriaId`, `provincia`, `distrito`, `precoMinimo`, `precoMaximo`, `produtorId` e `disponivel`. A paginação usa `page`, `size` e `sort`; o padrão é 20 resultados, ordenados por `dataPublicacao` descendente. Produtos novos ou editados ficam sujeitos à moderação antes de aparecerem no catálogo público.

## Encomendas

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/pedidos` | COMPRADOR | Cria uma encomenda. |
| GET | `/api/pedidos/meus` | COMPRADOR | Lista as encomendas do comprador autenticado. |
| GET | `/api/pedidos/{id}` | Participante da encomenda | Consulta uma encomenda acessível à conta atual. |
| POST | `/api/pedidos/{id}/cancelar` | COMPRADOR | Cancela uma encomenda permitida pelas regras de estado. |
| GET | `/api/encomendas/recebidas` | PRODUTOR | Lista encomendas recebidas; aceita filtro `estado`. |
| GET | `/api/encomendas/pendentes` | PRODUTOR | Lista encomendas pendentes. |
| POST | `/api/encomendas/{id}/aceitar` | PRODUTOR | Aceita uma encomenda recebida. |
| POST | `/api/encomendas/{id}/rejeitar` | PRODUTOR | Rejeita uma encomenda recebida. |
| POST | `/api/encomendas/{id}/concluir` | PRODUTOR | Marca uma encomenda como concluída. |
| GET | `/api/admin/pedidos` | ADMIN | Lista encomendas; aceita filtro opcional `estado`. |

As listas paginadas aceitam `page`, `size` e `sort`.

## Mensagens

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/mensagens` | Autenticado | Envia uma mensagem. |
| GET | `/api/mensagens/conversas` | Autenticado | Lista interlocutores com conversas. |
| GET | `/api/mensagens/conversas/{interlocutorId}` | Participante da conversa | Lista mensagens com o interlocutor; aceita paginação. |
| PATCH | `/api/mensagens/{mensagemId}/lida` | Destinatário | Marca uma mensagem como lida; resposta `204`. |

## Dashboards

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/admin/dashboard` | ADMIN | Indicadores gerais da plataforma. |
| GET | `/api/produtores/me/dashboard` | PRODUTOR | Indicadores do produtor atual. |
| GET | `/api/compradores/me/dashboard` | COMPRADOR | Indicadores do comprador atual. |

## Administração de utilizadores

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/admin/utilizadores` | ADMIN | Lista utilizadores; filtro opcional `tipo`, paginação disponível. |
| PATCH | `/api/admin/utilizadores/{id}/ativar` | ADMIN | Ativa uma conta. |
| PATCH | `/api/admin/utilizadores/{id}/desativar` | ADMIN | Desativa uma conta; resposta `204`. |

## Documentação e estado

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/swagger-ui.html` | Público | Interface Swagger UI. |
| GET | `/v3/api-docs` | Público | Especificação OpenAPI em JSON. |
| GET | `/actuator/health` | Público | Estado de saúde da aplicação. |
