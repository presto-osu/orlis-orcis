Tap the Black Tiles for Android
===============================

An android WebView-based build for my game *Tap the black tiles*.

## What?

It's a Javascript game embedded in a WebView.

I know, this sounds wrong, but I though since I had already coded that Javascript game, I should just try that to know if it works and if it could theoretically speed up the Android App development process.

## And? What were your results?

Well... It worked. Ish.

I faced a few weird bugs I managed to workaround with weird hacks.

Did it speed up the Android App development process ?

Well... I spent about twice the time it took me to build my Javascript app in embedding it in a WebView.

Still, it would probably have took me more time to rebuild the app from scratch, so... I don't know.

But now, I can focus on making the game better without having to write about 10000 lines of generic Java (aka getters, setters, abstract classes and other overengineering pleasures) for each simple feature.

## Wait! You said it was an experiment! Don't you have a chart or something to show your results?

Erf... Ok, here :

                     | Java | Javascript in WebView
-------------------- | ---- | ---------------------
Developing the app   | Slow | Fast
App speed at runtime | Fast | Slow
Requires icky hacks  | Nope | Yup
Requires icky code   | Yup  | Nope

## Can I contribute ?

Of course, I'd love that :)

Hopefully, the Java code is done, so adding features to this game can be done here : [https://github.com/316k/tap-the-black-tiles](https://github.com/316k/tap-the-black-tiles).
