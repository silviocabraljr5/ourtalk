# Segurança no OurTalk

O projeto está em desenvolvimento. A versão Android interna não passou por auditoria independente e não deve ser usada como único meio de comunicação ou orientação em emergências.

## Relatar uma vulnerabilidade

Use o relato privado de vulnerabilidades do GitHub, se estiver habilitado na aba Security. Se não estiver, abra uma issue solicitando um canal privado, sem detalhes exploráveis. Ainda não há endereço de segurança dedicado nem prazo de resposta contratado. Não publique chaves, contatos, coordenadas, históricos ou comprovantes em issues, prints ou logs.

## Regras para alterações

- Nenhuma credencial, chave de assinatura, arquivo de ambiente, banco real ou exportação pessoal entra no Git. Exemplos usam dados sintéticos.
- Permissões são solicitadas na função correspondente. Biblioteca e checklist não dependem da aceitação de localização ou conexão próxima.
- Presença próxima exige opt-in, prazo e encerramento visível. Nomes locais não autenticam pessoas. Comparação de código é obrigatória nos dois aparelhos.
- Não criar criptografia de transporte própria. Mudanças de dependências exigem revisar documentação, compatibilidade, manifestos mesclados e testes.
- Não mostrar entrega, leitura, identidade verificada, alcance ou segurança de rota sem evidência real.
- Falha ao autenticar armazenamento protegido não autoriza apagamento automático nem retorno a texto aberto.
- Grupos de rede, retransmissão e identidade permanente exigem projeto e revisão próprios antes de liberação.

O modelo de ameaças e as limitações estão em [android/docs/seguranca.md](android/docs/seguranca.md). A matriz de validação em aparelhos reais está em [android/docs/testes-dois-aparelhos.md](android/docs/testes-dois-aparelhos.md).
