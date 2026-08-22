# Product Requirements Document (PRD): Helper

## 1. Product Overview
**Helper** is a lightweight, pure client-side recipe viewer and item browser for Minecraft. The primary objective is to provide a fast, unintrusive alternative to heavy mod frameworks (like JEI, REI, or EMI). By focusing strictly on vanilla recipe types and rendering local client registries, Helper allows players to lookup crafting, smelting, and brewing recipes on any vanilla server without requiring server-side plugins or triggering anti-cheat mechanisms.

## 2. Technical Specifications
*   **Target Platform:** Fabric Loader (`>=0.16.x`)
*   **Target Version:** Minecraft `1.21.11`
*   **Java Version:** Java 21
*   **Mod Environment:** Client-only application (`client` entrypoint).
*   **Mapping Standard:** Yarn mappings.
*   **Data Synchronization:** Intercepts local game data to perform lookups. No server-side installation is required.

## 3. Core Features & Functions

### 3.1. Item Index & Discovery
*   **Global Registry Read:** On world join or initial GUI open, the mod must iterate through `BuiltInRegistries.ITEM` to compile a comprehensive list of all loaded blocks and items.
*   **Real-Time Filtering:** The item index must be searchable via a text query, filtering the displayed items by their localized display names in real-time.

### 3.2. Recipe Caching & Lookups
*   **Local Recipe Extraction:** The mod must read from the client's `RecipeManager` (`client.world.getRecipeManager()`) to cache output-to-ingredient mappings.
*   **Supported Vanilla Recipe Types:**
    *   Crafting (3x3 Shaped & Shapeless)
    *   Smelting, Blasting, Smoking, and Campfire Cooking
    *   Brewing Stand Potions
    *   Stonecutting and Smithing

### 3.3. Interactivity
*   **Click-to-View:** Clicking an item in the side-panel grid or hovering over an item in the player's inventory and pressing a hotkey (default `R`) must trigger the recipe lookup.
*   **Usage-Lookup:** Hovering over an item and pressing a secondary hotkey (default `U`) should show recipes where the targeted item is used as an ingredient.

## 4. UI Layout & Design

### 4.1. The Side-Panel Grid
*   **Injection Point:** Use a Mixin targeting `net.minecraft.client.gui.screen.ingame.HandledScreen`.
*   **Positioning:** Rendered on the right side of the screen, anchoring to the edge of the active inventory/container GUI.
*   **Grid Structure:** A clean, paginated matrix of item icons (standard 16x16 slots with spacing).
*   **Navigation:** Include left/right arrow buttons or support mouse-wheel scrolling to navigate pages of items.

### 4.2. Search Bar Widget
*   **Positioning:** Located at the bottom of the side-panel grid.
*   **Visuals:** A standard Minecraft text input field (`TextFieldWidget`) that remains focused when clicked.
*   **Clear Button:** An optional small "x" button inside the search field to quickly clear the current query.

### 4.3. Recipe Popup Screen
*   **Overlay Design:** A centered GUI overlay that darkens the background inventory.
*   **Recipe Cards:** Render visual representations of the crafting stations.
    *   *Crafting:* 3x3 slot grid -> Arrow -> Output Slot.
    *   *Furnace:* Input Slot -> Flame Icon -> Output Slot.
*   **Pagination:** If an item has multiple recipes (e.g., Wood Planks), display tabs or arrows at the top of the popup to cycle through the different recipes.

## 5. Settings & Configuration
Helper should include a lightweight configuration file (`helper-config.json` stored in the standard `config` directory) to manage basic user preferences.

### 5.1. Configurable Options
*   **`enableSidePanel` (boolean):** Master toggle to show/hide the right-side item grid (Default: `true`).
*   **`lookupHotkey` (string/key):** The keybind used to view a recipe when hovering over an item (Default: `R`).
*   **`usageHotkey` (string/key):** The keybind used to view item usage (Default: `U`).
*   **`togglePanelHotkey` (string/key):** A keybind to quickly hide or show the side panel (Default: `Ctrl + O`).
*   **`darkThemeMode` (boolean):** Toggles the recipe popup background between standard Minecraft gray and a darker, translucent texture (Default: `false`).

## 6. Implementation Milestones for AI Agent
1.  **Project Scaffold:** Initialize the `gradle.properties`, `build.gradle`, and `fabric.mod.json` for 1.21.11.
2.  **Core Mixin:** Implement `HandledScreenMixin.java` to inject basic rendering logic into container screens.
3.  **UI Widgets:** Build the `ItemPanelWidget` (grid layout) and the bottom search field.
4.  **Recipe Indexer:** Create the caching logic that reads `ClientRecipeManager` without crashing.
5.  **Recipe Display:** Build the popup screen that correctly formats and renders recipe ingredients via `DrawContext`.