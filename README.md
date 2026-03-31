# NovaLang

**A clean, expressive scripting language built on the JVM.**

NovaLang is a dynamically-typed interpreted language designed around one central idea: code should be easy to read, easy to write, and impossible to silently misuse. It draws inspiration from Python's readability and Java's numeric robustness — and adds its own personality on top.

```nova
use arrays as a
use string as str

fn greet(names) {
    for name in names {
        print $"Hello, {str.upper(name)}!", true
    }
}

let people = ["alice", "bob", "charlie"]
greet(people)
```

```
Hello, ALICE!
Hello, BOB!
Hello, CHARLIE!
```

---

## Contents

- [Why NovaLang](#why-novalang)
- [Getting Started](#getting-started)
- [Language Tour](#language-tour)
  - [Variables](#variables)
  - [Types](#types)
  - [Operators](#operators)
  - [Strings](#strings)
  - [Control Flow](#control-flow)
  - [Loops](#loops)
  - [Functions](#functions)
  - [Arrays](#arrays)
  - [Maps](#maps)
  - [First-Class Functions](#first-class-functions)
- [Modules](#modules)
- [Error Messages](#error-messages)
- [Running Nova Programs](#running-nova-programs)
- [Architecture](#architecture)
- [File Structure](#file-structure)

---

## Why NovaLang

Most scripting languages make trade-offs you have to live with. NovaLang makes different ones.

**Numbers never silently overflow.** When an `int` exceeds its range, Nova automatically promotes it to `long`, then to `bigint` — an arbitrary-precision integer that can hold numbers with thousands of digits. You never get a silent wrong answer from numeric overflow.

```nova
print 9223372036854775807 + 1, true
# 9223372036854775808  — correct, not -9223372036854775808
```

**Errors always tell you exactly what went wrong.** Every error message includes the line number and the offending source line. There are no cryptic stack traces for simple mistakes.

```
[line 3] TypeError: Operator '-' is not supported for types string and int
    --> print "hello" - 5, true
```

**The module system is opt-in and explicit.** Nothing is in scope unless you ask for it. There is no hidden global state, no implicit imports, no magic.

**Functions are isolated by design.** A function receives everything through parameters and returns everything through `send`. This makes functions predictable, testable, and reusable.

---

## Getting Started

NovaLang runs on the JVM (Java 11+). No installation of external tools is required beyond Java itself.

### Running from BlueJ

1. Clone or download this repository
2. Open the project in [BlueJ](https://bluej.org)
3. Write your Nova code in `code.txt` in the project folder
4. Right-click `Main` → `void main(String[] args)` → OK

### Running from the Command Line

Build the project into a JAR (via BlueJ's *Project → Create Jar File* or `javac`/`jar`), then use the `nova` launcher script:

```bash
nova run myprogram.nova       # run a program
nova compile myprogram.nova   # check syntax only
nova myprogram.nova           # shorthand for run
```

See [INSTALL.md](INSTALL.md) for full setup instructions for Windows, Mac, and Linux.

---

## Language Tour

### Variables

Declare a variable with `let`. Reassign it with `=` alone.

```nova
let name = "Nova"
let score = 100
let active = true

score = 150          # reassign
score += 25          # compound assignment: score is now 175
score++              # increment: score is now 176
```

Declare multiple variables at once, all set to the same value:

```nova
let i, j, k = 0
let x, y, done = false
```

Reassign multiple variables in one line:

```nova
i, j, k = 0          # reset all three at once
```

---

### Types

NovaLang has 11 types. All are inferred automatically — you never write type annotations.

| Type | Example | Notes |
|------|---------|-------|
| `int` | `42`, `-7` | 32-bit integer |
| `long` | auto-promoted | 64-bit integer |
| `bigint` | auto-promoted | Unlimited precision |
| `double` | `3.14`, `-0.5` | 64-bit float |
| `bigdecimal` | auto-promoted | High-precision decimal |
| `char` | `'A'`, `'7'` | Single character |
| `string` | `"hello"` | Text |
| `boolean` | `true`, `false` | |
| `null` | `null` | Absence of value |
| `array` | `[1, 2, 3]` | Ordered list |
| `map` | `{"key": value}` | Key-value pairs |

**Numeric promotion** happens automatically and silently:

```nova
let x = 2147483647     # int
print x + 1, true      # 2147483648  — auto-promoted to long

let big = 9223372036854775807   # long
print big + 1, true             # auto-promoted to bigint

print 999999999999 * 999999999999, true
# 999999999998000000000001  — bigint, exact
```

---

### Operators

**Arithmetic**

```nova
print 10 + 3, true    # 13
print 10 - 3, true    # 7
print 10 * 3, true    # 30
print 10 / 3, true    # 3    (integer division)
print 10.0 / 3, true  # 3.3333333333333335
print 10 % 3, true    # 1    (modulo — remainder)
```

**Comparison and logic**

```nova
print 5 == 5, true    # true
print 5 != 6, true    # true
print 5 > 3, true     # true
print 5 >= 5, true    # true

print true and false, true   # false
print true or false, true    # true
print not true, true         # false
```

**Assignment shortcuts**

```nova
let x = 10
x += 5     # 15
x -= 3     # 12
x *= 2     # 24
x /= 4     # 6
x %= 4     # 2
x++        # 3
x--        # 2
```

**String and array operators**

```nova
print "Hello" + " " + "Nova", true   # Hello Nova
print "ab" * 3, true                  # ababab
print [1, 2] + [3, 4], true           # [1, 2, 3, 4]
print [0] * 5, true                   # [0, 0, 0, 0, 0]
```

---

### Strings

**Plain strings**

```nova
let s = "Hello, Nova!"
```

**Escape sequences**

| Sequence | Character |
|----------|-----------|
| `\n` | Newline |
| `\t` | Tab |
| `\"` | Double quote |
| `\\` | Backslash |

**Triple-quoted strings** — for multi-line text

```nova
let message = """Dear user,

Welcome to NovaLang.
Enjoy your stay."""

print message, true
```

**String interpolation** — prefix with `$`, embed expressions in `{}`

```nova
let name = "Nova"
let version = 1

print $"Welcome to {name} v{version}!", true
# Welcome to Nova v1!

use math as m
let n = 25
print $"sqrt({n}) = {m.sqrt(n)}", true
# sqrt(25) = 5.0
```

Only strings that start with `$"` are interpolated. Regular `"..."` strings are never touched, which means JSON strings and other text containing `{}` are safe.

---

### Control Flow

**If / elif / else**

```nova
let score = 75

if score >= 90 {
    print "A", true
} elif score >= 80 {
    print "B", true
} elif score >= 70 {
    print "C", true
} else {
    print "F", true
}
```

Nova checks conditions top to bottom and runs the first matching branch.

---

### Loops

**While loop**

```nova
let i = 1
while i <= 5 {
    print i, true
    i++
}
```

**For loop** — counts from `start` to `end` (exclusive)

```nova
for i = 1 to 6 {
    print i, true     # prints 1 2 3 4 5
}
```

**For-in loop** — iterates over every element of an array

```nova
let fruits = ["apple", "banana", "cherry"]
for fruit in fruits {
    print fruit, true
}
```

**Break and continue**

```nova
for i = 1 to 100 {
    if i % 2 == 0 { continue }   # skip even numbers
    if i > 9      { break    }   # stop after 9
    print i, true
}
# 1 3 5 7 9
```

---

### Functions

Define with `fn`, return with `send`.

```nova
fn add(a, b) {
    send a + b
}

print add(3, 4), true     # 7
```

**Early return** — `send` exits the function immediately

```nova
fn classify(n) {
    if n < 0   { send "negative" }
    if n == 0  { send "zero"     }
    if n < 10  { send "small"    }
    send "large"
}

print classify(-5),  true    # negative
print classify(0),   true    # zero
print classify(7),   true    # small
print classify(100), true    # large
```

**Recursion**

```nova
fn factorial(n) {
    if n <= 1 { send 1 }
    send n * factorial(n - 1)
}

print factorial(10), true    # 3628800
print factorial(30), true    # 265252859812191058636308480000000
```

BigInt promotion means factorial never overflows, no matter how large `n` is.

**Function scope** — functions are fully isolated. They cannot read or write outer variables; everything they need must be passed as a parameter.

```nova
let x = 10

fn double(n) {
    send n * 2    # uses n, not x
}

print double(x), true    # 20
```

---

### Arrays

```nova
let nums = [1, 2, 3, 4, 5]
let mixed = ["hello", 42, true, null]
let empty = []

print nums[0], true     # 1
print nums[4], true     # 5

nums[2] = 99
print nums, true        # [1, 2, 99, 4, 5]
```

**Nested arrays**

```nova
let matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
print matrix[1][2], true    # 6
```

**Building arrays in loops**

```nova
use arrays as a

let squares = []
for i = 1 to 11 {
    a.push(squares, i * i)
}
print squares, true    # [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
```

---

### Maps

Maps store key-value pairs. Keys are always strings.

```nova
let person = {"name": "Nova", "age": 5, "active": true}

print person["name"], true    # Nova
print person["age"],  true    # 5

person["age"] = 6
person["email"] = "nova@lang.dev"
```

**Nested maps**

```nova
let config = {
    "server": {
        "host": "localhost",
        "port": 8080
    },
    "debug": true
}

print config["server"]["host"], true    # localhost
print config["server"]["port"], true    # 8080
```

---

### First-Class Functions

Functions are values. They can be stored in variables, passed as arguments, and returned from other functions.

```nova
fn square(n) { send n * n }
fn double(n) { send n * 2 }

let f = square
print f(5), true    # 25

let ops = [square, double]
print ops[0](4), true    # 16
print ops[1](4), true    # 8
```

**Passing functions to functions**

```nova
fn applyToFive(f) { send f(5) }

print applyToFive(square), true    # 25
print applyToFive(double), true    # 10
```

**map, filter, and reduce**

```nova
use arrays as a

fn isEven(n) { send n % 2 == 0 }
fn square(n) { send n * n }
fn add(acc, n) { send acc + n }

let nums = [1, 2, 3, 4, 5, 6]

let evens   = a.filter(nums, isEven)    # [2, 4, 6]
let squares = a.map(evens, square)      # [4, 16, 36]
let total   = a.reduce(squares, add, 0) # 56

print total, true    # 56
```

---

## Modules

Modules are loaded with `use` and called with dot notation. Aliasing keeps names short.

```nova
use math as m
use arrays as a
use string as str

print m.sqrt(144), true         # 12.0
print a.len([1, 2, 3]), true    # 3
print str.upper("hello"), true  # HELLO
```

Nova ships with **10 built-in modules**:

### `use math`

Mathematical functions and constants.

| Function | Description |
|----------|-------------|
| `sqrt(x)` | Square root |
| `pow(b, e)` | b to the power of e |
| `abs(x)` | Absolute value |
| `floor(x)` | Round down |
| `ceil(x)` | Round up |
| `round(x)` | Round to nearest |
| `min(a, b)` | Smaller of two values |
| `max(a, b)` | Larger of two values |
| `clamp(x, lo, hi)` | Constrain to range |
| `log(x)` | Natural logarithm |
| `log10(x)` | Base-10 logarithm |
| `sin(x)` `cos(x)` `tan(x)` | Trigonometry (radians) |
| `pi()` | 3.141592653589793 |
| `e()` | 2.718281828459045 |

```nova
use math as m
print m.pow(2, 10), true         # 1024
print m.clamp(150, 0, 100), true # 100
print m.pi(), true               # 3.141592653589793
```

---

### `use arrays`

Operations on array values. Functions that modify arrays do so in place (`push`, `pop`). Functions that produce new arrays never modify the original (`sort`, `reverse`, `slice`).

| Function | Returns | Description |
|----------|---------|-------------|
| `len(a)` | `int` | Number of elements |
| `push(a, v)` | `array` | Append element |
| `pop(a)` | `value` | Remove and return last |
| `first(a)` `last(a)` | `value` | First / last element |
| `contains(a, v)` | `boolean` | True if element exists |
| `indexOf(a, v)` | `int` | Index of first match, -1 if not found |
| `reverse(a)` | `array` | New reversed array |
| `slice(a, s, e)` | `array` | Sub-array from s to e (exclusive) |
| `join(a, sep)` | `string` | Join with separator |
| `sort(a)` | `array` | New sorted array (ascending) |
| `sortDesc(a)` | `array` | New sorted array (descending) |
| `sum(a)` | `number` | Sum of all elements |
| `min(a)` `max(a)` | `value` | Min / max element |
| `unique(a)` | `array` | Array without duplicates |
| `flatten(a)` | `array` | One level of nesting removed |
| `map(a, fn)` | `array` | Apply fn to each element |
| `filter(a, fn)` | `array` | Keep elements where fn returns true |
| `reduce(a, fn, init)` | `value` | Fold array into single value |

---

### `use string`

String manipulation. All functions return new strings — strings are immutable.

| Function | Returns | Description |
|----------|---------|-------------|
| `len(s)` | `int` | Length |
| `upper(s)` `lower(s)` | `string` | Case conversion |
| `trim(s)` | `string` | Remove leading/trailing whitespace |
| `contains(s, sub)` | `boolean` | True if substring present |
| `startsWith(s, p)` `endsWith(s, p)` | `boolean` | Prefix / suffix check |
| `indexOf(s, sub)` | `int` | Position, -1 if not found |
| `replace(s, old, new)` | `string` | Replace all occurrences |
| `split(s, sep)` | `array` | Split into array |
| `slice(s, start, end)` | `string` | Substring |
| `charAt(s, i)` | `string` | Character at index |
| `reverse(s)` | `string` | Reversed string |
| `repeat(s, n)` | `string` | Repeated string |
| `padLeft(s, n, c)` | `string` | Left-pad to length n |
| `padRight(s, n, c)` | `string` | Right-pad to length n |
| `isNumeric(s)` | `boolean` | True if parseable as number |

---

### `use convert`

Type conversion and type checking.

```nova
use convert as c

print c.toInt("42"), true       # 42
print c.toDouble("3.14"), true  # 3.14
print c.toString(true), true    # true
print c.toChar(65), true        # A
print c.typeOf([1,2,3]), true   # array
print c.isNull(null), true      # true
```

Conversion functions: `toInt`, `toLong`, `toDouble`, `toBigInteger`, `toBigDecimal`, `toString`, `toChar`, `toBool`

Type checking functions: `isInt`, `isLong`, `isDouble`, `isBigInteger`, `isBigDecimal`, `isString`, `isChar`, `isBool`, `isNull`, `isArray`, `isMap`, `isFunction`, `typeOf`

---

### `use map`

Operations on map values.

```nova
use map

let scores = {"Alice": 95, "Bob": 87, "Charlie": 92}

print map.has(scores, "Alice"), true      # true
print map.size(scores), true              # 3
print map.keys(scores), true              # ["Alice", "Bob", "Charlie"]
print map.values(scores), true            # [95, 87, 92]

map.remove(scores, "Bob")
print map.size(scores), true              # 2
```

Functions: `get`, `set`, `has`, `remove`, `keys`, `values`, `size`, `clear`, `merge`, `entries`, `toArray`

---

### `use json`

Parse JSON into Nova values and serialise Nova values back to JSON.

```nova
use json
use arrays as a

let data = json.parse("{\"name\": \"Nova\", \"scores\": [95, 87, 92]}")
print data["name"], true               # Nova
print a.len(data["scores"]), true      # 3

let output = json.stringify(data)
print output, true
# {"name": "Nova", "scores": [95, 87, 92]}
```

JSON objects map to Nova maps. JSON arrays map to Nova arrays. All primitive types map naturally.

---

### `use os`

Operating system interaction — user input, time, randomness, system information.

```nova
use os

let name = os.input("What is your name? ")
print $"Hello, {name}!", true

print os.platform(), true      # Linux / Windows / Mac OS X
print os.username(), true      # current user
print os.cpuCount(), true      # number of CPU cores

let roll = os.randomInt(1, 6)
print $"You rolled a {roll}", true
```

Functions: `input`, `exit`, `sleep`, `time`, `clock`, `random`, `randomInt`, `platform`, `username`, `javaVersion`, `cpuCount`, `totalMemory`, `freeMemory`, `workdir`, `homedir`, `env`, `separator`

---

### `use file`

Read from and write to files.

```nova
use file

file.write("log.txt", "Program started\n")
file.append("log.txt", "Processing data\n")

let content = file.read("log.txt")
print content, true

let lines = file.lines("data.csv")
print lines, true    # array of lines

print file.exists("missing.txt"), true    # false
print file.size("log.txt"), true          # bytes
```

Functions: `read`, `lines`, `write`, `append`, `writeLines`, `exists`, `delete`, `copy`, `move`, `size`, `name`, `extension`, `parent`, `mkdir`, `listFiles`, `isDir`

---

### `use datetime`

Dates, times, and date arithmetic.

```nova
use datetime as dt

print dt.now(), true           # 2026-03-22 14:30:45
print dt.year(), true          # 2026
print dt.dayOfWeek(), true     # Sunday
print dt.isWeekend(), true     # true

# Date arithmetic
print dt.addDays(2026, 3, 22, 30), true       # 2026-04-21
print dt.daysBetween(2026, 1, 1, 2026, 12, 31), true  # 364
print dt.isLeapYear(2024), true               # true
```

Functions: `now`, `date`, `time`, `year`, `month`, `day`, `hour`, `minute`, `second`, `millisecond`, `dayOfWeek`, `dayOfYear`, `weekOfYear`, `isWeekend`, `isWeekday`, `isLeapYear`, `daysInMonth`, `timestamp`, `fromTimestamp`, `daysBetween`, `addDays`, `addMonths`, `addYears`, `format`, `formatTime`, `formatFull`

---

### `use net`

Make HTTP requests. Every response is a map with four keys: `status` (int), `body` (string), `ok` (boolean), `headers` (map).

```nova
use net
use json

let res = net.get("https://jsonplaceholder.typicode.com/todos/1")

if res["ok"] {
    let data = json.parse(res["body"])
    print data["title"], true
} else {
    print "Error: " + res["status"], true
}
```

```nova
# POST with a JSON body
let body = json.stringify({"title": "Buy groceries", "completed": false})
let res = net.post("https://jsonplaceholder.typicode.com/todos", body)
print res["status"], true    # 201
```

```nova
# GET with custom headers
use map
let headers = {"Authorization": "Bearer my-token"}
let res = net.getWithHeaders("https://api.example.com/data", headers)
```

Functions: `get`, `post`, `put`, `delete`, `patch`, `request`, `getWithHeaders`, `postWithHeaders`, `encode`, `decode`, `isOk`

---

## Error Messages

Nova errors include the line number and the offending source line, making them easy to find and fix.

```
[line 5] UndefinedVariableException: Undefined variable: 'score'
    --> print score, true
```

```
[line 12] ArgumentException: Function 'add' expects 2 arguments but got 1
    --> print add(5), true
```

```
[line 8] NovaIndexOutOfBoundsException: Index 5 is out of bounds for length 3
    --> print arr[5], true
```

```
[line 3] TypeError: Division by zero
    --> print x / 0, true
```

**Exception hierarchy:**

```
NovaException
├── LexerException       — unrecognised character or unclosed string
├── ParseException       — structural syntax error
└── NovaRuntimeException
    ├── UndefinedVariableException   — variable used before declaration
    ├── UndeclaredVariableException  — assignment to undeclared variable
    ├── UndefinedFunctionException   — function called but not defined
    ├── ArgumentException            — wrong number of arguments
    ├── TypeError                    — wrong type for operation
    ├── NovaIndexOutOfBoundsException
    └── ReturnOutsideFunctionException
```

---

## Running Nova Programs

Nova programs can use either `.nova` or `.txt` as their file extension.

### In BlueJ

Place code in `code.txt` in the project folder, then run `Main`.

### From the command line (after building the JAR)

```bash
# Run a program
nova run myprogram.nova

# Check syntax without running
nova compile myprogram.nova

# Shorthand
nova myprogram.nova
```

See [INSTALL.md](INSTALL.md) for building the JAR and setting up the `nova` command.

---

## Architecture

Nova source code goes through three stages before executing:

```
Source text
    │
    ▼
┌─────────┐
│  Lexer  │  — breaks source into a flat list of tokens
└─────────┘
    │  List<Token>
    ▼
┌─────────┐
│ Parser  │  — builds an Abstract Syntax Tree (AST)
└─────────┘
    │  List<Node>
    ▼
┌─────────────┐
│ Interpreter │  — walks the AST and executes each node
└─────────────┘
    │
    ▼
Output / side effects
```

**Lexer** — reads source character by character. Produces typed tokens (`LET`, `IDENTIFIER`, `NUMBER`, `STRING`, `IF`, etc.), records line numbers on every token, handles comment stripping, escape sequences, and the `$"..."` interpolation prefix.

**Parser** — consumes the token stream using recursive descent. Produces 33 AST node types (`AssignmentNode`, `BinaryOpNode`, `IfNode`, `FunctionDefNode`, etc.). Enforces operator precedence and grammar rules.

**Interpreter** — walks the AST recursively. Maintains a `Scope` chain for variable lookup, a functions map for `fn` definitions, and a modules map for loaded modules. Numeric promotion happens here — every arithmetic operation checks for overflow and promotes automatically.

---

## File Structure

```
NovaLang/
│
├── Main.java                   Entry point — reads and runs code.txt
├── Lexer.java                  Tokeniser
├── Parser.java                 Recursive descent parser
├── Interpreter.java            Tree-walking interpreter
├── Scope.java                  Lexical scope chain
├── Token.java                  Token record
├── TokenType.java              Token type enum (37 types)
│
├── Node.java                   Abstract AST node base class
├── *Node.java                  33 concrete AST node classes
│
├── Value.java                  Abstract value base class
├── IntValue.java               int
├── LongValue.java              long
├── BigIntValue.java            bigint (arbitrary precision)
├── DoubleValue.java            double
├── BigDecimalValue.java        bigdecimal (high precision)
├── CharValue.java              char
├── StringValue.java            string
├── BooleanValue.java           boolean
├── NullValue.java              null
├── ArrayValue.java             array
├── MapValue.java               map
├── FunctionValue.java          function (first-class)
│
├── BuiltinFunction.java        Interface for module functions
├── MathModule.java             use math
├── ArraysModule.java           use arrays
├── StringModule.java           use string
├── ConvertModule.java          use convert
├── MapModule.java              use map
├── JsonModule.java             use json
├── OsModule.java               use os
├── FileModule.java             use file
├── DateTimeModule.java         use datetime
├── NetModule.java              use net
│
├── NovaException.java          Base exception
├── LexerException.java
├── ParseException.java
├── NovaRuntimeException.java
├── TypeError.java
├── UndefinedVariableException.java
├── UndeclaredVariableException.java
├── UndefinedFunctionException.java
├── ArgumentException.java
├── NovaIndexOutOfBoundsException.java
├── ReturnException.java        (internal — not a user-facing error)
├── BreakException.java         (internal)
├── ContinueException.java      (internal)
│
└── code.txt                    Write your Nova programs here
```

---

## Quick Reference

### Keywords

`let` `fn` `send` `print` `if` `elif` `else` `while` `for` `in` `to` `break` `continue` `use` `as` `true` `false` `null` `and` `or` `not`

### Operators

| Category | Operators |
|----------|-----------|
| Arithmetic | `+` `-` `*` `/` `%` |
| Assignment | `=` `+=` `-=` `*=` `/=` `%=` `++` `--` |
| Comparison | `==` `!=` `>` `<` `>=` `<=` |
| Logical | `and` `or` `not` |
| String/Array | `+` (concat) `*` (repeat) |

### String Syntax

| Syntax | Use |
|--------|-----|
| `"hello"` | Plain string |
| `$"Hello {name}"` | Interpolated string |
| `"""multi\nline"""` | Triple-quoted string |
| `\n` `\t` `\"` `\\` | Escape sequences |

### Modules at a Glance

| Module | What it does |
|--------|-------------|
| `use math` | `sqrt`, `pow`, `sin`, `cos`, `log`, `pi`, `clamp` … |
| `use arrays` | `len`, `push`, `sort`, `map`, `filter`, `reduce` … |
| `use string` | `upper`, `split`, `replace`, `padLeft`, `trim` … |
| `use convert` | `toInt`, `typeOf`, `isNull`, `isArray` … |
| `use map` | `has`, `keys`, `values`, `merge`, `size` … |
| `use json` | `parse`, `stringify` |
| `use os` | `input`, `randomInt`, `sleep`, `platform` … |
| `use file` | `read`, `write`, `append`, `exists`, `lines` … |
| `use datetime` | `now`, `addDays`, `daysBetween`, `format` … |
| `use net` | `get`, `post`, `put`, `delete`, `encode` … |

---

## Licence

MIT
