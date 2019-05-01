#!/bin/bash

#https://gist.github.com/wsargent/11023607

#export PW=`pwgen -Bs 10 1`
export PW=!soterium1
export DIR=/mnt/xdata/work/projects/soterium/resource/keys/certs
rm -rf $DIR
mkdir $DIR

# Create a self signed certificate & private key to create a root certificate authority.
keytool -genkeypair -v \
  -alias SOTERIUM_DEV_CA \
  -keystore $DIR/soterium_dev_ca.jks \
  -dname "CN=SOTERIUM_DEV_CA, OU=PKI, O=LUSIDITY, L=FAIRFAX STATION, ST=VA, C=US" \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 2048 \
  -ext KeyUsage="keyCertSign" \
  -ext BasicConstraints="ca:true" \
  -storetype pkcs12 \
  -validity 3650
