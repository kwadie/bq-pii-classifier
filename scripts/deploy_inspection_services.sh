# exit script when errors occur
set -e

# set the working dir as the scripts directory
cd "$(dirname "$0")"

gcloud auth configure-docker $COMPUTE_REGION-docker.pkg.dev

cd ../services
mvn install

cd dispatcher-inspection-app
mvn compile jib:build -Dimage=$INSPECTION_DISPATCHER_IMAGE

cd ../inspector-app
mvn compile jib:build -Dimage=$INSPECTOR_IMAGE

cd ../listener-app
mvn compile jib:build -Dimage=$LISTENER_IMAGE

