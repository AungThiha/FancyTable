# FancyTable
Android library to implement two-direction scrollable table with `adjustable number of docked/fixed columns and rows` and/or with one-column rows that fill the width of parent which makes it look like `multiple sub-tables with headers` inside the main table.

The library is written based on [TableFixHeaders](https://github.com/InQBarna/TableFixHeaders)

<img src="https://media.giphy.com/media/f6E5vnYcMz0KZBdJLz/giphy.gif" width="320" alt="Demo Gif"/>

# Usage


### Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
### Add the dependency
```groovy
dependencies {
  implementation 'com.github.AungThiha:FancyTable:1.0.0'
}
```
