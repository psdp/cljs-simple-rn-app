clean:
	rm -rf index.android.js index.ios.js target/

#--------------
# REPL
# -------------

repl: ##@repl Start REPL for iOS and Android
	clj -R:repl build.clj figwheel -p ios,android

repl-ios: ##@repl Start REPL for iOS
	clj -R:repl build.clj figwheel -p ios

repl-android: ##@repl Start REPL for Android
	clj -R:repl build.clj figwheel -p android
