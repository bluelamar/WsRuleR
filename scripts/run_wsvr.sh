
. ./scripts/cpath_svr.sh

PROPS="-Dwsruler.test.login.user=wsruler -Dwsruler.test.login.secret=oneringtorule"

java -classpath "${CPATH}:./svr/target/wsruler-svr-1.0.0-SNAPSHOT.jar:./svr/target/classes" ${PROPS} org.bluelamar.wsruler.svr.WsSvrRunner

