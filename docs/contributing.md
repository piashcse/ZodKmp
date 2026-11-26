# Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## How to Contribute

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Development Setup

To get started with development, you'll need to set up your Kotlin Multiplatform environment:

1. Install the latest version of Kotlin and the Kotlin Multiplatform plugin
2. Clone the repository
3. Build the project using Gradle: `./gradlew build`
4. Run the tests: `./gradlew test`

## Code Style

- Follow the Kotlin coding conventions
- Write clear, well-documented code
- Add tests for new functionality
- Keep changes focused and minimal
## Releasing

1. Update `library.version` in `gradle.properties` and commit it to `main`.
2. Create and push a tag that matches the version (`2.1.0` or `v2.1.0`), or publish a
   GitHub Release for that tag.
3. The **Publish** workflow runs automatically and uploads `zodkmp` and `zodkmp-ksp` to
   Maven Central.

The workflow validates that the tag matches `library.version` before publishing. A leading
`v` is optional. Releases created through the **Create Release** workflow publish in the same
run, so no separate action is needed there.

Documentation under `docs/` is deployed to GitHub Pages automatically when it changes on
`main`.
