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

## Phase 2 — DONE (2026-11-06)

JSON document engine + registry-based component architecture. The JSON
document is now the single source of truth for all designs.

**Included**

- `ir/` — `IRDocument` (envelope: schemaVersion, project, screens,
  theme, assets, editorMetadata; unknown fields preserved across
  round-trips), `IRSerializer` (pretty JSON), `IRDeserializer`
  (migrates legacy Phase 1 project JSON, validates, repairs nulls),
  `IRValidator` (unique ids, known types, dimensions, colours, launcher
  screen), `IRVersionManager` (schema 1; newer schemas rejected),
  `IRConstants`
- `model/` — new `LayoutModel` (width/height/margins/padding/gravity)
  and `ThemeModel`; `ComponentModel` gained `layout` + `editorMetadata`
  with null-safe accessors
- `components/` — `ComponentDefinition` (type, category, defaults,
  canvas/preview/XML strategies), `ComponentCategory`,
  `ComponentRegistry` (register/get/contains/getAll/getByCategory/
  unregister; 12 types incl. MaterialCardView), `ComponentFactory`
  (create/clone with fresh ids), and 11 component classes
  (TextView, Button, ImageView, EditText, LinearLayout, FrameLayout,
  ConstraintLayout, ScrollView, CheckBox, Switch, ProgressBar), each
  with canvas drawing, real preview View creation and XML attributes
- `repository/DocumentRepository` — save/load whole documents and
  single screens on the Phase 1 tables (theme in project_settings),
  one transaction per document save
- Dashboard import now loads through `IRDeserializer` (invalid files
  are rejected with a reason); new projects start from
  `ComponentFactory.createRootContainer()`

**Not in Phase 2 (later phases)**

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
