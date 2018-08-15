# INSTANT README VIEWER

This is a demo of keeping your readme or instructions that you usually keep in the root folder of your android project folder accessable to within your actual android app!

## 1. Build script modificaiton

Oh, btw, did you know that this readme file is auto copied from the root directory of the project folder, into this app's `/src/main/res/raw` folder before being compiled?

It's quite a handy way to keep your readme file accessible in the resource directory (e.g. so you can view the readme file from within your app!)

Here is how to do it. Copy the code below to your gradle buildfile in /app/src/build.gradle . (You might need to adjust the "from" settings, but should be self explanatory).

     /*
     * Based on https://discuss.gradle.org/t/how-to-copy-and-rename-a-single-file/5956
     * */
     task copyreadme(type: Copy) {
         from '../README.md'
         into 'src/main/res/raw'
         rename { String fileName ->
             fileName.replace("README.md", "readme.md")
         }
     }
     tasks.copyreadme.execute()

Once you do that, everytime you build your file. It will auto copy the readme file to the raw resource directory of your android project folder. Let me know if it was handy!

## 2. Move `ReadMe.java` to your source code folder

Adapting to your project:

- Change package on top of this file to your package name.

- Add to your manifest, so that you can locate it:

        <activity
            android:name=".ReadMe"
            android:label="ReadMe" >
        </activity>

- Add `startActivity( new Intent( this, ReadMe.class ));` to your buttons or anywhere you want to launch the readme.


## 3. Done!

That wasn't hard was it?


# Version

- V1.0 - First version released.


# Todo

- Revamp the markdown/styler to use a more regexp, or to use a proper markdown engine.


# Markdownish Syntax:

This is the supported syntax. Can't support full markdown, since this is only a lite hacky implementation. But kind handy for a no frills display that is easy to type on a mobile phone keypad.

```
# H1 only in first line (Due to technical hacks used)

## H2 headers as usual

## Styling
Like: *italic* **bold** ***bold_italic***

## Classic List
 - list item 1
 - list item 2

## Nonstandard List Syntax
- list item 1
- list item 2

## Block Quotes
> Quoted stuff

## codes
here is inline `literal` codes. Must have space around it.

    ```
    codeblocks
    Good for ascii art
    ```

        Or 4 space code indent like classic markdown.
```

# Extra

Category: tools
Source: https://github.com/mofosyne/instantReadmeApp
Licence: https://github.com/mofosyne/instantReadmeApp/blob/master/COPYING.txt - GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007


# Coding tips

If you are creating layouts programmically. To avoid clashing with pregenerated IDs from XML layout when assigning view IDs, use `View.generateViewId();` (http://stackoverflow.com/questions/8460680/how-can-i-assign-an-id-to-a-view-programmatically)


# Links

* http://android-er.blogspot.com.au/2010/07/display-text-file-in-resraw_01.html
