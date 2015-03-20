<img width="75px" height="75px" src="Icon.png" />  OpenKWSolver
=================================================================

Solve Captchas for [9kw.eu](http://www.9kw.eu/) the open-source and Material-Design-way!

<a href="https://play.google.com/store/apps/details?id=de.dotwee.openkwsolver">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

Using
-----

minSDK: 16 (4.1 Jelly Bean)
<br>targetSDK: 22 (5.1 Lollipop)

To use OpenKWSolver, you need an account on [9kw.eu](http://www.9kw.eu/), as well as an API-Key (grab one at [9kw.eu/userapi](http://www.9kw.eu/userapi.html)).
<br>Latest **stable** release (v1.5) is available on the Google Play Store [here](https://play.google.com/store/apps/details?id=de.dotwee.openkwsolver), as well as under the [release-section](https://github.com/dotWee/OpenKWSolver/releases) of this github repository.

Checksums
---------

<table>
  <tr>
    <td>md5 (release v1.5)</td>
    <td><code>37CF2C2EBE940C526F92C5DE8A237D0A</code></td>
  </tr>
  
  <tr>
    <th>md5 (debug v1.5)</th>
    <th><code>70351684EF8DA3F3B1CCAF94566A211B</code></th>
  </tr>
</table>

Building
--------

Automatically: use Android Studio's 'Check out from Version Control' - feature and use the URL of this project as source. <br>
Manually: clone this git-repository or download it as zip [here](https://github.com/dotwee/OpenKWSolver/archive/master.zip), extract and import it into Android Studio.

Used Permissions
----------------

+ .ACCESS_NETWORK_STATE to check if network is available
+ .INTERNET to request Captcha and download Image
+ .VIBRATE to signalize the arrivement of a new Captcha

Features
--------

Main
+ Support for self-only
+ Support for debug-mode

Automation
+ Loop-Mode (Auto-pull next captcha)
+ Loop-Timeout (Useful on slow network)
+ Auto updating balance

Layout and style
+ Selectable image size
+ EXPERIMENTAL Confirm-Solver

Notification
+ Vibration alert on new captcha

Screenshots
-----------

<table style="border: 0px;">
    <tr>
        <td><img width="200px" src="art/ScreenshotNormal.png" /></td>
        <td><img width="200px" src="art/ScreenshotWithCaptcha.png" /></td>
        <td><img width="200px" src="art/ScreenshotSettings.png" /></td>
    </tr>
</table>

Todo
----

+ Use 9kw's history API to view answered Captchas
+ Make it look beautiful and tidy up the code
+ Add Click mode and improve Confirm
+ Loading animation
+ Documentation

Found a bug / had a force-close?
--------------------------------

Open a new Issue and provide a logcat.

Dependencies
------------

+ Google's v7 AppCombat
+ Google's v13 Support Package
(Both used to bring Material Design to API < 21)

Credits
-------

+ Google: for their [Material Design Icons](https://github.com/google/material-design-icons) (Attribution 4.0 Internal license)

License
-------

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                    Version 2, December 2004

 Copyright (C) 2015 Lukas "dotwee" Wolfsteiner <lukas@wolfsteiner.de>

 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FUCK YOU WANT TO.
