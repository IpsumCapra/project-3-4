import com.pi4j.io.gpio.*;

import javax.swing.*;
import java.awt.*;

public class Test extends Thread{
    static JLabel l;

    public Test(JLabel l) {
        this.l = l;
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

    private static void createAndShowGUI() {
        // Create and set up the window.
        String uid = "";
        char key = 'x';

        JFrame frame = new JFrame("EVIL ATM GUI");
        l = new JLabel("FUCK");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = graphics.getDefaultScreenDevice();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setUndecorated(true);

        Container pane = frame.getContentPane();
        pane.add(l, BorderLayout.CENTER);

        //KeypadListener t = new KeypadListener(l);

        // Display the window.
        device.setFullScreenWindow(frame);
        //t.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            l.setText("asses");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            l.setText("butts");
        }
    }
}
