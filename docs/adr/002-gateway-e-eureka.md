# ADR-002: Entrada pelo Gateway e descoberta pelo Eureka

- **Data:** 03/11/2026
- **Status:** Adotado no MVP
- **Escopo:** Versão inicial funcional do gerenciador de documentos.

## Contexto

A interface precisa acessar APIs de vários serviços. Endereços fixos espalhados pelo código dificultariam a configuração dos clientes e a demonstração dos serviços disponíveis.

## Decisão

Utilizar o Gateway na porta 8080 como entrada das rotas públicas `/api/identidade`, `/api/modelos`, `/api/documentos` e `/api/acesso`. Utilizar o Eureka Server na porta 8761 para registro e descoberta pelo nome lógico.

Os quatro serviços de domínio e o Gateway são registrados no Eureka. O próprio servidor Eureka não se registra como cliente. O Gateway usa rotas `lb://`; os clientes HTTP entre serviços utilizam RestTemplate com LoadBalancer.

O Eureka informa onde os serviços estão disponíveis e mostra seus registros; ele não transporta as requisições de negócio nem apresenta o conteúdo dos documentos. As rotas `/interno` não são encaminhadas pelo Gateway.

## Alternativas consideradas

- Front-end acessando diretamente cada serviço: exigiria conhecer vários endereços e portas.
- Gateway com destinos fixos: reduziria componentes, mas não demonstraria a descoberta dinâmica solicitada para o projeto.

## Consequências e limites

- A interface utiliza uma única origem e as chamadas encontram os serviços pelo nome registrado.
- Gateway e Eureka acrescentam processos e configuração operacional.
- Após iniciar, existe um intervalo até os registros serem descobertos pelos clientes.
- O Gateway é um ponto de dependência para acesso externo. Não há alta disponibilidade configurada neste MVP.
- Registro no Eureka demonstra descoberta; os testes das APIs verificam o funcionamento de negócio separadamente.

## Evidências no repositório

- `gateway/src/main/resources/application.yml`: rotas públicas e nomes lógicos.
- `eureka-server/src/main/resources/application.yml`: configuração do registro.
- `*/src/main/resources/application.yml`: identificação e endereço anunciado pelos clientes.
- `testar_mvp.py`: verifica os cinco registros e o fluxo pelo Gateway.
