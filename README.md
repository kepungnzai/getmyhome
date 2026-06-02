
1. Install APK app to emulator
To install an APK, use the adb install command.

# Basic installation
adb install path/to/app.apk

# Reinstall an existing app (keeps data)
adb install -r path/to/app.apk

# Allow version downgrade
adb install -d path/to/app.apk

# Grant all runtime permissions automatically upon install
adb install -g path/to/app.apk

# Combine flags (Reinstall, downgrade, grant permissions)
adb install -r -d -g path/to/app.apk

-----

2. List processes currently running
You can use standard Linux commands via the ADB shell, or Android-specific package manager commands.
bash

# List all running processes (similar to Linux)
adb shell ps -A

# Filter for a specific app by package name
adb shell ps -A | grep com.example.app

# Get the exact Process ID (PID) of a running app
adb shell pidof com.example.app

# View real-time resource usage of running processes
adb shell top

# To send notification

adb shell am start -n "com.appcoreopc.getmyhome/.MainActivity" --ez extra_open_notification true --es extra_notification_title 'getmyhome' --es extra_notification_message 'hi there good job '

# To uninstall 

adb uninstall com.appcoreopc.getmyhome    


3. Extract installed app into an APK
To extract an APK, you first need to find where it is stored on the device, and then pull it to your host machine.
Step 1: Find the APK path

adb pull /data/app/~~random_string==/com.example.app-random_string==/base.apk ./extracted_app.apk

Output will look like: package:/data/app/~~random_string==/com.example.app-random_string==/base.apk

Step 2: Pull the APK to your computer

adb pull /data/app/~~random_string==/com.example.app-random_string==/base.apk ./extracted_app.apk

4. Decompile APK & Best Formats for Debugging
ADB cannot decompile APKs; you need external reverse engineering tools.

The Tools:
JADX (Recommended for reading): jadx -d output_folder app.apk. Converts DEX to highly readable Java source code.
Apktool (Recommended for modifying): apktool d app.apk. Decompiles DEX to Smali and decodes resources (XML, images).
What format is suitable for debugging purposes?

It depends on what you mean by "debugging":
For Static Analysis (Reading/Understanding): Java/Kotlin Source Code.
Why: It is human-readable. You can load JADX output into IntelliJ or VS Code to trace logic, find hardcoded secrets, and understand the control flow.

For Dynamic Debugging & Modifying (Patching): Smali.

Why: Smali is an assembly-like language that maps 1:1 with DEX bytecode. Java decompilation is often imperfect and cannot be recompiled back into a working APK. Smali can be modified and recompiled perfectly using apktool b. If you want to insert logging, bypass root detection, or step through code in a debugger, Smali is the absolute best format.


5. Other Good "Hacking" (Reverse Engineering) Approaches for DEX Binaries
Analyzing DEX files goes far beyond just decompiling. Here are the industry-standard approaches for deep analysis:
A. Dynamic Instrumentation (Frida)

Frida is the most powerful tool for Android reverse engineering. It allows you to inject JavaScript into running processes to hook methods, change variables, and bypass security controls on the fly without modifying the APK.

Use cases: Bypassing SSL pinning, bypassing root/jailbreak detection, dumping decryption keys from memory, tracing function calls.
Tooling: Use frida-tools CLI, or GUI wrappers like Objection (which automates common Frida tasks).

B. Native Code Analysis (JNI / .so files)

Many apps hide their core logic, cryptographic keys, or anti-tampering mechanisms in native C/C++ libraries (.so files) rather than DEX files.

Approach: Extract the .so files from the APK's lib/ folder.
Tools: Use Ghidra (free, by NSA) or IDA Pro (industry standard) to disassemble and decompile the ARM/x86 machine code back into readable C pseudocode.

C. Smali Debugging (Smalidea)
If you need to set breakpoints and step through the code line-by-line:
Decompile the APK to Smali using apktool.
Install the Smalidea plugin in Android Studio or IntelliJ.

Import the Smali project.

Attach the IDE debugger to the running Android process via ADB (adb forward tcp:8700 jdwp:<pid>).
You can now step through Smali code exactly as it executes on the VM.

D. Automated Vulnerability Scanning
Before doing manual analysis, run the APK through an automated scanner to find low-hanging fruit (hardcoded API keys, insecure permissions, exported activities, weak cryptography).
Tool: MobSF (Mobile Security Framework). You just drag and drop the APK into its web interface, and it generates a massive, detailed security report.

E. Network Traffic Interception

Analyzing the API calls the DEX binary makes is often easier than reading the code.
Approach: Route emulator traffic through Burp Suite or MITMproxy.
Challenge: Most modern apps use SSL Pinning.
Solution: Use Frida (via Objection: android sslpinning disable) or patch the APK's network security configuration (res/xml/network_security_config.xml) using apktool to allow user-installed CA certificates.


F. Memory Dumping and Analysis
Sometimes data is only available in RAM (e.g., decrypted DEX files in packed/obfuscated apps).

Tools: Use Fridump or GameGuardian (on rooted devices) to dump the memory of the running process. You can then run strings on the dump or use tools like dexdump to recover dynamically loaded DEX files that aren't present in the original APK.







