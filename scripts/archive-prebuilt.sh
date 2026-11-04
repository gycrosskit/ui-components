#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${VERSION:?Pass the candidate or release version; this script does not upload or tag}"
export GROUP=com.github.gycrosskit.ui-components
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
# staging 为本脚本独占的生成仓库，不能把旧版本混入候选归档。
rm -rf build/maven
bash gradlew --init-script "$PWD/scripts/ci-repositories.gradle" --no-daemon --max-workers=1 -Dorg.gradle.parallel=false :ui-components:publishAllPublicationsToStagingRepository
python3 scripts/jitpack-metadata.py build/maven/com/github/gycrosskit/ui-components
python3 scripts/check-maven.py build/maven com.github.gycrosskit.ui-components "$VERSION" ui-components ios_arm64,ios_x64,ios_simulator_arm64,ohos_arm64 ui-components,ui-components-android,ui-components-iosarm64,ui-components-iosx64,ui-components-iossimulatorarm64,ui-components-ohosarm64
mkdir -p build/prebuilt
COPYFILE_DISABLE=1 tar --no-xattrs -czf build/prebuilt/ui-components-maven.tar.gz -C build/maven com/github/gycrosskit/ui-components
(cd build/prebuilt && shasum -a 256 ui-components-maven.tar.gz > ui-components-maven.tar.gz.sha256)
