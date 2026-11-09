# Comunicação entre Android e iOS

A primeira distribuição pública do OurTalk será no Android. A comunicação deve funcionar também entre Android e iPhone. Para conversar por perto durante essa etapa, o iPhone precisará de um cliente OurTalk de teste. O cliente iOS e a validação física entre plataformas estão pendentes.

## Sessão próxima

O transporte escolhido para validar a interoperabilidade é o Nearby Connections, com o SDK Android e o SDK Swift. Ambos usam a estratégia ponto a ponto e o mesmo identificador de serviço. Reutilize os identificadores do Android 0.2 para preservar a descoberta nessa versão.

| Ambiente | Identificador compartilhado |
| --- | --- |
| Desenvolvimento | `br.com.ourtalk.app.dev.nearby.v1` |
| Produção | `br.com.ourtalk.app.nearby.v1` |

No iOS, configure as permissões e a descoberta de rede local conforme a documentação do SDK Swift. Em ambas as plataformas, exija ativação explícita, comparação do código de pareamento e aceite local antes de entregar qualquer mensagem. Mantenha apelido temporário no formato `OurTalk-xxxxxxxx`, com oito dígitos hexadecimais minúsculos. Encerre a sessão ao sair do app ou ao expirar o prazo.

## Formato compartilhado

O formato v1 em uso no Android está implementado em [ChatFrame.java](../android/core/src/main/java/br/com/ourtalk/core/ChatFrame.java). Cada carga começa com um cabeçalho de 22 bytes em ordem big endian.

| Offset | Comprimento | Campo |
| --- | --- | --- |
| 0 | 1 | Versão `01` |
| 1 | 1 | Texto `00` ou recibo `01` |
| 2 | 16 | UUID da mensagem em bytes |
| 18 | 4 | Comprimento exato do corpo |
| 22 | Variável | Texto UTF-8 estrito ou corpo vazio para recibo |

O limite é 8192 bytes por carga e 2000 unidades UTF-16 por texto. Rejeite versão, tipo, comprimento ou UTF-8 inválidos, textos vazios após o trim equivalente ao Java e controles abaixo de U+0020, exceto tabulação e quebra de linha. O recibo repete o UUID da mensagem recebida e confirma chegada ao cliente, sem indicar leitura humana.

Vetores hexadecimais para o UUID `00112233-4455-6677-8899-aabbccddeeff`.

```text
Texto “Olá”  010000112233445566778899aabbccddeeff000000044f6cc3a1
Recibo       010100112233445566778899aabbccddeeff00000000
```

## Critérios de validação

- Testar Android ↔ Android e Android ↔ iPhone em aparelhos físicos. No teste misto, iniciar a descoberta e enviar mensagens nos dois sentidos.
- Repetir com dados móveis desativados e Wi-Fi sem acesso à internet, mantendo os rádios necessários ativos.
- Conferir recusa de código divergente, permissões negadas, recibos, desconexão e expiração da sessão.
- Para comunicação a distância, usar um protocolo comum entre clientes Android e iOS. Backend, identidade e grupos continuam como entregas próprias.

Referências oficiais do Google: [Nearby Connections entre plataformas](https://developers.google.com/nearby/) e [configuração do SDK Swift](https://developers.google.com/nearby/connections/swift/get-started).
