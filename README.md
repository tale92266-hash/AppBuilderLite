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

## Phase 8 — Android Studio Project Generator & ZIP Export (2026-11-06)

Takes the Phase 7 generated project and packages it into a complete,
importable Android Studio project ZIP. Entry point: editor menu →
Export project.

- `export/` — `ProjectPackagingManager` (orchestrates: generate →
  validate → package → ZIP → verify → history, 10 progress stages,
  background executor, cancellation, temp workspace always cleaned
  up), `AndroidStudioProjectBuilder`, `ProjectStructureBuilder`,
  `ProjectFileManager` (safe relative paths, duplicate guard),
  `ProjectCopyManager` (copies generated files, genuine Gradle
  wrapper from app assets, proguard rules, `AppTheme.NoActionBar`
  styles, adaptive launcher icon + color, README), `ProjectCleanupManager`,
  `ProjectPackagingConfig` (validated config: name, app name, package,
  min/target SDK, version), `PackagingError/Report/Result`.
- `export/zip/` — `ZipExporter` (buffered streaming, never holds the
  whole project in RAM; File or SAF output), `ZipEntryWriter`
  (normalized names, duplicate guard), `ZipValidationManager`
  (opens the finished ZIP, checks required files, no duplicates or
  unsafe names), `ZipExportResult`.
- `export/validation/` — `ExportValidator` (structure, resources,
  manifest, Gradle, Java structural checks), `ProjectStructureValidator`,
  `ResourceValidator` (@string/@color/@dimen/@drawable refs resolve),
  `ManifestValidator` (application, launcher, exported flags),
  `GradleConfigValidator` (namespace/applicationId/SDK/AGP consistency).
- `export/storage/` — `ExportStorageManager` (default exports dir,
  unique filenames, free-space check; SAF tree URI in `export_prefs`),
  `ExportDirectoryManager` (per-export cache workspace),
  `ExportHistoryRepository` (SQLite `export_history`, DB v3; deleting a
  record never deletes the ZIP).
- `export/report/` — `ExportSummaryGenerator` (README.md inside every
  export, with an honest "structurally validated, not build-tested"
  note), `ExportLogManager` (per-export `.export.log` beside the ZIP).
- `ui/export/` — `ExportActivity` (config form, progress, result,
  history; SAF folder picker), `ExportConfigurationController`,
  `ExportProgressController`, `ExportResultController`,
  `ExportHistoryAdapter`, `ExportFileBrowserController` (open/share
  via FileProvider content URIs).
- The genuine `gradle-wrapper.jar` (59,203 B) ships as an app asset
  (`assets/export/`); if the asset is ever missing the export reports
  it clearly and never creates a fake JAR.
- A Phase 7 gap is fixed here: the generated manifest references
  `@mipmap/ic_launcher` but no mipmap files existed — the exporter
  now generates the adaptive icon XMLs, foreground vector and
  `ic_launcher_colors.xml`.

## Phase 9 — APK Build Engine, Build Logs, Diagnostics & APK Management (2026-11-06)

Honest build management: the app first detects whether the device
can genuinely compile Android projects, and only then offers a local
Gradle build. On a normal phone there is no JDK, Gradle or SDK, so
the honest path is the external build — export the Phase 8 ZIP and
build it in Android Studio. Nothing is ever faked: no simulated
progress, no fake APKs, no success without a real verified APK file.
Entry point: editor menu → Build APK.

- `build/` — `BuildManager` (validate → detect → pick engine → run →
  diagnose → history/report), `BuildConfiguration` (debug/release,
  app ID, versions, SDKs, shrinking, report/log options, optional
  SDK path), `BuildRequest/Result/Status/Task` (10 real stages),
  `BuildEnvironmentDetector` (PATH scan for java/javac/gradle,
  JAVA_HOME/ANDROID_HOME/ANDROID_SDK_ROOT, user-configured SDK path,
  StatFs storage; LOCAL_BUILD_SUPPORTED / EXTERNAL_BUILD_REQUIRED /
  ENVIRONMENT_INCOMPLETE / ENVIRONMENT_UNSUPPORTED — never assumes
  Termux), `BuildEnvironmentInfo`, `BuildValidator` (config + real
  Phase 7 generation check), `BuildQueueManager` (one build at a
  time, queue of 5), `BuildCancellationManager`, `BuildOutputManager`
  (finds real APKs under app/build/outputs/apk/).
