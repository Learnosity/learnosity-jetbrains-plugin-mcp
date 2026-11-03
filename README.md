# Learnosity Build Aide MCP for JetBrains IDEs

A thin plugin that adds the remote, authless Build Aide server
(`https://mcp.learnosity.com/build/build-aide`) to JetBrains AI Assistant and Junie. It bundles no
server code. The Marketplace listing, legal notice included, is the `<description>` in
`src/main/resources/META-INF/plugin.xml`.

## How it registers the server

Neither client has a public API for adding MCP servers, but each reads a user-level `mcp.json`:

| Client | File | New servers |
| --- | --- | --- |
| AI Assistant (2025.3+, build 253) | `~/.ai/mcp/mcp.json` | Disabled until the user enables them, unless "Automatically enable new and changed MCP servers" is on |
| Junie (IDE plugin and CLI) | `~/.junie/mcp/mcp.json` | Enabled |

On project open the plugin adds a `learnosity-build-aide` entry to the file of each client in use,
meaning its plugin is enabled or its config directory exists, leaving any other entries alone. It
does this once per client, so a user who removes the server keeps it removed, and a client
installed later still gets it. When AI Assistant is one of them, the notification links to its MCP
settings page to enable the server. If a file is not valid JSON, the plugin leaves it untouched and
shows a notification explaining how to add the server by hand.

Uninstalling or disabling the plugin removes the entries again, but only those the plugin added.
That runs when the IDE unloads the plugin without a restart; if it needs a restart instead, the
entries stay.

## Build

Gradle needs a JDK 17 or later to run, set as `JAVA_HOME` or on the `PATH`, and downloads the
JDK 21 it compiles with on first use.

```bash
./gradlew buildPlugin      # build/distributions/build-aide-mcp-<version>.zip
./gradlew verifyPlugin     # Plugin Verifier against the lowest and latest supported IDEs
./gradlew runIde           # sandbox IDE with AI Assistant and Junie installed
```

To test in a real IDE, use **Settings | Plugins | ⚙ | Install Plugin from Disk…** with the zip.

## Release

Bump `version` in `gradle.properties` and add a `<change-notes>` entry in `plugin.xml`. Learnosity
CI signs and publishes to the [JetBrains Marketplace](https://plugins.jetbrains.com/). Signing
reads `CERTIFICATE_CHAIN`, `PRIVATE_KEY` and `PRIVATE_KEY_PASSWORD`, and publishing reads
`PUBLISH_TOKEN`; without them `signPlugin` is skipped.

## License

[GNU General Public License v3.0](LICENSE).
