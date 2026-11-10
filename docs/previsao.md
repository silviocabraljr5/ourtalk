# Previsão do tempo

Em Início ou Recursos, abra **Previsão do tempo** e toque em **Atualizar previsão**. O Android guarda a última resposta válida para consulta offline. A tela identifica a data e a hora da obtenção da previsão. Após 12 horas, mantém a informação de quando a previsão foi salva e pede atualização antes de mostrar os valores. Uma falha de rede preserva o arquivo anterior.

O servidor consulta três dias de previsão para coordenadas fixas do centro de Ribeirão Preto. O Android baixa o mesmo arquivo público para todos os usuários, sem pedir GPS nem enviar a posição do aparelho. A coleta está agendada a cada três horas e publica o JSON por substituição atômica. Os dados são de [Open-Meteo](https://open-meteo.com/) sob CC BY 4.0. A previsão é regional. Alertas e comunicados oficiais seguem como recursos separados.

Para manutenção, instale `android/tools/publish_ribeirao_weather.py` fora da raiz pública e execute a cada três horas com o usuário da hospedagem, passando o caminho de `weather-ribeirao.json`. A publicação rejeita respostas incompletas ou inválidas.
