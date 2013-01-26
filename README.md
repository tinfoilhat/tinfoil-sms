Tinfoil-SMS
============

Tinfoil Head Manifesto
----------------------

We here stand as mortal men committed to creating a software application
that will protect the rights and freedom of all men, as all men are created
equal.

We hold the following as inalienable rights of men

+ A right to privacy
+ A right to security
+ A right to cryptography


In addition, we call upon all men to value security above all else, as
security is only as good as the weakest link. Heed the following.

1. Trust no one
2. Design systems with the intent that everyone is acting against the system
3. Security above all else, including performance


We view the tinfoil hat as a right of passage, as a quick view of
[facebook's privacy mess](http://www.nytimes.com/interactive/2010/05/12/business/facebook-privacy.html?ref=personaltech)
will traumatize even the most reserved facebook users.

Take heed of the warnings presented in this document, Tinfoil-SMS is designed with the tinfoilhead[1]
in mind.

[1]: https://en.wikipedia.org/wiki/Tinfoil_hat "Tinfoil Head"


Project Guidelines and Goals
----------------------------

+ Tinfoil-SMS will be released as open source software under the GNU Public License Version 3
+ Project is currently alpha, the goal is a beta release by September
+ Tinfoil-SMS will make use of public key cryptography using ECC[2]
+ Use message authentication (HMAC) to verify messages from contacts
+ Use steganography to obfuscate text messages
+ Automatically import contacts from the phone to Tinfoil-SMS
+ Enable automatically adding new contacts that are trusted

[2]: https://en.wikipedia.org/wiki/Elliptic_curve_cryptography "Elliptic Curve Cryptography"


Copyright (Really Copyleft)
---------------------------

All of the source code in this repository, where the copyright notice is indicated in the source
code, is licensed under the [GNU General Public License, Version 3](http://www.gnu.org/licenses/gpl.html).
For a complete listing of the authors and copyright owners of Tinfoil-SMS, please see the AUTHORS file.
Furthermore, we make use of several libraries, some of which are licensed under the Apache license, all of
these of course retain their original copyright. Please see the NOTICE and CONTRIBUTIONS files for a complete
listing of the other open source libraries which we used to make this project possible, our most humble gratitude
goes out to the developers of these libraries as this project would have not been possible otherwise.
