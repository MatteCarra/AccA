# Advanced Charging Controller App (AccA)



---
## LEGAL

© 2019-2020, [MatteCarra](https://github.com/MatteCarra/), [VR25](https://github.com/VR-25/), [Squabbi](https://github.com/Squabbi/)

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

ACC (the back-end) manipulates Android low level ([kernel](https://duckduckgo.com/?q=kernel+android)) parameters which control the charging circuitry.
The author assumes no responsibility under anything that might break due to the use/misuse of this software.
By choosing to use/misuse ACC, you agree to do so at your own risk!



---
## DESCRIPTION

ACC is an Android software mainly intended for [extending battery service life](https://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries).
In a nutshell, this is achieved through limiting charging current, temperature and voltage.
Any root solution is supported. A recent, preferably stable Magisk version is recommended.

AccA is an official ACC front-end app.
It targets mainly those who feel uncomfortable with terminal.
The command line functionality is also available.



---
## DOWNLOAD

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/mattecarra.accapp/)



---
## PREREQUISITES

- [Must read - how to prolong lithium ion batteries lifespan](https://batteryuniversity.com/index.php/learn/article/how_to_prolong_lithium_based_batteries/)
- Android or Android based OS
- Any root solution (e.g., [Magisk](https://github.com/topjohnwu/Magisk/))
- [Busybox*](https://github.com/search?o=desc&q=busybox+android&s=updated&type=Repositories/) (only if not rooted with Magisk)
- Terminal** emulator (recommended: [Termux](https://f-droid.org/en/packages/com.termux/))
- [curl***](https://github.com/search?o=desc&q=curl+android&s=updated&type=Repositories/) (for acc --upgrade)
- Text editor (optional)

\* Instead of a regular install, the binary can simply be placed in /data/adb/.
That's a fallback path. ACC sets permissions (rwx------) as needed.
Precedence: Magisk busybox > system busybox > /data/adb/busybox

\*\* It's optional, but recommended.
The command line is a very powerful tool.

\*\*\* The Magisk module [Cross Compiled Binaries (ccbins)](https://github.com/Magisk-Modules-Repo/ccbins/) installs `curl`.

Note: ACC comes bundled into AccA.
Any existing version/variant is replaced.



---
## SETUP/USAGE

AccA's user interface is intuitive and has configuration details/hints, so that users don't have to read extensive documentation to find their way.
However, it's still highly recommended to read [ACC's documentation](https://github.com/VR-25/acc/blob/master/README.md) in order to get a broader idea of how ACC and AccA work together.

The back-end can be upgraded from within the app (online), or by flashing the latest ACC zip.
However, unless you have a good reason to do so, don't fix what's not broken.
Anyway, the zip can be flashed from Magisk Manager or similar app.
Alternatively, one can run `acc --flash` (or `acc -F`) on terminal and follow the instructions.

Uninstalling AccA or clearing its data also removes ACC.
ACC daemon must be stopped - from AccA's main screen - beforehand, though.
Otherwise, ACC will remain running until the system shuts down or reboots.
Those who don't mind using terminal, can run `acc --uninstall` or `acc -U` to stop and remove ACC.



---
## TROUBLESHOOTING


### Battery Capacity (% Level) Doesn't Seem Right

The "smart" battery may require calibration.
Refer to the `FAQ` section below for details.


### Battery Idle Mode On OnePlus 7/Pro

Recent/custom kernels (e.g., Kirisakura) support battery idle mode.
However, at the time of this writing, the feature is not production quality.
ACC has custom code to cover its pitfalls, though.
`battery/op_disable_charge 0 1` must be enforced manually (`acc -s s` or `acc -s s="battery/op_disable_charge 0 1"`) and accd, restarted afterwards.


### Bootloop, ACC Not Found

ACC disables itself after a bootloop event.
Refer to `Diagnostics/Logs` below for details.


### Charging Switch

By default, ACC uses whatever [charging switch](https://github.com/VR-25/acc/blob/dev/acc/charging-switches.txt) works.
However, things don't always go well.

- Some switches are unreliable under certain conditions (e.g., screen off).

- Others hold a [wakelock](https://duckduckgo.com/?q=wakelock) - causing faster battery drain.
Refer back to `DEFAULT CONFIGURATION (wake_unlock)`.

- High CPU load and inability to re-enable charging we're also be reported.

- In the worst case scenario, the battery status is reported as `discharging`, while it's actually `charging`.

In such situations, you have to find a switch that works as expected.
Here's how to do it:

1. Run `acc --test` (or `acc -t`) to see which switches work.
2. Run `acc --set charging_switch` (or `acc -s s`) to enforce a working switch.
3. Test the reliability of the set switch. If it doesn't work properly, try another.

ACC daemon applies dedicated settings for specific devices (e.g., MTK, Asus, 1+7pro) to prevent charging switch issues.
These are are in `acc/oem-custom.sh`.


### Custom Max Charging Voltage And Current Limits

Unfortunately, not all kernels support these features.
While custom current limits are supported by most (at least to some degree), voltage tweaking support is _exceptionally_ rare.

That said, the existence of potential voltage/current control file doesn't necessarily mean these are writable* or the features are supported.

\* Root is not enough.
Kernel level permissions forbid write access to certain interfaces.


### Diagnostics/Logs

Volatile logs are in `/sbin/.acc/`.
Persistent logs are found at `/data/adb/acc-data/logs/`.

`/data/adb/acc-data/logs/bootlooped` is created automatically after a bootloop event.
It prevents acc initialization.

`acc -le` exports all acc logs, plus Magisk's and extras to `/data/media/0/acc-$device_codename.tar.gz`.
The logs do not contain any personal information and are never automatically sent to the developer.


### Restore Default Config

This can save you a lot of time and grief.
It can be done from the app or by running one of the commands below, on terminal.

`acc --set --reset`, `acc -s r` or `rm /data/adb/acc-data/config.txt` (failsafe)


### Slow Charging

At least one of the following may be the cause:

- Charging current and/or voltage limits
- Cooldown cycle (non optimal charge/pause ratio, try 50/10 or 50/5)
- Troublesome charging switch (refer back to `TROUBLESHOOTING > Charging Switch`)
- Weak adapter and/or power cord



---
## POWER SUPPLY LOG (HELP NEEDED)

Please upload `/sbin/.acc/acc-power_supply-*.log` to [my dropbox](https://www.dropbox.com/request/WYVDyCc0GkKQ8U5mLNlH/).
This file contains invaluable power supply information, such as battery details and available charging control files.
A public database is being built for mutual benefit.
Your cooperation is greatly appreciated.

Privacy Notes

- Name: phone brand and/or model (e.g., 1+7pro, Moto Z Play)
- Email: random/fake

See current submissions [here](https://www.dropbox.com/sh/rolzxvqxtdkfvfa/AABceZM3BBUHUykBqOW-0DYIa?dl=0).



---
## LOCALIZATION

Help us with translations at [CrowdIn](https://crowdin.com/project/advanced-charging-controller/)!



---
## TIPS


### Generic

Achieve _battery idle mode_ with a voltage limit: `acc 101 -1; acc -s v 3920`
The first command disables the regular - charging switch driven - pause/resume functionality.
The second sets a voltage limit that will dictate how much the battery should charge.
The battery enters the so called _idle mode_ when its voltage peaks.

Limiting the charging current to zero mA (`acc -s c 0`) may enable idle mode as well.
`acc -s c -` restores the default limit.

Force fast charge: `appy_on_boot="/sys/kernel/fast_charge/force_fast_charge::1::0 usb/boost_current::1::0 charger/boost_current::1::0"`


### Google Pixel Devices

Force fast wireless charging with third party wireless chargers that are supposed to charge the battery faster: `apply_on_plug=wireless/voltage_max:9000000`.



---
## FREQUENTLY ASKED QUESTIONS (FAQ)


> How do I report issues?

Open issues on GitHub or contact the developer on Facebook, Telegram (preferred) or XDA (links below).
Always provide as much information as possible.
Attach `/sdcard/acc-logs-*tar.gz` - generated by `acc -le` _right after_ the problem occurs.
Refer back to `TROUBLESHOOTING > Diagnostics/Logs` for additional details.


> Why won't you support my device? I've been waiting for ages!

Firstly, have some extra patience!
Secondly, several systems don't have intuitive charging control files; I have to dig deeper - and oftentimes, improvise; this takes extra time and effort.
Lastly, some systems don't support custom charging control at all;  in such cases, you have to keep trying different kernels and uploading the respective power supply logs.
Refer back to `POWER SUPPLY LOGS (HELP NEEDED)`.


> Why, when and how should I calibrate the battery?

With modern battery management systems, that's generally unnecessary.

However, if your battery is underperforming, you may want to try the following procedure:

1. Let the battery charge until VOLTAGE_NOW >= VOLTAGE_MAX* and CURRENT_NOW drops to 3% of the rated mAh capacity, or less.
The command `acc --watch` or `acc -w`. lets you monitor that.

2. Let it discharge until the phone shuts off.

3. Turn the phone back on to consume any "residual" charge left.
Repeat this until the device refuses to stay/turn on.
Next, try booting straight into download, fastboot or recovery mode.
Repeat until the phone totally refuses to stay/turn on.

4. Charge to 100% without turning it on.
Leave the device plugged in for another hour or so.
This emulates step 1.
Done.

For additional information, refer to https://batteryuniversity.com/index.php/learn/article/battery_calibration .


> What if even after calibrating the battery, ACC and Android battery level reports still differ?

It's most likely an Android OS issue. Refer back to `DEFAULT CONFIGURATION` (capacity_offset and capacity_sync).


> I set voltage to 4080 mV and that corresponds to just about 75% charge.
But is it typically safer to let charging keep running, or to have the circuits turn on and shut off between defined percentage levels repeatedly?

It's not much about which method is safer.
It's specifically about electron stability: optimizing the pressure (voltage) and current flow.

As long as you don't set a voltage limit higher than 4200 mV and don't leave the phone plugged in for extended periods of time, you're good with that limitation alone.
Otherwise, the other option is actually more beneficial - since it mitigates high pressure (voltage) exposure/time to a greater extent.
If you use both, simultaneously - you get the best of both worlds.
On top of that, if you enable the cooldown cycle, it'll give you even more benefits.

Anyway, while the battery is happy in the 3700-4100 mV range, the optimal voltage for [the greatest] longevity is said\* to be ~3920 mV.

If you're leaving your phone plugged in for extended periods of time, that's the voltage limit you should aim for.

Ever wondered why lithium ion batteries aren't sold fully charged? They're usually ~40-60% charged. Why is that?
If you ever purchase a battery that is fully drained, almost fully drained or 70%+ charged, you know it's probably f.*d up already!

Summing up my thoughts...

Night/heavy-duty profile: keep capacity within 40-60% and/or voltage around ~3920 mV

Day/regular profile: max capacity: 75-80% and/or voltage no higher than 4100 mV

Travel profile: capacity up to 95% and/or voltage no higher than 4200 mV

\* https://batteryuniversity.com/index.php/learn/article/how_to_prolong_lithium_based_batteries/


> I don't really understand what "charge to a given capacity once, uninterrupted and without other restrictions" is meant for.

Consider the following situation:

You're almost late for an important event.
You recall that I stole your power bank and sold it on Ebay.
You need your phone and a good battery backup.
The event will take the whole day and you won't have access to an external power supply in the middle of nowhere.
You need your battery charged fast and as much as possible.
However, you don't want to modify ACC config nor manually stop/restart the daemon.


> What's DJS?

It's a standalone program: Daily Job Scheduler.
As the name suggests, it's meant for scheduling "jobs" - in this context, acc profiles/settings.
Underneath, it runs commands/scripts at specified times - either once, daily and/or on boot.


> Do I have to install/upgrade both ACC and AccA?

To really get out of this dilemma, you have to understand what ACC and AccA essentially are.

ACC is a Android program that controls charging.
It can be installed as an app (e.g., AccA) module, Magisk module or standalone software. Its installer determines the installation path/variant. The user is given the power to override that.
A plain text file holds the program's configuration. It can be edited with any root text editor.
ACC has a command line interface (CLI) - which in essence is a set of Application Programing Interfaces (APIs). The main purpose of a CLI/API is making difficult tasks ordinary.

AccA is a graphical user interface (GUI) for the ACC command line. The main purpose of a GUI is making ordinary tasks simpler.
AccA ships with a version of ACC that is automatically installed when the app is first launched.

That said, it should be pretty obvious that ACC is like a fully autonomous car that also happens to have a steering wheel and other controls for a regular driver to hit a tree.
Think of AccA as a robotic driver that often prefers hitting people over trees.
Due to extenuating circumstances, that robot is not upgraded as frequently as the car.
Upgrading the car regularly makes the driver happier - even though I doubt it has any emotion to speak of.
The back-end can be upgraded from within the app (online), or by flashing the latest ACC zip.
However, unless you have a good reason to do so, don't fix what's not broken.


> Does acc work also when Android is off?

No.


> I have this wakelock as soon as charging is disabled. How do I deal with it?

The best solution is enforcing a charging switch that doesn't trigger a wakelock. Refer back to `TROUBLESHOOTING > Charging Switch`.
A common workaround is having `resume_capacity = pause_capacity - 1`.



---
## LINKS

- [ACC repository](https://github.com/VR-25/acc/)
- [AccA repository](https://github.com/MatteCarra/AccA/)
- [Daily Job Scheduler](https://github.com/VR-25/djs/)
- [Must read - how to prolong lithium ion batteries lifespan](http://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries/)
- [Telegram group](https://t.me/acc_group/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)



---
## LATEST CHANGES

**v1.0.23 (27)**
- ACC v2020.3.1-beta.3 (202003013)
- ...

**v1.0.18 (22)**
- ACC v2019.9.1 (201909010)
- Delete ACCA orphan schedules (for example when reinstalling the app after the database has been reset)
- DJS v2019.9.1 (201909010)
