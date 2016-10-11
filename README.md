##Sample
For sample usage in an android app, please see the sample folder.

##Install
- Place the `loginsdk.aar` in your libs folder.
- Add the below to your app level build.gradle file.
```
dependencies {
    compile(name:'loginsdk', ext:'aar')

    //Required for TTLogin - this is temporary
    compile "io.reactivex:rxandroid:1.2.0"
    compile "io.reactivex:rxjava:1.1.8"
    compile "com.mtramin:rxfingerprint:1.1.1"
    compile "com.squareup.okhttp3:okhttp-ws:3.4.1"
    compile "com.google.code.gson:gson:2.7"

}

repositories{
    flatDir{
        dirs 'libs'
    }
}
``` 
- Enjoy!