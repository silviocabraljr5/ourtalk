# Conversas, grupos e pessoas por perto

## Experiência definida

Conversas usa uma lista familiar de chats individuais e grupos, busca por nome, filtros e criação de grupo. O contato salvo é uma referência independente da sessão de comunicação. Pessoas próximas ficam em uma aba separada; uma conversa temporária não exige adicioná-las à agenda.

## Primeira versão interna

Contatos, grupos, participantes de referência e mensagens LOCAL_ONLY são persistidos no aparelho. Não existe envio, recebimento, descoberta de dispositivos ou confirmação de leitura. Não mostrar dois tiques, online ou entregue sem evento real do transporte. Mensagens LOCAL_ONLY não devem ser enviadas automaticamente por uma atualização futura.

## Próxima etapa de transporte

- Ativar visibilidade/descoberta por ação explícita, com prazo e indicação visível; interromper a sessão quando desativada.
- Anunciar um identificador temporário e capacidades do app. Não transmitir documentos, saúde, lista de contatos ou GPS no anúncio de presença.
- Separar identidade criptográfica de nomes locais e UUIDs do banco. Um nome cadastrado manualmente não autentica outra pessoa.
- Permitir pedido de conversa com um usuário descoberto sem salvar um contato. O destinatário pode aceitar, recusar ou bloquear a sessão.
- Verificar a identidade e a conexão antes de liberar o envio. Salvar um contato não substitui essa verificação.
- Manter fila, repetição segura, identificadores únicos e confirmações reais. O estado da interface deve seguir os eventos do transporte.
- Grupos precisam de convites, aceite e protocolo criptográfico apropriado; mudanças de membros exigem gestão de chaves. Não tratar a lista local de referências como grupo de rede estabelecido.
- Validar conexão em dois aparelhos reais, permissões negadas, saída de alcance, repetição de payload, bloqueios e falta de rede antes de liberar.

Criptografia de produção e protocolo de grupos serão adotados a partir de implementações revisadas; não criar um protocolo de segurança improvisado.
