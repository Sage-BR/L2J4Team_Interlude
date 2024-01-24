@echo off
title aCis geodata converter

java -Xmx512m -cp ./libs/*; com.l2j4team.geodataconverter.GeoDataConverter

pause
