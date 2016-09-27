##Sample
For sample usage in an android app, please see the sample folder.

##Install
- Place the `loginsdk.aar` in your libs folder.
- Add the below to your app level build.gradle file.
```
dependencies {
    compile(name:'loginsdk', ext:'aar')
}

repositories{
    flatDir{
        dirs 'libs'
    }
}
``` 
- Enjoy!
