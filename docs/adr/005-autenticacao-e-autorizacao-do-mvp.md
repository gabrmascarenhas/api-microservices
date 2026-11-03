# ADR-005: Sessão temporária e autorização pelo proprietário

- **Data:** 03/11/2026
- **Status:** Adotado no MVP
- **Escopo:** Versão inicial funcional do gerenciador de documentos.

## Contexto

O fluxo inicial precisa identificar o usuário e impedir a consulta individual de documentos pertencentes a outra pessoa. A senha não deve aparecer nas respostas das APIs.

## Decisão

Armazenar senhas usando BCrypt e gerar um token opaco aleatório no login. A sessão é mantida em memória pelo serviço Identidade e expira em uma hora. Os clientes enviam o token no cabeçalho `Authorization: Bearer`.

Ao consultar um documento individual, Documentos obtém o proprietário de seu próprio repositório e envia esse contexto ao serviço Acesso. Acesso consulta a sessão em Identidade e permite a operação quando o usuário corresponde ao proprietário. A listagem de documentos é filtrada pelo usuário autenticado no serviço Documentos.

Proteger as rotas internas por uma chave compartilhada no cabeçalho `X-Internal-Key`. Na execução local, os serviços escutam em loopback; no Compose, os serviços de domínio não publicam suas portas no host e a variável `INTERNAL_KEY` é obrigatória.

## Alternativas consideradas

- Aceitar um identificador de usuário informado pelo navegador: permitiria falsificar a identidade e não foi adotado.
- JWT e provedor externo de identidade: alternativas possíveis para evolução, com maior escopo de configuração.
- Autorizar apenas na interface: não protegeria as APIs contra chamadas diretas.

## Consequências e limites

- A consulta individual rejeita outro proprietário com HTTP 403 e sessões inválidas com HTTP 401.
- A validação de sessão depende da disponibilidade de Identidade; falhas de dependências chamadas pelo cliente HTTP retornam 503.
- A chave compartilhada é uma proteção demonstrativa entre serviços, não uma solução completa de identidade de serviço ou isolamento de produção.
- A chave padrão é exclusiva da demonstração local. Segredos próprios devem ser configurados em outros ambientes.
- Logout com revogação no servidor, bloqueio de conta, aprovação de acesso, perfis e compartilhamento ainda não estão implementados.
- Não há configuração de TLS nem de autenticação mútua nesta entrega.

## Evidências no repositório

- `identidade/src/main/java/com/example/documentos/identidade/service/IdentidadeService.java`: BCrypt, token e expiração.
- `documentos/src/main/java/com/example/documentos/documentos/service/DocumentoService.java`: consulta e contexto do proprietário.
- `acesso/src/main/java/com/example/documentos/acesso/service/AcessoService.java`: avaliação de propriedade.
- `testar_mvp.py`: credenciais inválidas e acesso negado a outro usuário.
