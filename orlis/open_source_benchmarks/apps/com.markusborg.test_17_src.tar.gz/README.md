# RacketGhost

- Android Open Source Ghosting Coach for Squash, Badminton, and Racketlon 

## Version History
< 1.0.0 - Initial development

1.0.0 - Released on Amazon Appstore: http://www.amazon.com/Markus-Borg-RacketGhost/dp/B013YY2MKE/

1.1.0 - Audio added. Released on Google Play: https://play.google.com/store/apps/details?id=com.markusborg.test&hl=en

1.2.0 - Released on Google Play Oct 3, 2015
- Added picture of squash ball
- Increased countdown from 3s to 5s
- Minor bug fixes

1.2.1 - Released on Google Play Oct 10, 2015
- Fixed sound bug on Sony Xperia

1.2.2 - Released on Google Play Oct 11, 2015
- Changed minSdkVersion to 9
- Added SharedPreferences to store the latest setting between sessions

1.3.0 - Released on Google Play and Amazon Appstore Oct 14, 2015
- First release with a stable implementation of all basic features
- Major layout fixes

1.3.1 - Released on Google Play Oct 18, 2015
- Updated the layout for ghosting sessions, added the logo in the center

1.3.2 - Released on Google Play Oct 18, 2015
- Fixed bug, no squash balls presented on volley left and right

1.4.0 - Released on Google Play Oct 21, 2015
- A badminton mode has been added
- Squash/badminton is chosen using a Spinner
- Seek bars replace the edit texts in the main activity

1.5.0 - Released on Google Play Nov 23, 2015
- Added a listview with a custom adapter to the ResultsActivity

1.5.1 - Released on Google Play Nov 24, 2015
- Bug fix for parsing the history file of early versions of RacketGhost

1.5.2 - Released on Google Play Nov 25, 2015
- Bug fix for API versions before LOLLIPOP
- Fixed presentation issue in MainActivity

1.6.0 - Released on Google Play Dec 23, 2015
- Major changes to the thread model

1.7.0 - Released on Google Play Jan 5, 2016
- Major changes to the graphics

1.7.1 - Released on Google Play Jan 13, 2016
- Updated the RacketGhost icon
- Improved text visibility
- Updated Help and About dialogs

1.8.0 - Released on Google Play Jan 28, 2016
- Added sport specific click sounds
- Added stereo effects during ghosting

## Background

Ghosting is an established training exercise in racket sports to practice court movement without a ball. Running to the various corners of the court improves both footwork and general stamina. In the best of worlds, your coach or sparring partner will always be ready to point you to random corners at a pace that pushes you to your limits.

Unfortunately, few people have access to a coach at all times, thus have to run the exercise on their own. That means the randomness in the order of corners is missing.

With RacketGhost you get to have a ghosting coach on your mobile device, always ready to run a ghosting session with you!

## Getting Started

Using RacketGhost means following these steps:

1. Configure your ghosting session.
2. Put your mobile device in front of you on the court.
3. Press "GO!" to start your ghosting session. 
4. Move to the starting position (mid court).
5. RacketGhost highlights court positions for you to reach.
6. Compare your recent session with your ghosting history.
7. Press "Back" to return to the main screen.

In the main screen you get to set the following parameters for your next ghosting session:

- Reps: The number of repetitions per set, i.e., the number of corners to reach. [DEFAULT=15]
- Sets: The number of sets in the ghosting session. [DEFAULT=3]
- Interval: The time (in milliseconds) between repetitions in the ghosting session. [DEFAULT=5000]
- Break btw. sets: The time (in seconds) to rest between sets. [DEFAULT=15]

Furthermore, there are two checkboxes [DEFAULT=CHECKED]:

- 6 corners: If checked, RacketGhost points you to six court positions, otherwise only the four corners are used.
- Audio: Play complementing audio cues

## Further Information

- The current version of RacketGhost is tailored for squash. Although it works fine for badminton as well, refering to court positions as volley left/right does obviously not make sense.
- As the frequent releases suggest, the project relies on continous deployment. 
- To compensate for the shorter distances, the interval for volley positions is automatically decreased to 2/3 of the setting.
- In the main screen the last 3 ghosting sessions are listed, but in the summary screen 15 sessions are presented.
- You can stop an ongoing ghosting session by pressing "Stop". Press "Back" to return to the main screen.