- `build/engine/` — `BuildEngine`, `LocalGradleBuildEngine` (only when
  the detector confirms a full toolchain; materialises the project
  with Phase 7 + Phase 8 builders, runs `./gradlew` through `sh`,
  locates and validates the APK, copies it to the builds dir),
  `ExternalBuildEngine` (Phase 8 ZIP export; result is
  EXTERNAL_BUILD_REQUIRED, never SUCCESS), `GradleCommandBuilder`
  (only the two fixed assemble tasks — user input never reaches a
  command line), `BuildProcessManager` (streams stdout+stderr,
  cancellation destroys the process), `BuildEnvironmentValidator`.
- `build/logs/` — `BuildLogManager/Entry/Parser/Filter/Repository/
  Exporter` (real Gradle output kept verbatim, task names, compiler
  errors, level + text filtering, TXT export, SQLite `build_history`
  DB v4 — deleting a record never deletes the APK).
- `build/diagnostics/` — `BuildDiagnosticsManager`, `BuildError/
  Warning`, `BuildIssueClassifier` (Gradle mismatch, missing SDK /
  platform, AAPT2, missing resource, Java compilation, dependency,
  duplicate resource, manifest merger, storage), `BuildSuggestionEngine`,
  `BuildReportGenerator` (TXT report, no signing secrets).
- `build/apk/` — `ApkManager`, `ApkMetadataReader` (PackageManager),
  `ApkValidator` (exists, non-empty, parses, matches config),
  `ApkRepository` (history-backed, missing files reported),
  `ApkInstaller` (system installer, never silent; unknown-apps
  permission flow), `ApkShareManager` (FileProvider URIs).
- `build/storage/` — `BuildStorageManager` (APKs + logs under
  external files `builds/`, FileProvider path added),
  `BuildWorkspaceManager`, `BuildCleanupManager` (temp workspaces
  only, canonical-path guard).

## Phase 10 — Professional Code Editor & Source Code Management (2026-11-06)

A real editor, not a multiline EditText: state-aware Java/XML
syntax highlighting (line-based, visible region only — never a full
re-highlight per keystroke), line numbers, current-line highlight,
bracket matching, auto-indentation, tab-to-spaces, undo/redo with
merge window, find & replace, offline completion, formatting,
diagnostics, tabs, file explorer and a per-project workspace.
Entry point: editor menu → Code editor. No database change.

- `codeeditor/` — `CodeEditorView` (AppCompatEditText + gutter +
  current-line highlight + bracket span + InputConnection auto-indent),
  `CodeEditorController` (session, undo, autosave, completion context,
  formatting, find/replace), `EditorDocument` (disk stamps, external
  modification detection), `EditorSession` (tabs, per-tab state),
  `EditorState/Cursor/Selection`, `EditorSettings` (SharedPreferences
  `code_editor_prefs`: theme, font size, line numbers, word wrap, tab
  width, auto-indent, highlighting, completion, bracket matching,
  autosave), `EditorLanguage`, `EditorUndoManager` (100 snapshots,
  800 ms merge window), `EditorHistoryManager` (session restore),
  `EditorAutoSaveManager` (800 ms debounce, atomic save), `EditorFileManager`
  (workspace-guarded reads; atomic save via .tmp + fsync + .bak).
- `codeeditor/syntax/` — `SyntaxHighlighter/Token/TokenType`,
  `JavaSyntaxHighlighter` and `XmlSyntaxHighlighter` (state-aware
  tokenizers — strings/comments are tracked, not regex-guessed),
  `SyntaxTheme` (light/dark palettes), `SyntaxColorResolver`.
- `codeeditor/completion/` — `CodeCompletionManager`, `CompletionProvider`,
  `JavaCompletionProvider` (keywords, Android classes, lifecycle
  methods, project classes, R.id.*), `XmlCompletionProvider` (tags,
  android attributes, @string/@color/@drawable/@dimen), `CompletionItem`,
  `CompletionAdapter`.
- `codeeditor/formatting/` — `CodeFormatter`, `JavaCodeFormatter`
  (brace-aware, string/comment-aware), `XmlCodeFormatter` (depth-based),
  `IndentationManager`, `FormattingResult` (formatting is undoable).
- `codeeditor/search/` — `CodeSearchManager` (in-document + project-wide
  over .java/.xml/.gradle/.properties, 2 MB cap, never binary assets),
  `FindReplaceController`, `SearchResult`, `SearchOptions`.
