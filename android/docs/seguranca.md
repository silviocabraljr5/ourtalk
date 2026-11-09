# Modelo de ameaças — Android 0.2

## Dados e fronteiras

Históricos locais, contatos de referência, checklist, favoritos, rascunhos e posição opcional são dados privados. Presença por perto é visível no rádio enquanto ativada; não promete anonimato contra observadores do rádio. Apelido temporário não é identidade permanente. Todo dado recebido de outro aparelho é entrada não confiável.

## Armazenamento

SecureStore cifra cada namespace como um documento JSON com AES-256-GCM/NoPadding. A chave não exportável é gerada no Android Keystore; hardware seguro depende do aparelho, sem promessa de StrongBox. Cada gravação usa IV novo de 12 bytes gerado pelo provedor. AAD vincula o conteúdo ao namespace e a tag de 128 bits detecta alteração. O envelope inclui versão e IV; não inclui chave. Migração autentica a gravação antes de limpar as preferências antigas. Uma falha preserva o arquivo, bloqueia leitura e não usa texto aberto como fallback.

Backup e transferência automática permanecem excluídos. A desinstalação remove dados e chave. Não existe recuperação de chave ou exportação segura nesta versão. Não há apagamento forense garantido de cópias antigas em flash. O armazenamento é pequeno e experimental: não substitui um banco transacional para históricos extensos; gravação criptografada síncrona deve ser revista antes de escala.

Não há autenticação biométrica por abertura. Quem controla um aparelho desbloqueado ou compromete o processo pode ler dados na tela/memória e usar operações da chave. Capturas de tela não são bloqueadas. Não prometer proteção contra root, malware, coerção ou um participante que copie mensagens.

## Conexão próxima

Nearby Connections 19.5.1, estratégia P2P_POINT_TO_POINT. Google Play Services fornece descoberta e link criptografado. O app nunca aceita automaticamente: o código da conexão deve ser comparado por canal confiável e confirmado por ambos. Uma SessionGate separa descoberta, pedido, comparação, aceite local e conexão concluída. Dados só passam no endpoint confirmado. O código confirma a conexão daquela sessão, sem autenticar um nome civil ou criar uma chave de contato permanente.

Opt-in com apelido aleatório OurTalk-xxxxxxxx, renovado por sessão. Não anuncia nome, documentos, agenda ou coordenadas. A sessão termina após dez minutos, recusa, erro, desconexão ou ON_STOP. Não há serviço em segundo plano, retorno automático, fila de reenvio, retransmissão ou publicação de posição. Google Play Services é dependência externa; seu SDK pode coletar diagnósticos conforme configurações de Uso e diagnóstico do Google. O app não implementa endpoint de analytics. Revise a declaração Data Safety antes da Play Store.

Mensagens próximas e o rascunho dessa conversa ficam somente em memória e são descartados ao encerrar a sessão neste aparelho. O texto digitado não é salvo no estado Android de recriação da tela; o rascunho local é recuperado pelo armazenamento criptografado. Não há mistura com históricos LOCAL_ONLY. O outro aparelho pode copiar o conteúdo recebido. Grupos locais não são grupos de rede; mensagens locais antigas nunca são transmitidas automaticamente.

## Protocolo de aplicação

ChatFrame não é protocolo criptográfico: é enquadramento de texto/recibo dentro do canal Nearby. Versão 1, tipo, UUID de 128 bits, tamanho e UTF-8 estrito; máximo 8.192 bytes e 2.000 unidades UTF-16 por texto. Rejeita truncamento, versão/tipo desconhecido, tamanho divergente, UTF-8 inválido e controles indevidos. Apenas payload BYTES. Limites: 30 peers exibidos, 300 mensagens por sessão e 60 frames recebidos por dez segundos. Não oferece resistência geral a DoS ou interferência de rádio.

UUIDs recebidos são deduplicados durante a sessão. ACK só altera uma mensagem pendente do endpoint confirmado. A confirmação é gerada após aceitar o texto na memória do app receptor; não prova leitura humana nem armazenamento durável. Trinta segundos sem ACK mostra recebimento não confirmado; não reenvia automaticamente. Nenhum payload, código, histórico ou coordenada é registrado em logs próprios.

## Cobertura e pendências

Testes automatizados cobrem transições, enquadramento, armazenamento adulterado, AAD, migração, novos IVs e fluxos locais. Não comprovam o rádio, implementação interna do SDK, identidade civil, segurança operacional ou GNSS real. Dois aparelhos físicos, Android 8–16, permissões negadas/revogadas, distância, perda de sinal e ciclo de vida são gates de lançamento. Auditoria independente, grupos com gestão de chaves, identidade persistente, bloqueio persistente, transporte por internet, mapas/rotas e TalK generativa continuam pendentes.

## Fontes primárias consultadas em 08/11/2026

- https://developer.android.com/privacy-and-security/keystore
- https://developers.google.com/nearby/connections/overview
- https://developers.google.com/nearby/connections/android/get-started
- https://developers.google.com/nearby/connections/android/manage-connections
- https://developers.google.com/nearby/connections/android/exchange-data
- https://developer.android.com/build/kotlin-support
- https://kotlinlang.org/docs/gradle-configure-project.html
