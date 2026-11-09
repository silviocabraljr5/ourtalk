# Roteiro de teste físico — conversa próxima

Status: **pendente**. Não há dois aparelhos físicos conectados ao ambiente. Não preencher resultados antecipadamente. Usar dispositivos de teste e pessoas que concordaram, sem dados pessoais reais.

## O que conferir com um Android

Instale o APK interno em Android 8 ou superior. Abra Biblioteca e checklist sem internet, crie um contato e um grupo de teste, salve uma mensagem local e reabra o app para conferir persistência. Na aba Por perto, confira o comportamento ao negar permissões, ativar por dez minutos e encerrar. Em aparelho com Google Play Services, o apelido deve mudar a cada ativação; uma busca vazia com apenas um aparelho não comprova falha nem sucesso de comunicação.

## Preparação

1. Registrar modelo, versão Android, versão Google Play Services, versão do APK e hash. Instalar o mesmo APK nos dois Androids; a versão .dev só encontra outra .dev.
2. Conferir funcionamento de biblioteca/checklist com todas as permissões recusadas. Em dispositivo sem Google Play Services, Por perto deve informar indisponibilidade sem impedir o restante.
3. Em ambos, abrir Conversas > Por perto > Ativar por 10 minutos. Autorizar permissões. Em Android 8–12L pode ser necessário ativar localização do sistema para descoberta; o app não lê GPS nessa função.

## Casos de aceite

| Caso | Resultado esperado | Resultado observado |
|---|---|---|
| Sem internet/operadora, Bluetooth e Wi-Fi ligados | Encontrar aparelho e realizar conversa próxima após verificação | Pendente |
| Não ativar descoberta | Não anunciar nem procurar pessoas | Pendente |
| Permissão negada ou revogada | Mensagem clara, sem busca ou crash; funções locais continuam | Pendente |
| Código não conferido ou divergente | Não liberar aceite sem marcar confirmação; recusar se divergente | Pendente |
| Aceite somente de um lado | Não liberar mensagens; expirar pedido em até 60 s | Pendente |
| Aceite nos dois lados | Abrir conversa sem adicionar contato; troca bidirecional | Pendente |
| ACK do app receptor | Mostrar recebido no outro app; sem tique de leitura | Pendente |
| Sair de alcance antes do ACK | Não inventar confirmação; encerrar sessão quando notificado | Pendente |
| Sair do app, bloquear tela ou recriar Activity | Parar conexão; apagar conversa temporária local | Pendente |
| Dez minutos de sessão | Parar busca/conexão e apagar mensagens locais temporárias | Pendente |
| Ativar novamente | Apelido novo e nova comparação de código | Pendente |
| Nome/agenda/GPS no anúncio | Nenhum desses dados anunciado | Pendente |
| Mensagens LOCAL_ONLY antigas | Nunca transmitir automaticamente | Pendente |
| Sem Play Services ou versão incompatível | Erro contextual, sem fallback inseguro | Pendente |
| App release reduzido com R8 | Mesmo comportamento do debug em aparelhos físicos | Pendente |

## Segurança e limites

Fazer ensaios controlados de payload inválido, repetido e excesso de frames com app de teste, sem procurar usuários externos. Testar concorrência de pedidos e reconexão. Não interpretar alcance de um aparelho como garantia de alcance do produto. Anexar evidência com dados sintéticos e horários, sem códigos de autenticação de sessões reais.

## GPS separado

Validar precisão/horário em ambiente externo, perda de satélites, localização aproximada e ausência de permissão. GPS do emulador não valida recepção real. Mapas e roteamento offline continuam sem implementação e não fazem parte do aceite desta versão.
