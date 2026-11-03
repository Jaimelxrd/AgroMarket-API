# Rotas da API AgroMarket

Base URL local: `http://localhost:8080`.

Para as rotas protegidas, envia o token recebido no login:

```http
Authorization: Bearer <accessToken>
```

Os papéis da API são `COMPRADOR`, `PRODUTOR` e `ADMIN`. “Autenticado” significa qualquer utilizador com sessão JWT válida; algumas operações também verificam se a conta ou o recurso pertence ao utilizador.

## Autenticação e conta

| Método | Endpoint | Permissão |
|---|---|---|
| `POST` | `/api/auth/registo` | Público |
| `POST` | `/api/auth/login` | Público |
| `GET` | `/api/utilizadores/me` | Autenticado |
| `PUT` | `/api/utilizadores/me` | Autenticado |
| `PUT` | `/api/utilizadores/me/palavra-passe` | Autenticado; resposta `204` |
| `DELETE` | `/api/utilizadores/me` | Autenticado; desativa a própria conta, resposta `204` |

## Perfil e dashboard

| Método | Endpoint | Permissão |
|---|---|---|
| `GET` | `/api/produtores/me` | `PRODUTOR` |
| `PUT` | `/api/produtores/me` | `PRODUTOR` |
| `GET` | `/api/produtores/me/dashboard` | `PRODUTOR` |
| `GET` | `/api/compradores/me/dashboard` | `COMPRADOR` |
| `GET` | `/api/admin/dashboard` | `ADMIN` |

## Categorias

| Método | Endpoint | Permissão |
|---|---|---|
| `GET` | `/api/categorias` | Público; lista categorias ativas |
| `GET` | `/api/admin/categorias` | `ADMIN` |
| `POST` | `/api/admin/categorias` | `ADMIN` |
| `PUT` | `/api/admin/categorias/{id}` | `ADMIN` |
| `DELETE` | `/api/admin/categorias/{id}` | `ADMIN`; desativa a categoria, resposta `204` |

## Produtos e fotografias

| Método | Endpoint | Permissão |
|---|---|---|
| `GET` | `/api/produtos` | Público |
| `GET` | `/api/produtos/{id}` | Público; produto disponível |
| `GET` | `/api/produtos/meus` | `PRODUTOR` |
| `POST` | `/api/produtos` | `PRODUTOR` |
| `PUT` | `/api/produtos/{id}` | `PRODUTOR`; apenas produto próprio |
| `DELETE` | `/api/produtos/{id}` | `PRODUTOR`; apenas produto próprio, resposta `204` |
| `POST` | `/api/produtos/{id}/fotografias` | `PRODUTOR`; apenas produto próprio, multipart no campo `ficheiros` |
| `GET` | `/api/produtos/{id}/fotografias/{fotografiaId}` | Público se o produto estiver disponível; proprietário ou `ADMIN` também podem consultar |
| `DELETE` | `/api/produtos/{id}/fotografias/{fotografiaId}` | `PRODUTOR`; apenas fotografia de produto próprio, resposta `204` |
| `GET` | `/api/admin/produtos` | `ADMIN`; moderação, filtro opcional `estado` (padrão `PENDENTE`) |
| `POST` | `/api/admin/produtos/{id}/aprovar` | `ADMIN` |
| `POST` | `/api/admin/produtos/{id}/rejeitar` | `ADMIN` |

A pesquisa `GET /api/produtos` aceita `nome`, `categoriaId`, `provincia`, `distrito`, `precoMinimo`, `precoMaximo`, `produtorId` e `disponivel`. Listas paginadas aceitam `page`, `size` e `sort`.

## Encomendas

| Método | Endpoint | Permissão |
|---|---|---|
| `POST` | `/api/pedidos` | `COMPRADOR` |
| `GET` | `/api/pedidos/meus` | `COMPRADOR`; encomendas próprias |
| `GET` | `/api/pedidos/{id}` | Comprador ou produtor associado à encomenda |
| `POST` | `/api/pedidos/{id}/cancelar` | `COMPRADOR`; apenas encomenda própria pendente |
| `GET` | `/api/encomendas/recebidas` | `PRODUTOR`; encomendas recebidas, filtro opcional `estado` |
| `GET` | `/api/encomendas/pendentes` | `PRODUTOR`; encomendas recebidas pendentes |
| `POST` | `/api/encomendas/{id}/aceitar` | `PRODUTOR`; apenas encomenda recebida própria |
| `POST` | `/api/encomendas/{id}/rejeitar` | `PRODUTOR`; apenas encomenda recebida própria |
| `POST` | `/api/encomendas/{id}/concluir` | `PRODUTOR`; apenas encomenda própria aceite |
| `GET` | `/api/admin/pedidos` | `ADMIN`; filtro opcional `estado` |

As listas de encomendas aceitam paginação com `page`, `size` e `sort`.

## Mensagens

| Método | Endpoint | Permissão |
|---|---|---|
| `POST` | `/api/mensagens` | Autenticado |
| `GET` | `/api/mensagens/conversas` | Autenticado; conversas da própria conta |
| `GET` | `/api/mensagens/conversas/{interlocutorId}` | Autenticado; apenas se existir conversa com esse interlocutor |
| `PATCH` | `/api/mensagens/{mensagemId}/lida` | Autenticado; apenas o destinatário da mensagem, resposta `204` |

## Administração de utilizadores

| Método | Endpoint | Permissão |
|---|---|---|
| `GET` | `/api/admin/utilizadores` | `ADMIN`; filtro opcional `tipo` |
| `PATCH` | `/api/admin/utilizadores/{id}/ativar` | `ADMIN` |
| `PATCH` | `/api/admin/utilizadores/{id}/desativar` | `ADMIN`; resposta `204` |

## Documentação e estado

| Método | Endpoint | Permissão |
|---|---|---|
| `GET` | `/swagger-ui.html` | Público |
| `GET` | `/v3/api-docs` | Público |
| `GET` | `/actuator/health` | Público |

## Formato das respostas e CORS

As respostas JSON de sucesso vêm no envelope `{ success, message, data, timestamp }`. Erros usam `{ success: false, message, errors, timestamp }`. Fotografias são conteúdo binário, e respostas `204` não têm corpo.

O CORS está atualmente configurado para `http://localhost:5500`, `http://127.0.0.1:5500` e `http://localhost:3000`. Se o frontend correr noutra origem ou porta, essa origem terá de ser adicionada à configuração CORS da aplicação.
