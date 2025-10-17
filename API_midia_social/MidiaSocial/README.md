# Sistema de Integração de APIs de Mídia Social com Padrão Adapter

## Objetivo

Este projeto implementa um sistema unificado para gerenciamento de publicações em múltiplas redes sociais (Twitter, LinkedIn, Instagram), utilizando o Padrão de Projeto Adapter. O objetivo é criar uma arquitetura flexível e desacoplada que permita ao sistema cliente interagir com diferentes APIs através de uma única interface comum, sem conhecer as complexidades e diferenças de cada uma.

## Arquitetura e Padrões de Projeto

A solução foi construída em torno dos seguintes conceitos e padrões:

1.  Adapter Pattern: É o coração do projeto. Para cada API externa (`TwitterAPI`, `LinkedInAPI`), foi criado um `Adapter` (`TwitterAdapter`, `LinkedInAdapter`) que "traduz" as chamadas da nossa interface unificada (`ISocialMediaAdapter`) para os métodos específicos da API correspondente. Foi utilizada composição sobre herança, onde cada adapter contém uma instância da API que ele adapta.

2.  Factory Method Pattern: A classe `SocialMediaFactory` é responsável por criar as instâncias corretas dos adaptadores. Isso desacopla o cliente (`GerenciadorMidiaSocial`) da lógica de instanciação. O cliente simplesmente solicita um adaptador para uma `Plataforma` (e.g., `TWITTER`) e a fábrica retorna o objeto apropriado.

3.  Interface Unificada (Target): A interface `ISocialMediaAdapter` define o contrato que o nosso sistema espera. Ela contém métodos genéricos como `publicar(Conteudo)` e `autenticar()`, que são implementados por todos os adaptadores.

4.  Sistema de Resposta Unificado: A classe `RespostaAPI<T>` padroniza todas as respostas do sistema. Usando Generics, ela pode encapsular qualquer tipo de dado de retorno e sempre informa o status (`sucesso`/`falha`) e uma mensagem, facilitando o tratamento de erros de forma granular e consistente.

## Estrutura do Projeto

O código está organizado da seguinte forma:

-   APIs Originais (Adaptees): `TwitterAPI`, `LinkedInAPI`, `InstagramAPI`.
    -   Simulam as APIs externas com interfaces incompatíveis entre si. Elas não são modificadas.

-   Interface Unificada (Target): `ISocialMediaAdapter`.
    -   O contrato que o nosso sistema utiliza.

-   Adapters: `TwitterAdapter`, `LinkedInAdapter`, `InstagramAdapter`.
    -   Implementam `ISocialMediaAdapter` e encapsulam a lógica para se comunicar com as APIs originais.

-   Factory: `SocialMediaFactory`.
    -   Cria e gerencia o ciclo de vida dos adaptadores. Implementa um cache thread-safe (`ConcurrentHashMap`) para reutilizar instâncias.

-   Modelos de Dados: `Conteudo`, `RespostaAPI`.
    -   Estruturas de dados padronizadas usadas em todo o sistema.

-   Cliente: `GerenciadorMidiaSocial` e `SocialMediaIntegration` (main).
    -   A classe que consome a funcionalidade, interagindo apenas com a `ISocialMediaAdapter` através da `SocialMediaFactory`.

## Como Executar

1.  Salve todo o código em um arquivo chamado `SocialMediaIntegration.java`.
2.  Compile o arquivo via terminal: `javac SocialMediaIntegration.java`
3.  Execute o programa: `java SocialMediaIntegration`

A saída no console demonstrará o fluxo de publicação em cada plataforma, incluindo uma falha controlada no Instagram (ao tentar postar sem imagem) e a verificação do cache da factory.