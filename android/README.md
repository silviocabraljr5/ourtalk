# OurTalk Android — início do aplicativo nativo

Kotlin e Jetpack Compose. Primeira base: navegação Início/Conversas/TalK/Mapa, Recursos no topo e checklist salvo localmente. Não é um WebView do site.

Esta base ainda não implementa comunicação, descoberta de pessoas, identidade criptográfica, IA ou mapas. Não envia SOS. O aplicativo não pede acesso a contatos, localização ou rede nesta etapa.

## Compilar

Abra esta pasta no Android Studio, use JDK 17 e instale Android SDK 36 com Build Tools. Depois execute `gradlew.bat :app:assembleDebug` no Windows ou `./gradlew :app:assembleDebug` em Linux/macOS. Aceite as licenças do SDK pessoalmente no Android Studio.

## Verificar o núcleo

`gradlew.bat :core:verifyCore` valida as transições de consentimento, sem depender de aparelho. O modelo local não substitui verificação segura de identidade.

## Próxima entrega

1. Preparar SDK e compilar a primeira APK.
2. Persistência de contatos e fila de mensagens com estados de entrega.
3. Prova de comunicação local e identidade, com testes entre dois aparelhos.
4. Pacotes de mapas assinados/versionados, GNSS e rotas offline.
5. TalK com conteúdo local e fontes.

Mapas, comunicação, IA e segurança exigem implementação e validação em aparelhos reais antes de qualquer anúncio de disponibilidade.
