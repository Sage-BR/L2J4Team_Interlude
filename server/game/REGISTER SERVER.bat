@echo off
title L2J4Team GameServer Registration Console
@java -Djava.util.logging.config.file=config/console.cfg -cp ./libs/*; com.l2j4team.gsregistering.GameServerRegister
@pause
