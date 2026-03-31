# Installing NovaLang

This guide covers every way to get NovaLang running on your machine — from the
zero-setup BlueJ option to a fully configured command-line tool you can call
from anywhere.

---

## Contents

- [Requirements](#requirements)
- [Option A — BlueJ (Quickest)](#option-a--bluej-quickest)
- [Option B — Command Line](#option-b--command-line)
  - [Step 1 — Build the JAR](#step-1--build-the-jar)
  - [Step 2 — Place the JAR](#step-2--place-the-jar)
  - [Step 3 — Install the launcher](#step-3--install-the-launcher)
    - [Windows](#windows)
    - [Mac](#mac)
    - [Linux](#linux)
  - [Step 4 — Verify](#step-4--verify)
- [Updating NovaLang](#updating-novalang)
- [Uninstalling](#uninstalling)
- [Troubleshooting](#troubleshooting)
- [All Commands](#all-commands)

---

## Requirements

| Requirement | Version | Check |
|-------------|---------|-------|
| Java (JDK or JRE) | 11 or higher | `java -version` |
| BlueJ *(Option A only)* | Any recent version | [bluej.org](https://bluej.org) |

Java 11 is the minimum because NovaLang uses `Files.readString`, `HttpClient`,
and switch expressions — all introduced in Java 11. Java 17 or 21 is recommended.

If `java -version` gives you an error, download and install the JDK from
[Adoptium](https://adoptium.net) (free, open source, works on Windows, Mac, and Linux).

---

## Option A — BlueJ (Quickest)

This option requires no command-line setup at all. You write code in a text
file and click Run.

1. Download and install [BlueJ](https://bluej.org) if you have not already.

2. Clone or download this repository and unzip it somewhere convenient.

3. Open BlueJ, go to **Project → Open Project**, and open the NovaLang folder.
   You should see all the classes appear in the BlueJ canvas.

4. Create a file called `code.txt` in the same folder as `Main.java`. This is
   where you write your Nova programs.

5. In BlueJ, right-click the `Main` class → **void main(String[] args)** →
   leave args blank → click **OK**.

6. Your output appears in the BlueJ terminal window.

**Example `code.txt`:**

```nova
let name = "Nova"
print $"Hello from {name}!", true
```

**Output:**

```
Hello from Nova!
```

That is all there is to Option A. Every time you want to run a program, edit
`code.txt` and click Run on `Main`.

---

## Option B — Command Line

This option lets you call `nova` from any terminal window, in any directory,
without BlueJ. Programs can be named whatever you like and placed anywhere.

```bash
nova run myprogram.nova
nova compile myprogram.nova
nova myprogram.nova
```

There are four steps: build the JAR, place it, install the launcher, and verify.

---

### Step 1 — Build the JAR

A JAR file bundles all 85 Java source files into a single executable archive.
You only need to do this once (and again whenever you update NovaLang).

#### Using BlueJ (recommended)

1. Open the NovaLang project in BlueJ.
2. Go to **Project → Create Jar File**.
3. In the dialog:
   - Set **Main class** to `Main`
   - Untick *Include source* unless you want it
4. Name the file `nova.jar` and save it anywhere for now.
5. Click **OK**.

#### Using the command line

If you have `javac` and `jar` on your PATH:

```bash
cd /path/to/NovaLang

# Compile all 85 source files
javac *.java

# Package into nova.jar using the provided manifest
jar cfm nova.jar MANIFEST.MF *.class

# Clean up class files (optional)
rm *.class
```

The `MANIFEST.MF` file in the repository tells the JAR that `Main` is the
entry point. Do not delete or rename it.

---

### Step 2 — Place the JAR

Pick a permanent home for `nova.jar`. The launcher script needs to know this
path, so choose somewhere you will not move it.

Recommended locations:

| OS | Path |
|----|------|
| Windows | `C:\nova\nova.jar` |
| Mac | `~/nova/nova.jar` |
| Linux | `~/nova/nova.jar` or `/usr/local/lib/nova/nova.jar` |

Create the folder and move `nova.jar` into it:

```bash
# Mac / Linux
mkdir -p ~/nova
mv nova.jar ~/nova/nova.jar
```

```powershell
# Windows (PowerShell)
New-Item -ItemType Directory -Path C:\nova
Move-Item nova.jar C:\nova\nova.jar
```

---

### Step 3 — Install the Launcher

The launcher is a tiny script that calls `java -jar nova.jar` for you. Once
it is on your PATH, typing `nova` anywhere will work.

---

#### Windows

1. Open `nova.bat` from the repository in Notepad or any text editor.

2. Find this line and update the path to match where you put `nova.jar`:

   ```batch
   set NOVA_JAR=%USERPROFILE%\nova\nova.jar
   ```

   If you placed the JAR at `C:\nova\nova.jar`, change it to:

   ```batch
   set NOVA_JAR=C:\nova\nova.jar
   ```

3. Save the file and copy it to the same folder as `nova.jar`:

   ```
   copy nova.bat C:\nova\nova.bat
   ```

4. Add `C:\nova` to your system PATH so Windows can find `nova.bat`:

   - Press **Win + S** and search for **Environment Variables**
   - Click **Edit the system environment variables**
   - Click **Environment Variables...**
   - Under **System variables**, find **Path** and click **Edit**
   - Click **New** and type `C:\nova`
   - Click **OK** on all open dialogs

5. Open a **new** Command Prompt (the current one will not see the updated PATH).

6. Continue to [Step 4 — Verify](#step-4--verify).

---

#### Mac

1. Open `nova` (the shell script, no extension) from the repository in a text
   editor.

2. Find this line and update the path to match where you put `nova.jar`:

   ```sh
   NOVA_JAR="$HOME/nova/nova.jar"
   ```

   If you placed it somewhere else, for example `/usr/local/lib/nova/nova.jar`,
   change it to:

   ```sh
   NOVA_JAR="/usr/local/lib/nova/nova.jar"
   ```

3. Copy the script to `/usr/local/bin` and make it executable:

   ```bash
   sudo cp nova /usr/local/bin/nova
   sudo chmod +x /usr/local/bin/nova
   ```

4. `/usr/local/bin` is already on the default PATH for most Mac users.
   Open a new terminal and continue to [Step 4 — Verify](#step-4--verify).

   > If you get *permission denied* when copying to `/usr/local/bin`, your
   > account does not have administrator rights. Use `~/bin/` instead — see
   > the Linux section below for how to set that up.

---

#### Linux

1. Open `nova` (the shell script, no extension) from the repository in a text
   editor.

2. Update the `NOVA_JAR` path at the top of the file:

   ```sh
   NOVA_JAR="$HOME/nova/nova.jar"
   ```

   Change this to wherever you placed `nova.jar`. The default `$HOME/nova/`
   is fine if you followed Step 2 exactly.

3. **System-wide install** (available to all users — requires sudo):

   ```bash
   sudo cp nova /usr/local/bin/nova
   sudo chmod +x /usr/local/bin/nova
   ```

4. **Per-user install** (no sudo required):

   ```bash
   mkdir -p ~/bin
   cp nova ~/bin/nova
   chmod +x ~/bin/nova
   ```

   Then add `~/bin` to your PATH if it is not there already. Open
   `~/.bashrc` (or `~/.zshrc` if you use zsh) and add this line at the
   bottom:

   ```bash
   export PATH="$HOME/bin:$PATH"
   ```

   Apply it immediately without opening a new terminal:

   ```bash
   source ~/.bashrc
   ```

5. Continue to [Step 4 — Verify](#step-4--verify).

---

### Step 4 — Verify

Open a **new** terminal and run:

```bash
nova version
```

You should see:

```
NovaLang 0.1 - Archimedes
```

Then create a test file and run it:

```bash
echo 'print "Installation successful!", true' > hello.nova
nova run hello.nova
```

Expected output:

```
Installation successful!
```

If both of those work, NovaLang is fully installed. You can delete `hello.nova`.

---

## Updating NovaLang

When a new version is released:

1. Download or pull the updated source files.
2. Rebuild `nova.jar` using the same steps as [Step 1](#step-1--build-the-jar).
3. Replace the old `nova.jar` in your installation folder with the new one.

The launcher scripts (`nova` and `nova.bat`) do not need to change unless the
JAR moves to a different location.

---

## Uninstalling

**Remove the JAR and its folder:**

```bash
# Mac / Linux
rm -rf ~/nova

# Windows (Command Prompt)
rd /s /q C:\nova
```

**Remove the launcher script:**

```bash
# Mac / Linux — system-wide install
sudo rm /usr/local/bin/nova

# Mac / Linux — per-user install
rm ~/bin/nova
```

**Windows:** Delete `nova.bat` from wherever you placed it, then remove the
folder from your PATH by reversing the steps in [Step 3 — Windows](#windows).

**Remove installed packages** (if you used the package manager):

```bash
# Mac / Linux
rm -rf ~/.nova

# Windows (Command Prompt)
rd /s /q %USERPROFILE%\.nova
```

---

## Troubleshooting

### `nova: command not found` or `'nova' is not recognised`

The launcher script is not on your PATH.

- **Windows:** Check that `C:\nova` (or wherever you put `nova.bat`) appears
  in your PATH environment variable. Make sure you opened a **new** Command
  Prompt after editing PATH — the old one does not see the change.
- **Mac / Linux:** Run `echo $PATH` and check that `/usr/local/bin` or
  `~/bin` appears. If it is missing, add it to `~/.bashrc` or `~/.zshrc`
  as described in [Step 3 — Linux](#linux), then run `source ~/.bashrc`.

---

### `Error: Unable to access jarfile nova.jar`

The `NOVA_JAR` path in your launcher script does not point to where `nova.jar`
actually lives.

Open `nova` or `nova.bat` in a text editor and verify the path. Then confirm
the file exists:

```bash
# Mac / Linux
ls -lh ~/nova/nova.jar

# Windows
dir C:\nova\nova.jar
```

If the file is in a different location, update the `NOVA_JAR` line in the
launcher to match.

---

### `java: command not found` or `'java' is not recognised`

Java is not installed or not on your PATH.

1. Download the JDK from [Adoptium](https://adoptium.net). Choose the
   **LTS** release (Java 21 is recommended).
2. Run the installer. On Windows it will update PATH automatically. On
   Mac and Linux, follow the post-install instructions printed by the installer.
3. Open a **new** terminal and run `java -version` to confirm.

---

### `UnsupportedClassVersionError`

Your installed Java version is too old. NovaLang requires Java 11 or higher.

Run `java -version` — if it shows Java 8 or Java 10, download and install a
newer JDK from [Adoptium](https://adoptium.net) and make sure it is the
version that `java -version` finds (you may have multiple JDKs installed).

---

### `Error: Main method not found` or `no main manifest attribute`

The JAR was built without the correct manifest. Rebuild it following
[Step 1](#step-1--build-the-jar) and make sure the `MANIFEST.MF` file from
the repository is used.

If building from the command line, the `m` flag in `jar cfm` is what reads
the manifest:

```bash
jar cfm nova.jar MANIFEST.MF *.class
#     ^
#     m = read manifest from MANIFEST.MF
```

Without `m`, the JAR has no entry point and cannot be run with `java -jar`.

---

### `nova compile` reports errors in a file that looks correct

Check for these common causes:

- **Curly quotes** — some word processors and editors replace straight quotes
  `"` with smart quotes `"` or `"`. Nova only accepts straight double quotes.
  Make sure you are editing `.nova` files in a plain text editor, not a word
  processor.
- **Wrong file encoding** — save your `.nova` file as **UTF-8**. Most code
  editors do this by default.
- **Missing `code.txt`** — if running via BlueJ and you see a file-not-found
  error, make sure `code.txt` exists in the same folder as `Main.java`.

---

### Output appears in the wrong order or nothing prints

If you are running via BlueJ and the terminal window is empty after a run,
check that your Nova code actually calls `print`. A program that only assigns
variables produces no output.

---

## All Commands

Once installed, the full `nova` command set is:

```
nova run <file>          Run a Nova program
nova compile <file>      Check syntax, print line and statement count
nova check <file>        Alias for compile
nova <file>              Shorthand — same as nova run <file>
nova version             Print version number
nova help                Show help
```

### Examples

```bash
nova run myprogram.nova
nova run code.txt
nova compile myprogram.nova
nova check myprogram.nova
nova myprogram.nova
nova version
nova help
```

---

> **File extensions:** Nova programs can use either `.nova` or `.txt`. Both
> work with all commands. The `.nova` extension is recommended because it
> makes the file type obvious and enables syntax highlighting in supported
> editors.
