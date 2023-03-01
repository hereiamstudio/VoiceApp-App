# VoiceApp - App

## Building

This project is fairly standard, with only a small number of setup steps required before the project
will build.

### Firebase

This project uses a number of Firebase features, such as Firestore (for data transport and storage)
and Crashlytics. As such, `google-services.json` files need to be placed in your project so that the
app can authenticate with the Firebase services. Please consult the Firebase documentation to learn
more about how this works.

Specifically in this project there are two environments set up: production and staging. These are
represented as three different build flavours (debug uses the staging environment). However you
could trivially configure this differently to point at a single environment, or three environments,
or whatever your setup is.

The `google-services.json` file needs to point towards the Firebase project linked to the server
project. Please see the server code repository for details.

Once you have the `google-services.json` files for your Firebase projects, place them as appropriate
in the following dirs;

- `app/src/debug/`
- `app/src/staging/`
- `app/src/release/`

If any of the dirs are missing then create them.

If the required `google-services.json` files are not supplied, the build will error.

### Public key

Some transmissions with the server have extra encryption applied. This requires a public key that
pairs with a private key on the server.

When you have the public key, this should then be Base64 encoded (no wrap). This Base64 string
should then be placed in your global Gradle properties file, typically located at
`~/.gradle/gradle.properties`. The following keys are supported for the different environments;

- `voiceappPublicKeyStaging`
- `voiceappPublicKeyProduction`

For example;

`voiceappPublicKeyStaging=<no-wrap Base64 of public key goes here>`

If these properties are not supplied, the app will still build and run, but data encryption will 
fail.

### App Signing Keystores

#### Debug / staging

A debug keystore should be created and placed at `<project root>/debug.keystore`. The SHA1 signature
of this keystore should be registered with the package name on Firestore so that only the known
package/signature pairs can authenticate with Firebase. Consult `<project root>/app/build.gradle`
for the credentials of this debug keystore.

If no debug key is supplied, the build will error.

#### Release

Just like debug builds, the release keystore signature should be registered against Firebase.

Release keystore credentials can be injected at build time, either through
`~/.gradle/gradle.properties` or supplying properties to the Gradle command line.

The following property key names are used;

- `voiceappStoreFile`: the path to the keystore file
- `voiceappStorePassword`: the keystore password
- `voiceappKeyAlias`: the key alias
- `voiceappKeyPassword`: the key alias password

If no valid release key credentials are supplied, the build will error.

### Firebase App Distribution

This project has support for Firebase App Distribution. Please consult Google's documentation for
instructions on how to set up authentication.

## App architecture

Some brief notes on app architecture.

### Data storage

The backend is Firebase Firestore which is a NoSQL (document storage) database. Firestore takes care
of data transport and data storage.

### Data Sync

The data is synced from Firestore periodically using a `Worker` currently set to do so every 15
minutes. The syncing can also be manually triggered by clicking the sync icon on the main screen
`ProjectListActivity`, this is likely unnecessary but may be useful in poor connectivity
environments.

### Encryption

Some data stored in Firestore is encrypted with our own public key and is then decrypted on the
server.

### Online/offline

Internet is required for app login and data syncs (both up and down). Otherwise, the app is usable
offline.

Interviews can be conducted offline and the data is stored locally on the device within a local
copy of the Firestore database. When conditions allow, collected data will be sent up to the server.

Poor connections or lack of internet may affect the performance of speech-to-text support. Some
languages, e.g. English, have offline support, but most do not. Therefore if the device is offline
and there is no offline language pack installed for the interview language then speech-to-text will
not be functional.

# Suggested changes

- Use Hilt for dependency injection.
- Refactor the entire data layer to use a DAO pattern that abstracts the source of the data.
- Better, more structured architecture.
- Better use of ViewModels.
- Kotlin Coroutines/Flow introduced around the app.
- UI rewritten in Jetpack Compose.
- Automated tests.