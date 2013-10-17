keytool -genkey -alias MakeJava -dname "cn=LUPOSDATE, c=de"
keytool -selfcert -alias MakeJava -dname "cn=LUPOSDATE, c=de"
jarsigner distributionGui/target/lupos.jar MakeJava
jarsigner -verify -verbose distributionGui/target/lupos.jar