- `codeeditor/navigation/` — `CodeNavigationManager`, `SymbolNavigator`
  (class/method symbols), `BracketMatcher` (code-aware, skips strings
  and comments).
- `codeeditor/diagnostics/` — `CodeDiagnosticManager` (real XML parse,
  duplicate @+id, brace balance, package-vs-path checks — heuristic
  only; Phase 9 build errors are the real verdict), `CodeDiagnostic`,
  `CodeDiagnosticType`, `DiagnosticAdapter`.
- `codeproject/` — `CodeProjectManager` (workspace under
  filesDir/code-workspaces/&lt;projectId&gt;; regeneration writes only
  new or hash-identical files — a `.generated-hashes.json` sidecar
  records what the generator last wrote, user-edited files are left
  alone and reported as conflicts), `CodeFileNode/Repository/Validator`
  (canonical-path guard, protected files: settings.gradle, build.gradle,
  gradle.properties, AndroidManifest.xml, wrapper files), `CodeProjectExplorer`,
  `CodeFileOperations` (delete keeps a .bak, protected files refused).
- `ui/codeeditor/` — `CodeEditorActivity` (tabs, explorer, completion
  panel, find/replace bar, diagnostics, external-change reload-vs-keep
  dialog, unsaved-changes guard, session restore), `CodeEditorToolbarController`,
  `CodeFileExplorerController`, `CodeTabController`, `CodeBottomPanelController`
  (line/col, language, save state, diagnostics), `CodeSearchPanelController`,
  `CodeSettingsController`.

## Phase 11 — Visual Editor and Code Synchronization Engine (2026-11-06)

Bidirectional sync between the visual editor, JSON IR, layout XML
and native preview. JSON IR stays canonical. Visual changes flow
IR → XML (one screen at a time); supported XML edits flow back
XML → IR; the preview re-renders live. Java sync is deliberately
limited to recognizing generated listener patterns — arbitrary Java
is never parsed as design data.

- `sync/` — `SynchronizationManager` (debounced, per-project locked,
  snapshotted runs; visual/code/external entry points; conflict
  resolution entry points), `SynchronizationController` (facade for
  activities), `SynchronizationState` (Synchronized / Syncing /
  Visual / Code pending / Conflict / Invalid XML / Unsupported /
  Manual required), `SynchronizationDirection/Event/Listener/Result/
  Error`, `SynchronizationScheduler` (500 ms debounce),
  `SynchronizationQueue` (bounded 50, dedup per screen),
  `SynchronizationPolicy` (SharedPreferences `sync_prefs`: auto-sync,
  debounce, conflict policy, preserve-unknown),
  `SynchronizationLockManager`, `SynchronizationTransaction` (full-IR
  JSON snapshot + rollback), `ChangeOrigin` (visual/code/external/
  generator/preview/recovery), `ChangeSet/ChangeOperation`,
  `ChangeTracker` (SHA-256 hashes per file — a disk hash that differs
  from the recorded one means "edited by the user", never overwrite).
- `sync/parser/` — `XMLLayoutParser` (DOM, DOCTYPE disallowed,
  errors instead of crashes), `XMLComponentMapper` (tag → type via
  the registry, reverse of the generator), `XMLPropertyExtractor`
  (supported attrs → component + layout props; unknown attrs
  reported), `XMLHierarchyParser` (tree by android:id, type changes
  honoured, unsupported elements skipped + reported),
  `XMLParseResult/Error`, `XMLResourceResolver` (@string/@color/@dimen
  against the workspace values files), `XMLRoundTripValidator`
  (IR → XML → IR diff).
- `sync/mapper/` — `IRToXMLMapper` (Phase 7 XMLGenerator, one screen),
  `XMLToIRMapper` (applies parsed XML; duplicate ids warned; changes
  as a ChangeSet), `ComponentSyncMapper` (stable id ↔ android:id,
  never position), `PropertySyncMapper` (two-way, via the Phase 4
  PropertyRegistry), `EventSyncMapper` (recognized listeners only),
  `ResourceSyncMapper` (theme tokens).
- `sync/diff/` — `ProjectDiffEngine`, `ScreenDiffEngine` (by stable
  id: add/remove/modify/reorder), `ComponentDiffEngine`,
  `PropertyDiffEngine`, `DiffResult/Operation/Conflict`.
