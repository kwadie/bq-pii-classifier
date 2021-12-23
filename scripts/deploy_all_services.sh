# exit script when errors occur
set -e

# set the working dir as the scripts directory
cd "$(dirname "$0")"

./deploy_common_services.sh

./deploy_inspection_services.sh

