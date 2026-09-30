# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-09-30

The first stable release - and the first one with ready-to-run packages.

### Added

- Packages for Windows (`.msi` installer and portable `.zip`) and Linux (`.deb` and `.tar.gz`) with a bundled Java runtime - no Java installation needed.
- A folder dialog when iorg is started without a folder that contains images, e.g. from the Start menu.
- Keyboard shortcuts: Enter and Left arrow in the compare modes, number keys for ratings and categories.
- Redesigned user interface with animations for the compare modes, colored category chips, a preview of the latest images per rating or category, and an image gallery in the summary.
- Undo and restart in the order, rate and categorize modes.
- Application icon.

### Changed

- The rating is now prefixed as a two-digit number (`07-example.jpg`), so file managers sort ten stars after nine.
- English is the default language for all systems that are neither English nor German.
- Revised English texts in the user interface.
- The home folder is shown as `~` in the header on Linux.
- Requires Java 21 and JavaFX 21 when built from source.

### Fixed

- The executable jar failed to start with "JavaFX runtime components are missing".
- Images with upper-case extensions such as `.JPG` were ignored.
- The summary of the knockout modes listed the images from the last to the first placement.
- iorg crashed at startup on systems with a language other than English or German.
- Status texts in the footer were not translated.
- iorg quit silently when the folder contained no or too few images - it now shows a message.

## [0.8.0-beta] - 2023-07-22

Restart and undo, with some UI polishing.

## [0.7.1-beta] - 2023-07-18

Some fixes and an additional label.

## [0.7.0-beta] - 2023-07-13

First public pre-release with the order, knockout, rate and categorize modes.

[1.0.0]: https://github.com/Skrrytch/ImageOrganizr/compare/0.8.0-beta...v1.0.0
[0.8.0-beta]: https://github.com/Skrrytch/ImageOrganizr/releases/tag/0.8.0-beta
[0.7.1-beta]: https://github.com/Skrrytch/ImageOrganizr/releases/tag/v0.7.1-beta
[0.7.0-beta]: https://github.com/Skrrytch/ImageOrganizr/releases/tag/v0.7.0-beta
