import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.Timer;

public class NumberGuessingGame extends JFrame {

    // Color Palette - Warm & Vibrant (no blue/purple)
    private static final Color BG_DARK       = new Color(15, 15, 20);
    private static final Color BG_CARD       = new Color(28, 28, 35);
    private static final Color BG_CARD2      = new Color(35, 32, 28);
    private static final Color AMBER         = new Color(255, 176, 0);
    private static final Color AMBER_DARK    = new Color(200, 130, 0);
    private static final Color CRIMSON       = new Color(220, 50, 50);
    private static final Color CRIMSON_DARK  = new Color(170, 30, 30);
    private static final Color EMERALD       = new Color(16, 185, 100);
    private static final Color EMERALD_DARK  = new Color(10, 140, 70);
    private static final Color ORANGE        = new Color(255, 110, 30);
    private static final Color ORANGE_DARK   = new Color(200, 80, 10);
    private static final Color GOLD          = new Color(255, 215, 0);
    private static final Color CORAL         = new Color(255, 127, 80);
    private static final Color TEXT_PRIMARY  = new Color(245, 240, 228);
    private static final Color TEXT_MUTED    = new Color(160, 150, 135);
    private static final Color TEXT_HINT     = new Color(120, 115, 105);

    // Game State
    private int secretNumber;
    private int attemptsLeft;
    private int currentRound;
    private int totalScore;
    private int totalRounds = 3;
    private int maxAttempts = 7;
    private int minRange = 1;
    private int maxRange = 100;
    private boolean gameOver = false;
    private boolean roundWon = false;

    // UI Components
    private JTextField guessField;
    private JButton submitBtn;
    private JLabel feedbackLabel;
    private JLabel attemptsLabel;
    private JLabel roundLabel;
    private JLabel scoreLabel;
    private JLabel rangeHintLabel;
    private JProgressBar attemptsBar;
    private JPanel historyPanel;
    private JScrollPane historyScroll;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private Timer pulseTimer;
    private float pulseValue = 0f;

    public NumberGuessingGame() {
        setTitle("Number Guessing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 680);
        setMinimumSize(new Dimension(700, 620));
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(BG_DARK);

        mainContainer.add(buildWelcomeScreen(), "welcome");
        mainContainer.add(buildGameScreen(), "game");
        mainContainer.add(buildResultScreen(), "result");

        add(mainContainer);
        cardLayout.show(mainContainer, "welcome");

        // Pulse timer for animations
        pulseTimer = new Timer(50, e -> {
            pulseValue += 0.05f;
            if (pulseValue > (float)(2 * Math.PI)) pulseValue = 0f;
            if (feedbackLabel != null) feedbackLabel.repaint();
        });
        pulseTimer.start();
    }

    // ─── WELCOME SCREEN ─────────────────────────────────────────────────────────

    private JPanel buildWelcomeScreen() {
        JPanel panel = new GradientPanel(BG_DARK, new Color(25, 20, 15));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("NUMBER", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 72));
        title.setForeground(AMBER);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("GUESSING GAME", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        subtitle.setForeground(ORANGE);
        gbc.gridy = 1; gbc.insets = new Insets(0, 20, 20, 20);
        panel.add(subtitle, gbc);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 50, 35));
        sep.setPreferredSize(new Dimension(500, 2));
        gbc.gridy = 2; gbc.insets = new Insets(5, 60, 20, 60);
        panel.add(sep, gbc);

        // Info card
        JPanel infoCard = new RoundedPanel(18, new Color(35, 30, 22));
        infoCard.setLayout(new GridLayout(0, 1, 0, 4));
        infoCard.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        String[] lines = {
            "🎯  Guess a random number between 1 and 100",
            "⚡  You have " + maxAttempts + " attempts per round",
            "🏆  Play " + totalRounds + " rounds — best score wins",
            "💎  Fewer guesses = More points!"
        };
        for (String line : lines) {
            JLabel l = new JLabel(line);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            l.setForeground(TEXT_PRIMARY);
            l.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            infoCard.add(l);
        }
        gbc.gridy = 3; gbc.insets = new Insets(5, 60, 25, 60);
        panel.add(infoCard, gbc);

