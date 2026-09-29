PROJECT:
  name: Offline Food Gacha Gallery Android App
  goal: >
    Build an offline native Android app that scans a mother folder containing
    many first-level subfolders of images. Each subfolder is one item/card.
    The app displays a grid of items with title image, name, author, and tags.
    Users can manually edit metadata. The app can randomly roll one item.
    MVP uses simple random roll. Phase 2 adds CS:GO-style horizontal reel animation.

TARGET_PLATFORM:
  os: Android
  min_sdk: 26
  language: Kotlin
  ui: Jetpack Compose
  architecture: MVVM + Repository
  async: Kotlin Coroutines + Flow
  database: Room
  image_loading: Coil
  dependency_injection: Hilt

CORE_RULES:
  - App works fully offline.
  - One item = one first-level subfolder inside the selected mother folder.
  - Scan only first-level subfolders for MVP.
  - Do not scan nested folders deeply for MVP.
  - Randomization selects one item/folder, not one individual image.
  - Each item has one title image/cover image.
  - Default title image is the first image found inside the folder.
  - User can manually change title image.
  - User can view all images inside an item folder.
  - Metadata is assigned per item/folder, not per image.
  - Tags allow multiple values per item.
  - Tags are selected from an existing tag list manually imported by user.
  - Metadata is stored in local Room database.
  - App must not write JSON files into image folders.
  - App must support metadata export to JSON.
  - Image formats supported: JPG, JPEG, PNG, WEBP.
  - Target smooth performance for 100-200 items.

PERMISSIONS:
  - Use full storage permission for MVP.
  - On Android 11+, request MANAGE_EXTERNAL_STORAGE.
  - Provide a settings redirect if permission is denied.
  - Later optionally add SAF folder picker for Play Store compatibility.

DATA_MODEL:
  entities:
    ItemEntity:
      fields:
        - id: Long, primary key, autoGenerate
        - folderPath: String, unique
        - folderName: String
        - name: String
        - author: String
        - coverImageFileName: String?
        - dateAdded: Long
        - dateUpdated: Long

    TagEntity:
      fields:
        - id: Long, primary key, autoGenerate
        - name: String, unique

    ItemTagCrossRef:
      fields:
        - itemId: Long
        - tagId: Long
      primary_key:
        - itemId
        - tagId

    ItemImageEntity:
      fields:
        - id: Long, primary key, autoGenerate
        - itemId: Long, foreign key
        - filePath: String
        - fileName: String
        - dateAdded: Long

SCREENS:
  - HomeScreen:
      features:
        - Grant storage permission
        - Choose or enter mother folder path
        - Scan/rescan folder
        - Navigate to Library
        - Navigate to Roll
        - Navigate to Settings/Export

  - LibraryScreen:
      layout: Grid
      item_display:
        - thumbnail
        - name
        - author
        - tags
      actions:
        - tap opens ItemDetailScreen
        - long-press opens editor or context menu

  - ItemDetailScreen:
      display:
        - large title image
        - name
        - author
        - tags
        - all images in folder
      actions:
        - edit metadata
        - change cover image
        - view fullscreen image

  - ItemEditScreen:
      fields:
        - name
        - author
        - tags
        - cover image selection
      tag_input:
        - autocomplete from imported tags
        - allow selecting existing tags
      actions:
        - save
        - cancel

  - RollScreen:
      mvp:
        - Roll button
        - randomly select one item with equal probability
        - show result screen
      result_display:
        - title image
        - name
        - author
        - tags
      actions:
        - roll again
        - view details
      phase_2:
        - CS:GO-style horizontal scrolling reel
        - determine winning item first
        - build reel list with winning item at target position
        - animate horizontal scroll and land on winner

