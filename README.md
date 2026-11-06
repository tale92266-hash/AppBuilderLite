# AppBuilder Lite

A professional visual Android app builder that runs directly on an Android
phone. Design interfaces with drag-and-drop, then export a complete,
buildable Android Studio project (Java + XML + Gradle + Manifest).

- Language: **Java only** (no Kotlin, no Jetpack Compose)
- UI: Android XML + Material Components
- Min SDK: 26 (Android 8.0) · Compile/Target SDK: 35
- Database: SQLite · JSON: Gson
- Build stack: Gradle 8.11.1 + Android Gradle Plugin 8.9.2 + JDK 17

## Phase 1 — DONE (2026-11-06)

Project foundation, dashboard, SQLite and project models.

**Included**

- Gradle setup (root + app `build.gradle`, `settings.gradle`,
  `gradle.properties`, wrapper 8.11.1 with jar included so ACS import
  does not hang on indexing)
- `AndroidManifest.xml` (MainActivity launcher, DashboardActivity,
  FileProvider)
- Dashboard: branding, Create New Project dialog (app name, package
  name, min SDK, initial screen name), recent-projects list with cards,
  search, rename, duplicate, delete, JSON import via Storage Access
  Framework, settings (dark theme, autosave)
- SQLite: 5 tables — `projects`, `screens`, `assets`,
  `project_history`, `project_settings` — foreign keys on, transactions
  for create/duplicate
- Models: `ProjectModel`, `ScreenModel`, `ComponentModel`, `EventModel`
  (Gson, schema version 1)
- Utils: `IDGenerator`, `DimensionUtils`, `FileUtils`

**Not in Phase 1 (later phases)**

- Phase 2: JSON engine + component registry
- Phase 3: Canvas editor + drag-and-drop (`DesignCanvasView`)
- Phase 4: Properties inspector + hierarchy
- Phase 5: Undo/redo + autosave
- Phase 6: Native preview renderer
- Phase 7: XML + Java generators
- Phase 8: Full Android Studio project export as ZIP
- Phase 9: Testing + performance

Tapping a project card opens its details dialog. The visual editor
(Phase 3) will be wired to the card tap then.

## Build

On the phone (Android Code Studio / ACS):

```
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions also builds every push to `main`
(`.github/workflows/build.yml`) and uploads the debug APK as an artifact.

## Project structure

```
AppBuilderLite/
├── app/src/main/java/com/nobi/appbuilder/
│   ├── MainActivity.java
│   ├── ui/dashboard/DashboardActivity.java
│   ├── model/      (ProjectModel, ScreenModel, ComponentModel, EventModel)
│   ├── database/   (DatabaseHelper, ProjectRepository, ScreenRepository,
│   │               AssetRepository)
│   └── utils/      (FileUtils, DimensionUtils, IDGenerator)
├── app/src/main/res/ (layout, drawable, values, menu, xml, mipmap-anydpi-v26)
├── app/src/main/AndroidManifest.xml
├── build.gradle / settings.gradle / gradle.properties
└── README.md
```
