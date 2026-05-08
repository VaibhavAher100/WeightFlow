#!/bin/bash
# Test: What happens when credentials are missing?

export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

# Unset all signing env vars
unset KEYSTORE_PATH
unset KEYSTORE_PASSWORD
unset KEY_ALIAS
unset KEY_PASSWORD

# Create temp dir with no local.properties
TEMP_WF=$(mktemp -d)
cd "$TEMP_WF"
git clone https://github.com/VaibhavAher100/WeightFlow.git . 2>/dev/null
rm -f local.properties

echo "=== Testing validation with NO credentials ==="
./gradlew validateReleaseSigning 2>&1 | tail -20
