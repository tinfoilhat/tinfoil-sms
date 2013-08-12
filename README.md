Tinfoil-SMS
========================================

## What is Tinfoil-SMS

You can never be sure who or what is viewing your messages or what they’re going 
to do with them if they should get them. Tinfoil-SMS is an encrypted messaging 
application so your texts don’t fall into the wrong hands. Tinfoil-SMS uses 256 
bit ECC public keys as well as a unique signed key exchange to prevent any 
“man-in-the-middle” attacks.

The only way an unwanted party will see your messages is if they know your secret 
passphrases and trick you into accepting a key exchange from them. If you think 
we’re bluffing or you need to see it to believe it, feel free to poke around the 
source code. It’s open source and we’ve got nothing to hide.


## Get Tinfoil-SMS

You can get the latest release of Tinfoil-SMS from the Google Play Store, 
please see the project [Github page](http://tinfoilhat.github.io/tinfoil-sms/) 
for the relevant download URLs and screenshots of the application. Or if you 
prefer, you can get a copy of the latest code from the master branch, feel free 
to submit pull requests for any contributions you would like to make.


## Guiding Principles

1. The worst security is a false sense of security
2. The best security is the appearance of no security
3. Design systems with the intent that everyone is acting against the system
4. Security above all else, including performance



## Project Goals

* Always free for anyone to use and with no limitations
* Released as open source software under the GNU General Public License Version 3
* Simple to use and understand UI
* Easy to import contacts from the phone to Tinfoil-SMS
* Public key cryptography using Elliptic Curve Cryptography (ECC)
* A secure and reliable public key signing scheme to mitigate man-in-the-middle attacks
* AES-256 block cipher with SHA-256 message HMAC
* Incorporate steganography to obfuscate text messages
* Comprehensive source code documentation and wiki
* Thorough guide to help mitigate any security risks as a result of improper use
* Project is currently alpha, the goal is a beta release by September



## Future Plans

* Thorough beta phase with comprehensive bug testing and reporting using ACRA and Bug Sense
* Security audits and a detailed cryptanalysis of the application and the library, [Orwell](https://github.com/gnu-user/Orwell)
* Finalizing the cryptography during the beta release, so that it can remain unchanged for the stable release
* A stable release within 6 months of the beta release
* Incorporating steganography to obfuscate text messages for the next major release



## Copyright (Really Copyleft)

All of the source code in this repository, where the copyright notice is indicated in the source
code, is licensed under the [GNU General Public License, Version 3](http://www.gnu.org/licenses/gpl.html).
For a complete listing of the authors and copyright owners of Tinfoil-SMS, please see the AUTHORS file.
Furthermore, we make use of several libraries, some of which are licensed under the Apache license, all of
these of course retain their original copyright. Please see the NOTICE and CONTRIBUTIONS files for a complete
listing of the other open source libraries which we used to make this project possible, our most humble gratitude
goes out to the developers of these libraries as this project would have not been possible otherwise.
