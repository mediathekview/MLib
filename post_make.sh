#!/bin/sh

if [ $(hostname) = "beta" ]
then
# nur für den Entwicklungsrechner sinnvoll

cp -r /mnt/daten/software/Mediathek/MSearch/bin/* /mnt/daten/software/Mediathek/MSearch/dist

# Aufräumen
rm /mnt/daten/software/Mediathek/MSearch/dist/README.TXT

# Anlegen
mkdir /mnt/daten/software/Mediathek/MSearch/dist/info

# release
relNr=$(cat /mnt/daten/software/Mediathek/MSearch/src/version.properties | grep BUILD | sed 's#BUILD=##g')
datum=$(date +%d.%m.%Y )
echo Datum: $datum >> /mnt/daten/software/Mediathek/MSearch/dist/info/$relNr.build
echo MSearch Buildnummer: $relNr >> /mnt/daten/software/Mediathek/MSearch/dist/info/$relNr.build

# zip erstellen
cd /mnt/daten/software/Mediathek/MSearch/dist/
datum=$(date +%Y.%m.%d )
zip -r MSearch_$datum.zip .

# Dateien ins share-Verzeichnis von VmWare kopieren
cp -r /mnt/daten/software/Mediathek/MSearch/dist/* /mnt/daten/virtualbox/share/MSearch

fi
