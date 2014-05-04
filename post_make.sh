#!/bin/sh

if [ $(hostname) = "beta" ] || [ $(hostname) = "lt" ]
then
# nur für den Entwicklungsrechner sinnvoll

dir=`dirname "$0"`
cd "$dir"


cp -r res/* dist
cp -r dist/lib/* libs

# Aufräumen
rm dist/README.TXT

# Anlegen
mkdir dist/info

# release
relNr=$(cat src/version.properties | grep BUILD | sed 's#BUILD=##g')
datum=$(date +%d.%m.%Y )
echo Datum: $datum >> dist/info/$relNr.build
echo MSearch Buildnummer: $relNr >> dist/info/$relNr.build

# zip erstellen
cd dist/
datum=$(date +%Y.%m.%d )
zip -r MSearch_$datum.zip .

cd $OLDPWD

fi
