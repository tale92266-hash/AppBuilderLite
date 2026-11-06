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

## Phase 3 — DONE (2026-11-06)

Professional visual canvas editor (drag-and-drop Android UI designer).

**Included**

- `editor/` — `DesignCanvasView` (custom View), `CanvasRenderer`
  (renders every component through its Phase 2 definition, pooled
  Paints/RectFs, zero per-frame allocation), `CanvasViewport` +
  `CanvasTransform` (dp design coords; pinch/double-tap/button zoom
  25–300% around the focal point, pan, fit-to-screen), `CanvasGridRenderer`
  (8dp grid, 40dp major lines), `CanvasState` (selection, tool,
  clipboard, hierarchy expansion — never saved to JSON),
  `ComponentHitTester` (deepest topmost hit, MATCH_PARENT/WRAP_CONTENT
  bounds), `SnapEngine` + `AlignmentEngine` (grid + edge/centre guides,
  threshold-limited, clamped to parent), `SelectionController` (tap,
  additive, 8 handles), `DragController` (multi-move, subtree moves,
  library drop into the deepest valid container), `ResizeController`
  (8 handles, min sizes, grid snap, ImageView aspect lock),
  `GestureController` (touch state machine, touch-slop tap-vs-drag,
  long-press menu) + tools `SelectionTool`/`HandTool`/`DragTool`
- `ui/editor/` — `EditorActivity` (load/save via DocumentRepository,
  debounced 800ms autosave, snapshot undo/redo, context menu:
  duplicate/copy/paste/delete/bring forward/send backward/rename ID),
  `EditorToolbarController`, `ComponentPanelController` (searchable
  library, tap-to-add + real drag-and-drop), `HierarchyController`
  (expandable tree, canvas-synced selection), `EditorPanelController`
  (collapsible bottom panel with live selection info)
- Dashboard: project menu and details dialog now open the editor
  (launcher screen); `EditorActivity` registered in the manifest

**Not in Phase 3 (later phases)**

- Phase 4: Properties inspector + hierarchy
- Phase 5: Command-pattern undo/redo (replaces the snapshot stack)
- Phase 6: Live preview renderer
- Phase 7: XML + Java generators
- Phase 8: Full Android Studio project export as ZIP
- Phase 9: Testing + performance

## Phase 4 — DONE (2026-11-06)

Properties inspector + advanced component hierarchy.

**Included**

- `properties/` — schema-driven inspector: `PropertyType` (10 types),
  `PropertyGroup` (General/Layout/Spacing/Appearance/Typography/Advanced),
  `PropertyValue` (typed wrapper), `PropertyDefinition` (builder),
  `PropertyRegistry` (full schema for all 12 component types, only
  relevant properties per type; constraint fields only under
  ConstraintLayout; `xmlAttribute` mapping per property for Phase 7),
  `PropertyValidator` (id uniqueness, colour/dimension formats, ranges),
  `PropertyBinding` (strict separation: model fields / LayoutModel /
  editorMetadata; unknown properties preserved),
  `PropertyController` (validate → apply → notify; integrates with the
  existing snapshot undo stack — the spec's command classes belong to
  Phase 5, so none are created here), `PropertyChangeListener`
- `properties/editors/` — 7 real editors, no placeholders:
  `TextPropertyEditor` (debounced live text), `ColorPropertyEditor`
  (HEX + swatch + presets + recent colours), `DimensionPropertyEditor`
  (dp/sp + match_parent/wrap_content), `NumberPropertyEditor`
  (+/− buttons, min/max clamp), `BooleanPropertyEditor` (switch),
  `EnumPropertyEditor` (dropdown), `ResourcePropertyEditor`
  (reference field + preset list)
- `hierarchy/` — `ComponentTreeNode`, `ComponentTreeAdapter`
  (thumb, id label, expand arrow, eye + lock toggles),
  `ComponentTreeController` (tree build from CanvasState, rename with
  validation, delete/duplicate/copy/paste, reorder, reparent),
  `HierarchyDragController` (ItemTouchHelper: reorder siblings, drop
  onto a container to reparent; one undo snapshot per drag),
  `HierarchyValidator` (container rules, no cycles, ScrollView
  single-child, duplicate ids — readable errors, never silent)
