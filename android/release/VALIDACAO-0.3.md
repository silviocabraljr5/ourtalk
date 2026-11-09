# Validação interna do OurTalk Android 0.3.0

Em 09/11/2026, o Gradle concluiu o núcleo Java, APK, APK de instrumentação, lint e AAB. O lint registrou zero erros e dez avisos. O manifesto de lançamento passou pela verificação de permissões, componentes exportados, backup e tráfego em claro. O APK tem assinatura de teste. O AAB aguarda assinatura de upload.

O emulador Android 36 executou oito testes em 68,489 segundos. A nova verificação abriu o mapa sem pedir localização, filtrou as cinco unidades municipais e selecionou a UPA Ribeirão Verde com busca sem acentos. O aplicativo carrega o mapa de um arquivo incluído e seu manifesto não pede acesso à internet. O emulador manteve uma interface de dados móveis simulada.

A validação do cadastro em campo e a comunicação entre aparelhos físicos continuam pendentes. O mapa mostra 31 unidades das fontes listadas em docs/mapas-e-navegacao.md, sem rede de ruas ou cálculo de rotas.

## SHA-256

- APK de teste: BF7A9C464335B2F0F1A667B8207746D838236E1D1BCA77143D9F30951F68E1D5
- AAB sem assinatura: 9612643FB1F0CB5DADE344B0B7D584573B0FA5EE54E8F50B7976E6DB9D82DFA0
