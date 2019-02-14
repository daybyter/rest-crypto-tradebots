#!/bin/sh
java -ms64m -mx192m -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=changeit -jar "tradeapp-0.2.0.jar" -daemon $@