        // Start Button
        JButton startBtn = createGradientButton("START PLAYING", ORANGE, AMBER, 56);
        startBtn.addActionListener(e -> startNewGame());
        gbc.gridy = 4; gbc.insets = new Insets(10, 100, 8, 100);
        panel.add(startBtn, gbc);

        JLabel tagline = new JLabel("Can you crack the code?", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        tagline.setForeground(TEXT_HINT);
        gbc.gridy = 5; gbc.insets = new Insets(0, 20, 10, 20);
        panel.add(tagline, gbc);

        return panel;
    }

    // ─── GAME SCREEN ────────────────────────────────────────────────────────────

    private JPanel buildGameScreen() {
        JPanel panel = new GradientPanel(BG_DARK, new Color(20, 18, 12));
        panel.setLayout(new BorderLayout(0, 0));

        // Top bar
        JPanel topBar = new JPanel(new GridLayout(1, 3));
        topBar.setBackground(new Color(22, 20, 14));
        topBar.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        roundLabel = statLabel("ROUND", "1 / " + totalRounds, AMBER);
        attemptsLabel = statLabel("ATTEMPTS LEFT", String.valueOf(maxAttempts), ORANGE);
        scoreLabel = statLabel("SCORE", "0", EMERALD);

        topBar.add(roundLabel);
        topBar.add(attemptsLabel);
        topBar.add(scoreLabel);
        panel.add(topBar, BorderLayout.NORTH);

        // Center
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 40, 6, 40);

        // Attempts bar
        attemptsBar = new JProgressBar(0, maxAttempts);
        attemptsBar.setValue(maxAttempts);
        attemptsBar.setPreferredSize(new Dimension(600, 10));
        attemptsBar.setBorderPainted(false);
        attemptsBar.setBackground(new Color(45, 40, 30));
        attemptsBar.setForeground(EMERALD);
        attemptsBar.setStringPainted(false);
        gbc.gridy = 0; gbc.insets = new Insets(20, 40, 14, 40);
        center.add(attemptsBar, gbc);

        // Feedback label
        feedbackLabel = new JLabel("Enter your first guess!", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        feedbackLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        feedbackLabel.setForeground(TEXT_PRIMARY);
        feedbackLabel.setPreferredSize(new Dimension(600, 48));
        gbc.gridy = 1; gbc.insets = new Insets(6, 40, 2, 40);
        center.add(feedbackLabel, gbc);

        // Range hint
        rangeHintLabel = new JLabel("Range: 1 – 100", SwingConstants.CENTER);
        rangeHintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rangeHintLabel.setForeground(TEXT_MUTED);
        gbc.gridy = 2; gbc.insets = new Insets(0, 40, 16, 40);
        center.add(rangeHintLabel, gbc);

        // Input card
        JPanel inputCard = new RoundedPanel(20, BG_CARD);
        inputCard.setLayout(new BorderLayout(16, 0));
        inputCard.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        guessField = new JTextField();
        guessField.setFont(new Font("Segoe UI", Font.BOLD, 36));
        guessField.setForeground(AMBER);
        guessField.setBackground(new Color(22, 22, 28));
        guessField.setCaretColor(AMBER);
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(10, new Color(60, 55, 40)),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        guessField.addActionListener(e -> submitGuess());

        submitBtn = createGradientButton("GUESS!", ORANGE, AMBER, 48);
        submitBtn.setPreferredSize(new Dimension(130, 60));
        submitBtn.addActionListener(e -> submitGuess());

        inputCard.add(guessField, BorderLayout.CENTER);
        inputCard.add(submitBtn, BorderLayout.EAST);
        gbc.gridy = 3; gbc.insets = new Insets(6, 40, 16, 40);
        center.add(inputCard, gbc);

        // History
        JLabel histTitle = new JLabel("GUESS HISTORY");
        histTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        histTitle.setForeground(TEXT_MUTED);
        histTitle.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        gbc.gridy = 4; gbc.insets = new Insets(4, 40, 2, 40);
        center.add(histTitle, gbc);

        historyPanel = new JPanel();
        historyPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        historyPanel.setBackground(new Color(20, 20, 26));
        historyPanel.setOpaque(true);

        historyScroll = new JScrollPane(historyPanel);
        historyScroll.setPreferredSize(new Dimension(600, 64));
        historyScroll.setBorder(new RoundBorder(12, new Color(45, 40, 30)));
        historyScroll.setBackground(new Color(20, 20, 26));
        historyScroll.getViewport().setBackground(new Color(20, 20, 26));
        historyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 20, 40);
        center.add(historyScroll, gbc);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ─── RESULT SCREEN ──────────────────────────────────────────────────────────

