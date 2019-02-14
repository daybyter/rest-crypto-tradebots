#!/bin/sh
java -ms512m -mx4096m -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=changeit -jar "tradeapp-0.2.0.jar" -daemon -startserver Yunga $@