FOLDER_SCANNING_LOGIC:
  input:
    - motherFolderPath: String
  rules:
    - list only direct child directories
    - ignore files at root level
    - for each subfolder, collect supported image files:
        - .jpg
        - .jpeg
        - .png
        - .webp
    - sort images by filename for stable default cover
    - if subfolder has no supported images, mark as invalid or skip
  item_creation:
    folderPath: subfolder.absolutePath
    folderName: subfolder.name
    name: subfolder.name
    author: empty string
    coverImageFileName: first image filename or null
    images: all supported images in subfolder
  update_behavior:
    - if folderPath already exists, update images but preserve user-edited metadata
    - if folder no longer exists, optionally mark missing instead of deleting metadata

TAG_IMPORT:
  supported_formats:
    - txt
    - csv
  txt_format:
    - one tag per line
  csv_format:
    - one tag per line or single column named tag
  behavior:
    - trim whitespace
    - ignore empty lines
    - deduplicate tags
    - insert into TagEntity table

METADATA_EXPORT:
  format: JSON
  destination: user-selected export path or app-accessible Documents folder
  structure:
    version: 1
    exportedAt: ISO datetime
    items:
      - folderPath: string
        folderName: string
        name: string
        author: string
        coverImageFileName: string or null
        tags: array of strings
        images: array of file names

IMPLEMENTATION_PHASES:
  phase_1_project_setup:
    - create Android project
    - setup Kotlin, Jetpack Compose, Hilt, Room, Coil, Coroutines
    - add storage permissions

  phase_2_folder_access:
    - implement permission request flow
    - implement mother folder path selection/input
    - validate path access

  phase_3_scanner:
    - implement folder scanner
    - detect first-level subfolders
    - detect supported images
    - insert/update items and images in Room

  phase_4_library_ui:
    - build grid library screen
    - load thumbnails efficiently
    - show name, author, tags

  phase_5_metadata_editor:
    - build item edit screen
    - edit name, author, tags, cover image
    - save to Room

  phase_6_item_detail:
    - show item detail
    - show all images inside item folder
    - support fullscreen image viewing

  phase_7_tag_import:
    - import tags from TXT/CSV
    - provide tag autocomplete in editor

  phase_8_simple_roll:
    - implement equal-probability random item selection
    - show result screen with image, name, author, tags

  phase_9_export:
    - export item metadata to JSON

  phase_10_csgo_animation:
    - implement horizontal reel animation
    - choose winner first, then animate to winner

DATABASE_REQUIREMENTS:
  - Room DAOs for Item, Tag, ItemTagCrossRef, ItemImage
  - query items with tags
  - query all tags
  - upsert items by folderPath
  - replace item images when rescanning
  - preserve metadata when rescanning same folderPath

UI_REQUIREMENTS:
  - simple clean modern Android UI
  - dark mode optional
  - grid thumbnails must be cached
  - use lazy grid for performance
  - image loading must be asynchronous
  - avoid full-resolution decoding in grid

RANDOM_ROLL_REQUIREMENTS:
  mvp:
    - randomly select one item from all valid scanned items
    - equal probability
    - no rarity or weighting
  phase_2:
    - horizontal CS:GO-style reel
    - result predetermined
    - animation only visual

ACCEPTANCE_CRITERIA:
  - User can grant storage permission.
  - User can select a mother folder.
  - App scans direct subfolders only.
  - Each subfolder becomes one item.
  - Default item name equals folder name.
  - Default cover image equals first image in folder.
  - User can edit name, author, tags, cover image.
  - User can view all images inside an item.
  - Library displays items in grid with thumbnails.
  - Tags are selected from imported tag list.
  - Metadata persists after app restart.
  - Rescan does not overwrite user-edited metadata for existing folderPath.
  - Simple roll selects one item randomly.
  - Result screen shows title image, name, author, tags.
  - Metadata can be exported to JSON.
  - App does not modify original image folders.

NON_GOALS_FOR_MVP:
  - no online features
  - no server
  - no user accounts
  - no nested folder scanning
  - no rarity system
  - no weighted random
  - no roll history
  - no duplicate prevention
  - no search/filter unless extra time
  - no writing metadata files into image folders