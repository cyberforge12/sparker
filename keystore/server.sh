#!/bin/zsh

STORE="server.jks"
TRUST="server_truststore.jks"
ALIAS=server
PASS=password
EXPORT="server.cer"
IMPORT="client.cer"

keytool -genkeypair -keyalg RSA -keysize 2048 -alias $ALIAS -dname "CN=Basil,OU=Intern,O=FinCert,C=RU" -ext \
"SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore $STORE -storepass $PASS -keypass $PASS \
-deststoretype pkcs12

keytool -exportcert -keystore $STORE -storepass $PASS -alias $ALIAS \
 -rfc -file $EXPORT

keytool -keystore $TRUST -importcert -file $IMPORT -alias $ALIAS -storepass $PASS
