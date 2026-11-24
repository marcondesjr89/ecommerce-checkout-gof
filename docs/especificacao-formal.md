# Especificação Formal de Requisitos (SRS) - ProjetoGOF

## 1. Objetivo do Documento
Especificar a arquitetura, requisitos funcionais e não funcionais do projeto de demonstração prática dos **Padrões de Projeto GoF (Gang of Four)** em uma API REST corporativa desenvolvida com **Java 21**, **Spring Boot 3.4.x** e **PostgreSQL**.

---

## 2. Padrões de Projeto (GoF) Implementados

### 2.1. Strategy (Comportamental)
- **Problema:** O cálculo de frete e o processamento de pagamento possuem múltiplas variações e regras algorítmicas que mudam dinamicamente dependendo da escolha do cliente.
- **Solução:** 
  1. `ShippingStrategy`: Interface que define o contrato de cálculo de frete. Implementada por `ExpressShippingStrategy` e `EconomyShippingStrategy`.
  2. `PaymentStrategy`: Interface que define o contrato de processamento de pagamento. Implementada por `PixPaymentStrategy` (aplica desconto à vista), `CreditCardPaymentStrategy` (taxa de operação/parcelamento) e `BoletoPaymentStrategy`.
  3. `PaymentStrategyResolver`: Componente que localiza a estratégia adequada usando o container de injeção de dependência do Spring (`Map<PaymentMethod, PaymentStrategy>`).

### 2.2. Facade (Estrutural)
- **Problema:** O processo de checkout envolve múltiplos subsistemas complexos: validação de itens, cálculo de frete, reserva de estoque, criação de cobrança no gateway de pagamento e emissão do pedido. Um cliente ou controller que chame esses serviços diretamente geraria alto acoplamento e duplicidade.
- **Solução:** `CheckoutFacade` atua como interface simplificada e unificada para o cliente (ex: `CheckoutController`), orquestrando as chamadas aos subsistemas na sequência correta de forma transacional.

### 2.3. Singleton (Criacional)
- **Problema:** Serviços de negócio, orquestradores e conexões com subsistemas precisam de gerenciamento centralizado de ciclo de vida e economia de recursos computacionais, evitando instanciações repetidas.
- **Solução:** Utilização do mecanismo nativo de **Spring Singleton Scope** (padrão em beans `@Service`, `@Component` e `@Configuration`). Cada serviço possui uma única instância compartilhada e thread-safe no `ApplicationContext`.

---

## 3. Requisitos Funcionais (RF)
- **RF-01:** O sistema deve permitir a realização de checkout recebendo identificador do cliente, lista de itens, método de envio e forma de pagamento.
- **RF-02:** O sistema deve calcular o frete dinamicamente conforme a estratégia selecionada (`EXPRESS`, `ECONOMY`).
- **RF-03:** O sistema deve processar o pagamento aplicando a estratégia correspondente (`PIX`, `CREDIT_CARD`, `BOLETO`).
- **RF-04:** O sistema deve persistir os pedidos gerados e seus respectivos itens no banco de dados relacional.
- **RF-05:** O sistema deve permitir a consulta detalhada de pedidos por ID e listagem geral.
- **RF-06:** O sistema deve disponibilizar documentação OpenAPI / Swagger UI para execução e teste dos endpoints.

---

## 4. Requisitos Não Funcionais (RNF)
- **RNF-01 - Desempenho e Concorrência:** As classes gerenciadas pelo Spring devem ser livres de estado mutável compartilhado (thread-safe).
- **RNF-02 - Persistência:** PostgreSQL com suporte a Docker Compose.
- **RNF-03 - Testabilidade:** Testes unitários cobrindo as estratégias (`Strategy`), a orquestração (`Facade`) e os endpoints da API.
- **RNF-04 - Padronização de Erros:** Respostas de erro padronizadas seguindo RFC 7807 (Problem Details).
