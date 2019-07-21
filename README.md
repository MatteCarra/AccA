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

AccA is an official ACC front-end app. It targets mainly those who feel uncomfortable with terminal.



---
## PREREQUISITES


- Any root solution
- Busybox (only if not rooted with Magisk)


Notes

- ACC comes bundled into AccA. Any existing version is automatically uninstalled.
- Uninstalling AccA also removes ACC.
- The GUIs for ACC upgrade/downgrade are work in progress. Meanwhile, use `acc --upgrade`command; for details, run `acc --help`.



---
## USAGE

ACC is designed to run out of the box, without user intervention. You can simply install it and forget. However, as it's been observed, most people will want to tweak settings - and obviously everyone will want to know whether the thing is actually working.

AccA's user interface is intuitive and displays configuration information/tips so that users don't have to read documentation to find their way.
However, it's still highly recommended to read [ACC's documentation](https://github.com/VR-25/acc/blob/master/README.md) in order to have a broader understanding of how ACC and AccA work together.



---
## TROUBLESHOOTING


### AccA Says "Daemon Is Not Running", Despite `acc -D` Showing Otherwise

- AccA must run as root.
- Make sure you have the latest versions.
- Ensure ACC language is set to English (`language=en`). The app doesn't "understand" other ACC languages.


### Charging Switch

By default, ACC uses whatever [charging switch](https://github.com/VR-25/acc/blob/master/acc/switches.txt) works.

If `prioritizeBattIdleMode` is set to `true`, charging switches that support battery idle mode take precedence - allowing the device to draw power directly from the external power supply when charging is paused.

However, things don't always go well.

- Some switches are unreliable under certain conditions (e.g., screen off).
- Others hold a [wakelock](https://duckduckgo.com/?q=wakelock) - causing faster battery drain.
- High CPU load and inability to re-enable charging may also be experienced.

In such situations, you have to find and enforce a switch that works as expected. Here's how to do it:

1. Run `acc -test --` (or acc -t --) to see which switches work.
2. Run `acc --set chargingSwitch` (or acc -s s) to enforce a working switch. Alternatively, this can be done from the app.
3. Test the reliability of the set switch. If it doesn't work properly, try another one.


### Charging Voltage And Current Limits

Unfortunately, not all devices/kernels support these features.
Those that do are rare.
Most OEMs don't care about that.

The existence of potential voltage/current control file doesn't necessarily mean these features are supported.


### Restore Default Config

`acc --set reset` (or `acc -s r`)


### Slow Charging

Check whether charging current in being limited by `applyOnPlug` or `applyOnBoot`.

Set `coolDownCapacity` to `101`, nullify coolDownRatio (`acc --set coolDownRatio`), or change its value. By default, coolDownRatio is null.


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
- Email: `myEmail@isCool.com`


See current submissions [here](https://www.dropbox.com/sh/rolzxvqxtdkfvfa/AABceZM3BBUHUykBqOW-0DYIa?dl=0).



---
## TIPS


### Generic

Control the max USB input current: `applyOnPlug=usb/current_max:MICRO_AMPS` (e.g., 1000000, that's 1A)

Force fast charge: `applyOnBoot=/sys/kernel/fast_charge/force_fast_charge:1`

Use voltage control file as charging switch file: `chagingSwitch=FILE DEFAULT_VOLTAGE STOP_VOLTAGE`


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
## FREQUENTLY ASKED QUESTIONS (FAQ)


- How do I report issues?

Open issues on GitHub or contact the developers on Telegram/XDA (linked below). Always provide as much information as possible, and attach `/sdcard/acc-logs-*tar.bz2`. This file is generated automatically. When this doesn't happen, run `acc --log --export` shortly after the problem occurs.


- What's "battery idle" mode?

That's a device's ability to draw power directly from an external power supply when charging is disabled or the battery is pulled out. The Motorola Moto G4 Play and many other smartphones can do that. Run `acc -t --` to test yours.


- What's "cool down" capacity for?

It's meant for reducing stress induced by prolonged high charging voltage (e.g., 4.20 Volts). It's a fair alternative to the charging voltage limit feature.


- Why won't you support my device? I've been waiting for ages!

First, never lose hope! Second, several systems don't have intuitive charging control files; I have to dig deeper and improvise; this takes extra time and effort. Lastly, some systems don't support custom charging control at all;  in such cases, you have to keep trying different kernels and uploading the respective [power supply logs](https://github.com/VR-25/acc#power-supply-log).



---
## ACC LINKS

- [Git repository](https://github.com/vr-25/acc/)
- [Battery University](http://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries/)
- [Facebook page](https://facebook.com/VR25-at-xda-developers-258150974794782/)
- [Telegram channel](https://t.me/vr25_xda/)
- [Telegram group](https://t.me/acc_magisk/)
- [Telegram profile](https://t.me/vr25xda/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)
