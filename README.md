# Advanced Charging Controller App (AccA)



---
## LEGAL

Copyright (c) 2019, [MatteCarra](https://github.com/MatteCarra/), [VR25](https://github.com/VR-25/), [Squabbi](https://github.com/Squabbi/)

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
## DOWNLOAD

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/mattecarra.accapp/)



---
## PREREQUISITES


- Android or Android based OS
- Any root solution (e.g., Magisk)
- Busybox (only if not rooted with Magisk)


Notes

- ACC comes bundled into AccA. Any existing version is automatically replaced.
- Uninstalling AccA also removes ACC.
- The GUIs for ACC upgrade/downgrade are work in progress. Meanwhile, you can upgrade/downgrade from Magisk Manager, EX/FK Kernel Manager, or similar app. Alternatively, the `acc --upgrade` command can be used; for details, run `acc --help`.



---
## USAGE

ACC is designed to run out of the box, without user intervention. You can simply install it and forget. However, as it's been observed, most people will want to tweak settings - and obviously everyone will want to know whether the thing is actually working.

AccA's user interface is intuitive and displays configuration information/tips so that users don't have to read documentation to find their way.
However, it's still highly recommended to read [ACC's documentation](https://github.com/VR-25/acc/blob/master/README.md) in order to have a broader understanding of how ACC and AccA work together.



---
## TROUBLESHOOTING


### AccA Says "Daemon Is Not Running", Despite `acc -D` Showing Otherwise

- ACC language must be set to English (`language=en`). AccA doesn't work with other ACC languages.
- AccA must run as root.
- Make sure you have the [latest version](https://github.com/MatteCarra/AccA/releases/).


### Battery Capacity (% Level) is Misreported

The "smart" battery must be calibrated. Refer to the `FAQ` section below for details.


### Charging Switch

By default, ACC uses whatever [charging switch](https://github.com/VR-25/acc/blob/master/acc/switches.txt) works.

If `prioritizeBattIdleMode` is enabled, charging switches that support battery idle mode take precedence - allowing the device to draw power directly from the external power supply when charging is paused.

However, things don't always go well.

- Some switches are unreliable under certain conditions (e.g., screen off).
- Others hold a [wakelock](https://duckduckgo.com/?q=wakelock) - causing faster battery drain.
- High CPU load and inability to re-enable charging may also be experienced.

In such situations, you have to find and enforce a switch that works as expected. Here's how to do it:

1. Run `acc -test --` (or acc -t --) to see which switches work.
2. Run `acc --set chargingSwitch` (or acc -s s) to enforce a working switch. Alternatively, this can be done from the app.
3. Test the reliability of the set switch. If it doesn't work properly, try another one.


### Charging Voltage And Current Limits

Unfortunately, not all kernels support these features.
Those that do are rare.
Most OEMs don't care about that.

The existence of potential voltage/current control file doesn't necessarily mean these features are supported.


### Restore Default Config

The app offers that option. Alternatively, you can run `acc --set reset` (or `acc -s r`).


### Slow Charging

Check whether charging current in being limited by `applyOnPlug` or `applyOnBoot`.

Set `coolDownCapacity` to `101`, nullify coolDownRatio (from the app, or by running `acc --set coolDownRatio`), or change its value. By default, `coolDownRatio` is unset/null.


### Diagnostics/Logs

Logs are stored at `/sbin/.acc/`. You can export all to `/sdcard/acc-logs-$device.tar.bz2` with `acc --log --export`.
In addition to acc logs, the archive includes `charging-ctrl-files.txt`, `charging-voltage-ctrl-files.txt`, `config.txt`, `magisk.log`, and everything from `/data/adb/acc-*/logs/`.

Installation and initialization logs are located at `/data/adb/acc-*/logs/`.

The existence of `/dev/acc-modpath-not-found` indicates a fatal ACC initialization error.



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
## LOCALIZATION

Help us with translations at [CrowdIn](https://crowdin.com/project/advanced-charging-controller/)!



---
## TIPS


### Generic

Control the max USB input current: `applyOnPlug=usb/current_max:MICRO_AMPS` (e.g., 1000000, that's 1A)

Force fast charge: `applyOnBoot=/sys/kernel/fast_charge/force_fast_charge:1`

Use voltage control file as charging switch file (beta, battery idle mode support): `chagingSwitch=FILE DEFAULT_VOLTAGE STOP_VOLTAGE` (e.g., `chagingSwitch=battery/voltage_max 4380000 3500000`)


### Google Pixel

Force fast wireless charging with third party wireless chargers that are supposed to charge the battery faster: `applyOnPlug=wireless/voltage_max:9000000`.


### Razer Phone

Alternate charging control configuration:

`applyOnBoot=razer_charge_limit_enable:1 usb/device/razer_charge_limit_max:80 usb/device/razer_charge_limit_dropdown:70 --exit`


### Samsung

The following files could be used for controlling charging current and voltage (with `applyOnBoot` or `applyOnPlug`):
```
battery/batt_tune_fast_charge_current

battery/batt_tune_input_charge_current

battery/batt_tune_float_voltage
```


---
## FREQUENTLY ASKED QUESTIONS (FAQ)


> How do I report issues?

Open issues on GitHub or contact the developers on Telegram/XDA (linked below). Always provide as much information as possible, and attach `/sdcard/acc-logs-*tar.bz2`. This file is generated automatically. When this doesn't happen, run `acc --log --export` _shortly after_ the problem occurs.


> What's "battery idle" mode?

That's a device's ability to draw power directly from an external power supply when charging is disabled or the battery is pulled out. The Motorola Moto G4 Play and many other smartphones can do that. Run `acc -t --` or use the app to test yours.


> What's "cool down" capacity for?

It's meant for reducing stress induced by prolonged high charging voltage (e.g., 4.20 Volts). It's a fair alternative to the charging voltage limit feature.


> Why won't you support my device? I've been waiting for ages!

First, never lose hope! Second, several systems don't have intuitive charging control files; I have to dig deeper and improvise; this takes extra time and effort. Lastly, some systems don't support custom charging control at all;  in such cases, you have to keep trying different kernels and uploading the respective [power supply logs](https://github.com/VR-25/acc#power-supply-log).


> Why, when and how should I calibrate the battery?

Refer to https://batteryuniversity.com/index.php/learn/article/battery_calibration


> How do I get rid of the annoying screen constantly lighting up issue?

This is a device-specific issue. Use the app [SnooZZy Charger](http://snoozy.mudar.ca/) to prevent it.



---
## LINKS

- [ACC repository](https://github.com/VR-25/acc/)
- [AccA repository](https://github.com/MatteCarra/AccA/)
- [Battery University](http://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries/)
- [Daily Job Scheduler](https://github.com/VR-25/djs/)
- [Telegram group](https://t.me/acc_group/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)



---
## LATEST CHANGES

**v1.0.18 (22)**
> ACC (201909010)
- acc -u: always use current installDir
- Back-end can be upgraded from Magisk Manager, EX/FK Kernel Manager, and similar apps (alternative to acc -u)
- Attribute back-end files ownership to front-end app
- Automatically copy installation log to <front-end app data>/logs/
- Back-end can be upgraded from Magisk Manager, EX/FK Kernel Manager, and similar apps (alternative to acc -u)
- bundle.sh - bundler for front-end app
- Enhanced power supply logger (psl.sh)
- Fixed busybox and loopDelay handling issues
- Fixed coolDownRatio delays
- Flashable uninstaller: /sdcard/acc-uninstaller.zip
- Major optimizations
- Prioritize nano -l for text editing
- Richer installation and initialization logs (/data/adb/acc-*/logs/)
- Updated build.sh and documentation
- Updated Telegram group link (t.me/acc_group/)
- Use umask 077 everywhere
- Workaround for front-end autostart blockage (Magisk service.d script)
> AccA
- Delete ACCA orphan schedules (for example when reinstalling the app after the database has been reset)
> DJS (201909010)
- Fixed : --boot option

**v1.0.16 (21)**
> AccA
- Fixed execute on boot regex
- Fixed battery idle mode support test

**v1.0.14 (19)**
> ACC (201907211)
- Fixed `install-latest.sh` inconsistencies
- Fixed voltage limit typo (3920-4349, 3500-4350)
> AccA
- Fixed incorrect voltage limit range (3920-4350, 3500-4350)

**v1.0.13 (18)**
> ACC (201907210)
- Enhanced busybox detection and handling (solves installation and a bunch of other issues)
- Fixed `acc -x` "file not found" error
- Start accd immediately after installation (no more ~30 seconds delay)
- Show more descriptive installation info (including which logs to share on failure, and where to send them to)
> AccA
- Updated documentation
