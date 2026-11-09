# OurTalk Android 0.2.0 — validação interna

Data: 08/11/2026. Versão de teste, sem publicação na Play Store.

- Compilação do APK e do AAB otimizado por R8: aprovada. APK com assinatura Android Debug verificada, AAB sem assinatura de produção.
- Lint Android: 0 erros e 9 avisos (compatibilidade de atributos por API, atualizações de ferramentas/dependências e ícone).
- Núcleo Java: consentimento, bloqueio de mensagens antes da confirmação, endpoint correto, encerramento, limites e rejeição de frames inválidos/UTF-8 malformado.
- Instrumentação AOSP ATD API 36: **7 testes aprovados, em 51,19 s**. Criação de contato/grupo, histórico após recriação, biblioteca/favoritos/checklist, funcionamento local sem Google Play Services, migração, detecção de adulteração, AAD entre namespaces e IVs distintos.
- Manifesto de produção: permissões, componentes exportados, backup, tráfego em claro e debug verificados. Controle negativo com permissão de microfone inserida foi rejeitado pelo verificador.
- APK: alinhamento ZIP de bibliotecas em 16 KiB verificado. Isso não substitui os testes de compatibilidade de produção.
- Site: 10 testes aprovados (Pix, checkout simulado e energia).

## Instalação em um Android

Abra o APK de teste em um Android 8 ou superior. Pacote: br.com.ourtalk.app.dev. Se o Android solicitar, permita a instalação para o aplicativo que abriu o arquivo. Use dados de teste. Com um aparelho é possível verificar a instalação, biblioteca/checklist, grupos locais, persistência e permissões da aba Por perto.

A troca próxima real continua **pendente de validação em dois Androids físicos**. A instrumentação não confirma descoberta, alcance ou entrega por rádio. Grupos de rede, identidade permanente, comunicação pela internet, mapas e rotas offline e TalK generativa ainda não estão disponíveis. Pedir ajuda não envia SOS.

## SHA-256

- `ourtalk-android-0.2.0-dev.apk`: `ABFD95BAA00A868F728C35ABB255EB1D27F21E4267E4AF608DBA654828CB8DC4`
- `ourtalk-android-0.2.0-unsigned.aab`: `1560D0EB60A9F9046ED2738A1410032044A9DA0CD5DB737BC4DC29D3F1D1A9F6`
