#!/usr/bin/env bash
export USERNAME_P4=ivmalopi
export PROJECT_P4=/u/$USERNAME_P4/Documents/p434-assignment5

export HOST_P4=lh115linux-01.soic.indiana.edu
export BROKER_P4=129.79.49.181
export TUNNELPORT_P4=43413
export SERVERID_P4=$(id -u)$(hostname | cut -d'.' -f1 | cut -d'-' -f2)