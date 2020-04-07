import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import javax.smartcardio.Card;
import javax.swing.*;
import javax.swing.text.BoxView;

public class Main {
    private JPanel cards; //a panel that uses CardLayout
    private final static String WELCOME_SCREEN = "Welcome screen";
    private final static String LOGIN_SCREEN = "Login screen";
    private final static String MAIN_MENU = "Main menu";
    private final static String TEST_WINDOW = "test window";

    private final static String[] NUMPAD_CONTENT = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"};

    final static int MAX_PIN_SIZE = 6;

    ActionListener backToMainMenu = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(MAIN_MENU);
        }
    };

    ActionListener abortTransaction = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(WELCOME_SCREEN);
        }
    };

    public void addComponentToPane(Container pane) {
        //Create the atm "screens".

        /* WELCOME SCREEN */
        JPanel welcomeScreen = new JPanel();
        welcomeScreen.setLayout(new GridBagLayout());
        welcomeScreen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        welcomeScreen.add(new JLabel("Welcome to the Evil corp ATM. Insert your card to continue."));

        /* temp login */
        JButton testCont = new JButton("continue (TEST PURPOSES)");
        testCont.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setCard(LOGIN_SCREEN);
            }
        });
        welcomeScreen.add(testCont);


        /* LOGIN SCREEN */
        JPanel loginScreen = new JPanel();

        JPasswordField passwordField = new JPasswordField(MAX_PIN_SIZE);
        passwordField.setEditable(false);
        loginScreen.add(passwordField);

        JButton loginAbortButton = new JButton("Abort");
        loginAbortButton.addActionListener(abortTransaction);
        loginScreen.add(loginAbortButton);

        /* Numpad for login screen */
        JPanel numpad = new JPanel();
        numpad.setLayout(new GridLayout(4, 3, 1, 1));
        JButton[] numpadButtons = new JButton[NUMPAD_CONTENT.length];
        for (int i = 0; i < NUMPAD_CONTENT.length; i++) {
            numpadButtons[i] = new JButton(NUMPAD_CONTENT[i]);
            numpadButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            numpadButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    loginNumpadButtonActionPerformed(evt);
                }
            });
            numpad.add(numpadButtons[i]);
        }
        loginScreen.add(numpad);

        /* MAIN MENU */
        JPanel mainMenu = new JPanel();
        mainMenu.add(new JLabel("THIS IS THE MAIN MENU"));

        JButton mainMenuAbortButton = new JButton("Abort");
        mainMenuAbortButton.addActionListener(abortTransaction);
        mainMenu.add(mainMenuAbortButton);

        JButton testButton = new JButton("to test window");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setCard(TEST_WINDOW);
            }
        });
        mainMenu.add(testButton);

        /* TEST WINDOW */
        JPanel testWindow = new JPanel();
        testWindow.add(new JLabel("TEST WINDOW"));

        JButton testAbort = new JButton("Abort");
        testAbort.addActionListener(abortTransaction);
        testWindow.add(testAbort);

        JButton testMain = new JButton("back to main menu");
        testMain.addActionListener(backToMainMenu);
        testWindow.add(testMain);


        //Create the panel that contains the "screens".
        cards = new JPanel(new CardLayout());
        cards.add(welcomeScreen, WELCOME_SCREEN);
        cards.add(loginScreen, LOGIN_SCREEN);
        cards.add(mainMenu, MAIN_MENU);
        cards.add(testWindow, TEST_WINDOW);

        pane.add(cards, BorderLayout.CENTER);
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("EVIL ATM GUI");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = graphics.getDefaultScreenDevice();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setUndecorated(true);


        //Create and set up the content pane.
        Main demo = new Main();
        demo.addComponentToPane(frame.getContentPane());
        demo.setCard(LOGIN_SCREEN);

        //Display the window.
        device.setFullScreenWindow(frame);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }

    public void setCard(String cardId) {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, cardId);
    }

    public void loginNumpadButtonActionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().toString().equalsIgnoreCase("#")) {
            setCard(MAIN_MENU);
        }
    }
}