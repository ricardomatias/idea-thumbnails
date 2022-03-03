# idea-thumbnails

![Build](https://github.com/hamoid/idea-thumbnails/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.hamoid.ideathumbnails.svg)](https://plugins.jetbrains.com/plugin/com.hamoid.ideathumbnails)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.hamoid.ideathumbnails.svg)](https://plugins.jetbrains.com/plugin/com.hamoid.ideathumbnails)

## Work in progress!

Not ready for public consumption. 
Probably dangerous: it does edit your .kt files automatically!

And it's my first Idea plugin.

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [x] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
This Fancy IntelliJ Platform Plugin is going to be your implementation of the brilliant ideas that you have.

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be extracted by the [Gradle](/build.gradle.kts) during the build process.

To keep everything working, do not remove `<!-- ... -->` sections. 
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "idea-thumbnails"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/hamoid/idea-thumbnails/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

# Thumbnails

- [x] Scan .kt files, sort 
- [x] ~~filter files that contain `application`~~ Show all .kt files instead
- [x] load file contents into model
- [x] Figure out drag and drop
- [x] From those files, try to read header comment which includes hashId, description, date
- [x] If hashId missing, create it, set date, description placeholder.
- [x] Add reload button
- [x] Decide folder name for images: .thumbnails
- [x] Create .thumbnails on start if missing
- [x] Allow drag & drop image to image placeholder to set it.
- [x] Ask in Slack how to detect item in list under mouse. Figured out!
- [x] Copy image upon drag and drop
- [x] Activate updating real .kt files with header (now using /tmp)
- [x] For programs without hash show image placeholder. Clicking on it opens file to edit.
- [x] Make it possible to click on image (enlarge) and name (open file to edit)
- [x] Paste thumbnail
- [x] Copy thumbnail- [ ] Enable plugin only if project is openrndr-template
- [x] Fix enlarged image popup window not showing image
- [x] Reduce margins in cells
- [x] Use double click to open editor (instead of single click)
- [x] Add filter box
- [x] Timer for filter: https://stackoverflow.com/q/31666428
- [x] Filter: https://www.logicbig.com/tutorials/java-swing/list-filter.html
- [x] Add filter-by-name
- [x] Fix Drag & Drop no longer works. move code to main panel?
- [ ] Resize JLabel not working when updating icon.
- [ ] Detect files added, moved, deleted and update list.
- [ ] Only enable plugin if it's an OPENRNDR project. How to detect?
- [ ] Add filter for with/missing/both thumbnail
- [ ] Add filter-by-age
