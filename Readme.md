# Documentação
### Argumentos de vm
### Obrigatorios
- acessKey (String) -> KEY DE ACESSO
- server (String) ->SERVIDORES
### Opcionais
- topicResponse (String) = [LOCATION_DETECTOR_RESPONSE] -> TOPICO DE RESPOSTA
- topicRequest (String) = [LOCATION_DETECTOR_REQUEST] -> TOPICO DE ESCUTA
- https (boolean) = [TRUE] -> USA HTTPS?
- groupId (String) =  [LOCATION_DETECTOR] -> GRUPO DE LEITURA
- maxCacheIp (int) = [1000] -> QUANTIDADE MAXIMA DE IPS EM CACHE
- cacheTimeout(int) = [30] > TEMPO MAXIMO DE CACHE EM MINUTOS

## Testes
para se executar os testes:
- kafka  em localhost:9092
- Token da api em:
> teste/java/br.com.victorandrej.locationdetector.ApiToken.TOKEN


