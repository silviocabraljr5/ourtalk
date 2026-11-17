# Convites pessoais

Em Conversas → Convidar pessoa, o Android cria um link pessoal da Google Play. O código do convite acompanha a instalação. Na primeira abertura, o app recupera o código e pede o nome que a outra pessoa verá. Ao continuar, o serviço cria os dois vínculos sem exigir que os usuários adicionem o contato manualmente. Quem convidou recebe o contato ao reabrir Conversas com internet. Os vínculos sincronizados permanecem disponíveis no aparelho.

Cada link vale por sete dias e pode vincular um aparelho. Repetir o resgate pelo mesmo aparelho, dentro desse prazo, não duplica o vínculo. O serviço rejeita o resgate pelo próprio emissor ou por um terceiro após o primeiro uso. A transação SQLite grava os dois lados em conjunto. O identificador secreto do aparelho fica no armazenamento protegido do Android e segue por HTTPS. O serviço guarda somente seu hash, os nomes escolhidos e os vínculos. A agenda, a posição e o conteúdo das conversas não fazem parte dessa operação.

Para reconfigurar a hospedagem: o endpoint fica inativo quando o diretório privado `ourtalk-invite-data` não existe ao lado da raiz pública. Para a ativação, configure o diretório com acesso restrito ao usuário da hospedagem e agende a execução horária de `php /caminho/contact-invites.php --purge-expired` sob o mesmo usuário. A rotina remove convites vencidos e contadores temporários mesmo quando ninguém usa o serviço. Os vínculos permanecem até uma das partes excluir seus dados. Revise os prazos de cópias de segurança na hospedagem antes de ativar.

Em Conversas → Contatos → Privacidade dos convites, a exclusão cancela convites e remove os vínculos dos dois lados no servidor. O app apaga seu identificador de convite e limpa a lista sincronizada após a resposta. O outro aparelho descarta sua cópia ao sincronizar. Contatos manuais e históricos locais são independentes e continuam sujeitos à limpeza de armazenamento do Android.

A política pública descreve o serviço ativo desde 17/11/2026. Na declaração Segurança dos dados, leve em conta o nome escolhido, o identificador de instalação, a lista de vínculos e a referência da Google Play. A declaração depende da versão efetivamente distribuída. Antes de distribuir um novo pacote, valide também a primeira abertura após instalação pela Play Store e a sincronização dos dois aparelhos. Links já abertos em aparelhos com o app instalado e a entrega de mensagens remotas ficam para etapas separadas.

Validação do servidor em ambiente isolado com PHP e SQLite da hospedagem. A criação, o vínculo nos dois sentidos, a repetição sem duplicidade, a rejeição de autorresgate e de terceiros e a expiração passaram. A instalação real pela Play Store ainda precisa ser conferida. O [Install Referrer da Google Play](https://developer.android.com/google/play/installreferrer/library) transporta a referência após a instalação.

A verificação de privacidade confirmou que um terceiro não consegue remover vínculos alheios, que a exclusão pode ser repetida, que os vínculos somem dos dois lados, que convites pendentes e já usados são invalidados e que a rotina remove registros vencidos sem depender de uma requisição.

## Entrega de 17/11/2026

Serviço ativado na hospedagem: diretório de dados fora da raiz pública, modo 0700, SQLite 0600 e limpeza horária executada pelo usuário da hospedagem (`/etc/cron.d/ourtalk-invites`, minuto 23). Não foi criada cópia adicional da base nesta entrega. A retenção de eventuais backups gerais da hospedagem ainda deve ser verificada; não se promete exclusão instantânea desses backups.

A política pública foi atualizada para descrever o serviço ativo. Verificação HTTPS com dois identificadores descartáveis confirmou criação, vínculos recíprocos, resgate idempotente e exclusão bilateral; os dados de teste foram removidos. Isso não comprova instalação ou transporte de mensagens.

Quem já tem o Android instalado pode usar Convidar pessoa → Usar convite recebido, colar o link completo ou código e confirmar a adição. O parser limita tamanho, aceita somente o pacote oficial em HTTPS na Google Play e rejeita parâmetros duplicados e domínios diferentes. O convite validado é guardado no armazenamento protegido para retomada após fechar o app. Não se abre o link colado no navegador.

A declaração Segurança dos dados deverá corresponder a nomes, identificadores e vínculos antes de distribuir este pacote. Nenhum pacote foi enviado à loja nesta entrega. A recuperação automática após instalação pela Google Play e a interface em aparelho continuam sem validação nesta data.