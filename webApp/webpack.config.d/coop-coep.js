// Required for Room 3 WebWorkerSQLiteDriver + SQLite WASM OPFS.
// See room-web-demo and https://developer.android.com/jetpack/androidx/releases/room3
;(function (config) {
    config.devServer = config.devServer || {}
    config.devServer.headers = Object.assign({}, config.devServer.headers || {}, {
        "Cross-Origin-Opener-Policy": "same-origin",
        "Cross-Origin-Embedder-Policy": "require-corp",
    })
})(config)
