eve-control
============

Android Application to control tickets with an existing e-venement instance.

When the app is launched, the user is prompted to enter the host name of his e-venement instance as well as his login credentials wich will be stored for future authentifications.

Once the form submitted, the app will connect to the instance's server and display the access control page (tck.php/ticket/control), and tickets can be controlled directly from there.

During use, the app will automatically re-authenticate the user on server periodically to keep the session alive as long as needed.

Installation :
--------------

- Install the Java Development Kit (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Install Android Studio (http://developer.android.com/sdk/installing/index.html?pkg=studio -> show instructions for all platforms)

In Android studio click VCS → Checkout form version control → Github, then enter the repository's url (https://github.com/betaglop/eve-control.git)

If you want to generate a signed apk to install on normal devices: Build → Generate signed apk, then under keystore path click create new and fill in the form to create the keystore that will be used to sign your apk.
By default, the generated apk will be stored in %PROJECT_HOME%/app/app-release.apk.
Debug builds are stored in %PROJECT_HOME%/app/build/intermediates/outputs/apk/.

Details:
--------

The app connects to the e-venement instance using HttpUrlConnection, then passes its cookies to a webview wich displays the control screen.
Login credentials and host name are stored through android sharedPreferences mechanism.
Login and session extending network tasks, are executed in AsyncTasks not to freeze the main (UI) thread.
The login form has been put in a fragment acting as a slide drawer.
