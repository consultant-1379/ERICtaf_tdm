# Need to switch to the latest tag, created by Maven release plugin, to get the release code

# Get new tags from the remote
git fetch --tags

# Get the latest tag name
latestTag=$(git describe --tags `git rev-list --tags --max-count=1`)

# Checkout the latest tag
git checkout $latestTag
