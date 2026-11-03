# Diagramas do AgroMarket

Esta página usa blocos Mermaid, que o GitHub renderiza diretamente como diagramas. Os arquivos PlantUML continuam disponíveis como fontes editáveis:

- [Diagrama de classes PlantUML](diagrama-classes.puml)
- [Diagrama de casos de uso PlantUML](diagrama-casos-de-uso.puml)

## Diagrama de classes

```mermaid
classDiagram
    class Utilizador {
        +Long id
        +String nomeCompleto
        +String email
        +String numeroTelefone
        +String palavraPasseHash
        +TipoUtilizador tipo
        +EstadoConta estado
        +LocalDateTime dataRegisto
    }

    class PerfilProdutor {
        +Long id
        +String nomeExploracao
        +String descricao
        +String provincia
        +String distrito
        +String localidade
        +String endereco
        +String tipoProducao
        +String contactoAlternativo
    }

    class Categoria {
        +Long id
        +String nome
        +String descricao
        +EstadoCategoria estado
    }

    class Produto {
        +Long id
        +String nome
        +String descricao
        +BigDecimal precoUnidade
        +UnidadeMedida unidadeMedida
        +BigDecimal quantidadeDisponivel
        +String provincia
        +String distrito
        +String localidade
        +EstadoProduto estado
        +LocalDateTime dataPublicacao
        +Long versao
    }

    class FotografiaProduto {
        +Long id
        +String caminhoFicheiro
        +Integer ordem
    }

    class PedidoCompra {
        +Long id
        +BigDecimal quantidadeSolicitada
        +BigDecimal precoUnitarioAcordado
        +BigDecimal valorTotal
        +String mensagem
        +EstadoPedido estado
        +LocalDateTime dataCriacao
        +LocalDateTime dataAtualizacao
    }

    class Mensagem {
        +Long id
        +String conteudo
        +LocalDateTime dataEnvio
        +boolean lida
    }

    class TipoUtilizador {
        <<enumeration>>
        ADMIN
        PRODUTOR
        COMPRADOR
    }
    class EstadoConta {
        <<enumeration>>
        ATIVA
        INATIVA
    }
    class EstadoCategoria {
        <<enumeration>>
        ATIVA
        INATIVA
    }
    class UnidadeMedida {
        <<enumeration>>
        KG
        TONELADA
        UNIDADE
        SACO
        CAIXA
        LITRO
    }
    class EstadoProduto {
        <<enumeration>>
        DISPONIVEL
        INDISPONIVEL
        ESGOTADO
        PENDENTE
        REJEITADO
    }
    class EstadoPedido {
        <<enumeration>>
        PENDENTE
        ACEITE
        REJEITADO
        CANCELADO
        CONCLUIDO
    }

    Utilizador "1" --> "0..1" PerfilProdutor : perfil
    Utilizador "1" --> "0..*" Produto : produtor publica
    Categoria "1" --> "0..*" Produto : categoria classifica
    Produto "1" --> "0..*" FotografiaProduto : fotografias
    Produto "1" --> "0..*" PedidoCompra : produto solicitado
    Utilizador "1" --> "0..*" PedidoCompra : comprador cria
    Utilizador "1" --> "0..*" PedidoCompra : produtor recebe
    Utilizador "1" --> "0..*" Mensagem : remetente envia
    Utilizador "1" --> "0..*" Mensagem : destinatario recebe
    Utilizador --> TipoUtilizador : tipo
    Utilizador --> EstadoConta : estado
    Categoria --> EstadoCategoria : estado
    Produto --> EstadoProduto : estado
    Produto --> UnidadeMedida : unidade
    PedidoCompra --> EstadoPedido : estado
```

## Diagrama de casos de uso

```mermaid
flowchart LR
    Visitante([Visitante])
    Comprador([Comprador])
    Produtor([Produtor])
    Admin([Administrador])

    subgraph Sistema["Sistema AgroMarket"]
        UC_Categorias((Consultar categorias))
        UC_Pesquisar((Pesquisar produtos))
        UC_Detalhes((Consultar produto e fotografias públicas))
        UC_Registar((Registar conta))
        UC_Login((Iniciar sessão))

        UC_Conta((Gerir dados pessoais))
        UC_Senha((Alterar palavra-passe))
        UC_Desativar((Desativar própria conta))
        UC_MensagemEnviar((Enviar mensagens))
        UC_Conversas((Consultar conversas))
        UC_Lida((Marcar mensagem como lida))

        UC_DashComprador((Consultar dashboard de comprador))
        UC_CriarPedido((Criar pedido de compra))
        UC_Acompanhar((Consultar pedidos próprios))
        UC_Cancelar((Cancelar pedido pendente))

        UC_DashProdutor((Consultar dashboard de produtor))
        UC_PerfilProdutor((Gerir perfil de produtor))
        UC_Produtos((Gerir produtos e fotografias próprias))
        UC_Recebidos((Consultar pedidos recebidos))
        UC_AceitarRejeitar((Aceitar ou rejeitar pedido))
        UC_Concluir((Concluir pedido aceite))

        UC_DashAdmin((Consultar dashboard administrativo))
        UC_CategoriasAdmin((Gerir categorias))
        UC_Moderar((Aprovar ou rejeitar produtos))
        UC_Utilizadores((Ativar ou desativar contas))
        UC_PedidosAdmin((Consultar pedidos da plataforma))
    end

    Visitante --> UC_Categorias
    Visitante --> UC_Pesquisar
    Visitante --> UC_Detalhes
    Visitante --> UC_Registar
    Visitante --> UC_Login

    Comprador --> UC_Conta
    Comprador --> UC_Senha
    Comprador --> UC_Desativar
    Comprador --> UC_MensagemEnviar
    Comprador --> UC_Conversas
    Comprador --> UC_Lida
    Comprador --> UC_DashComprador
    Comprador --> UC_CriarPedido
    Comprador --> UC_Acompanhar
    Comprador --> UC_Cancelar

    Produtor --> UC_Conta
    Produtor --> UC_Senha
    Produtor --> UC_Desativar
    Produtor --> UC_MensagemEnviar
    Produtor --> UC_Conversas
    Produtor --> UC_Lida
    Produtor --> UC_DashProdutor
    Produtor --> UC_PerfilProdutor
    Produtor --> UC_Produtos
    Produtor --> UC_Recebidos
    Produtor --> UC_AceitarRejeitar
    Produtor --> UC_Concluir

    Admin --> UC_Conta
    Admin --> UC_Senha
    Admin --> UC_Desativar
    Admin --> UC_MensagemEnviar
    Admin --> UC_Conversas
    Admin --> UC_Lida
    Admin --> UC_DashAdmin
    Admin --> UC_CategoriasAdmin
    Admin --> UC_Moderar
    Admin --> UC_Utilizadores
    Admin --> UC_PedidosAdmin
```

As operações autenticadas exigem um token JWT. As permissões também dependem do papel e da propriedade do recurso; consulte [rotas.md](../rotas.md) para as regras por endpoint.