- `ui/editor/` — `PropertiesPanelController` (right-side overlay panel,
  grouped rows, live canvas refresh + debounced autosave on every
  change, Reset to defaults, panel survives typing without losing
  scroll/focus) and `HierarchyPanelController` (collapsible bottom
  panel + search + selection info + drag). Supersedes the Phase 3
  `HierarchyController` and `EditorPanelController` (deleted).
- Canvas integration: `visibility` (visible/invisible/gone) and
  `alpha` now render on the canvas (gone subtrees skipped in drawing
  and hit-testing); locked components cannot be dragged, resized,
  deleted or reordered; padding insets now shift text in the
  TextView/Button/EditText/CheckBox/Switch canvas renderers

**Not in Phase 4 (later phases)**

- Phase 5: Command-pattern undo/redo (replaces the snapshot stack)
- Phase 6: Live preview renderer
- Phase 7: XML + Java generators (uses `PropertyRegistry.xmlAttributeFor`)
- Phase 8: Full Android Studio project export as ZIP
- Phase 9: Testing + performance

## Phase 5 — Editor Productivity & Project Management Engine (2026-11-06)

Replaces the Phase 3 snapshot undo stack with a real command engine and
adds clipboard, multi-selection, grouping, autosave/recovery and screen
management. Phases 1–4 stay fully functional; no Phase 1–4 class was
recreated or replaced.

**Included**

- `history/` — `EditorCommand` interface, `CommandHistory` (undo/redo
  stacks, max 100, no full-document copies), `CommandManager`
  (execute/undo/redo, grouped transactions, failed-command safety),
  `CommandTransaction` (composite) and 9 commands: Add, Delete, Move
  (one entry per drag), Resize (one entry per gesture), UpdateProperty
  (incl. visibility + lock), Duplicate, Reparent, Rename, Group
  (with nested Ungroup), Reorder. Drag/resize commit hooks in
  `GestureController` + `DragController`/`ResizeController` start-state
  getters build the command at gesture end.
- `clipboard/` — `EditorClipboard` (one ClipboardData per editor),
  `ClipboardData` (cloned subtrees + source screen id),
  `ComponentCloner` (fresh ids on every clone/paste, no duplicate ids),
  `PasteController` (offset paste, parent-bound clamping, returns
  AddComponentCommands; cross-screen paste within the project).
- `selection/` — `MultiSelectionManager` (tap, additive, select-all,
  deselect-all, marquee rectangle — topmost only, never a parent with
  its own children), `SelectionBounds` (combined outline),
  `GroupSelectionController` (group/ungroup entry points + validation).
  Marquee: rubber-band rect drawn by `CanvasRenderer`, handled by
  `SelectionTool`.
- `autosave/` — `AutosaveManager` (800 ms debounce, background write,
  explicit Save + exit flush, toolbar save-status subtitle),
  `RecoveryManager` (30 s periodic snapshots + on-pause snapshot,
  atomic INSERT OR REPLACE, restore/discard dialog on open),
  `RecoverySnapshot`, `ProjectBackupManager` (last saved document kept
  before every overwrite). DB migration v1 → v2 adds the
  `recovery_snapshots` and `project_backups` tables (FK cascade) plus
  `screen_type`/`orientation` columns on screens.
- `screens/` — `ScreenManager` (create/rename/duplicate/delete/reorder/
  launcher/type/orientation), `ScreenNavigator` (open by stable id,
  starting screen, validates `navigate` event targets against existing
  screens), `ScreenDuplicator` (deep clone, fresh ids), `ScreenValidator`
  (name uniqueness, last-screen protection). Reuses the Phase 1
  `database.ScreenRepository` — deliberately no second one.
  `ScreenModel` gains `screenType` (activity/dialog/bottom_sheet/custom)
  and `orientation`.
- `project/` — `ProjectManager` (recent/name sort, search, duplicate,
  rename, delete with recovery+backup cleanup), `ProjectImportManager`
  (IR JSON import, fresh ids on collision), `ProjectExportManager`
  (IR JSON export), `ProjectSettingsManager` (app/package/version/SDK/
  orientation/starting-screen, validated before saving; build files
  untouched).
- UI — toolbar gains Copy/Paste/Duplicate/Screens; a contextual
  selection toolbar (Group/Ungroup/Delete/Lock/Visibility/Select all);
  a screen manager dialog (list, add, duplicate, rename, delete, set
  starting screen); dashboard sort (recent/name), per-project Export
  and Project Settings dialog.

