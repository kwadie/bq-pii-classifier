#!/bin/sh

# exit script when errors occur
set -e

# set the working dir as the scripts directory
cd "$(dirname "$0")"

cd ../terraform

terraform init \
    -backend-config="bucket=${BUCKET_NAME}" \
    -backend-config="prefix=terraform-state"

terraform workspace select $CONFIG

terraform apply -lock=false -var-file=$VARS -auto-approve