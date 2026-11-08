# Publicação Android — OurTalk

Destino autorizado pelo responsável: Google Play, depois de o aplicativo estar pronto e validado.

## Artefatos

- APK debug: uso interno e testes; pacote br.com.ourtalk.app.dev.
- AAB release: pacote br.com.ourtalk.app. O bundle gerado atualmente não contém assinatura de produção. Não enviar como lançamento concluído.
- A chave de upload e a conta proprietária serão configuradas antes do primeiro envio. Não salvar chaves ou senhas no Git.

## Antes de enviar

1. Validar os recursos anunciados e atualizar as descrições para a versão que realmente será entregue.
2. Testar persistência, acessibilidade, falta de rede, permissões negadas e consumo de bateria em aparelhos reais.
3. Validar identidade, privacidade e entrega de mensagens entre pelo menos dois aparelhos antes de anunciar comunicação pronta.
4. Definir conta Play Console, suporte, política de privacidade do app, classificação indicativa e declarações de segurança de dados conforme o comportamento real.
5. Verificar os requisitos vigentes de API e compatibilidade de páginas de memória no momento da submissão.
6. Configurar assinatura de produção, executar lint/testes e gerar AAB release assinado.
7. Enviar ao teste interno; evoluir para teste fechado e produção conforme as exigências da conta.
8. Publicar a URL oficial no site e preparar o aviso para a lista que autorizou contato. Nenhum disparo de lançamento deve ocorrer antes de o download estar disponível.

## Requisitos consultados em 08/11/2026

Google Play usa Android App Bundle e requer assinatura. Contas pessoais criadas após 13/11/2023 podem exigir teste fechado com pelo menos 12 testadores por 14 dias consecutivos e aprovação de acesso à produção. Conferir o enquadramento da conta e as regras atuais antes de enviar.

Fontes oficiais:
- https://support.google.com/googleplay/android-developer/answer/9859152?hl=pt-BR
- https://support.google.com/googleplay/android-developer/answer/14151465

## Primeira versão interna 0.1

Guias incluídos no app, busca offline por palavras, favoritos, checklist, contatos de referência, rascunhos locais e consulta GPS por ação explícita. Ainda não há transporte de mensagens, descoberta próxima, mapas baixados, rotas ou modelo generativo conectado ao TalK.
