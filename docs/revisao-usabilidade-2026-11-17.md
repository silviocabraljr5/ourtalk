# Revisão de usabilidade — 17/11/2026

Conversas agora mostra Convidar pessoa antes das abas; Recursos oferece outro acesso ao mesmo fluxo. O serviço público de convites permanece em preparação: acesso ao botão não comprova criação do vínculo ou entrega de mensagens.

O mapa abre aproximado na cidade, desenha as ruas disponíveis no grafo offline e oferece ampliar, reduzir e centralizar. A malha é parcial; não representa todas as ruas do município. As 31 unidades cadastradas não constituem um cadastro completo de todos os serviços de amparo.

Validação: `:core:verifyCore :app:assembleDebug --offline --console=plain` concluído com sucesso. Não havia aparelho conectado; interface, GPS, rádio e instalação via Google Play não foram validados nesta entrega. Nenhuma nova versão foi enviada à loja.

Pendências essenciais: ativar e validar convites de ponta a ponta; mensagens remotas e grupos reais; interoperabilidade iOS; ampliar cobertura e orientação offline; implementar TalK; concluir testes físicos e revisão de segurança antes de uma versão final.
## Continuação — convites e mapa

Serviço de convites ativado com SQLite privado e limpeza horária. Os testes HTTPS de criação, reciprocidade, repetição e exclusão passaram; identidades descartáveis de teste foram excluídas. Leia `convites.md` para os limites e o checklist de distribuição.

Android: opção de colar convite em aparelhos já instalados, confirmação explícita, retomada no armazenamento protegido e cancelamento. O parser rejeita host/pacote incorretos, HTTP e parâmetros duplicados. Testes do núcleo incluem essas rejeições. Um teste de interface foi escrito para validar colagem, confirmação e cancelamento sem rede; precisa ser executado em aparelho ou emulador.

Mapa: zoom por gesto com ponto de ancoragem, arrastar, ordenação das unidades por distância em linha reta e invalidação de rota quando o GPS muda ou fica antigo. A ordenação não representa tempo de viagem nem unidade disponível. A cobertura das ruas continua parcial.
Validação final: verificação do núcleo, APK debug, APK de testes e lint debug concluídos com sucesso. O lint mantém avisos; nenhum erro. Os testes de interface foram compilados, mas não executados. Status e política públicos conferidos por HTTPS.
