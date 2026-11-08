# OurTalk Android — versão interna 0.1

Aplicativo nativo em Kotlin/Jetpack Compose. Identidade escura com verde; navegação Início, Conversas, TalK e Mapa. Recursos fica no cabeçalho.

## Implementado

- Biblioteca incluída no app, fontes e datas de revisão.
- Busca offline por palavras com TalK; não é IA generativa nesta etapa.
- Favoritos e checklist persistentes, com inclusão de itens.
- Contatos de referência, conversas locais, criação de grupos, participantes e mensagens em balões.
- Históricos persistentes, sem falsas confirmações de envio ou leitura.
- Consulta GPS sob permissão explícita, precisão e horário da leitura; salvar a última posição é opcional. Sem acesso em segundo plano.
- Compartilhamento voluntário do link público do projeto.

Ainda não há transporte de mensagens, descoberta de pessoas próximas, mapa baixado, rotas, IA generativa ou criptografia de ponta a ponta. A aba Por perto descreve o fluxo planejado e não exibe pessoas fictícias. Veja `docs/conversas-e-proximos.md`.

## Dados

Dados pessoais permanecem na área privada do app. Backup e transferência automática estão excluídos nas regras Android. A desinstalação remove os dados locais. Esta versão não declara permissão de internet, contatos, câmera, microfone ou localização em segundo plano. Permissões de localização são solicitadas apenas na função de GPS.

## Compilar

JDK 17, Android SDK 36 e Build Tools 35.0.0. Configure seu SDK em `local.properties`, que não é versionado. No Windows:

```sh
gradlew.bat :core:verifyCore :app:assembleDebug :app:lintDebug
gradlew.bat :app:assembleDebugAndroidTest
```

Os APKs de teste usam o pacote br.com.ourtalk.app.dev. Em um emulador/aparelho de teste com recursos suficientes, execute `gradlew.bat :app:connectedDebugAndroidTest`. Em máquinas limitadas, compile os dois APKs antes de iniciar o emulador e execute a instrumentação via adb.

## Google Play

O pacote de produção é br.com.ourtalk.app. O comando `gradlew.bat :app:bundleRelease` gera o AAB; assinatura de produção e conta Play Console ainda precisam ser configuradas. A publicação foi autorizada para quando os recursos estiverem prontos e validados. Consulte `release/PUBLICACAO.md`.