- `sync/conflict/` — `ConflictManager` (persisted records),
  `ConflictResolver` (keep visual = regenerate XML, keep code =
  re-parse, backup = both versions under .sync-backup/),
  `ConflictResolution` (5 options), `ConflictType`,
  `ConflictRecord`, `ConflictResolutionPolicy` (ask / keep visual /
  keep code).
- `sync/reload/` — `LiveReloadManager` (static hub, 300 ms debounced
  fan-out; screens keep their own selection/zoom/cursor state),
  `VisualEditorRefreshManager`, `CodeEditorRefreshManager`,
  `PreviewRefreshManager`, `ReloadRequest`.
- `sync/history/` — `SynchronizationHistory` (100 snapshots, JSON in
  filesDir/sync-state/), `SynchronizationSnapshot` (full IR JSON),
  `ChangeOriginTracker`, `SynchronizationRecoveryManager` (restores
  the latest snapshot into the database).
- `ui/sync/` — `SyncStatusController`, `ConflictDialogController`
  (5-option dialog + change preview), `SyncHistoryController`,
  `SyncSettingsController`. Status UI: editor menu → Sync status.
- Loop prevention: debounced queue, per-project lock, content
  hashes, origin tracking, manager-owned writes bypassing editor
  save paths, and transactions with rollback. A screen whose XML
  has unsupported constructs stays code-only until resolved
  manually — its XML is never overwritten.

## Phase 12 — Component Library, Custom Components & Design System (2026-11-06)

- Component library (`library/`, `ui/library/`): browse/search/filter
  17 registered component types plus preset variants (text styles,
  button styles, input variants, image variants), with favourites,
  recent use, details + lightweight preview, and Add to Canvas
  (ComponentFactory → JSON IR → canvas renders, inspector shows
  supported properties, native preview renders, XML generator
  produces valid XML).
- Five genuinely new widget types added with full canvas + preview
  + XML stacks: RadioButton, Spinner, RatingBar, SeekBar, Divider
  (a plain View with a background colour — `<Divider>` is not a
  valid Android tag). WebView, Toolbar, BottomNavigation, TabLayout,
  RecyclerView, HorizontalScrollView and RelativeLayout are listed
  as unsupported with their reason — never faked as working.
- Custom components (`library/custom/`): save a selected subtree as
  a reusable definition with auto-derived public properties
  (text/background/text colour), versioned in SQLite
  (custom_components + custom_component_versions); instances are
  cloned trees with fresh ids, so editing the definition never
  destroys instance overrides.
- Templates (`library/templates/`): six original starter templates
  (Login, Dashboard, Profile, Settings, Contact, Empty), personal
  templates in SQLite, import/export as versioned JSON, apply as a
  new screen with fresh component ids every time.
- Design system (`library/design/`): colour tokens (11 defaults),
  typography tokens (Display…Caption), spacing (4–48dp), shape
  (Small/Medium/Large/Full), elevation (None/Low/Medium/High);
  light/dark/custom themes in SQLite. Applying a theme updates the
  JSON IR theme metadata only — explicit per-component colours are
  never overwritten.
- Presets (`library/presets/`): Primary/Secondary/Outlined Button,
  Filled Text Field, Search Field, Profile Image, Heading Text, Card
  Container, plus user presets; applying one updates supported
  properties without changing component identity.
- Database: version 5, ten new tables (component_library,
  custom_components, custom_component_versions, component_favorites,
  component_usage, screen_templates, design_systems, design_tokens,
  project_themes, component_presets) with safe step migration —
  existing project data is never deleted.
- Editor menu: Component library, Templates, Design system.

## Phase 13 — Asset Manager & Android Resource Management (2026-11-06)

- One asset manager, not two: `assets/` (import, search, folders,
  preview, validation, references, usage) + `assets/image/`,
  `assets/drawable/`, `assets/vector/`, `assets/fonts/`,
  `assets/colors/`, `assets/resources/` and `ui/assets/`. Entry
  point: editor menu → Assets.
- Import: SAF copy with SHA-256 computed during the copy; duplicate
  detection is by content hash, so the same file imported twice
  points at the existing asset instead of a second copy. Invalid
  files are deleted after validation fails and reported, never
  silently kept. Resource names are sanitized (`^[a-z][a-z0-9_]*$`)
  and made unique. ImageView stores the stable asset id in its
  "assetId"/"src" properties, never a temp path.
