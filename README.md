# DBC Support for JetBrains IDEs

Full-featured language support for **.dbc** files — the industry-standard CAN bus database format used in automotive and embedded systems development.

## Features

- **Syntax Highlighting** — Rich, customizable highlighting for keywords, message IDs, signal names, strings, numbers, comments, and more
- **Full Parser** — Complete DBC grammar support: VERSION, NS_, BS_, BU_, BO_, SG_, CM_, BA_DEF_, BA_DEF_DEF_, BA_, VAL_, VAL_TABLE_, SIG_GROUP_, BO_TX_BU_, EV_, SG_MUL_VAL_, SIG_VALTYPE_, and all other standard constructs
- **Structure View** — Hierarchical tree showing nodes, messages (with nested signals), value tables, and environment variables
- **Go to Symbol** — Navigate to any message, signal, node, or value table by name (Ctrl+Alt+Shift+N)
- **Find Usages** — Find all references to nodes, messages, and signals throughout DBC files
- **Code Completion** — Smart completion for keywords, node names, signal names, message names, and attribute names
- **Quick Documentation** — Hover documentation showing message layout, signal properties (bit position, range, factor/offset, physical formula), value descriptions, and node communication matrix
- **Code Folding** — Collapse message definitions, comment blocks, and attribute sections
- **Inspections** — Detect duplicate message IDs, duplicate signal names, signal overflow beyond DLC, overlapping signals, and undefined node references
- **Reference Resolution** — Navigate from transmitter/receiver node references to their BU_ definitions
- **Line Markers** — Gutter icons for messages, signals, and nodes
- **Commenter** — Toggle line comments with Ctrl+/
- **Brace Matching** — Matching parentheses and brackets in signal definitions
- **Live Templates** — Predefined snippets: `bo` (message), `sg` (signal), `cm` (comment), `val` (values), `badef` (attribute), `dbcfile` (file skeleton)
- **Custom File Icon** — Distinct icon for .dbc files in the project tree
- **Color Settings** — Fully customizable via Settings > Editor > Color Scheme > DBC

## About DBC Files

DBC files define CAN (Controller Area Network) bus communication databases. They are used extensively in automotive ECU development for describing messages, signals, nodes, and their relationships. The format was created by Vector Informatik for use with CANdb++ and is the de facto standard in the automotive industry.

## Compatibility

- IntelliJ IDEA 2024.1+
- All JetBrains IDEs (IntelliJ IDEA, CLion, PyCharm, etc.)

## Building

### Prerequisites

You need **JDK 21** installed. On macOS:

```bash
brew install temurin@21
```

On other platforms, install JDK 21 from [Eclipse Temurin](https://adoptium.net/).

### Build the plugin

```bash
git clone https://github.com/adrianjagielak/intellij-dbc.git
cd intellij-dbc
./gradlew buildPlugin
```

The plugin ZIP will be at `build/distributions/intellij-dbc-1.0.0.zip`.

### Install in your IDE

**Settings > Plugins > gear icon > Install Plugin from Disk...** and select the built ZIP file.
