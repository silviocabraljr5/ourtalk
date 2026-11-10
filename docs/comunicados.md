# Comunicados meteorológicos

Em Recursos → Comunicados, o usuário atualiza os avisos do INMET para Ribeirão Preto e pode consultar a cópia salva sem rede enquanto a coleta tiver menos de 45 minutos. O app mostra o período e um link para o aviso original. Uma coleta antiga é identificada como desatualizada. O recurso não envia notificações.

O servidor consulta o catálogo do INMET a cada 15 minutos. Filtra pelo código IBGE 3543402, mantém a sequência mais recente de cada aviso e exclui itens cancelados ou vencidos. A publicação é atômica. Falhas de origem preservam o arquivo anterior com o horário original. O Android consulta o mesmo arquivo público para todos os aparelhos e dispensa acesso ao GPS. [Fonte oficial](https://portal.inmet.gov.br/servicos/avisos-especiais)

A verificação isolada cobriu o filtro municipal, o cancelamento, a validade e a conversão de horário. A consulta real por HTTPS retornou um resumo válido. A verificação em aparelho e a inclusão em uma versão da Google Play seguem pendentes.
