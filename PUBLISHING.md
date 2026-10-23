# TapOut

## Releases

## Publishing / Updating on Google Play

1. Download the latest `TapOut-X.Y.Z.aab` and the matching `*-native-debug-symbols.zip` from the GitHub release assets (or build them yourself with `./gradlew bundleRelease`).
2. In Play Console, go to your app > **Release** > **Production** (or a test track) > **Create new release**.
3. Upload the `.aab` file.
4. **Important:** If you see the warning *"This App Bundle contains native code, and you've not uploaded debug symbols"*, click the 3-dot menu on the uploaded AAB and choose **Upload native debug symbols (.zip)**. Select the `TapOut-...-native-debug-symbols.zip` that was built alongside the AAB.
   - This is caused by a transitive dependency (`androidx.graphics-path`) that ships native `.so` libraries for graphics performance.
   - The `ndk { debugSymbolLevel = "FULL" }` setting in `app/build.gradle.kts` + the symbols zip ensure crashes and ANRs can be symbolicated in Play Console.
5. Fill in the release notes and roll out.

## Store listing graphics

Upload these from `docs/play-store/` on the Main store listing page. The application id stays `com.galynte.tapblok`.

* `icon-512.png` — 512×512, 32-bit PNG. This is the same full-bleed mark as `app/src/main/ic_launcher-playstore.png`. Play applies its own mask, so the file stays square.
* `feature-graphic.png` — 1024×500, 24-bit PNG with no transparency.

The GitHub Actions workflow automatically builds the AAB + APK and attaches all three artifacts (AAB, APK, and native debug symbols zip) directly to the GitHub release on every tagged release.