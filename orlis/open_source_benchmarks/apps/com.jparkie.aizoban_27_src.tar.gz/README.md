Aizoban
=======

![Feature Graphic](https://cloud.githubusercontent.com/assets/9499097/5429148/16e40ee2-83af-11e4-9588-f52f1104b9c4.jpg)

An online and offline Manga reading application for Android.

This Android application allows users to browse a catalogue of mangas from various sources in a local SQLite database. This catalogue can be searched by name and filtered by genres, status, name, and rank. Similarly, users can fetch latest updates for their chosen source. Clicking a manga, launches the MangaActivity in which they can view more information about a chosen manga and a list of available chapters. From this activity, users can either read a chapter online or add it to queue for a later download. When reading a chapter, the user has various reading controls and image gestures. Finally, the application allows the users to maintain their own libraries of downloaded manga, favourite manga, and recent chapters.

### APK Download

[**Direct Download**: (Version 1.2.3)](Aizoban/Aizoban_25.apk)

<a href="https://play.google.com/store/apps/details?id=com.jparkie.aizoban">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

## Source

The source code reflects beyond Version 1.0.0 of the application. It was architectured to test out the MVP design pattern backed by an Observable API to interact with the domain layer. Some points of interest in the source is the Observable API and the DownloadService. Specifically, the DownloadService downloaded chapters from a SQLite database backed task queue while handling network changes, application crashes, failure retries, and pausing through the use of RxJava.

Import the project with Android Studio.

## Development Process

This is my first official fully-featured Android application. I first started developing this application in October. However, the versions I released until December were a part of a public beta testing period during which I gathered more requirements for my application. Accordingly, from December 1st to December 14th, I decided to use the requirements I gathered to redevelop my application from scratch to adopt Material Design and to architecture the source in a manner which eased refractoring the code base for new features; a task which was very difficult in the beta versions through its updates. Nonetheless, I am new to many concepts in software engineering, so I caution anyone viewing the source to take what I did with a grain of salt.

## Libraries

- **Cupboard**: https://bitbucket.org/qbusict/cupboard/

This library was used to assist persisting the local SQLite database which stored the Manga and another database for application-specific and user data. Furthermore, the ability to work with content values, cursors, and databases by mapping results to POJOs helped the presenters to populate the views and the download manager to maintain a persistent task queue.

- **Disk Lru Cache**: https://github.com/JakeWharton/DiskLruCache

This library was used to persist a chapter's meta data temporarily for online reading.

- **Event Bus**: https://github.com/greenrobot/EventBus

This library was used to loosely couple communication between controllers and presenters in the application. For example, sending querying events to presenters to re-query and to repopulate their views with new data.

- **FloatingActionButton**: https://github.com/makovkastar/FloatingActionButton
- **Glide**: https://github.com/bumptech/glide

This library was used over Picasso (while they have a similar API) for custom Targets and Transcoders which eased the use of the Palette API.

- **Jsoup**: http://jsoup.org/
- **OkHttp**: http://square.github.io/okhttp/

This library was used to conduct all the application's HTTP requests. It was used in favour of using a more abstract library like Volley (which I used originally), so I can more control over the responses without needing custom requests and threading.

- **Okio**: https://github.com/square/okio
- **RoundImageView**: https://github.com/vinc3m1/RoundedImageView
- **RxJava**: https://github.com/ReactiveX/RxJava

This library was relied upon heavily to create an Observable API to fetch my data. Furthermore, it was used to handle most Activity- or Fragment- bound asynchronous tasks. I really enjoyed creating an Observable API as I had complete control of the execution of the code so that in the Activities and Fragments, data would be fetched asynchronously so the UI thread will not be blocked. Meanwhile, the download manager can transpose the API call to blocking alternatives to maintain a synchronous flow of execution. Finally, the rich API set of RxJava eased composing and refractoring implementations of user cases (i.e. mapping cursors to data, zipping multiple cursors, or handling errors...).

## License

    Copyright 2014 Jacob Park
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
## Disclaimer

Aizōban is developed by me, an individual, who does not have any affiliation with the content providers of the mangas available in the application.
