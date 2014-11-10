#!/usr/bin/env bash
source variables.sh
ssh -vf -N -L $TUNNELPORT_P4:$BROKER_P4:61616 $USERNAME_P4@$HOST_P4
