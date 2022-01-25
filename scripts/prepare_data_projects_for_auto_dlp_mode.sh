#!/bin/bash

for project in "$@"
do

  echo "Preparing data project ${project} .."

    # Tagging Dispatcher needs to know the location of datasets
  gcloud projects add-iam-policy-binding "${project}" \
      --member="serviceAccount:${SA_TAGGING_DISPATCHER_EMAIL}" \
     --role="roles/bigquery.metadataViewer"

  # Tagger needs to read table schema and update tables policy tags
  gcloud projects add-iam-policy-binding "${project}" \
      --member="serviceAccount:${SA_TAGGER_EMAIL}" \
     --role="roles/bigquery.dataOwner"

done
