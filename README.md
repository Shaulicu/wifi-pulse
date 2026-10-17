# WiFi Pulse 📶

A native Java Android app showing your current Wi-Fi SSID, signal strength, frequency, negotiated link speed, IPv4 address, and access point BSSID. Dark dashboard, refresh every 3 seconds.

## Build in GitHub Actions (no computer needed)

1. Create an **empty** repository called `wifi-pulse` on GitHub (preferably **Private**). Do not add an initial README.
2. From the repository, open **Code → Codespaces → Create codespace on main** (may require available Codespaces quota).
3. In the Codespaces browser editor, upload the project ZIP to the **repository root** using the file explorer context menu **Upload...**. The ZIP should show as `WiFi-Pulse-GitHub.zip`.
4. Open the **Terminal** inside Codespaces and run:

   ```bash
   unzip -o WiFi-Pulse-GitHub.zip
   rm WiFi-Pulse-GitHub.zip
   git add .
   git commit -m "Add WiFi Pulse Android app and APK workflow"
   git push origin main
   ```

5. Open GitHub **Actions → Build Android APK**. A push to `main` automatically starts a build; alternatively choose **Run workflow**.
6. Wait for a green check. Open the completed run and download the **WiFi-Pulse-debug-APK** artifact, then extract the ZIP and install `app-debug.apk` on Android.

The resulting **debug APK** is for personal testing, not a Play Store release. Android may ask to allow APK installs from your browser or Files app. Only install an APK you built from your trusted repository.

### Alternative when Codespaces unavailable

Use GitHub's web editor (`github.dev`) or a Git-enabled terminal application such as Termux to upload the *extracted directory contents*, preserving `.github/workflows/`, `app/` and build files. Uploading the ZIP directly into the repository without extracting it will **not** build an APK.

## Android permissions

Wi-Fi metadata is read locally. Android usually requires **Precise location permission** and Location services enabled to reveal SSID/BSSID; permission alone may not be sufficient on every vendor/Android version. The app does not request INTERNET permission or transmit network data.

## Tech stack

- Native Java / Android Views; minSdk 26, compile/target SDK 35
- Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 17
- Cloud build: `.github/workflows/android-apk.yml`

## Limitations

The APK has **not** been built or device-tested in the creation environment. The GitHub Actions run is the first real compilation check. Some Wi-Fi properties can be redacted by Android or device vendors. A connected Wi-Fi network may be shown even when mobile data is the default internet route.
