package com.bernaferrari.remarkor.ui.components

expect fun supportsSharedStorageMode(): Boolean

/** True when notes live in the browser's site storage (web), where clearing site data deletes them. */
expect fun storesNotesInBrowser(): Boolean
