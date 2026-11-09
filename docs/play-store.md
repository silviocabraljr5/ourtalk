# Preparação para a Google Play

O cadastro do app `br.com.ourtalk.app` e a ficha pt-BR foram criados no Play Console. A ficha inclui ícone, arte de destaque e capturas da interface Android. O app ainda não foi enviado para revisão.

A [política pública do aplicativo](https://ourtalk.com.br/privacidade-app.html) explica dados locais, mensagens entre aparelhos e a coleta opcional de diagnósticos pelo SDK Nearby. A política do cadastro de avisos continua separada. O acesso pelo Android fica em Recursos → Sobre e privacidade.

Validação: `:app:bundleRelease` compilou o AAB sem assinatura de upload; a política e a atualização de andamento responderam por HTTPS. Essa compilação não valida a comunicação em aparelhos físicos.

O contato público da loja é `contato@ourtalk.com.br`. O questionário IARC da versão atual foi salvo e retornou classificação de 12 anos para o Brasil. Revise a classificação quando o chat mudar. O AAB 0.3.0 foi assinado com uma chave de upload da OurWorld, verificado com `jarsigner -verify` e aceito no rascunho do teste interno (código de versão 3). A faixa ainda não tem testadores e não foi publicada. O Play também recomenda símbolos de depuração para o código nativo. Guarde o PKCS#12 e a senha em um cofre controlado pela organização antes de depender deles em outra máquina. A chave e a senha não pertencem ao repositório.

Antes de promover uma versão: assinar o AAB com a chave de upload da organização, revisar Segurança dos dados com base no pacote assinado, concluir classificação e público-alvo, e validar as mensagens em aparelhos físicos. O chat experimental também precisa de bloqueio no app e de um fluxo de termos e moderação adequado antes da distribuição pública. Veja a [política de conteúdo gerado por usuários do Google Play](https://support.google.com/googleplay/android-developer/answer/9876937).
