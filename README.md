# Introduction

It is the very first day at your new job as an Android developer. You have been given a task to finish a proof of concept for the new image viewer feature of the company’s flagship application.
The application is supposed to download a picture from the web via `OkHttp` and display it in the `ImageView` using `Picasso`.

It works fine on `Lollipop`, `Marshmallow`, `Nougat` and so on.
The problem is it should also work on `KitKat`, but apparently it is not.

# Problem Statement

One of the developers has already had a look into the application and has identified the problem. He said that the old platforms lack support for `TLS 1.2`.

Adjust the `MainActivity` implementation so that the `OkHttp` client supports `TLS 1.2`, even if it has been disabled by the Operating System.

# Hints

Note: Please do NOT modify any tests. **You can only change the existing files.**

## Note

Please be careful when editing `build.gradle` in your project. This task as it is doesn’t require any changes to it. It is generally ok to add new dependencies but changing or removing existing dependencies or configuration can cause the project and verification tests to not function in the expected way and give a unreliable score.