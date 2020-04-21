import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import javax.smartcardio.Card;
import javax.swing.*;
import javax.swing.text.BoxView;

public class Main {
    private JPanel cards; // a panel that uses CardLayout
    private JPasswordField passwordField = new JPasswordField(MAX_PIN_SIZE);
    private JTextField cashField = new JTextField(4);
    private JLabel result = new JLabel();
    private  JLabel moneyLeft = new JLabel();
    private  JLabel errorLabel = new JLabel();
    private String cash = "";
    int finalCash;
    int e50 = 0;
    int e20 = 0;
    int e10 = 0;
    int e5 = 0;
    private final static String WELCOME_SCREEN = "Welcome screen";
    private final static String LOGIN_SCREEN = "Login screen";
    private final static String MAIN_MENU = "Main menu";
    private final static String TEST_WINDOW = "test window";
    private final static String TRANSACTION_SCREEN = "transaction screen";
    private final static String BILLSELECTION_SCREEN = "bill selection screen";
    private final static String RECEIPT_SCREEN = "receipt screen";

    private final static String[] NUMPAD_CONTENT = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" };
    private final static String[] WITHDRAW_OPTIONS = { "10", "20", "30", "40", "50", "60", "70" };
    private final static String[] BILLS = { "5", "10", "20", "50"};

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

