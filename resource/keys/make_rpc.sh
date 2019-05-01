#!/bin/bash

#https://gist.github.com/wsargent/11023607

#export PW=`pwgen -Bs 10 1`
export PW=!soterium1
export DIR=/mnt/xdata/work/projects/soterium/resource/keys/certs
export FN=SOTERIUM_RPC
export CN=rpc.soterium-dev.com
export EMAIL=partners@venioinc.com
export TITLE="SOTERIUM RPC"
export STORE=soterium_dev_ca.jks

keytool -delete -alias $FN -keystore $DIR/$STORE -noprompt -storepass $PW

if true
then
# Create another key pair that will act as the client.  We want this signed by the client CA.
keytool -genkeypair -v \
  -alias $FN \
  -keystore $DIR/$STORE \
  -dname "CN=$CN, OU=$TITLE, OU=VENIO, O=LUSIDITY, L=FAIRFAX STATION, ST=VA, C=US, EMAILADDRESS=$EMAIL" \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650

# Create a certificate signing request from the client certificate.
keytool -certreq -v \
  -alias $FN \
  -storepass:env PW \
  -keypass:env PW \
  -storepass:env PW \
  -keystore $DIR/$STORE \
  -file $DIR/$FN.crq

# Make SOTERIUM_DEV_CA create a certificate chain saying that client is signed by SOTERIUM_DEV_CA.
# EKU can be serverAuth, clientAuth, codeSigning, emailProtection, timeStamping, OCSPSigning, or any OID string.
keytool -gencert -v \
  -alias SOTERIUM_DEV_CA \
  -storepass:env PW \
  -keypass:env PW \
  -storepass:env PW \
  -keystore $DIR/$STORE \
  -infile $DIR/$FN.crq \
  -outfile $DIR/$FN.crt \
  -ext EKU="clientAuth,serverAuth" \
  -validity 3650 \
  -rfc

# Export the client-ca certificate from the keystore.
# and is presented in the CertificateRequest.
keytool -export -v \
  -alias SOTERIUM_DEV_CA \
  -storepass:env PW \
  -file $DIR/SOTERIUM_DEV_CA.crt \
  -storepass:env PW \
  -keystore $DIR/$STORE \
  -rfc

# Import the signed certificate back into $STORE.  This is important, as JSSE won't send a client
# certificate if it can't find one signed by the client-ca presented in the CertificateRequest.
keytool -import -v \
  -alias $FN \
  -file $DIR/$FN.crt \
  -keystore $DIR/$STORE \
  -storetype JKS \
  -storepass:env PW

  # Export the client to pkcs12, so it's safe.
keytool -importkeystore -v \
  -srcalias $FN \
  -srckeystore $DIR/$STORE \
  -srcstorepass:env PW \
  -destkeystore $DIR/$FN.p12 \
  -deststorepass:env PW \
  -deststoretype PKCS12
fi