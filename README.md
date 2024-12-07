## Android app template 1
This is a template for making your base Android app

[![Android CI](https://github.com/MDP43140/AndroidAppTemplate1/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/MDP43140/AndroidAppTemplate1/actions/workflows/android.yml)
[![GitHub issues](https://img.shields.io/github/issues/mdp43140/AndroidAppTemplate1)](/issues)
[![License](https://img.shields.io/github/license/mdp43140/AndroidAppTemplate1)](/blob/master/LICENSE)

### Features/TODO:
+ Free and open-source
+ Adopts some best standards
	+ REUSE Compliant
	+ Semantic versioning
+ Panic Responder Activity
+ Material Design 3
+ Uses Kotlin, both source code and Gradle
+ Uses Fragment
+ Allows changing app language through app info
+ Generate baseline profile
+ Latest dependency and tooling
	+ Java 21
	+ Minimum Android 7 (SDK 24, still has some majority of users, while also having smaller result apk), Targets Android 15 (SDK 35)
+ Compiler analytics disabled (shame on google for that)
+ Optimizations (fast compiling speed, fast and small app
	compiling configurations) while not obfuscating the codes
+ (partially) Reproducible builds
+ Memory leak detection library on debug builds
- Compose (as another variant, this one itself will be super complex)
- Additional features that will add another dependency
- Whatever...
- PNG Optimization disabled (use `optipng` instead)

### Formatting
+ Uses tabs (displayed as 2 or 4 spaces, depending on your preference) instead of spaces
+ As compact but readable and nicely structured as possible :)
+ etc etc etc...

### Tips
+ This project optionally depends on AEL (Android Error Logger),
	if you dont want it, remove the dependency in app/build.gradle.kts,
	and remove any imports or codes related to io.mdp43140.ael
	in all Kotlin codes.
+ Don't forget to update the dependencies :)

### Compiling the app
First, export some variables (for Linux users. Windows user might want to adjust this a bit):
```bash
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-~/.android/SDK}"```

Then go to the project root directory, and run this command:
```bash
./gradlew :app:assembleRelease```

if you're using Windows, change `./gradlew` to `gradlew.bat`

### Contributing

If you have a problem, question or an idea, just open an issue!

If you would like to help with developing, have a look at the issues, or think of something that could be improved, and open an issue for it.

Please tell me what you are going to do, to avoid implementing the same thing at the same time :-)

### Credits
- mdp43140 - 2024

### License
[![GNU GPL v3](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)

Android app template 1 is Free Software: You can use, study, share, and improve it at
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
