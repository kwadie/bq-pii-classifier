#!/bin/sh

echo "Setting access for TAGGING_DISPATCHER service account.. "

bq query --location $DATA_REGION --nouse_legacy_sql \
"GRANT \`roles/bigquery.dataViewer\` ON SCHEMA ${AUTO_DLP_DATASET} TO 'serviceAccount:${SA_TAGGING_DISPATCHER_EMAIL}'"

echo "Setting access for TAGGER service account.. "

bq query --location $DATA_REGION --nouse_legacy_sql \
"GRANT \`roles/bigquery.dataViewer\` ON SCHEMA ${AUTO_DLP_DATASET} TO 'serviceAccount:${SA_TAGGER_EMAIL}'"

