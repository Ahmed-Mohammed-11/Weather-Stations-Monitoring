# Weather-Stations-Monitoring

Be cautious with _**`KAFKA_AUTO_CREATE_TOPICS_ENABLE`**_,<br> 
as you might inadvertently copy an incorrect value from a tutorial.<br>  
The `KAFKA_CFG_LISTENERS`, `KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP`, and` KAFKA_CFG_ADVERTISED_LISTENERS` are configured in such a way that<br> 
you can connect to Kafka **outside of the Docker** container via the` EXTERNAL` listener, with host` kafka` and port 9094.<br><br>  
Ensure that the hostname in the config matches the advertised listeners.


