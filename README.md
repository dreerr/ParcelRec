# ParcelRec

![Handle with Care at FORUM STADTPARK](https://julian.palacz.at/site/assets/files/1422/210507_handle_with_care_c_lena_prehal_002_no_visitor-1.1300x0.jpg)

## Overview
ParcelRec is an Android application designed to log sensor data and environmental information to analyze the movement and handling of parcels during transportation. As a foundation for the [Handle with Care](https://julian.palacz.at/en/traces/handle-with-care) project, this app captures a variety of data streams, including motion, location, and environmental changes, to provide insights into the journey of a package.

## Features
### Sensor Logging
- **Accelerometer**: Logs linear acceleration to detect movement and impacts.
- **Gyroscope**: Captures angular velocity to monitor rotation.
- **Magnetometer**: Measures magnetic field changes.
- **GPS**: Tracks location and altitude.
- **Wi-Fi**: Logs nearby Wi-Fi networks (found and lost).
- **Battery**: Monitors battery level during operation.

### Video and Audio Recording
- Records video and audio when movement is detected.
- Configurable resolution, duration, and motion-only recording mode.

### Data Upload
- Periodic background uploads of log files and recordings to a server.
- Configurable upload intervals and server URLs.
- Backup server support for failover.

### User Interface
- Start/stop measurement with a single button.
- Configure camera and upload settings via intuitive dialogs.
- Real-time status display for all sensors and upload progress.

## Logging Logic
- **Movement Detection**: Logging is triggered when acceleration exceeds a configurable threshold. The device remains in a "moving" state for a defined duration after the threshold is exceeded.
- **Threshold Configuration**: Threshold values for sensors can be adjusted in the source code (`Config.kt`).
- **Sampling Frequency**: Adjustable for each sensor to balance performance and data granularity.

## Configuration
- **Camera Settings**: Resolution, recording duration, and motion-only mode can be configured via the UI or `Config.kt`.
- **Upload Settings**: Server URLs, upload intervals, and upload enablement are configurable via the UI.
- **Thresholds and Sampling Rates**: Modify `Config.kt` to adjust sensor thresholds and sampling rates.

## Permissions
The app requires the following permissions:
- **Camera**: For video recording.
- **Location**: For GPS tracking.
- **Microphone**: For audio recording.
- **Storage**: To save log files and recordings.
- **Internet**: For uploading data to the server.

## How to Use
1. **Setup**: Grant all required permissions when prompted.
2. **Start Logging**: Press the "START" button to begin logging and recording.
3. **Configure Settings**: Use the "Camera Settings" and "Upload Settings" buttons to adjust configurations.
4. **Stop Logging**: Press the "STOP" button to end the session and trigger an immediate upload of all collected data.

## Installation
- Minimum Android version: 8.0 (Oreo).
- Clone the repository and build the app using Android Studio.
- Install the APK on your device.

## Data Privacy
All collected data is stored locally on the device and uploaded securely to the configured server. Users are responsible for ensuring compliance with data protection regulations.