    private JLabel resultTitleLabel;
    private JLabel resultScoreLabel;
    private JLabel resultMsgLabel;
    private JPanel roundBreakdownPanel;

    private JPanel buildResultScreen() {
        JPanel panel = new GradientPanel(BG_DARK, new Color(20, 14, 8));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        resultTitleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        resultTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 60));
        resultTitleLabel.setForeground(GOLD);
        gbc.gridy = 0; gbc.insets = new Insets(20, 50, 0, 50);
        panel.add(resultTitleLabel, gbc);

        resultMsgLabel = new JLabel("", SwingConstants.CENTER);
        resultMsgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        resultMsgLabel.setForeground(TEXT_MUTED);
        gbc.gridy = 1; gbc.insets = new Insets(4, 50, 20, 50);
        panel.add(resultMsgLabel, gbc);

        JPanel scoreBox = new RoundedPanel(20, new Color(40, 35, 18));
        scoreBox.setLayout(new BoxLayout(scoreBox, BoxLayout.Y_AXIS));
        scoreBox.setBorder(BorderFactory.createEmptyBorder(22, 40, 22, 40));

        JLabel scoreLbl = new JLabel("FINAL SCORE");
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreLbl.setForeground(TEXT_MUTED);
        scoreBox.add(scoreLbl);

        resultScoreLabel = new JLabel("0");
        resultScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        resultScoreLabel.setForeground(AMBER);
        scoreBox.add(resultScoreLabel);

        JLabel pointsLbl = new JLabel("points");
        pointsLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pointsLbl.setForeground(TEXT_MUTED);
        scoreBox.add(pointsLbl);

        gbc.gridy = 2; gbc.insets = new Insets(0, 80, 16, 80);
        panel.add(scoreBox, gbc);

        roundBreakdownPanel = new JPanel(new GridLayout(1, totalRounds, 12, 0));
        roundBreakdownPanel.setOpaque(false);
        gbc.gridy = 3; gbc.insets = new Insets(0, 50, 20, 50);
        panel.add(roundBreakdownPanel, gbc);

        JButton playAgainBtn = createGradientButton("PLAY AGAIN", EMERALD, EMERALD_DARK, 50);
        playAgainBtn.addActionListener(e -> startNewGame());
        gbc.gridy = 4; gbc.insets = new Insets(4, 100, 6, 100);
        panel.add(playAgainBtn, gbc);

        JButton quitBtn = createGradientButton("QUIT", CRIMSON, CRIMSON_DARK, 44);
        quitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy = 5; gbc.insets = new Insets(0, 140, 16, 140);
        panel.add(quitBtn, gbc);

        return panel;
    }

    // ─── GAME LOGIC ─────────────────────────────────────────────────────────────

    private int[] roundScores;
    private int[] roundAttempts;

    private void startNewGame() {
        currentRound = 1;
        totalScore = 0;
        roundScores = new int[totalRounds];
        roundAttempts = new int[totalRounds];
        startRound();
        cardLayout.show(mainContainer, "game");
    }

    private void startRound() {
        Random rand = new Random();
        secretNumber = rand.nextInt(maxRange - minRange + 1) + minRange;
        attemptsLeft = maxAttempts;
        gameOver = false;
        roundWon = false;
        minRange = 1;
        maxRange = 100;

        roundLabel.setText("<html><div style='text-align:center'><span style='font-size:10px;color:#a09080'>ROUND</span><br><b style='font-size:18px;color:#FFB000'>" + currentRound + " / " + totalRounds + "</b></div></html>");
        updateAttemptsUI();
        scoreLabel.setText("<html><div style='text-align:center'><span style='font-size:10px;color:#a09080'>SCORE</span><br><b style='font-size:18px;color:#10B964'>" + totalScore + "</b></div></html>");
        feedbackLabel.setText("Round " + currentRound + " — Enter your first guess!");
        feedbackLabel.setForeground(TEXT_PRIMARY);
        rangeHintLabel.setText("Range: 1 – 100");
        historyPanel.removeAll();
        historyPanel.revalidate();
        historyPanel.repaint();
        guessField.setText("");
        guessField.setEnabled(true);
        submitBtn.setEnabled(true);
        guessField.requestFocus();
    }

    private void submitGuess() {
        if (gameOver) return;
        String input = guessField.getText().trim();
        int guess;
        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("⚠  Please enter a valid number!");
            feedbackLabel.setForeground(CORAL);
            guessField.setText("");
            return;
        }

        if (guess < 1 || guess > 100) {
            feedbackLabel.setText("⚠  Number must be between 1 and 100!");
            feedbackLabel.setForeground(CORAL);
            guessField.setText("");
            return;
        }

        attemptsLeft--;
        addHistoryChip(guess, guess == secretNumber ? "correct" : guess < secretNumber ? "low" : "high");
        updateAttemptsUI();

        if (guess == secretNumber) {
            int points = calculatePoints(attemptsLeft);
            roundScores[currentRound - 1] = points;
            roundAttempts[currentRound - 1] = maxAttempts - attemptsLeft;
            totalScore += points;
            scoreLabel.setText("<html><div style='text-align:center'><span style='font-size:10px;color:#a09080'>SCORE</span><br><b style='font-size:18px;color:#10B964'>" + totalScore + "</b></div></html>");
            feedbackLabel.setText("🎉  Correct! The number was " + secretNumber + "! +" + points + " pts");
            feedbackLabel.setForeground(EMERALD);
            guessField.setEnabled(false);
            submitBtn.setEnabled(false);
            gameOver = true;
            roundWon = true;
            Timer t = new Timer(2000, e -> nextRoundOrEnd());
            t.setRepeats(false);
            t.start();
        } else if (attemptsLeft == 0) {
            roundScores[currentRound - 1] = 0;
            roundAttempts[currentRound - 1] = maxAttempts;
            feedbackLabel.setText("💀  Out of attempts! The number was " + secretNumber);
            feedbackLabel.setForeground(CRIMSON);
            guessField.setEnabled(false);
            submitBtn.setEnabled(false);
            gameOver = true;
            Timer t = new Timer(2200, e -> nextRoundOrEnd());
            t.setRepeats(false);
            t.start();
        } else {
            if (guess < secretNumber) {
                minRange = Math.max(minRange, guess + 1);
                feedbackLabel.setText("📈  Too Low! Go Higher.");
                feedbackLabel.setForeground(ORANGE);
            } else {
                maxRange = Math.min(maxRange, guess - 1);
                feedbackLabel.setText("📉  Too High! Go Lower.");
                feedbackLabel.setForeground(CORAL);
            }
            rangeHintLabel.setText("Range: " + minRange + " – " + maxRange);
        }
        guessField.setText("");
        guessField.requestFocus();
    }

    private void nextRoundOrEnd() {
        if (currentRound < totalRounds) {
            currentRound++;
            minRange = 1;
            maxRange = 100;
            startRound();
        } else {
            showResults();
        }
    }

    private void showResults() {
        resultScoreLabel.setText(String.valueOf(totalScore));

        int maxPossible = totalRounds * calculatePoints(maxAttempts - 1);
        if (totalScore >= (maxPossible * 0.75)) {
            resultTitleLabel.setText("🏆  AMAZING!");
            resultMsgLabel.setText("You're a guessing genius — outstanding performance!");
            resultTitleLabel.setForeground(GOLD);
        } else if (totalScore >= (maxPossible * 0.4)) {
            resultTitleLabel.setText("👏  WELL DONE!");
            resultMsgLabel.setText("Good game! Practice makes perfect.");
            resultTitleLabel.setForeground(EMERALD);
        } else {
            resultTitleLabel.setText("😅  KEEP TRYING!");
            resultMsgLabel.setText("The numbers got the better of you this time!");
            resultTitleLabel.setForeground(CORAL);
        }

        roundBreakdownPanel.removeAll();
        Color[] roundColors = { AMBER, ORANGE, CORAL };
        for (int i = 0; i < totalRounds; i++) {
            JPanel card = new RoundedPanel(14, new Color(35, 30, 22));
            card.setLayout(new GridLayout(0, 1, 0, 2));
            card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            JLabel rndLbl = new JLabel("Round " + (i + 1), SwingConstants.CENTER);
            rndLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            rndLbl.setForeground(roundColors[i % roundColors.length]);
            card.add(rndLbl);

            JLabel ptsLbl = new JLabel(roundScores[i] + " pts", SwingConstants.CENTER);
            ptsLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
            ptsLbl.setForeground(TEXT_PRIMARY);
            card.add(ptsLbl);

            JLabel attLbl = new JLabel(roundAttempts[i] + " attempts", SwingConstants.CENTER);
            attLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            attLbl.setForeground(TEXT_MUTED);
            card.add(attLbl);

            roundBreakdownPanel.add(card);
        }
        roundBreakdownPanel.revalidate();
        roundBreakdownPanel.repaint();
        cardLayout.show(mainContainer, "result");
    }

    private int calculatePoints(int attemptsRemaining) {
        return Math.max(10, 100 + attemptsRemaining * 15);
    }

    private void updateAttemptsUI() {
        attemptsLabel.setText("<html><div style='text-align:center'><span style='font-size:10px;color:#a09080'>ATTEMPTS LEFT</span><br><b style='font-size:18px;color:#FF6E1E'>" + attemptsLeft + "</b></div></html>");
        attemptsBar.setValue(attemptsLeft);
        float ratio = (float) attemptsLeft / maxAttempts;
        if (ratio > 0.6f) attemptsBar.setForeground(EMERALD);
        else if (ratio > 0.3f) attemptsBar.setForeground(AMBER);
        else attemptsBar.setForeground(CRIMSON);
    }

    private void addHistoryChip(int number, String type) {
        JLabel chip = new JLabel(String.valueOf(number), SwingConstants.CENTER);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        chip.setPreferredSize(new Dimension(52, 32));
        switch (type) {
            case "correct":
                chip.setBackground(EMERALD_DARK);
                chip.setForeground(Color.WHITE);
                break;
            case "low":
                chip.setBackground(new Color(50, 35, 10));
                chip.setForeground(AMBER);
                break;
            default:
                chip.setBackground(new Color(50, 18, 18));
                chip.setForeground(CORAL);
                break;
        }
        chip.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(8, type.equals("correct") ? EMERALD : type.equals("low") ? AMBER_DARK : CRIMSON_DARK),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        historyPanel.add(chip);
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    // ─── HELPER COMPONENTS ──────────────────────────────────────────────────────

    private JLabel statLabel(String title, String value, Color valueColor) {
        JLabel lbl = new JLabel("<html><div style='text-align:center'><span style='font-size:10px;color:#a09080'>" + title + "</span><br><b style='font-size:18px;color:#" + colorToHex(valueColor) + "'>" + value + "</b></div></html>", SwingConstants.CENTER);
        return lbl;
    }

    private String colorToHex(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private JButton createGradientButton(String text, Color c1, Color c2, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, height));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setFont(btn.getFont().deriveFont(Font.BOLD, 16f));
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setFont(btn.getFont().deriveFont(Font.BOLD, 15f));
                btn.repaint();
            }
        });
        return btn;
    }

    // ─── INNER PANELS ───────────────────────────────────────────────────────────

    static class GradientPanel extends JPanel {
        Color c1, c2;
        GradientPanel(Color c1, Color c2) {
            this.c1 = c1; this.c2 = c2;
            setOpaque(true);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    static class RoundedPanel extends JPanel {
        int radius;
        Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundBorder extends AbstractBorder {
        int radius;
        Color color;
        RoundBorder(int radius, Color color) {
            this.radius = radius; this.color = color;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(radius / 2, radius / 2, radius / 2, radius / 2); }
        @Override
        public Insets getBorderInsets(Component c, Insets i) {
            i.set(radius / 2, radius / 2, radius / 2, radius / 2); return i;
        }
    }

    // ─── MAIN ───────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Apply dark defaults globally
        UIManager.put("Panel.background", new Color(15, 15, 20));
        UIManager.put("OptionPane.background", new Color(15, 15, 20));
        UIManager.put("OptionPane.foreground", new Color(245, 240, 228));
        UIManager.put("ScrollBar.thumb", new Color(70, 60, 45));
        UIManager.put("ScrollBar.track", new Color(25, 22, 16));

        SwingUtilities.invokeLater(() -> {
            NumberGuessingGame game = new NumberGuessingGame();
            game.setVisible(true);
        });
    }
}
