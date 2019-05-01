#!/bin/bash

#https://gist.github.com/wsargent/11023607

#export PW=`pwgen -Bs 10 1`
export PW=!soterium1
export DIR=/mnt/xdata/work/projects/soterium/resource/keys/certs
export FN="SOTERIUM_USER_2"
export CN=evans.bob.l.4444444444
export EMAIL=bob.l.evans@fake.com
export TITLE="Software Craftsman 2"
export STORE=soterium_dev_ca.jks

keytool -delete -alias $FN -keystore $DIR/$STORE -noprompt -storepass $PW

echo Create another key pair that will act as the client.  We want this signed by the client CA.
keytool -genkeypair -v \
  -alias $FN \
  -keystore $DIR/$STORE \
  -dname "CN=$CN, OU=PKI, OU=$TITLE, OU=VENIO, O=LUSIDITY, L=FAIRFAX STATION, ST=VA, C=US, EMAILADDRESS=$EMAIL" \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650

echo Create a certificate signing request from the client certificate.
keytool -certreq -v \
  -alias $FN \
  -keypass:env PW \
  -storepass:env PW \
  -keystore $DIR/$STORE \
  -storetype pkcs12 \
  -file $DIR/$FN.crq

echo Make SOTERIUM_DEV_CA create a certificate chain saying that client is signed by SOTERIUM_DEV_CA.
keytool -gencert -v \
  -alias SOTERIUM_DEV_CA \
  -keypass:env PW \
  -storepass:env PW \
  -keystore $DIR/$STORE \
  -infile $DIR/$FN.crq \
  -outfile $DIR/$FN.crt \
  -ext EKU="clientAuth" \
  -validity 1095\
  -storetype pkcs12 \
  -rfc

echo Export the client-ca certificate from the keystore.
echo and is presented in the CertificateRequest.
keytool -export -v \
  -alias SOTERIUM_DEV_CA \
  -file $DIR/SOTERIUM_DEV_CA.crt \
  -storepass:env PW \
  -keystore $DIR/$STORE \
  -rfc

echo Import the signed certificate back into $STORE.  This is important, as JSSE won't send a client
echo certificate if it can't find one signed by the client-ca presented in the CertificateRequest.
keytool -import -v \
  -alias $FN \
  -file $DIR/$FN.crt \
  -keystore $DIR/$STORE \
  -storetype JKS \
  -storepass:env PW

echo "Export the client to pkcs12, so it's safe."
keytool -importkeystore -v \
  -srcalias $FN \
  -srckeystore $DIR/$STORE \
  -srcstorepass:env PW \
  -destkeystore $DIR/$FN.p12 \
  -deststorepass:env PW \
  -deststoretype PKCS12