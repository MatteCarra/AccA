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

ACC manipulates Android low level ([kernel](https://duckduckgo.com/?q=kernel+android)) parameters which control the charging circuitry.
The author assumes no responsibility under anything that might break due to the use/misuse of this software.
By choosing to use/misuse ACC, you agree to do so at your own risk!



---
## DESCRIPTION

ACC is primarily intended for [extending battery service life](https://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries). On the flip side, the name says it all.

AccA is an official ACC configuration/management app. Its main target are those unfamiliar with terminal.



---
## PREREQUISITES

AccA won't run without ACC. the latter has prerequisites of its own. Refer to its [documentation](https://github.com/VR-25/acc/blob/master/README.md) for details.

When first launched, AccA attempts to install ACC automatically. Less than 100kb of data are downloaded. Future versions of the app will ship with ACC built-in.



---
## USAGE

ACC is designed to run out of the box, without user intervention. You can simply install it and forget. However, as it's been observed, most people will want to tweak settings - and obviously everyone will want to know whether the thing is actually working.

AccA's user interface is intuitive and displays configuration information/tips so that users don't have to read documentation to find their way. However, it's still highly recommended to read [ACC's documentation](https://github.com/VR-25/acc/blob/master/README.md) in order to have a broader understanding of how AccA works with it.



---
## TROUBLESHOOTING


### AccA Says "Daemon Is Not Running", Despite `acc -D` Showing Otherwise

- AccA must run as root.
- Make sure you have the latest versions.
- Ensure ACC language is set to English (`language=en`). The app doesn't "understand" other languages.


### Charging Switch

By default, ACC cycles through all available [charging control files](https://github.com/VR-25/acc/blob/master/acc/switches.txt) until it finds one that works.

Charging switches that support battery idle mode take precedence - allowing the device to draw power directly from the external power supply when charging is paused.

However, things don't always go well.
Some switches may be unreliable under certain conditions (e.g., screen off).
Others may hold a [wakelock](https://duckduckgo.com/?q=wakelock) - causing faster battery drain - while in plugged in, not charging state.

Run `acc --set chargingSwitch` (or `acc -s s` for short) to enforce a particular switch.

Test default/set switch(es) with `acc --test`.

Evaluate custom switches with `acc --test <file onValue offValue>`.


### Charging Voltage Limit

Unfortunately, not all devices/kernels support custom charging voltage limit.
Those that do are rare.
Most OEMs don't care about that.

The existence of a potential voltage control file doesn't necessarily mean it works.


### Restore Default Config

`acc --set reset` (or `acc -s r`)


### Slow Charging

Check whether charging current in being limited by `applyOnPlug` or `applyOnBoot`.

Nullify coolDownRatio (`acc --set coolDownRatio`) or change its value. By default, coolDownRatio is null.


### Logs

Logs are stored at `/sbin/.acc/`. You can export all to `/sdcard/acc-logs-$device.tar.bz2` with `acc --log --export`. In addition to acc logs, the archive includes `charging-ctrl-files.txt`, `charging-voltage-ctrl-files.txt`, `config.txt` and `magisk.log`.



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



## TIPS


### Generic

Force fast charge: `applyOnBoot=/sys/kernel/fast_charge/force_fast_charge:1`


### Google Pixel Family

Force fast wireless charging with third party wireless chargers that are supposed to charge the battery faster: `applyOnPlug=wireless/voltage_max:9000000`.


### Razer Phone

Alternate charging control configuration:
```
capacity=5,60,0,101
applyOnBoot=razer_charge_limit_enable:1 usb/device/razer_charge_limit_max:80 usb/device/razer_charge_limit_dropdown:70
```

### Samsung

The following files could be used to control charging current and voltage (with `applyOnBoot`):
```
battery/batt_tune_fast_charge_current (default: 2100)

battery/batt_tune_input_charge_current (default: 1800)

battery/batt_tune_float_voltage (max: 4350)
```


---
## ACC LINKS

- [Git repository](https://github.com/vr25/Acc/)
- [Battery University](http://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries/)
- [Facebook page](https://facebook.com/VR25-at-xda-developers-258150974794782/)
- [Telegram channel](https://t.me/vr25_xda/)
- [Telegram group](https://t.me/acc_magisk/)
- [Telegram profile](https://t.me/vr25xda/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)