**Notes**

- The properties panel never rebuilds or re-reads its rows on a command,
  so typing in a text editor keeps focus and cursor position.
- Editor-only hidden state is the `visibility` model property; it is
  preserved in JSON and only Phase 7 generation decides what the app
  shows.

## Phase 6 — Native Android Preview Engine (2026-11-06)

Renders the JSON document as real native Android Views inside the
app. The editor's Preview menu item is live since this phase.
(Fix in Phase 7: the menu item was still `android:enabled="false"`
in `menu_editor.xml` and nothing ever enabled it, so Preview was
unreachable in the Phase 6 build — the attribute was removed.)

**Included**

- `preview/` — `PreviewRenderer` (pre-assigns stable Android view
  ids so ConstraintLayout targets resolve in any order, root fills
  the device frame), `PreviewController` (loads the document off the
  main thread, validates + renders on it, never writes back),
  `PreviewSession` (project/screen, theme, device, orientation,
  navigation history, temporary input values — never saved),
  `PreviewContext` (session + theme + navigator + dispatcher +
  component-id → View registry), `PreviewState` (zoom 50–200%,
  frame on/off), `PreviewNavigator` (stable screen ids, history,
  restart), `PreviewValidator` ("componentId: message" errors;
  one bad component becomes a red placeholder, never a crash).
- `preview/factory/` — `PreviewViewFactory` (real TextView,
  MaterialButton, ImageView, EditText, Linear/Frame/ConstraintLayout,
  ScrollView, CheckBox, Switch, ProgressBar, MaterialCardView),
  `PreviewLayoutFactory` (Linear weight/gravity, Frame gravity,
  ConstraintLayout constraints + bias, ScrollView child; dimensions
  from LayoutModel strings first, model ints second),
  `PreviewComponentFactory` (recursive build, per-component error
  isolation, event wiring).
- `preview/components/` — one real-property applier per type
  (text/size/colour/gravity/lines, button tint + radius, image
  scale types + rounded outline, hint/inputType, orientation,
  fillViewport, checked, max/progress, card radius/elevation).
  Images resolve from drawable resources, project assets or file
  paths; large files decode downsampled on a background thread;
  missing images get a clear placeholder.
- `preview/theme/` — `PreviewThemeManager` (Light / Dark / project
  theme; switching never overwrites component properties),
  `PreviewColorResolver` (hex + theme tokens), `PreviewStyleResolver`
  (background + radius/border, text, padding/alpha/visibility/
  elevation, input types, scale types).
- `preview/device/` — profiles Compact 360×800, Standard 393×873,
  Large 412×915, Small tablet 600×960 dp + custom sizes;
  `PreviewViewport` sizes the frame by profile × orientation × zoom.
- `preview/events/` — `PreviewEventDispatcher` (click, long click,
  text-changed) and `PreviewActionExecutor` (only the predefined
  actions: toast, navigate, changeText, validateInput, finish,
  changeVisibility — never arbitrary code). Navigation uses stable
  screen ids; a missing destination toasts instead of crashing.
- `ui/preview/` — `PreviewActivity` (top toolbar: back, screen
  name, restart, theme, close; bottom toolbar: device, orientation,
  zoom; first-render error dialog, later renders toast the count;
  state survives rotation; closing returns to the editor with its
  selection and zoom intact), `PreviewToolbarController`,
  `DeviceFrameView` (optional rounded phone border),
  `DeviceSelectorDialog` (profiles + custom size + frame switch).

## Phase 7 — XML & Java Code Generation Engine (2026-11-06)

Converts the JSON IR into a complete native Android project
(layouts, Java Activities, resources, manifest, Gradle config).
JSON stays the single source of truth.

**Included**

- `generator/` — `ProjectGenerator` (pipeline: validate IR →
  validate events → XML layouts → Java Activities → resources →
  manifest → Gradle → cross-reference validation → report; a run
  with errors is never reported as success), `GenerationContext`
  (document, package/SDK/version settings, screen→class/layout
  maps, shared ResourceCollector, id map, problem list),
  `GenerationResult` / `GenerationReport` / `GenerationError`.
