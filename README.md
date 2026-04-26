# SmartCity Hub 🏙️

> A participatory Smart City Android platform for the Municipality of Marousi, enabling citizens to discover, report, and track local infrastructure hazards in real time.

---

## Overview

**SmartCity Hub** is a native Android application (Java) built on a clean **MVVM architecture** with Firebase as the backend. The platform empowers citizens to geolocate and submit hazard reports — potholes, broken lighting, flooding, graffiti — which are instantly surfaced on a live map for the entire community.

The app targets the Municipality of Marousi (Athens, GR) as its operational zone, with the default map view centred at the Marousi civic area (`38.04095, 23.81654`) at a hyper-local 19f street-level zoom.

---

## ✅ Implemented Features

### 🗺️ Precision Map Interface
- Full **Google Maps SDK** integration with a real-time `SupportMapFragment`.
- **High-accuracy GPS centering** via `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY)`.
- Native **blue dot** user-position indicator (`setMyLocationEnabled`).
- Custom `setOnMyLocationButtonClickListener` with `return true` to override the default zoom-out and enforce a strict **19f hyper-local zoom** on every GPS center tap.
- **Map tap-to-pin** feature: citizens can drop a custom pin anywhere on the map to pre-populate the report form location, bypassing the GPS requirement.

### 🔍 Advanced Address Search
- Floating `MaterialCardView` search bar with a live `EditText` wired to `Geocoder.getFromLocationName()`.
- Results animate to **19f zoom** to match the GPS centering experience.
- **Timestamp debounce** (`lastSearchTime` guard, 1-second window) prevents race conditions and duplicate Geocoder calls on hardware Enter key double-fire.
- Errors surfaced via **Material Snackbar** (anchored, swipe-dismissible) — not ephemeral Toasts.

### 📋 Real-time Hazard Dashboard
- **Reactive UI** powered by `LiveData` observing a Firebase Realtime Database stream through the Repository pattern.
- **Active / Past** tab segmentation via `TabLayout`.
- Each hazard row (`MaterialCardView`) displays title, description, timestamp, and a coloured **Material Design 3 status badge** (`?attr/colorSecondary` / `?attr/colorSurfaceVariant`).
- Tap any hazard to open a full-detail view (`HazardDetailFragment`) with location, submitter, and attached images.

### 📝 Authenticated Hazard Reporting
- Mandatory **Firebase Authentication** gate — anonymous users are redirected to the Account tab.
- Report form (`ReportBottomSheet`) captures: title, description, GPS coordinates (or map-pinned location), and up to **5 photos** (camera or gallery).
- Images uploaded to **Firebase Storage**; download URLs stored in the report document.
- Pre-validation: title, description, and at least one photo are all required before submission.

### 🌗 Full Dark Mode Support
- All layouts use **semantic Material 3 colour attributes** (`?attr/colorSurface`, `?attr/colorOnSurface`, `?attr/colorOnBackground`) — zero hardcoded hex values for text or backgrounds.
- Night-mode **custom map style** applied via `R.raw.map_style_dark` when dark theme is active.
- `colorPrimary` in `values-night/themes.xml` set to **Teal `#008080`** to eliminate the default lilac accent.

### ⚙️ Settings & Localisation
- **Light / Dark** theme toggle with `AppCompatDelegate.setDefaultNightMode()` + `Activity.recreate()` for instant application.
- **English / Greek (Ελληνικά)** full localisation via `attachBaseContext` locale injection.
- **Device GPS Settings** shortcut button (`ACTION_LOCATION_SOURCE_SETTINGS` Intent).
- Location permission denied → silent fallback to Marousi default coordinates (`38.04169, 23.80496`).

### 👤 Account & Profile
- Firebase Auth email/password sign-in and registration.
- Editable name and age fields persisted to Firestore user profile.
- **"Total Reports Submitted"** stat card on the profile screen.
- Glass-morphism `MaterialCardView` profile container with a translucent white stroke.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│                     UI Layer                    │
│  MainActivity · MapFragment · HazardsFragment   │
│  ReportBottomSheet · AccountFragment            │
│  SettingsFragment · HazardDetailFragment        │
└────────────────────┬────────────────────────────┘
                     │ observes LiveData
┌────────────────────▼────────────────────────────┐
│               ViewModel Layer                   │
│        MapViewModel · AuthViewModel             │
└────────────────────┬────────────────────────────┘
                     │ calls
┌────────────────────▼────────────────────────────┐
│              Repository Layer                   │
│             FirebaseRepository                  │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│                 Data Sources                    │
│  Firebase Realtime DB · Firebase Auth           │
│  Firebase Storage · SharedPreferences           │
└─────────────────────────────────────────────────┘
```

| Layer | Pattern |
|---|---|
| UI | Single-Activity, Navigation via `BottomNavigationView` |
| State management | `ViewModel` + `LiveData` (lifecycle-aware) |
| Backend | Firebase Realtime Database (NoSQL, real-time sync) |
| Auth | Firebase Authentication (email/password) |
| Storage | Firebase Storage (image uploads) |
| Language | Java 11 |
| UI toolkit | Material Design 3 (`Theme.Material3.DayNight`) |

---

## 🛠️ Tech Stack

| Technology | Version / Notes |
|---|---|
| Android SDK | `minSdk 24` · `targetSdk 34` |
| Google Maps SDK | `play-services-maps` |
| FusedLocationProvider | `play-services-location` (Priority API) |
| Firebase BoM | Realtime Database · Auth · Storage |
| Material Components | Material 3 |
| Architecture | MVVM + Repository pattern |
| Build system | Gradle (Kotlin DSL) |

---

## 🚀 Setup Instructions

1. Clone the repository.
2. Open in **Android Studio Hedgehog** (or later).
3. Place your project's `google-services.json` in `app/`.
4. Add your Maps API key to `AndroidManifest.xml`:
   ```xml
   <meta-data android:name="com.google.android.geo.API_KEY"
              android:value="YOUR_KEY_HERE" />
   ```
5. Sync Gradle, build, and run on a device or emulator (API 24+).

---

## 🔭 Future Roadmap

| Feature | Description |
|---|---|
| **AI Hazard Categorisation** | Integrate ML Kit or a custom TFLite model to automatically tag uploaded photos by hazard type (road damage, flooding, graffiti, etc.) |
| **Citizen Upvoting** | Allow registered users to upvote existing reports, surfacing high-impact issues for prioritised municipal response |
| **Marker Clustering** | Stress-test with 200+ stub data entries and implement `MarkerClusterManager` to evaluate and optimise rendering performance at city scale |
| **Push Notifications** | FCM-based alerts when a citizen's submitted report changes status (e.g. "In Progress" → "Resolved") |
| **Offline Mode** | Firebase offline persistence + local Room cache for report submission without connectivity |

---

## 📝 Development Notes

This project was developed with iterative code optimization and debugging sessions assisted by **Antigravity** (Google DeepMind), which enabled parallel processing of multi-file surgical edits — accelerating the MVVM refactor, Dark Mode migration, and GPS logic hardening significantly.

---

## 📄 License

Academic / demonstration use only. Not affiliated with the Municipality of Marousi.
