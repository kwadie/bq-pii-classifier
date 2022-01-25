#!/bin/bash

# exit script when errors occur
set -e

# set the working dir as the scripts directory
cd "$(dirname "$0")"


./deploy_all_services.sh

./deploy_terraform.sh
