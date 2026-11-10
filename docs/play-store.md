# Preparação para a Google Play

O cadastro do app `br.com.ourtalk.app` e a ficha pt-BR foram criados no Play Console. A ficha inclui ícone, arte de destaque e capturas da interface Android. O app ainda não foi enviado para revisão.

O Play Console tem a versão 0.3.0 preparada em produção para o Brasil. A versão 0.4.0 usa o código 4 e acrescenta aceite das regras antes de descobrir aparelhos, bloqueio persistente por chave de instalação e um atalho para redigir uma denúncia. O bloqueio encerra a sessão e recusa novas mensagens da mesma instalação após verificar a chave. A reinstalação pode gerar outra chave. A denúncia abre um rascunho no cliente de e-mail e depende do envio pelo usuário e da triagem pelo suporte.

Antes de enviar a 0.4.0, confira a troca de provas e o bloqueio em dois aparelhos, confirme o canal de denúncia e atualize a classificação e a declaração de dados para o pacote que for distribuído.

O código após a 0.4.0 assinada já inclui Pedir ajuda. O texto é preparado no aparelho e o usuário escolhe o destino no seletor do Android. A localização é opcional, sem armazenamento nessa tela, e uma leitura antiga ou simulada fica fora da mensagem. O atalho para o SAMU abre o discador. Para distribuir essa mudança, gere um novo pacote e reveja a ficha de saúde e dados conforme o comportamento da versão.

A [política pública do aplicativo](https://ourtalk.com.br/privacidade-app.html) explica dados locais, mensagens entre aparelhos e a coleta opcional de diagnósticos pelo SDK Nearby. A política do cadastro de avisos continua separada. O acesso pelo Android fica em Recursos → Sobre e privacidade.

Validação: `:app:bundleRelease` compilou o AAB sem assinatura de upload; a política e a atualização de andamento responderam por HTTPS. Essa compilação não valida a comunicação em aparelhos físicos.

O contato público da loja é `contato@ourtalk.com.br`. O questionário IARC da versão atual foi salvo e retornou classificação de 12 anos para o Brasil. Revise a classificação quando o chat mudar. O AAB 0.3.0 foi assinado com uma chave de upload da OurWorld, verificado com `jarsigner -verify` e aceito no rascunho do teste interno (código de versão 3). A faixa ainda não tem testadores e não foi publicada. O Play também recomenda símbolos de depuração para o código nativo. Guarde o PKCS#12 e a senha em um cofre controlado pela organização antes de depender deles em outra máquina. A chave e a senha não pertencem ao repositório.

Antes de promover uma versão: assinar o AAB com a chave de upload da organização, revisar Segurança dos dados com base no pacote assinado, concluir classificação e público-alvo, e validar as mensagens em aparelhos físicos. O chat experimental também precisa de bloqueio no app e de um fluxo de termos e moderação adequado antes da distribuição pública. Veja a [política de conteúdo gerado por usuários do Google Play](https://support.google.com/googleplay/android-developer/answer/9876937).