- `generator/xml/` — `XMLGenerator` (one layout per screen, root
  carries the android/app namespaces, output re-parsed by
  `XMLValidator` before acceptance), `LayoutXMLGenerator`
  (Linear weight/gravity, Frame gravity, ConstraintLayout
  constraints + bias, ScrollView single child; dimensions from
  LayoutModel strings first), `XMLDocumentWriter` (structured
  writer — no unsafe string concatenation), `XMLAttributeMapper`
  (properties → XML attributes; text/hint → @string, textColor →
  @color, textSize → @dimen; cornerRadius/border → per-component
  shape drawable; unmapped properties are reported, never silently
  dropped), `XMLIdResolver` (unique ids, Activity class names,
  field names).
- `generator/java/` — `JavaGenerator` + `ActivityGenerator`
  (deterministic `ActivityTemplate`: imports, fields, findViewById
  bindings only for components with events, listeners),
  `ViewBindingGenerator`, `EventCodeGenerator` (only the predefined
  actions: toast, navigate, changeText, validateInput, finish,
  changeVisibility, openUrl, setEnabled — templates with escaped
  literals, never compiled code from the document),
  `NavigationCodeGenerator` (explicit Intents by stable screen id),
  `JavaSourceValidator` (brace/paren balance, package + class
  match, every R.id.* / R.layout.* resolves).
- `generator/resources/` — `ResourceGenerator` (runs after layouts,
  the collector is full by then): `ColorsGenerator`,
  `StringsGenerator` (app_name + extracted strings),
  `DimensionsGenerator`, `StylesGenerator` (themes.xml from the
  project ThemeModel), `DrawableGenerator` (per-component shapes +
  bg_button/bg_card defaults), `AssetGenerator` (images →
  res/drawable, other files → assets/, formats preserved).
- `generator/mapping/` — `ComponentMappingRegistry` (XML tag +
  Java class + import per type), `PropertyMappingRegistry`
  (attribute mapping + value conversion: dp/sp, inputType,
  scaleType, fontWeight, visibility, elevation), `EventMappingRegistry`
  (supported triggers/actions + validation: toast needs a message,
  navigate needs an existing target, openUrl needs http(s)).
- `generator/manifest/` — `ManifestGenerator` (one activity per
  screen, launcher exported with MAIN/LAUNCHER, INTERNET only when
  an openUrl event exists, landscape orientation only for landscape
  screens).
- `generator/gradle/` — `GradleGenerator` (settings + root +
  app build.gradle, gradle.properties, wrapper properties; pinned
  to AGP 8.9.2 / Gradle 8.11.1 / compileSdk 35 — the same combo this
  project builds with; the wrapper JAR itself cannot be generated,
  the report warns to run `gradle wrapper` once).
- `generator/templates/` — `ProjectTemplate` (version constants),
  `ActivityTemplate`, `ResourceTemplate` (themes + shape XML).
- `ui/code/` — `CodeStudioActivity` (file explorer on top,
  read-only source viewer below; refresh, copy, in-file search),
  `CodePreviewFragment` (monospace view, regex syntax highlighting
  for Java/XML/Gradle, search with next-match scrolling),
  `CodeFileAdapter` (directory headers + file rows with a red dot
  on files the report flags), `CodeGenerationController` (runs the
  generator off the main thread; settings from
  ProjectSettingsManager). Entry point: editor menu → View code.
- Phase 6 fix included: Preview menu item was still disabled by a
  leftover `android:enabled="false"` in `menu_editor.xml` — removed,
  so Preview is actually reachable now.

## Signing

Every debug APK is signed with one fixed key (keystore
`~/workspace/AppBuilderLite-signing/appbuilder.p12`, alias `appbuilder`,
cert SHA-256 `79:5D:37:29:C0:D4:CA:00:80:C5:32:2E:4F:93:62:66:AB:42:DD:70:BB:B3:E3:B1:DC:83:13:B9:BF:F2:0B:9E`).
The keystore lives in the `APPBUILDER_KEYSTORE_*` repo secrets; the
workflow restores it and `app/build.gradle` signs the debug build with
it when the env vars exist. Local/ACS builds still use the default
debug key. APKs from Phase 1–3 used ephemeral runner keys, so the
first fixed-key APK needs one uninstall, then every later APK installs
over the previous one.

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
