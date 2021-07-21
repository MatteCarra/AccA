# Advanced Charging Controller App (AccA)



---
## LICENSE

Copyright 2019-2020, [MatteCarra](https://github.com/MatteCarra/), [Squabbi](https://github.com/Squabbi/), [VR25](https://github.com/VR-25/)

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

To prevent fraud, do NOT mirror any link associated with this project; do NOT share APKs!
Share the official [releases link](https://github.com/MatteCarra/AccA/releases/) instead.



---
## WARNING

ACC (the back-end) manipulates Android low level ([kernel](https://duckduckgo.com/?q=kernel+android)) parameters which control the charging circuitry.
The author assumes no responsibility under anything that might break due to the use/misuse of this software.
By choosing to use/misuse ACC, you agree to do so at your own risk!



---
## DESCRIPTION

ACC is an Android software mainly intended for [extending battery service life](https://batteryuniversity.com/learn/article/how_to_prolong_lithium_based_batteries).
In a nutshell, this is achieved through limiting charging current, temperature and voltage.
Any root solution is supported.

AccA is an official ACC front-end app.
It targets mainly people (and aliens alike) who feel uncomfortable with terminal.
The command line functionality is also available.



---
## DOWNLOAD

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/mattecarra.accapp/)



---
## PREREQUISITES

- [Must read - how to prolong lithium ion batteries lifespan](https://batteryuniversity.com/article/bu-808-how-to-prolong-lithium-based-batteries/)
- Android or Android based OS
- Any root solution (e.g., [Magisk](https://github.com/topjohnwu/Magisk/))
- Busybox\* (only if not rooted with Magisk)
- curl\*\* (for acc --upgrade, optional)
- Terminal emulator (optional, for debugging and advanced/extra functionality)
- Text editor (optional)

\* Instead of a regular install, the [busybox binary](https://github.com/search?o=desc&q=busybox+android&s=updated&type=Repositories/) can simply be placed in /data/adb/bin/.
ACC sets 0700 permissions as needed.
Precedence: /data/adb/bin/busybox > Magisk's busybox > system's busybox

\*\* A [static curl binary](https://github.com/search?o=desc&q=curl+android&s=updated&type=Repositories/) (optional) can also be placed in `/data/adb/bin/` (with execute permission).
Alternatively, one may install the Magisk module [Cross Compiled Binaries (ccbins)](https://github.com/Magisk-Modules-Repo/ccbins/).

Note: ACC comes bundled into AccA.
Any existing version/variant is replaced.



---
## SETUP/USAGE

Neither ACC nor AccA require any setup if the default settings fit your bill.

AccA's user interface is intuitive and has configuration details/hints, so that users don't have to read extensive documentation to find their way.
However, it's still highly recommended to read [ACC's documentation](https://github.com/VR-25/acc/blob/master/README.md) in order to get a broader idea of how ACC and AccA work together.

The back-end can be upgraded from within the app (online), or by flashing the latest ACC zip.
However, unless you have a good reason to do so, don't fix what's not broken.
Anyway, the zip can be flashed from Magisk Manager or similar app.
Alternatively, one can run `acc --flash` (or `acc -F`) on terminal and follow the instructions.

Uninstalling AccA or clearing its data signals ACC removal (next boot).
Removing AccA or wiping its data does NOT immediately stop ACC daemon.
Those who don't mind using terminal can uninstall AccA, then run `acc --uninstall` or `acc -U` to stop accd and completely remove ACC right away.



---
## ACC TROUBLESHOOTING


### `acc -t` Reports Total Failure

Refer back to `DEFAULT CONFIGURATION (switch_delay)`.


### Battery Capacity (% Level) Doesn't Seem Right

When Android's battery level differs from that of the kernel, ACC daemon automatically syncs it by stopping the battery service and feeding it the real value every few seconds.

Pixel devices are known for having battery level discrepancies for the longest time.

If your device shuts down before the battery is actually empty, capacity_freeze2 may help.
Refer to the `default configuration` section above for details.


### Battery Idle Mode On OnePlus 7/8 Variants (Possibly 5 and 6 Too)

Recent/custom kernels (e.g., Kirisakura) support battery idle mode.
However, at the time of this writing, the feature is not production quality.
ACC has custom code to cover the pitfalls, though.
`battery/op_disable_charge 0 1` must be enforced manually (`acc -ss` or `acc -s s="battery/op_disable_charge 0 1"`).


### Bootloop

While uncommon, it may happen.

It's assumed that you already know at least one of the following: temporary disable root (e.g., Magisk), disable Magisk modules or enable Magisk core-only mode.

Most of the time, though, it's just a matter of plugging the phone before turning it on.
Battery level must be below pause_capacity.
Once booted, one can run `acc --uninstall` (or `acc -U`) to remove ACC.

From recovery, you can mount system and flash `/sdcard/acc-uninstaller.zip` or run `mount /system; /data/adb/acc/uninstall.sh`.


### Charging Switch

By default, ACC uses whichever [charging switch](https://github.com/VR-25/acc/blob/dev/acc/charging-switches.txt) works.
However, things don't always go well.

- Some switches are unreliable under certain conditions (e.g., screen off).

- Others hold a [wakelock](https://duckduckgo.com/lite/?q=wakelock).
This causes fast battery drain when charging is paused and the device remains plugged.
Refer back to `DEFAULT CONFIGURATION (wake_unlock)`.

- High CPU load and inability to re-enable charging were also reported.

- In the worst case scenario, the battery status is reported as `discharging`, while it's actually `charging`.

In such situations, one has to enforce a switch that works as expected.
Here's how to do it:

1. Run `acc --test` (or `acc -t`) to see which switches work.
2. Run `acc --set charging_switch` (or `acc -ss`) to enforce a working switch.
3. Test the reliability of the set switch. If it doesn't work properly, try another.

Since not everyone is tech savvy, ACC daemon applies dedicated settings for specific devices (e.g., MTK, Asus, 1+7pro) to prevent charging switch issues.
These are are in `acc/oem-custom.sh`.


### Custom Max Charging Voltage And Current Limits

Unfortunately, not all kernels support these features.
While custom current limits are supported by most (at least to some degree), voltage tweaking support is _exceptionally_ rare.

That said, the existence of potential voltage/current control file doesn't necessarily mean these are writable* or the features, supported.

\* Root is not enough.
Kernel level permissions forbid write access to certain interfaces.


### Diagnostics/Logs

Volatile logs (gone on reboot) are stored in `/dev/.acc/`.
Persistent logs: `/data/adb/acc-data/logs/`.

`/dev/.acc-removed` is created by the uninstaller.
The storage location is volatile.

`acc -le` exports all acc logs, plus Magisk's and extras to `/sdcard/acc-$device_codename.tar.bz2`.
The logs do not contain any personal information and are never automatically sent to the developer.
Automatic exporting (local) happens under specific conditions (refer back to `SETUP/USAGE > Terminal Commands > Exit Codes`).


### Restore Default Config

This can save you a lot of time and grief.

`acc --set --reset`, `acc -sr` or `rm /data/adb/acc-data/config.txt` (failsafe)


### Slow Charging

At least one of the following may be the cause:

- Charging current and/or voltage limits
- Cooldown cycle (non optimal charge/pause ratio, try 50/10 or 50/5)
- Troublesome charging switch (refer back to `TROUBLESHOOTING > Charging Switch`)
- Weak adapter and/or power cord



---
## ACC POWER SUPPLY LOG (HELP NEEDED)

Please run `acc -le` and upload `/data/adb/acc-data/logs/power_supply-*.log` to [my dropbox](https://www.dropbox.com/request/WYVDyCc0GkKQ8U5mLNlH/) (no account/sign-up required).
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
## ACC TIPS


### Generic

Emulate _battery idle mode_ with a voltage limit: `acc 101 -1; acc -s v 3920`.
The first command disables the regular - charging switch driven - pause/resume functionality.
The second sets a voltage limit that will dictate how much the battery should charge.
The battery enters a _pseudo idle mode_ when its voltage peaks.

Limiting the charging current to zero mA (`acc -sc 0`) may emulate idle mode as well.
`acc -sc -` restores the default limit.

Force fast charge: `appy_on_boot="/sys/kernel/fast_charge/force_fast_charge::1::0 usb/boost_current::1::0 charger/boost_current::1::0"`


### Google Pixel Devices

Force fast wireless charging with third party wireless chargers that are supposed to charge the battery faster: `apply_on_plug=wireless/voltage_max::9000000`.


### Using [Termux:API](https://wiki.termux.com/wiki/Termux:API) for Text-to-Speech


1) Install Termux, Termux:Boot and Termux:API APKs.
If you're not willing to pay for Termux add-ons, go for the F-Droid versions of these AND Termux itself.
Since package signatures mismatch, you can't install the add-ons from F-Droid if Termux was obtained from Play Store and vice versa.


2) Exclude Termux:Boot from battery optimization, then launch (to enable auto-start) and close it.


3) Paste and run the following on Termux, as a regular (non-root) user:
```
mkfifo ~/acc-fifo; mkdir -p ~/.termux/boot; pkg install termux-api; echo -e '#!/data/data/com.termux/files/usr/bin/sh\nwhile :; do\n  cat ~/acc-fifo\ndone | termux-tts-speak' > ~/.termux/boot/acc-tts.sh; chmod 0755 ~/.termux/boot/acc-tts.sh; sh ~/.termux/boot/acc-tts.sh &
```
Let that session run in the background.


4) ACC has the following:

auto_shutdown_alert_cmd (asac)
charg_disabled_notif_cmd (cdnc)
charg_enabled_notif_cmd (cenc)
error_alert_cmd (eac)

As the names suggest, these properties dictate commands acc/d should run on each event.
The default command is "vibrate <number of vibrations> <interval (seconds)>"

Let's assume you want the phone to say _Warning! Battery is low. System will shutdown soon._
To set that up, paste and run the following on a terminal, as root:

`echo -e "\nautoShutdownAlertCmd=('! pgrep -f acc-tts.sh || echo \"Warning! Battery is low. System will shutdown soon.\" > /data/data/com.termux/files/home/acc-fifo')" >> /data/adb/acc-data/config.txt`


That's it.
You only have to go through these steps once.



---
## ACC FREQUENTLY ASKED QUESTIONS (FAQ)


> How do I report issues?

Open issues on GitHub or contact the developer on Facebook, Telegram (preferred) or XDA (links below).
Always provide as much information as possible.
Attach `/sdcard/acc-logs-*tar.gz` - generated by `acc -le` _right after_ the problem occurs.
Refer back to `TROUBLESHOOTING > Diagnostics/Logs` for additional details.


> Why won't you support my device? I've been waiting for ages!

Firstly, have some extra patience!
Secondly, several systems don't have intuitive charging control files; I have to dig deeper - and oftentimes, improvise; this takes time and effort.
Lastly, some systems don't support custom charging control at all;  in such cases, you have to keep trying different kernels and uploading the respective power supply logs.
Refer back to `POWER SUPPLY LOGS (HELP NEEDED)`.


> Why, when and how should I calibrate the battery?

With modern battery management systems, that's generally unnecessary.

However, if your battery is underperforming, you may want to try the procedure described at https://batteryuniversity.com/article/bu-603-how-to-calibrate-a-smart-battery .


> I set voltage to 4080 mV and that corresponds to just about 75% charge.
But is it typically safer to let charging keep running, or to have the circuits turn on and shut off between defined percentage levels repeatedly?

It's not much about which method is safer.
It's specifically about electron stability: optimizing the pressure (voltage) and current flow.

As long as you don't set a voltage limit higher than 4200 mV and don't leave the phone plugged in for extended periods of time, you're good with that limitation alone.
Otherwise, the other option is actually more beneficial - since it mitigates high pressure (voltage) exposure/time to a greater extent.
If you use both, simultaneously - you get the best of both worlds.
On top of that, if you enable the cooldown cycle, it'll give you even more benefits.

Anyway, while the battery is happy in the 3700-4100 mV range, the optimal voltage for [the greatest] longevity is said\* to be ~3920 mV.

If you're leaving your phone plugged in for extended periods of time, that's the voltage limit to aim for.

Ever wondered why lithium ion batteries aren't sold fully charged? They're usually ~40-60% charged. Why is that?
Keeping a battery fully drained, almost fully drained or 70%+ charged for a long times, leads to significant (permanent) capacity loss

Putting it all together in practice...

Night/heavy-duty profile: keep capacity within 40-60% and/or voltage around ~3920 mV

Day/regular profile: max capacity: 75-80% and/or voltage no higher than 4100 mV

Travel profile: capacity up to 95% and/or voltage no higher than 4200 mV

\* https://batteryuniversity.com/article/bu-808-how-to-prolong-lithium-based-batteries/


> I don't really understand what the "-f|--force|--full [capacity]" is meant for.

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
Due to extenuating circumstances, that robot may not be upgraded as frequently as the car.
Upgrading the car regularly makes the driver happier - even though I doubt it has any emotion to speak of.
The back-end can be upgraded by flashing the latest ACC zip.
However, unless you have a good reason to do so, don't fix what's not broken.


> Does acc work also when Android is off?

No, but this possibility is being explored.
Currently, it does work in recovery mode, though.


> I have this wakelock as soon as charging is disabled. How do I deal with it?

The best solution is enforcing a charging switch that doesn't trigger a wakelock.
Refer back to `TROUBLESHOOTING > Charging Switch`.
A common workaround is having `resume_capacity = pause_capacity - 1`. e.g., resume_capacity=74, pause_capacity=75.



---
## LINKS

- [ACC repository](https://github.com/VR-25/acc/)
- [AccA repository](https://github.com/MatteCarra/AccA/)
- [Daily Job Scheduler](https://github.com/VR-25/djs/)
- [Must read - how to prolong lithium ion batteries lifespan](https://batteryuniversity.com/article/bu-808-how-to-prolong-lithium-based-batteries/)
- [Telegram group](https://t.me/acc_group/)
- [XDA thread](https://forum.xda-developers.com/apps/magisk/module-magic-charging-switch-cs-v2017-9-t3668427/)



---
## LATEST CHANGES

**v$ver_string ($ver_code)**
- ...
- Updated readme
- ...

**v1.0.23 (27)**
- ACC v2020.3.1-beta.3 (202003013)
- ...

**v1.0.18 (22)**
- ACC v2019.9.1 (201909010)
- Delete ACCA orphan schedules (for example when reinstalling the app after the database has been reset)
- DJS v2019.9.1 (201909010)
