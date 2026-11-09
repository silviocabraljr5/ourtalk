# OurTalk Android — versão interna 0.3

Aplicativo nativo Kotlin/Jetpack Compose. Identidade escura com verde; navegação Início, Conversas, TalK e Mapa. Recursos fica no cabeçalho. Nenhum lançamento na Play Store nesta etapa.

## Implementado

- Biblioteca incluída, fontes, datas de revisão, busca offline por palavras com TalK, favoritos e checklist.
- Contatos de referência, chats e grupos locais; mensagens e rascunhos persistentes, sem falsas confirmações de entrega/leitura.
- Dados locais e posição opcional cifrados com AES-256-GCM/Android Keystore. Migração do formato anterior sem fallback para texto aberto.
- Consulta GPS por ação e permissão explícitas, com precisão e horário; sem acesso em segundo plano.
- Mapa municipal offline de Ribeirão Preto com hospitais, pronto atendimento, busca por unidade ou endereço e marcador GPS opcional. Consulte o cadastro sem conceder localização.
- Conexão próxima individual experimental via Nearby Connections 19.5.1: opt-in por dez minutos, apelido temporário, comparação de código obrigatória, aceite bilateral, mensagens de texto e confirmação do aplicativo receptor.
- Conversar por perto não exige salvar contato. A sessão termina ao sair do app; histórico próximo é temporário, somente em memória.
- Compartilhamento voluntário do endereço público do projeto.

## Limitações

O transporte de rádio **ainda precisa ser validado em dois aparelhos físicos**. Google Play Services e Bluetooth/Wi-Fi compatíveis são necessários. O aparelho precisa manter os rádios ligados, mesmo sem internet/operadora. Não há alcance ilimitado ou garantia de disponibilidade. O SDK Google é uma dependência com licença própria e pode coletar diagnósticos conforme as configurações de Uso e diagnóstico do aparelho. Sem Play Services, os recursos locais continuam disponíveis.

Grupos continuam locais, sem convites ou chaves de grupo de rede. Nomes de contatos não autenticam usuários próximos. Identidade permanente de rede, bloqueio persistente, envio pela internet, retransmissão, outras áreas de mapas, rotas e IA generativa estão pendentes. TalK é busca de conteúdos. Pedir ajuda não envia SOS. Mensagens locais antigas nunca são transmitidas automaticamente.

Leia docs/seguranca.md, docs/conversas-e-proximos.md e docs/testes-dois-aparelhos.md. O escopo completo está em ../ROADMAP.md.

## Dados e permissões

Backup e transferência automática excluídos. Desinstalar remove dados e chave, sem recuperação implementada. A chave não exportável não impede leitura em aparelho desbloqueado ou processo comprometido. Não existe promessa de proteção contra root/malware ou de apagamento forense.

GPS pede localização somente na função. Proximidade pede Bluetooth/wi-fi próximos em Android recente; Android até 12L também requer localização para descoberta. Essa função não lê coordenadas. Sem agenda, câmera, microfone ou localização em segundo plano. Revisar sempre o manifesto mesclado das dependências.

## Compilar e verificar

JDK 17, SDK 36 e Build Tools 35.0.0. Kotlin 2.4.21 é compatível com o metadata do Nearby atual. R8 9.5.29 está fixado explicitamente para suportar Kotlin 2.4 mantendo AGP 8.13.2; revisar o conjunto ao atualizar. Wrapper Gradle tem checksum fixado. Configure sdk.dir em local.properties, ignorado pelo Git.

```sh
gradlew.bat :core:verifyCore :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug :app:bundleRelease --no-daemon
```

No Linux: bash ./gradlew com as mesmas tarefas. O workflow ../.github/workflows/checks.yml valida compilação, núcleo, lint e testes do site; não publica releases, não usa chaves e não testa rádios físicos.

Pacote debug: br.com.ourtalk.app.dev. Pacote release: br.com.ourtalk.app. Cada um usa serviceId distinto; instalar o mesmo tipo nos dois aparelhos para descoberta.

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w br.com.ourtalk.app.dev.test/androidx.test.runner.AndroidJUnitRunner
```

A instrumentação limpa somente os dados do aplicativo de teste; use um aparelho/emulador de teste. Em computadores com pouca memória, compile antes de iniciar o emulador e execute a instrumentação pelo adb.

## Google Play

AAB em app/build/outputs/bundle/release/app-release.aab, sem assinatura de produção. Chave de upload não deve entrar no Git. Publicação autorizada para quando estiver pronto e validado; gates e requisitos em release/PUBLICACAO.md.
