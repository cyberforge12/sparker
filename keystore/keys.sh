#!/bin/zsh

PASS=password

SERVERSTORE="server.jks"
SERVERTRUST="server_truststore.jks"
SERVERALIAS=server
SERVERCERT="server.cer"

CLIENTSTORE="client.jks"
CLIENTTRUST="client_truststore.jks"
CLIENTALIAS=client
CLIENTCERT="client.cer"

keytool -genkeypair -keyalg RSA -keysize 2048 -alias $SERVERALIAS -dname "CN=server_out,O=Server,C=RU" -ext \
"SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore $SERVERSTORE -storepass $PASS -keypass $PASS \
-deststoretype pkcs12

keytool -genkeypair -keyalg RSA -keysize 2048 -alias $CLIENTALIAS -dname "CN=client_out,O=Client,C=RU" \
 -validity 3650 -keystore $CLIENTSTORE -storepass $PASS -keypass $PASS \
-deststoretype pkcs12

keytool -exportcert -keystore $SERVERSTORE -storepass $PASS -alias $SERVERALIAS \
 -rfc -file $SERVERCERT
keytool -exportcert -keystore $CLIENTSTORE -storepass $PASS -alias $CLIENTALIAS \
 -rfc -file $CLIENTCERT

keytool -keystore $SERVERTRUST -importcert -file $CLIENTCERT -alias $SERVERALIAS -storepass $PASS
keytool -keystore $CLIENTTRUST -importcert -file $SERVERCERT -alias $CLIENTALIAS -storepass $PASS
