# cljs-simple-rn-app
A simple ClojureScript React Native app built without re-natal and leiningen 

### Development

Start figwheel + CLJS REPL / nREPL

```
clj -R:repl build.clj figwheel --help

  -p, --platform BUILD-IDS   [:android]  Platform Build IDs - Android / iOS
  -n, --nrepl-port PORT                  nREPL Port
  -a, --android-device TYPE              Android Device Type
  -i, --ios-device TYPE                  iOS Device Type
  -h, --help

```
The following will be trigged when starting figwheel:

1. Enable source maps.
2. Generate `env/dev/env/dev.cljs` with host IP config based on selected platform and device, which is then needed for creating a connection with figwheel and re-frisk.
3. Generate `index.ios.js` and `index.android.js` in the project root directory, JS dependencies and resource dirs will be scanned in order to create necessary modules.

**Notes**

- Build settings are stored in `build-cfg.edn`. With `app-name`, `cljsbuild`, `js-modules`, `resource-dirs` configurable.
- Host IP is automatically detected based on selected platform and device. Can also be set arbitrarily in `.lan-ip`. Ideas borrowed from [Expo template for Clojurescript React Native](https://github.com/seantempesta/expo-cljs-template).

**Example usage**

Run for Android and iOS. Android on real device, iOS using simulator; start nREPL on port 9999

`clj -R:repl build.clj figwheel -p android,ios -a real -i simulator -n 9999`
