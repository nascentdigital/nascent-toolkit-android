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
    compile 'com.nascentdigital.nascenttoolkit:nascenttoolkit:3.0.0'
}`


for instructions on how to update this library on Maven, follow this link:
http://code.tutsplus.com/tutorials/creating-and-publishing-an-android-library--cms-24582