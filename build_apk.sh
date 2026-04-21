#!/usr/bin/env bash
set -e

# ── Paths ────────────────────────────────────────────────────────────────────
SDK=/usr/lib/android-sdk
PLATFORM=$SDK/platforms/android-23
ANDROID_JAR=$PLATFORM/android.jar
BT=$SDK/build-tools/debian
AAPT=$BT/aapt
ZIPALIGN=$BT/zipalign
APKSIGNER=$BT/apksigner

SRC_ROOT=$(pwd)/app/src/main
RES=$SRC_ROOT/res
MANIFEST=$SRC_ROOT/AndroidManifest.xml
JAVA_SRC=$SRC_ROOT/java

BUILD=$(pwd)/build_out
mkdir -p $BUILD/gen $BUILD/obj $BUILD/dex $BUILD/apk_staging

echo "==> Generating R.java with aapt..."
$AAPT package -f -m \
    -S $RES \
    -J $BUILD/gen \
    -M $MANIFEST \
    -I $ANDROID_JAR

echo "==> Compiling Java sources..."
find $JAVA_SRC $BUILD/gen -name "*.java" > $BUILD/sources.txt
javac -source 8 -target 8 \
    -bootclasspath $ANDROID_JAR \
    -classpath $ANDROID_JAR \
    -d $BUILD/obj \
    @$BUILD/sources.txt

echo "==> Converting to DEX..."
dalvik-exchange --dex --output=$BUILD/dex/classes.dex $BUILD/obj

echo "==> Packaging resources into APK..."
$AAPT package -f \
    -S $RES \
    -M $MANIFEST \
    -I $ANDROID_JAR \
    -F $BUILD/apk_staging/battle-city-unsigned.apk

echo "==> Adding classes.dex..."
(cd $BUILD/dex && zip -j $BUILD/apk_staging/battle-city-unsigned.apk classes.dex)

echo "==> Generating debug keystore..."
KEYSTORE=$BUILD/debug.keystore
if [ ! -f $KEYSTORE ]; then
    keytool -genkeypair -v \
        -keystore $KEYSTORE \
        -alias androiddebugkey \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass android -keypass android \
        -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null
fi

echo "==> Signing APK..."
UNSIGNED=$BUILD/apk_staging/battle-city-unsigned.apk
SIGNED=$BUILD/battle-city-debug.apk
$APKSIGNER sign \
    --ks $KEYSTORE \
    --ks-key-alias androiddebugkey \
    --ks-pass pass:android \
    --key-pass pass:android \
    --in $UNSIGNED \
    --out $SIGNED

echo "==> Aligning APK..."
ALIGNED=$(pwd)/battle-city.apk
$ZIPALIGN -f 4 $SIGNED $ALIGNED

echo ""
echo "✓ Build complete: $ALIGNED"
ls -lh $ALIGNED