    ActionListener login = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(LOGIN_SCREEN);
        }
    };

    public void addComponentToPane(Container pane) {
        // Create the atm "screens".

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

        /* TRANSACTION SCREEN */
        JPanel transactionScreen = new JPanel();
        transactionScreen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        transactionScreen.add(new JLabel("Transaction Menu"));

        cashField.setEditable(false);
        transactionScreen.add(cashField);

        


        JButton transactionAbortButton = new JButton("Abort");
        transactionAbortButton.addActionListener(abortTransaction);
        transactionScreen.add(transactionAbortButton);

        JButton transactionMain = new JButton("back to main menu");
        transactionMain.addActionListener(backToMainMenu);
        transactionScreen.add(transactionMain);

        /* Numpad for withdrawal */
        JPanel withdrawOptions = new JPanel();
        withdrawOptions.setLayout(new GridLayout(2, 4, 1, 1));
        JButton[] withdrawButtons = new JButton[WITHDRAW_OPTIONS.length];
        for (int i = 0; i < WITHDRAW_OPTIONS.length; i++) {
            withdrawButtons[i] = new JButton(WITHDRAW_OPTIONS[i]);
            withdrawButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            withdrawButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    System.out.println(evt.getActionCommand().toString());
                    switch (evt.getActionCommand().toString()) {
                        case "10":
                            result.setText("You receive 1 E10 bill");
                            setCard(RECEIPT_SCREEN);
                            break;
                        case "20":
                            result.setText("You receive 1 E20 bill");
                            setCard(RECEIPT_SCREEN);
                            break;
                        case "30":
                            result.setText("You receive 1 E10 bill and 1 E20 bill");
                            setCard(RECEIPT_SCREEN);
                            break;
                        case "40":
                            result.setText("You receive 2 E20 bills");
                            setCard(RECEIPT_SCREEN);
                            break;
                        case "50":
                            result.setText("You receive 1 E50 bill");
                            setCard(RECEIPT_SCREEN);
                            break;
                        case "60":
                            result.setText("You receive 3 E20 bills");
                            setCard(RECEIPT_SCREEN);
                            break;
                        case "70":
                            result.setText("You receive 1 E50 bill and 1 E20 bill");
                            setCard(RECEIPT_SCREEN);
                            break;
                        default:
                            result.setText("Something went wrong");
                            break;
                    }
                }
            });
            withdrawOptions.add(withdrawButtons[i]);
        }
        transactionScreen.add(withdrawOptions);


        /* Numpad for withdrawal */
        JPanel customPad = new JPanel();
        customPad.setLayout(new GridLayout(4, 3, 1, 1));
        JButton[] customPadButtons = new JButton[NUMPAD_CONTENT.length];
        for (int i = 0; i < NUMPAD_CONTENT.length; i++) {
            customPadButtons[i] = new JButton(NUMPAD_CONTENT[i]);
            customPadButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            customPadButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    customWithdrawalButtonActionPerformed(evt);
                }
            });
            customPad.add(customPadButtons[i]);
        }
        transactionScreen.add(customPad);
        transactionScreen.add(errorLabel);

        /* MONEY SELECTION SCREEN */
        JPanel billSelectionScreen = new JPanel();
        billSelectionScreen.add(new JLabel("Bill selection Menu"));

        JButton billSelectionAbortButton = new JButton("Abort");
        billSelectionAbortButton.addActionListener(abortTransaction);
        billSelectionScreen.add(billSelectionAbortButton);

        calculateBills();

        JButton billSelectionMain = new JButton("back to main menu");
        billSelectionMain.addActionListener(backToMainMenu);
        billSelectionScreen.add(billSelectionMain);

        JPanel billOptions = new JPanel();
        billOptions.setLayout(new GridLayout(2, 4, 1, 1));
        JButton[] billButtons = new JButton[BILLS.length];
        for (int i = 0; i < BILLS.length; i++) {
            billButtons[i] = new JButton(BILLS[i]);
            billButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            billButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    System.out.println(evt.getActionCommand().toString());

                    switch (evt.getActionCommand().toString()) {
                        case "5":
                            if (finalCash < 5) {
                                errorLabel.setText("You do not have enough money left");
                                break;
                            }
                            finalCash -= 5;
                            e5++;
                            calculateBills();
                            break;
                        case "10":
                            if (finalCash < 10) {
                                errorLabel.setText("You do not have enough money left");
                                break;
                            }
                            finalCash -= 10;
                            e10++;
                            calculateBills();
                            break;
                        case "20":
                            if (finalCash < 20) {
                                errorLabel.setText("You do not have enough money left");
                                break;
                            }
                            finalCash -= 20;
                            e20++;
                            calculateBills();
                            break;
                        case "50":
                            if (finalCash < 50) {
                                errorLabel.setText("You do not have enough money left");
                                break;
                            }
                            finalCash -= 50;
                            e50++;
                            calculateBills();
                            break;
                        default:
                            errorLabel.setText("Something went wrong");
                            break;
                    }
                    if (finalCash == 0) {
                        setCard(RECEIPT_SCREEN);
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        setCard(MAIN_MENU);
                        cash = "";

                    }

                }
            });
            billOptions.add(billButtons[i]);
        }
        billSelectionScreen.add(billOptions);

        billSelectionScreen.add(moneyLeft);
        billSelectionScreen.add(result);

        /* RECEIPT SCREEN */
        JPanel receiptScreen = new JPanel();
        receiptScreen.add(new JLabel("Receipt Screen"));
        receiptScreen.add(result);
        receiptScreen.add(new JLabel("Bedankt voor het gebruiken van Evil Corp."));

        JButton receiptMain = new JButton("back to main menu");
        receiptMain.addActionListener(backToMainMenu);
        receiptScreen.add(receiptMain);


        /* LOGIN SCREEN */
        JPanel loginScreen = new JPanel();

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

        JButton mainLogin = new JButton("To login");
        mainLogin.addActionListener(login);
        mainMenu.add(mainLogin);

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

        // Create the panel that contains the "screens".
        cards = new JPanel(new CardLayout());
        cards.add(welcomeScreen, WELCOME_SCREEN);
        cards.add(loginScreen, LOGIN_SCREEN);
        cards.add(mainMenu, MAIN_MENU);
        cards.add(testWindow, TEST_WINDOW);
        cards.add(transactionScreen, TRANSACTION_SCREEN);
        cards.add(billSelectionScreen, BILLSELECTION_SCREEN);
        cards.add(receiptScreen, RECEIPT_SCREEN);

        pane.add(cards, BorderLayout.CENTER);
    }

    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("EVIL ATM GUI");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = graphics.getDefaultScreenDevice();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setUndecorated(true);

        // Create and set up the content pane.
        Main demo = new Main();
        demo.addComponentToPane(frame.getContentPane());
        demo.setCard(LOGIN_SCREEN);

        // Display the window.
        device.setFullScreenWindow(frame);
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
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
        } else if (evt.getActionCommand().toString().equalsIgnoreCase("*")) {
            setCard(TRANSACTION_SCREEN);
        } else {
            passwordField.setText(evt.getActionCommand().toString());
        }
    }

    public void customWithdrawalButtonActionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().toString().equalsIgnoreCase("#")) {
            finalCash = Integer.parseInt(cash);
            if (!(finalCash % 5 == 0)) return;
            setCard(BILLSELECTION_SCREEN);

        } else if (evt.getActionCommand().toString().equalsIgnoreCase("*")) {
            cash = "";
            cashField.setText(cash);
        } else if (cash.length() >= 4) {
            return;
        } else {
            cash += evt.getActionCommand().toString();
            cashField.setText(cash);
        }
    }

    public void calculateBills() {
        String bills = "You get " + e50 + " E50 bills, " + e20 + " E20 bills, " + e10 + " E10 bills, " + e5 + " E5 bills";
        result.setText(bills);
        moneyLeft.setText("Money left: " + finalCash);
    }
}