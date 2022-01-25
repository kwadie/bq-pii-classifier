#!/bin/bash

gcloud iam service-accounts create "${TF_SA}" \
    --description="Used by Terraform to deploy GCP resources" \
    --display-name="Terraform Service Account"

gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${TF_SA}@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/owner"

gcloud iam service-accounts add-iam-policy-binding \
    "${TF_SA}@${PROJECT_ID}.iam.gserviceaccount.com"    \
    --member="user:${ACCOUNT}" \
    --role="roles/iam.serviceAccountUser"

gcloud iam service-accounts add-iam-policy-binding \
    "${TF_SA}@${PROJECT_ID}.iam.gserviceaccount.com" \
    --member="user:${ACCOUNT}" \
    --role="roles/iam.serviceAccountTokenCreator"