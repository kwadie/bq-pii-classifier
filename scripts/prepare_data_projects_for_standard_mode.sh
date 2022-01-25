#!/bin/bash

for project in "$@"
do

  echo "Preparing data project ${project} .."

  # Inspection Dispatcher needs to list datasets and tables in a project and know the location of datasets
  gcloud projects add-iam-policy-binding "${project}" \
      --member="serviceAccount:${SA_INSPECTION_DISPATCHER_EMAIL}" \
     --role="roles/bigquery.metadataViewer"

  # Tagging Dispatcher needs to know the location of datasets
  gcloud projects add-iam-policy-binding "${project}" \
      --member="serviceAccount:${SA_TAGGING_DISPATCHER_EMAIL}" \
     --role="roles/bigquery.metadataViewer"

  # Inspector needs to view table's metadata (row count)
  gcloud projects add-iam-policy-binding "${project}" \
     --member="serviceAccount:${SA_INSPECTOR_EMAIL}" \
     --role="roles/bigquery.metadataViewer"

  # Tagger needs to read table schema and update tables policy tags
  gcloud projects add-iam-policy-binding "${project}" \
      --member="serviceAccount:${SA_TAGGER_EMAIL}" \
     --role="roles/bigquery.dataOwner"

  # DLP service account needs to read and inspect bigquery data
  gcloud projects add-iam-policy-binding "${project}" \
     --member="serviceAccount:${SA_DLP_EMAIL}" \
      --role="roles/bigquery.dataViewer"

done
