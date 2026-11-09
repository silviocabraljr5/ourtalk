# Preparação para a Google Play

O cadastro do app `br.com.ourtalk.app` e a ficha pt-BR foram criados no Play Console. A ficha inclui ícone, arte de destaque e capturas da interface Android. O app ainda não foi enviado para revisão.

A [política pública do aplicativo](https://ourtalk.com.br/privacidade-app.html) explica dados locais, mensagens entre aparelhos e a coleta opcional de diagnósticos pelo SDK Nearby. A política do cadastro de avisos continua separada. O acesso pelo Android fica em Recursos → Sobre e privacidade.

Validação: `:app:bundleRelease` compilou o AAB sem assinatura de upload; a política e a atualização de andamento responderam por HTTPS. Essa compilação não valida a comunicação em aparelhos físicos.

Antes de promover uma versão: assinar o AAB com a chave de upload da organização, revisar Segurança dos dados com base no pacote assinado, concluir classificação e público-alvo, e validar as mensagens em aparelhos físicos. O chat experimental também precisa de bloqueio no app e de um fluxo de termos e moderação adequado antes da distribuição pública. Veja a [política de conteúdo gerado por usuários do Google Play](https://support.google.com/googleplay/android-developer/answer/9876937).
