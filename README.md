# 🏧 ATM Interface — Java Swing

A modern, fully functional **ATM Interface** built with Java Swing.  
Dark fintech theme · Secure login · 5 operations · Transaction history

---

## 📁 Project Structure

```
ATMInterface/
├── src/
│   └── ATMInterface.java     ← All source code
├── bin/                      ← Compiled .class files (auto-created)
├── run.bat                   ← Windows launcher
├── run.sh                    ← Mac / Linux launcher
├── .vscode/
│   ├── settings.json
│   └── launch.json
└── README.md
```

---

## ▶️ How to Run

### VS Code (Recommended)
1. Open the `ATMInterface` folder in VS Code
2. Install **Extension Pack for Java** (by Microsoft) if needed
3. Open `src/ATMInterface.java`
4. Click the **▷ Run** button (top right) or press **F5**

### Windows
Double-click `run.bat`

### Mac / Linux
```bash
chmod +x run.sh && ./run.sh
```

### Manual (any OS)
```bash
mkdir -p bin
javac -d bin src/ATMInterface.java
java -cp bin ATMInterface
```

---

## 🔐 Demo Accounts

| User ID | PIN  | Name             | Starting Balance |
|---------|------|------------------|-----------------|
| 1001    | 1234 | Alex Morgan      | $12,500.00      |
| 2002    | 5678 | Jordan Smith     | $4,750.50       |
| 3003    | 9999 | Taylor Williams  | $28,000.00      |

---

## 🎮 Features

- **Login** — User ID + PIN authentication with error handling
- **Withdraw** — Take cash with quick-select buttons ($100, $500, $1000, $2000)
- **Deposit** — Add funds with quick-select buttons
- **Transfer** — Send money to another account by User ID
- **Transaction History** — Full scrollable table of all transactions
- **Live balance** — Updates instantly after every operation

---

## ⚙️ Requirements

- **Java 8 or higher** — check with `java -version`
- No external libraries — pure Java SE
