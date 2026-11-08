# Cadastro de lançamento

Endpoint público: POST /newsletter.php, JSON com email, whatsapp, consent booleano e company (campo antispam). Só aceita origem HTTPS do domínio, exige consentimento não pré-marcado, valida contatos e limita cadastros por identificador de acesso por hora.

SQLite fica fora da pasta pública: /home/ourbusinesscom/ourtalk-data/subscribers.sqlite. Permissões restritas à conta. O endpoint não possui listagem pública. E-mail e WhatsApp são registros separados, com unicidade por canal/endereço, texto e data da autorização. O campo verified_at permanece vazio; cadastrar não prova que a pessoa controla o canal.

Não há disparo automático, SMTP ou API WhatsApp nesta entrega. Antes da campanha, configurar remetente autorizado, verificação de contatos e cancelamento individual em cada mensagem. Usuários também podem solicitar remoção pelo Instagram oficial, conforme a página pública de privacidade. Não exportar a lista para o repositório ou o diretório público.

O servidor Node de prévia responde indisponível para esse endpoint; a captura é executada pelo PHP da hospedagem. Nunca simular sucesso quando o armazenamento falhar.
