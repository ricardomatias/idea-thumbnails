<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# idea-thumbnails Changelog

## [Unreleased]

## [0.0.3]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
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
- [x] Use double click to open editor (instead of single click)
- [x] Add filter box
- [x] ADd timer for filter: https://stackoverflow.com/q/31666428
- [x] Add filter: https://www.logicbig.com/tutorials/java-swing/list-filter.html
- [x] Add filter-by-name
- [x] Add filter by tokens, tags

### Changed
- Reduce margins in cells
- Move all mentions of icon from ThumbsPanel to KotlinFile
- Show tags, description
- Show date (from git)

### Fixed
- Enlarged image popup window not showing image
- Drag & Drop no longer works. move code to main panel?
- Resize JLabel not working when updating icon.
- Delete does not update
- Drag & drop does not update

## [0.0.4]
### Changed
- Clean up

## [0.0.5]
### Added
- Call gc after removing all elements

### Changed
- Increase heap size to 2G in dev mode
- Implement Thumb class, remove code dealing with images from other classes
- Accept JPG drops
- Cache small thumbs to avoid recreating them on start (very inefficient)
