# 🎯 Number Guessing Game — Java Swing

A visually stunning **Number Guessing Game** built with Java Swing.  
Warm color palette · Score tracking · 3 rounds · 7 attempts per round

---

## 📁 Project Structure

```
NumberGuessingGame/
├── src/
│   └── NumberGuessingGame.java   ← All source code (single file)
├── bin/                          ← Compiled .class files (auto-created)
├── run.bat                       ← Windows launcher
├── run.sh                        ← Mac / Linux launcher
├── .vscode/
│   ├── settings.json             ← VS Code Java settings
│   └── launch.json               ← Run config for VS Code
└── README.md                     ← You are here
```

---

## ▶️ How to Run

### Option 1 — Double-click launcher (easiest)
- **Windows:** Double-click `run.bat`
- **Mac/Linux:** Open terminal → `chmod +x run.sh && ./run.sh`

### Option 2 — VS Code
1. Open the `NumberGuessingGame` folder in VS Code
2. Install the **Extension Pack for Java** (Microsoft) if you haven't
3. Open `src/NumberGuessingGame.java`
4. Press **F5** or click **Run ▷** at the top right

### Option 3 — Command Line (any OS)
```bash
# From inside the NumberGuessingGame folder:
mkdir -p bin
javac -d bin src/NumberGuessingGame.java
java -cp bin NumberGuessingGame
```

---

## 🎮 How to Play

1. The computer picks a secret number between **1 and 100**
2. Type your guess and press **GUESS!** or hit **Enter**
3. The game tells you if you're **too high** or **too low**
4. The active range narrows with each guess — use it!
5. Guess correctly in fewer attempts → **More points**
6. Complete **3 rounds**, then see your total score

### 🏆 Scoring
| Attempts remaining when you guess | Points |
|---|---|
| 6 (1st try!) | 190 |
| 5 | 175 |
| 4 | 160 |
| 3 | 145 |
| 2 | 130 |
| 1 | 115 |
| 0 (last attempt) | 100 |
| Didn't guess | 0 |

---

## ⚙️ Requirements

- **Java 8 or higher** (Java 11+ recommended)
- No external libraries needed — pure Java SE

Check your Java version:
```bash
java -version
```

---

## 📤 Uploading to Platforms

### GitHub
```bash
git init
git add .
git commit -m "Initial commit — Number Guessing Game"
git remote add origin https://github.com/YOUR_USERNAME/number-guessing-game.git
git push -u origin main
```

### Replit
1. Create a new Java Repl
2. Upload all files maintaining the folder structure
3. Set **Run command** to: `javac -d bin src/NumberGuessingGame.java && java -cp bin NumberGuessingGame`

### Other platforms (Glitch, CodeSandbox, etc.)
- Upload `src/NumberGuessingGame.java`
- Compile: `javac NumberGuessingGame.java`
- Run: `java NumberGuessingGame`
