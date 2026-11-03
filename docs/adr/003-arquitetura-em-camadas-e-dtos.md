# ADR-003: Organização interna em camadas e contratos com DTOs

- **Data:** 03/11/2026
- **Status:** Adotado no MVP
- **Escopo:** Versão inicial funcional do gerenciador de documentos.

## Contexto

Os serviços precisam manter separadas as responsabilidades HTTP, as operações do domínio e o armazenamento. A organização deve permitir evoluir a persistência e tornar o fluxo compreensível na apresentação.

## Decisão

Organizar cada serviço de domínio nas camadas `controller`, `service`, `repository`, `model` e `dto`. Controllers recebem requisições e encaminham operações; Services concentram as operações e regras; Repositories encapsulam o armazenamento; Models representam dados internos; DTOs definem entradas e saídas das APIs.

Clientes HTTP ficam em `client` e a configuração em `config`. Utilizar injeção de dependências por construtor. Validar os campos de entrada com Bean Validation e não expor o hash de senha nos DTOs de usuário.

Essa estrutura é a arquitetura em camadas interna dos serviços. Ela não substitui a divisão externa em microserviços.

## Alternativas consideradas

- Concentrar HTTP, regras e armazenamento no Controller: reduziria arquivos inicialmente, mas dificultaria manutenção e testes.
- Retornar entidades internas diretamente: acoplaria os contratos HTTP à representação de armazenamento e poderia expor campos indevidos.

## Consequências e limites

- A organização separa responsabilidades e favorece coesão e testes por camada.
- DTOs permitem alterar dados internos preservando contratos externos, desde que as mudanças sejam compatíveis.
- Existem classes e conversões adicionais, além de contratos locais repetidos entre aplicações independentes.
- A separação de pastas, por si só, não comprova todos os princípios SOLID e GRASP nem os nove padrões GoF exigidos. Essas aplicações devem ser demonstradas no código à medida que forem implementadas.

## Evidências no repositório

- `identidade/src/main/java/com/example/documentos/identidade/`: camadas e DTOs de usuário.
- `documentos/src/main/java/com/example/documentos/documentos/`: fluxo Controller → Service → Repository.
- `modelos/` e `acesso/`: mesma organização por responsabilidade.
