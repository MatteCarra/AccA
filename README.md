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

While no cats have been harmed, the author assumes no responsibility for anything that might break due to the use/misuse of it.

To prevent fraud, do NOT mirror any link associated with this project; do NOT share builds (zips)! Share official links instead.



---
## WARNING

ACC manipulates Android low level (kernel) parameters which control the charging circuitry.
While nothing went wrong with my devices so far, I assume no responsibility under anything that might break due to the use/misuse of this software.
By choosing to use/misuse ACC, you agree to do so at your own risk!



---
## DESCRIPTION

AccA is an app that allows to have access to all ACC features without the need to open a terminal!
ACC is primarily intended for extending battery service life. On the flip side, the name says it all.



---
## PREREQUISITES

- Magisk 17-19


---
## USAGE

TODO

---
## TROUBLESHOOTING

- Charging switch
By default, ACC cycles through all available charging control files until it finds one that works. However, things don't always go well.
Some switches may be unreliable under certain conditions (e.g., screen off).
Others may hold a wakelock - causing faster battery drain - while in plugged in, not charging state.
Run `acc --set chargingSwitch` to enforce a particular switch.
Test default/set switch(es) with `acc --test`.
Evaluate custom switches with `acc --test <file onValue offValue>`.

- Charging voltage limit
Unfortunately, not all devices/kernels support custom charging voltage limit.
Since I don't own every device under the sun, I cannot tell whether yours does.
Use `acc --voltage :millivolts` (e.g., acc -v :4050) for evaluating charging voltage control files.

- Restore default config
`acc --set reset`

- Slow charging
Nullify coolDownRatio (`acc --set coolDownRatio`) or change its value. By default, coolDownRatio is null.

- Logs are stored at `/dev/acc/`. You can export all to `/sdcard/acc-logs-$device.tar.bz2` with `acc --log --export`.


---
## Power Supply Log


Please upload `/dev/acc/acc-power_supply-*.log` to [this dropbox](https://www.dropbox.com/request/WYVDyCc0GkKQ8U5mLNlH/).
This file contains invaluable power supply information, such as battery details and available charging control files.
I'm creating a public database for mutual benefit.
Your cooperation is greatly appreciated.


Privacy Notes

- When asked for a name, give your `XDA username`.
- For the email, you can type something like `noway@areyoucrazy.com`.

Example
- Name: `VR25 .`
- Email: `myemail@iscool.com`


See current submissions [here](https://www.dropbox.com/sh/rolzxvqxtdkfvfa/AABceZM3BBUHUykBqOW-0DYIa?dl=0).



---
## LINKS

- [ACC](https://github.com/vr25/Acc/)
- [Battery University](http://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries/)
- [Donate](https://paypal.me/vr25xda)
- [Facebook page](https://facebook.com/VR25-at-xda-developers-258150974794782/)
- [Git repository](https://github.com/VR-25/acc/)
- [Telegram channel](https://t.me/vr25_xda/)
- [Telegram group](https://t.me/acc_magisk/)
- [Telegram profile](https://t.me/vr25xda/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)
