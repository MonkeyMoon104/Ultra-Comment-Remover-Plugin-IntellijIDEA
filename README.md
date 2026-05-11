# UltraCommentRemover

IntelliJ IDEA plugin that removes PSI-detected comments from the selected file, directory, or project root.

## Features

- Adds a `Remove Comments` action to the Project View context menu
- Works on a single file, a folder, or the project root
- Traverses source files recursively using IntelliJ PSI APIs
- Removes `PsiComment` nodes inside `WriteCommandAction`
- Preserves special comments such as shebangs, formatter directives, `noinspection`, and region markers
- Skips excluded directories such as `.git`, `build`, `target`, `node_modules`, `.idea`, `out`, and `dist`

## Build

```powershell
./gradlew.bat build
```

## Run In Sandbox

```powershell
./gradlew.bat runIde
```

## Implementation Notes

- Comment discovery is language-agnostic and relies on `PsiComment`
- File selection resolution is specific to the Project View context menu
- Comment removal is split into planner, writer, progress, and preservation-policy components
