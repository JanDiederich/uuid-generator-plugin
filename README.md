<div align="center">
    <a href="https://plugins.jetbrains.com/plugin/8320-uuid-generator">
        <img src="./src/main/resources/META-INF/pluginIcon.svg" width="320" height="320" alt="logo"/>
    </a>
</div>
<h1 align="center">Intellij UUID Generator</h1>
<ul>
    <li>UUID
      <ul>
        <li>Version 4 <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier#Version_4_(random)">Wiki</a>,
            <a href="https://tools.ietf.org/html/rfc4122">RFC</a></li>
        <li>Version 7 <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier#Version_7_(timestamp_and_random)">Wiki</a></li>
      </ul>
    </li>
    <li>ULID <a href="https://github.com/ulid/spec">ULID</a></li>
    <li>CUID <a href="https://github.com/ericelliott/cuid">CUID</a></li>
</ul>
generator plugin for IntelliJ based IDEs.</p>

<p align="center">
    <a href="https://github.com/leomillon/uuid-generator-plugin/actions?query=workflow%3A%22Build+branch%22+branch%3Amaster"><img src="https://github.com/leomillon/uuid-generator-plugin/workflows/Build%20branch/badge.svg?branch=master"></a>
    <a href="https://github.com/leomillon/uuid-generator-plugin/releases"><img src="https://img.shields.io/github/release/leomillon/uuid-generator-plugin/all.svg"></a>
    <a href="https://github.com/leomillon/uuid-generator-plugin/blob/master/LICENSE.md"><img src="https://img.shields.io/github/license/leomillon/uuid-generator-plugin.svg"></a>
    <a href="https://plugins.jetbrains.com/plugin/8320-uuid-generator"><img src="https://img.shields.io/jetbrains/plugin/v/8320-uuid-generator.svg"></a>
    <a href="https://plugins.jetbrains.com/plugin/8320-uuid-generator"><img src="https://img.shields.io/jetbrains/plugin/r/stars/8320"></a>
    <a href="https://plugins.jetbrains.com/plugin/8320-uuid-generator"><img src="https://img.shields.io/jetbrains/plugin/d/8320-uuid-generator.svg"></a>
</p>

## JetBrains plugin

Link to the official plugin page : [UUID Generator](https://plugins.jetbrains.com/plugin/8320-uuid-generator)

## Description

<!-- Plugin description -->
UUID [Version 4](https://tools.ietf.org/html/rfc4122),
[Version 7](https://en.wikipedia.org/wiki/Universally_unique_identifier#Version_7_(timestamp_and_random));
[ULID](https://github.com/ulid/spec)
and [CUID](https://github.com/ericelliott/cuid) generator plugin for IntelliJ based IDEs.

For example : `123e4567-e89b-12d3-a456-426655440000`.
    
You will find it in the **Generate popup** -> **Random UUIDv4**.

## Available actions

- Random `UUIDv4` / `UUIDv7` / `ULID` / `CUID` (also as quick fix)
- Generate `UUIDv4` / `UUIDv7` / `ULID` / `CUID` to clipboard
- Generate `UUIDv4` / `UUIDv7` / `ULID` / `CUID` Popup
- Toggle `UUID` dashes (also as quick fix)
- Reformat `UUID` / `CUID` with settings (also as quick fix)
- Replace Distinct `UUID`s in Selection
- Replace Random `UUID` / `ULID` / `CUID` Placeholders in Selection

`UUID`/`ULID`/`CUID` highlight in any language with context info (Timestamp extraction for `UUIDv7` and `ULID`) and
quick fix suggestions

## Demo

- `UUID` / `ULID` / `CUID` highlight

![UUID / ULID / CUID highlight](./screenshots/ulid_highlight_with_timestamp.png)

- `UUID` / `ULID` / `CUID` quick fixes

![UUID / ULID / CUID quick fixes](./screenshots/uuid_quick_fixes.png)

- Random `UUIDv4` / `UUIDv7` generation

![Random UUID](./screenshots/generate_random_uuid.png)

- `UUID` Generate Popup

![UUID Generate Popup](./screenshots/generate_popup.png)

- Multi-caret support

![Multi-caret support](./screenshots/multi_caret_support.png)

- Toggle dashes

![Toggle dashes](./screenshots/toggle_dashes.png)

- `UUID` to clipboard

![UUID to clipboard](./screenshots/uuid_to_clipboard.jpg)

- `UUID` format settings dialogue

![UUID format settings dialogue](./screenshots/uuid_settings_dialogue.png)

- ID placeholders highlight

![ID placeholders highlight](./screenshots/id_placeholders_highlight.png)

- ID placeholders replacement action

![ID placeholders replacement action](./screenshots/id_placeholders_replacement.apng)

- Live debugger insights

![Live debugger insights](./screenshots/debugger_insights.png)

## Contributors

Special thanks to:

- [Davide Maggio (DVDAndroid)](https://plugins.jetbrains.com/author/683c57fa-d7ec-4d24-ae4d-82442d3aa75a)

<!-- Plugin description end -->
