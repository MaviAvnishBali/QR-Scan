#!/bin/bash

echo "🚀 Building optimized QR Scan app..."
echo "=================================="

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build release AAB
echo "📦 Building release AAB..."
./gradlew bundleRelease

# Build release APKs
echo "📱 Building release APKs..."
./gradlew assembleRelease

echo ""
echo "📊 Build Results:"
echo "=================="

# Show AAB size
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    AAB_SIZE=$(ls -lh app/build/outputs/bundle/release/app-release.aab | awk '{print $5}')
    echo "📦 Release AAB: $AAB_SIZE"
else
    echo "❌ AAB not found"
fi

# Show APK sizes
echo ""
echo "📱 Release APKs:"
if [ -d "app/build/outputs/apk/release" ]; then
    ls -lh app/build/outputs/apk/release/*.apk | awk '{print "   " $9 " - " $5}'
else
    echo "   ❌ APKs not found"
fi

echo ""
echo "✅ Build completed!"
echo ""
echo "💡 Size reduction tips:"
echo "   - AAB should be smaller than previous 21MB"
echo "   - Individual APKs should be much smaller than universal"
echo "   - Check app/build/outputs/ for all generated files"
