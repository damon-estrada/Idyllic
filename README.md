# Idyllic
Application is still in development :smile:



The following gifs represent the current state of the application with a lot of behind the scenes things not yet shown. The first gif shows the login screen which then redirects the user to the home screen. From the home screen, the user is able to see various statistics about their listening habbits and what is currently playing (shows track audio features).

![](https://media.giphy.com/media/jpR3MrnJZXecYjirJn/giphy.webp) ![](https://media.giphy.com/media/RJ1UjGhHKzva5ARAZb/giphy.webp) ![](https://media.giphy.com/media/Vd2Bgm9vhbT2PUF0UB/giphy.webp)

Utilizes https://github.com/kaaes/spotify-web-api-android wrapper to make most API calls and others myself that are not supported.

## TODO
* Implement the other features on the home screen.
* Include more graphical representation of tracks saved by the user.
* Implment a song memories feature which will display songs saved on their "anniversary".
* Keep track of seconds, minutres, ... , years of time listening to music.
* Let the user search for a track to look up audio features for instead of the current song being played.
* Improve UI
* Support tests for the app.
* Support horizontal phone view.
* Handle the chance a user might not have the app installed since it requires it to be installed (redirect to google play).
* If build API < 26, need a way to handle the music playback continuing when a user launches the application.
* Dynamic picture creation based on listening habbits which will color a picture based on mood of songs user listens to.
