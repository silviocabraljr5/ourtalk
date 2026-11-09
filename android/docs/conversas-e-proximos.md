# Conversas, grupos e pessoas por perto

## Experiência definida

Conversas usa uma lista familiar de chats individuais e grupos, busca por nome, filtros e criação de grupo. O contato salvo é uma referência independente da sessão de comunicação. Pessoas próximas ficam em uma aba separada; uma conversa temporária não exige adicioná-las à agenda.

## Históricos locais (desde 0.1)

Contatos, grupos, participantes de referência e mensagens LOCAL_ONLY são persistidos no aparelho. Esses históricos não têm envio, recebimento ou confirmação de leitura. Mensagens LOCAL_ONLY nunca são enviadas automaticamente pela integração próxima da 0.2.

## Conexão próxima experimental (0.2)

A comunicação Android ↔ iOS é requisito do produto. A primeira distribuição pública será Android. O [contrato de interoperabilidade](../../docs/interoperabilidade.md) define o formato e os critérios para um cliente iOS de teste.

Implementado em código: Nearby P2P_POINT_TO_POINT, apelido temporário, ativação de até dez minutos, comparação de código obrigatória, aceite dos dois aparelhos, texto e confirmação do app receptor. O histórico dessa sessão é separado, fica na memória e é apagado ao encerrar ou sair do app. Conversar não exige adicionar contato. O envio só é liberado depois de o SDK concluir a conexão e o consentimento local estar confirmado.

O rádio ainda depende de validação em dois Androids físicos; o responsável dispõe atualmente de apenas um Android. Emulador não substitui esse gate. Ver docs/testes-dois-aparelhos.md e docs/seguranca.md. Não há identidade permanente, bloqueio persistente, grupos de rede, fila, retry automático, internet ou retransmissão.

## Requisitos para evolução do transporte

- Ativar visibilidade/descoberta por ação explícita, com prazo e indicação visível; interromper a sessão quando desativada.
- Anunciar um identificador temporário e capacidades do app. Não transmitir documentos, saúde, lista de contatos ou GPS no anúncio de presença.
- Separar identidade criptográfica de nomes locais e UUIDs do banco. Um nome cadastrado manualmente não autentica outra pessoa.
- Permitir pedido de conversa com um usuário descoberto sem salvar um contato. O destinatário pode aceitar, recusar ou bloquear a sessão.
- Verificar a identidade e a conexão antes de liberar o envio. Salvar um contato não substitui essa verificação.
- Manter fila, repetição segura, identificadores únicos e confirmações reais. O estado da interface deve seguir os eventos do transporte.
- Grupos precisam de convites, aceite e protocolo criptográfico apropriado; mudanças de membros exigem gestão de chaves. Não tratar a lista local de referências como grupo de rede estabelecido.
- Validar conexão em dois aparelhos reais, permissões negadas, saída de alcance, repetição de payload, bloqueios e falta de rede antes de liberar.

Criptografia de produção e protocolo de grupos serão adotados a partir de implementações revisadas; não criar um protocolo de segurança improvisado.
