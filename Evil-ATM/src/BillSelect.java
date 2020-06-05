import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BillSelect {

    static private final int[] NOTE_VALUES = new int[]{50, 20, 10, 5};
    static private int noteAmounts[] = new int[]{0, 0, 0, 0};
    static private JLabel[] noteAmountText = new JLabel[4];


    private static void createAndShowGUI() {
        // Create and set up the window
        JFrame frame = new JFrame("EVIL ATM GUI");

        noteAmounts = new int[]{1, 5, 2, 1};

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = graphics.getDefaultScreenDevice();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setUndecorated(true);

        JPanel content = new JPanel();

        content.add(new JLabel("Note Test"));

        JTextField totalAmount = new JTextField(4);
        JButton compute = new JButton("calculate bills");

        compute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                computeBills(Integer.parseInt(totalAmount.getText()));
            }
        });

        JPanel selector = new JPanel();

        selector.setLayout(new GridLayout(4, 4));

        selector.add(new JLabel("E50"));
        selector.add(new JLabel("E20"));
        selector.add(new JLabel("E10"));
        selector.add(new JLabel("E5"));

        for (int i = 0; i < 4; i++) {
            int column = i;
            JButton button = new JButton("+");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    addNote(column);
                }
            });
            selector.add(button);
        }
        for (int i = 0; i < 4; i++) {
            noteAmountText[i] = new JLabel(String.valueOf(noteAmounts[i]));
            selector.add(noteAmountText[i]);
        }
        for (int i = 0; i < 4; i++) {
            int column = i;
            JButton button = new JButton("-");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    subNote(column);
                }
            });
            selector.add(button);
        }

        content.add(totalAmount);
        content.add(compute);
        content.add(selector);

        frame.add(content);

        // Display the window.
        device.setFullScreenWindow(frame);
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.

        /*
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        */

        DatabaseInterfacer d = new DatabaseInterfacer("145.24.222.190", 665);
        d.requestTransaction("US-EVIL-00000001", "0420", "2000");
    }

    public static void computeBills(int amount) {
        noteAmounts = new int[]{0, 0, 0, 0};
        for (; amount > 49; amount -= 50) {
            noteAmounts[0]++;
        }
        for (; amount > 19; amount -= 20) {
            noteAmounts[1]++;
        }
        for (; amount > 9; amount -= 10) {
            noteAmounts[2]++;
        }
        for (; amount > 4; amount -= 5) {
            noteAmounts[3]++;
        }
        updateNoteText();
    }

    public static void addNote(int column) {
        int total = 0;
        int amount = NOTE_VALUES[column];
        for (int i = column + 1; i < 4; i++) {
            total += noteAmounts[i] * NOTE_VALUES[i];
        }
        System.out.println(total);
        if (total >= NOTE_VALUES[column]) {
            noteAmounts[column]++;
            for (int i = column + 1; i < 4; i++) {
                System.out.println(noteAmounts[i]);
                while (noteAmounts[i] > 0) {
                    if (amount - NOTE_VALUES[i] >= 0) {
                        noteAmounts[i]--;
                        amount -= NOTE_VALUES[i];
                    } else {
                        break;
                    }
                }
                System.out.println(i + ": " + amount);
            }
            System.out.println("");
            updateNoteText();
        }
    }

    public static void subNote(int column) {
        if (noteAmounts[column] > 0) {
            if (column == 0) {
                noteAmounts[0]--;
                noteAmounts[1] += 2;
                noteAmounts[2]++;
            } else if (column == 3) {
                addNote(2);
            } else {
                noteAmounts[column]--;
                noteAmounts[column + 1] += 2;
            }
            updateNoteText();
        }
    }

    public static void updateNoteText() {
        for (int i = 0; i < 4; i++) {
            noteAmountText[i].setText(String.valueOf(noteAmounts[i]));
        }
    }
}
