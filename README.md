nascent-toolkit-android
=======================

To include this library in your android project, add the following lines to your gradle configuration files:

project build.gradle file:

`allprojects {
    repositories {
        maven {
            url  "http://dl.bintray.com/nascentdigital/maven"
        }
    }
}`

module build.gradle file:

`dependencies {
    compile 'com.nascentdigital.nascenttoolkit:nascenttoolkit:2.1.0'
}`