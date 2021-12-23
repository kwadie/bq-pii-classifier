# exit script when errors occur
set -e

# set the working dir as the scripts directory
cd "$(dirname "$0")"

gcloud auth configure-docker $COMPUTE_REGION-docker.pkg.dev

cd ../services
mvn install

cd dispatcher-tagging-app
mvn compile jib:build -Dimage=$TAGGING_DISPATCHER_IMAGE

cd ../tagger-app
mvn compile jib:build -Dimage=$TAGGER_IMAGE