- Images: density variants are always generated from the original
  (xxxhdpi 4.0x baseline, mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi), never by
  re-resizing an output, never upscaled; the original is also copied
  to drawable/. Image editor (resize/crop/WebP/optimize) always
  writes a NEW asset file + row — the original is never overwritten.
  Animated GIFs are detected (magic bytes) and refused for WebP
  conversion with a reason.
- Drawables: shape/gradient/selector/ripple generators with XML
  validation (root-tag check, DOCTYPE disallowed). Vectors: only
  Android-supported path commands are accepted — arbitrary SVG is
  not a VectorDrawable. Fonts: TTF/OTF magic only, other formats
  are rejected, never claimed supported. Colours live in the Phase
  12 DesignSystemManager colour tokens; gradients are stored as
  JSON in color_resources (linear angle must be a multiple of 45).
- References & safety: renaming an asset updates every IR "src"
  reference and reports how many; deleting a referenced asset is
  refused with a reason. Unused assets are those with no IR
  references and no recorded use.
- Export: `AssetExportManager` writes density variants into res/,
  fonts into res/font/, vectors and drawables as res/drawable XML,
  collecting a problems list — nothing is silently dropped.
- Database: version 6. `assets` gains folder_id/hash/resource_name;
  nine new tables (asset_folders, asset_metadata, asset_references,
  asset_favorites, asset_usage, vector_assets, font_assets,
  color_resources, drawable_resources) with safe step migration.
  Note: `assets/AssetRepository.java` is a domain repository that
  delegates row reads to the existing `database/AssetRepository` —
  there is intentionally only one asset table.

## Phase 14 — Navigation Designer & Event Workflow Engine (2026-11-06)

- One event model everywhere: `EventModel` stays the canonical
  store (JSON IR, preview, Phase 7 generator). `events/` wraps it
  (`EventDefinition`, `EventTrigger` with extended triggers,
  `EventValidator`, `EventBindingManager`, `EventManager`,
  `EventRepository`) — no second event system was created.
- Navigation (`navigation/`): graph nodes keyed by stable screen id
  (never display names), edges derived from navigate events plus
  hand-drawn designer edges, stored as JSON in project_settings
  ("navigation_graph_json") — no new tables. Validation reports
  missing destinations as errors; unreachable screens and cycles
  are warnings only (Back navigation is a cycle by design).
- Workflows (`flow/`): Start/Event/Action/Condition/Navigation/
  Variable/End nodes, connection compatibility validation, and an
  engine that REFUSES invalid graphs instead of executing them.
  Limits: 100 steps, 10 visits per node — a cycle can warn but
  never loop forever. Conditions are typed comparisons
  (equals/not_equals/contains/is_empty/greater_than/less_than/
  boolean) — no expression evaluation, no JavaScript eval.
- Actions (`actions/` + `actions/builtins/`): twelve built-ins
  (navigate, toast, change text, visibility, enabled, open URL,
  finish, validate, set variable, show dialog, delay, condition)
  sharing ids with the Phase 7 EventMappingRegistry. The restricted
  `ActionExecutor` validates parameters and delegates preview runs
  to the Phase 6 PreviewActionExecutor; it never executes arbitrary
  generated Java. Delay is recorded, never a UI-thread sleep.
- Transitions (`transitions/`): none/fade/slide left/right/up/down,
  duration 100–1000ms, applied in preview via
  overridePendingTransition with framework anims only — a
  transition never references a missing animation resource. Slide
  up/down fall back to fade (no framework anim exists). The custom
  duration is stored and validated on the edge. Note: the Phase 7
  generator still emits a plain startActivity for navigate events
  (its context has no settings access); the exact transition line
  is available as `TransitionDefinition.javaSnippet()`.
- Data flow (`dataflow/`): typed screen arguments with defaults and
  required validation (Intent extras carry strings only — never
  large images or objects), screen results, variable definitions
  (screen/global), and data bindings with fixed transforms.
- Editor wiring: menu → Navigation; inspector Events section (On
  Click / On Long Click / Text Changed, ● bound / ○ none) opens the
  event editor, which opens the workflow editor; hierarchy rows
  show ⚡ on components with events; the flow debugger shows the
  current node, executed actions, variables, condition results,
  navigation events and errors, with Start/Step/Run/Reset.

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
