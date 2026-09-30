# Contributing

Thanks for your interest in Image Organizr! Feedback is the most valuable contribution right now.

## Feedback, bugs and ideas

- **Bugs**: open an [issue](https://github.com/Skrrytch/ImageOrganizr/issues/new/choose) with the bug report template.
- **Ideas and questions**: start a [discussion](https://github.com/Skrrytch/ImageOrganizr/discussions) -
  it helps a lot to know how you organize your photos and which mode you use.

## Building from source

You need JDK 21 and Maven 3.8+.

```bash
mvn verify                  # compile and run the tests
mvn javafx:run              # start the application
java -jar target/ImageOrganizr-*.jar ~/Pictures/holiday   # or run the fat jar
packaging/package.sh        # build the self-contained packages for the current platform into dist/
```

`packaging/package.sh` builds a `.tar.gz` and a `.deb` on Linux, and a `.zip` and an `.msi` on Windows (Git Bash, needs the
[WiX Toolset 3](https://github.com/wixtoolset/wix3/releases)).

## Pull requests

- Keep pull requests focused on one topic and describe the motivation.
- Run `mvn verify` before you push.
- New user-facing texts belong in `src/main/resources/labels.properties` (English) and `labels_de.properties` (German).

## Releases

1. Update `CHANGELOG.md` and set the version in `pom.xml`.
2. Push a tag `vX.Y.Z` - the release workflow builds the packages and creates a draft release.
3. Review the draft on GitHub and publish it.
