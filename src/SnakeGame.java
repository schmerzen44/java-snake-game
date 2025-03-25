import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SnakeGame {

    public static void main(String[] args) {
        new GameFrame();
    }
}

class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Змейка - КГТУ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TILE_SIZE = 25;
    private static final int ALL_TILES = (WIDTH * HEIGHT) / (TILE_SIZE * TILE_SIZE);
    private static final int DELAY = 100;
    private static final int LETTER_SPACING = 5; // Расстояние между буквами

    private final int[] x = new int[ALL_TILES];
    private final int[] y = new int[ALL_TILES];

    private int bodyParts = 3;
    private int applesEaten;
    private int appleX;
    private int appleY;

    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private Random random;
    private JButton restartButton;

    public GamePanel() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null); // Для абсолютного позиционирования кнопки

        // Кнопка рестарта (изначально невидима)
        restartButton = new JButton("Начать заново");
        restartButton.setBounds(WIDTH/2 - 100, HEIGHT/2 + 50, 200, 40);
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setBackground(new Color(50, 150, 50));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> resetGame());
        restartButton.setVisible(false);
        add(restartButton);

        addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        // Инициализация начальной позиции змейки
        for (int i = 0; i < bodyParts; i++) {
            x[i] = WIDTH/2 - i * TILE_SIZE;
            y[i] = HEIGHT/2;
        }

        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void resetGame() {
        bodyParts = 3;
        applesEaten = 0;
        direction = 'R';
        restartButton.setVisible(false);
        startGame();
    }

    public void newApple() {
        appleX = random.nextInt(WIDTH / TILE_SIZE) * TILE_SIZE;
        appleY = random.nextInt(HEIGHT / TILE_SIZE) * TILE_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= TILE_SIZE;
            case 'D' -> y[0] += TILE_SIZE;
            case 'L' -> x[0] -= TILE_SIZE;
            case 'R' -> x[0] += TILE_SIZE;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            restartButton.setVisible(true);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (running) {
            // Отрисовка яблока
            g.setColor(Color.RED);
            g.fillOval(appleX, appleY, TILE_SIZE, TILE_SIZE);

            // Отрисовка змейки
            for (int i = 0; i < bodyParts; i++) {
                // Голова змейки - зеленый, тело - темно-зеленый
                g.setColor(i == 0 ? Color.GREEN : new Color(45, 180, 0));
                g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);

                // Отрисовка букв "КГТУ" при длине змейки >= 15
                if (bodyParts >= 15 && i < 4) {
                    drawLetterOnSnake(g, i);
                }
            }

            // Отрисовка счета
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Счет: " + applesEaten, 10, 20);
        } else {
            gameOver(g);
        }
    }

    private void drawLetterOnSnake(Graphics g, int segmentIndex) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));

        // Координаты для текста
        int textX = x[segmentIndex] + TILE_SIZE/4;
        int textY = y[segmentIndex] + TILE_SIZE/2 + 5;

        // Буквы "К", "Г", "Т", "У" на первых 4 сегментах
        if (segmentIndex == 0) g.drawString("К", textX, textY);
        else if (segmentIndex == 1) g.drawString("Г", textX, textY);
        else if (segmentIndex == 2) g.drawString("Т", textX, textY);
        else if (segmentIndex == 3) g.drawString("У", textX, textY);
    }

    public void gameOver(Graphics g) {
        // Отрисовка счета
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Счет: " + applesEaten, 10, 20);

        // Отрисовка Game Over
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Игра окончена",
                (WIDTH - metrics.stringWidth("Игра окончена")) / 2,
                HEIGHT / 2 - 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
            }
        }
    }
}