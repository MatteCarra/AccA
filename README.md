# Advanced Charging Controller App (AccA)



---
## LEGAL

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.



---
## DISCLAIMER

Always read/reread this reference prior to installing/upgrading this software.

While no cats have been harmed, the authors assume no responsibility for anything that might break due to the use/misuse of it.

To prevent fraud, do NOT mirror any link associated with this project; do NOT share APKs! Share the official [releases link](https://github.com/MatteCarra/AccA/releases/) instead.



---
## WARNING

ACC manipulates Android low level (kernel) parameters which control the charging circuitry.
The author assumes no responsibility under anything that might break due to the use/misuse of this software.
By choosing to use/misuse ACC, you agree to do so at your own risk!



---
## DESCRIPTION

ACC is primarily intended for extending battery service life. On the flip side, the name says it all.

AccA is an official ACC configuration/management app. Its main target are those unfamiliar with terminal.



---
## PREREQUISITES

AccA won't run without ACC. the latter has prerequisites of its own. Refer to its documentation for details.

When first launched, AccA attempts to install ACC automatically. Less than 100kb of data are downloaded.

Future versions of the app will ship with ACC built-in.



---
## USAGE

ACC is designed to run out of the box, without user intervention. You can simply install it and forget. However, as it's been observed, most people will want to tweak settings - and obviously everyone will want to know whether the thing is actually working.

AccA's user interface is intuitive and displays configuration information/tips so that users don't have to read documentation to find their way. However, it's still highly recommended to read [ACC's documentation](https://github.com/VR-25/acc/blob/master/README.md) in order to have a broader understanding of how things work.



---
## TROUBLESHOOTING


### Charging Switch

By default, ACC cycles through all available charging control files until it finds one that works.

However, things don't always go well.
Some switches may be unreliable under certain conditions (e.g., screen off).
Others may hold a wakelock - causing faster battery drain - while in plugged in, not charging state.

In such situation a particular switch has to be enforced. In other words, the charging switch should NOT be set to "automatic".


### Charging Voltage Limit

Unfortunately, not all devices/kernels support custom charging voltage limit.

Since the authors don't own every device under the sun, they cannot tell whether yours does - but AccA can and will.


### Restore Default Config

Delete `/sdcard/acc/config.txt` and restart ACC daemon.


### Slow Charging

Nullify `coolDownRatio` or change its value. By default, the value is null.


### Logs

Logs are stored at `/sbin/.acc/`. You can export all to `/sdcard/acc-logs-$device.tar.bz2` with `acc --log --export` (acc -l -e for short). In addition to acc logs, the archive includes `config.txt` and `magisk.log`. AccA has a button to display the log in real time. Future versions will be able to export the log as the aforementioned terminal command does.



---
## POWER SUPPLY LOG


Please upload `/sbin/.acc/acc-power_supply-*.log` to [this dropbox](https://www.dropbox.com/request/WYVDyCc0GkKQ8U5mLNlH/).
This file contains invaluable power supply information, such as battery details and available charging control files.
A public database is being built for mutual benefit.
Your cooperation is greatly appreciated.


Privacy Notes

- When asked for a name, give your `XDA username` or any random name.
- For the email, you can type something like `noway@areyoucrazy.com`.

Example
- Name: `user .`
- Email: `myemail@iscool.com`


See current submissions [here](https://www.dropbox.com/sh/rolzxvqxtdkfvfa/AABceZM3BBUHUykBqOW-0DYIa?dl=0).



---
## ACC LINKS

- [Git repository](https://github.com/vr25/Acc/)
- [Battery University](http://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries/)
- [Facebook page](https://facebook.com/VR25-at-xda-developers-258150974794782/)
- [Telegram channel](https://t.me/vr25_xda/)
- [Telegram group](https://t.me/acc_magisk/)
- [Telegram profile](https://t.me/vr25xda/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)
