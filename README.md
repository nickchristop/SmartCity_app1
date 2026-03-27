# SmartCity_app1 🏙️

An interactive "Smart City" Android app designed to help citizens track and report local issues. 

## Features
* **Three-Window Layout**: Seamlessly navigate between the Home dashboard, Map, and the Reporting form using a sleek Bottom Navigation bar.
* **Interactive Map**: A hub designed for Google Maps SDK integration to visualize your city's active reports and issue density.
* **Real-time Reporting**: Integrated with Firebase Realtime Database. Users can quickly document issues (e.g. potholes, broken street lights) and submit them to be pinned globally.

## Setup Instructions
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync the Gradle files. 
4. Provide a valid `google-services.json` for Firebase connection (if not already included/available).
5. Build and run on an Android device or emulator!

## Tech Stack
* Language: Java
* UI System: Native Android XML Layouts 
* Backend: Firebase Realtime Database
* Architecture: Single-Activity with Fragment transitions (`BottomNavigationView`)

## 🛠️ What Needs Fixing / TODOs
* **Google Maps Integration**: The `MapFragment` is currently a placeholder. The Google Maps SDK needs to be initialized and configured to display the interactive map.
* **Reporting Logic**: The `ReportFragment` UI is ready, but the "Submit" button needs Java logic to capture inputs (Title, Description, Coordinates) and save them to the Firebase Realtime Database.
* **Data Synchronization**: The map needs to actively read from Firebase and generate pins/markers for everyone to see based on the active reports.
* **Location Services**: The app needs Android Location permissions to allow users to automatically attach their current GPS coordinates when submitting a new issue.
