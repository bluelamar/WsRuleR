
. ./scripts/cpath.sh

GF=/home/mark/.m2/repository/org/glassfish/
GFC=${GF}/jersey/core/jersey-client/2.27/jersey-client-2.27.jar
GFCOM=${GF}/jersey/core/jersey-common/2.27/jersey-common-2.27.jar
JK2=${GF}/jersey/inject/jersey-hk2/2.27/jersey-hk2-2.27.jar
JAVAX=/home/mark/.m2/repository/javax
HK2API=${GF}/hk2/hk2-api/2.5.0-b42/hk2-api-2.5.0-b42.jar
HK2UTILS=${GF}/hk2/hk2-utils/2.5.0-b42/hk2-utils-2.5.0-b42.jar
HK2LOC=${GF}/hk2/hk2-locator/2.5.0-b42/hk2-locator-2.5.0-b42.jar
HK2INJ=${GF}/hk2/external/javax.inject/2.5.0-b42/javax.inject-2.5.0-b42.jar
ANNO=${JAVAX}/annotation/javax.annotation-api/1.2/javax.annotation-api-1.2.jar
CLASSPATH=/home/mark/Downloads:${HK2INJ}:${HK2LOC}:${HK2UTILS}:${HK2API}:${JK2}:${GFC}:${GFCOM}:${ANNO}:${JAVAX}/ws/rs/javax.ws.rs-api/2.1.1/javax.ws.rs-api-2.1.1.jar:./core/target/wsruler-core-client-1.0.0-SNAPSHOT.jar

#java -classpath "${CLASSPATH}" org.bluelamar.wsruler.RestConnectionTest

java -classpath "${CPATH}:./core/target/wsruler-core-client-1.0.0-SNAPSHOT.jar" org.bluelamar.wsruler.RestConnectionTest

