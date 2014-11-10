#!/usr/bin/env bash
source variables.sh
scp -r src $USERNAME_P4@$HOST_P4:$PROJECT_P4
