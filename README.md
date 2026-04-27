# 📋 TaskFlow — Android To-Do List App

A clean, feature-rich Android productivity app built with Java. TaskFlow lets users manage daily tasks with priority levels, real-time weather awareness, and a modern dark UI — all without requiring a backend or paid API.

---

## 📸 Screenshots

<img width="1080" height="2220" alt="Screenshot_20260426_215253" src="https://github.com/user-attachments/assets/b0164bc5-db01-4acd-86b6-5823f64d4a2e" />

---

## ✨ Features

- **Task Management** — Add, complete, and delete tasks with a single tap
- **Priority Levels** — Tag each task as Low, Medium, or High priority with color-coded indicators
- **Task Completion** — Check off tasks with a satisfying strikethrough animation
- **Live Weather Widget** — Displays current temperature, conditions, wind speed, and daily high/low using the device's GPS location
- **No API Key Required** — Weather data is fetched from [Open-Meteo](https://open-meteo.com/), a fully free and open weather API
- **Dynamic Task Counter** — Header updates in real time to reflect how many tasks are active
- **Modern Dark UI** — Deep purple/navy color scheme with Material Design cards and a Floating Action Button

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| UI Framework | Android SDK + Material Design 3 |
| Weather API | Open-Meteo (REST, no auth required) |
| Location | Google Play Services — FusedLocationProviderClient |
| Networking | HttpURLConnection (no third-party library needed) |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 (Android 14) |

---

## 🏗 Architecture & Design Decisions

**Why Open-Meteo?**  
Most weather APIs (OpenWeatherMap, WeatherAPI) require account registration and have rate limits on free tiers. Open-Meteo is open-source, requires zero authentication, and has no rate limits for non-commercial use — making it ideal for a portfolio project that anyone can clone and run instantly.

**Why HttpURLConnection over Retrofit?**  
To keep the project dependency-free for the networking layer and demonstrate understanding of raw HTTP requests, threading with `ExecutorService`, and `Handler` for posting results back to the main thread.

**Threading model**  
Network calls run on a dedicated `ExecutorService` thread pool. UI updates are dispatched back via `Handler(Looper.getMainLooper())` — following Android's strict rule that only the main thread may touch views.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android device or emulator running API 24+
- Internet connection (for weather data)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/taskflow-android.git
   ```

2. Open the project in Android Studio:
   ```
   File → Open → select the project folder
   ```

3. Let Gradle sync finish, then run on a device or emulator:
   ```
   Run → Run 'app'  (or press Shift+F10)
   ```

4. When prompted, grant **location permission** to enable the weather widget.

> No API keys or environment variables needed — the app works out of the box.

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/to_dolistapp/
│   └── MainActivity.java        # Core logic: tasks, weather, dialog, threading
├── res/
│   ├── layout/
│   │   ├── activity_main.xml    # Root layout with weather card + FAB
│   │   ├── card.xml             # Individual task card with checkbox + priority dot
│   │   └── dialog.xml           # Add-task dialog with priority selector
│   ├── drawable/
│   │   └── circle.xml           # Priority indicator dot shape
│   └── values/
│       └── themes.xml           # Dark dialog theme + app color scheme
└── AndroidManifest.xml          # Internet + location permissions
```

---

## 🔌 Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | Fetch weather data from Open-Meteo |
| `ACCESS_FINE_LOCATION` | Get precise GPS coordinates for accurate local weather |
| `ACCESS_COARSE_LOCATION` | Fallback if fine location is unavailable |

---

## 🌤 Weather API Details

This app uses the [Open-Meteo Forecast API](https://open-meteo.com/en/docs):

```
GET https://api.open-meteo.com/v1/forecast
  ?latitude={lat}
  &longitude={lon}
  &current=temperature_2m,weathercode,windspeed_10m
  &daily=temperature_2m_max,temperature_2m_min
  &temperature_unit=fahrenheit
  &forecast_days=1
  &timezone=auto
```

Weather condition codes (WMO standard) are mapped to human-readable descriptions and emoji icons within the app — no external icon library required.

---

## 🔮 Potential Future Improvements

- [ ] Persist tasks using **Room Database** so they survive app restarts
- [ ] Add **due dates** with calendar picker and local notifications via `WorkManager`
- [ ] **Swipe to delete** gesture on task cards
- [ ] **Reorder tasks** with drag-and-drop
- [ ] Export task list to a text file or share via Android's share sheet

---
