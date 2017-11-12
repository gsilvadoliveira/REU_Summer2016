package conTest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.jdt.annotation.Nullable;

public class MockUp extends JFrame
{
    private static final long serialVersionUID = -5322198287146821168L;

    public MockUp()
    {
        UIstart();
    }

    public MockUp(int i)
    {
        if (i == 0)
            UIstart();
        else if (i == 1)
            UIloadTests();
        else if (i == 2)
            UIwithProgressBars();
        else if (i == 3)
            UIfailed();
    }

    public final void UIstart()
    {

        JPanel panel = new JPanel();
        getContentPane().add(panel);

//       panel.setLayout(null);
        panel.setLayout(new BorderLayout());

        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(40, 80, 120, 30);
        quitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(@Nullable ActionEvent event)
            {
                System.exit(0);
            }
        });

        panel.add(quitButton, BorderLayout.PAGE_END);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Database");
        JMenuItem menuItem = new JMenuItem("Connect...");
        menu.add(menuItem);

        menuBar.add(menu);

        menu = new JMenu("Tests");
        menuItem = new JMenuItem("Load...");
        menu.add(menuItem);
        menuBar.add(menu);

        panel.add(menuBar, BorderLayout.PAGE_START);

        setTitle("ConTest");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public final void UIloadTests()
    {

        int startX = 20;
        int startY = 10;
        int width = 120;
        int height = 35;
        int stepX = 140;
        int stepY = 50;

        JPanel panel = new JPanel();
        getContentPane().add(panel);

//        panel.setLayout(null);
        panel.setLayout(new BorderLayout());

        JButton quitButton = new JButton("Pause Testing");
        quitButton.setBounds(40, 80, 120, 30);
        quitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(@Nullable ActionEvent event)
            {
                System.exit(0);
            }
        });

        panel.add(quitButton, BorderLayout.PAGE_END);

//        JMenuBar menuBar = new JMenuBar();
//        JMenu menu = new JMenu("Database");
//        JMenuItem menuItem = new JMenuItem("Connect...");
//        menu.add(menuItem);
//        
//        menuBar.add(menu);
//        
//        menu = new JMenu("Tests");
//        menuItem = new JMenuItem("Load...");
//        menu.add(menuItem);
//        menuBar.add(menu);
//        
//        panel.add(menuBar,BorderLayout.PAGE_START);

        JPanel testpanel = new JPanel();
        testpanel.setLayout(null);

        JTextField tf = new JTextField();

        tf.setText("Test1");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX, startY, width, height);
//        tf.setBackground(Color.green);

        testpanel.add(tf);

        tf = new JTextField();

        tf.setText("Test2");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX, startY + stepY, width, height);

        testpanel.add(tf);

        tf = new JTextField();
        tf.setText("Test3");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX, startY + 2 * stepY, width, height);

        testpanel.add(tf);

        tf.setToolTipText("testing...");

        tf = new JTextField();

        tf.setText("Test4");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX, startY + 3 * stepY, width, height);

        testpanel.add(tf);

        tf = new JTextField();

        tf.setText("Test5");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX + stepX, startY, width, height);

        testpanel.add(tf);

        tf = new JTextField();

        tf.setText("Test6");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX + stepX, startY + stepY, width, height);

        testpanel.add(tf);

        tf = new JTextField();
        tf.setText("Test7");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX + stepX, startY + 2 * stepY, width, height);

        testpanel.add(tf);

        tf.setToolTipText("testing...");


        tf = new JTextField();
        tf.setText("Test8");
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBounds(startX + stepX, startY + 3 * stepY, width, height);

        testpanel.add(tf);

        // Progress bar

        JPanel currentTest = new JPanel();
        currentTest.setLayout(null);
        currentTest.setBounds(startX + stepX + 3, startY + 3 * stepY + 25, width - 5, height - 5);

        JProgressBar p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(7);
        p.setString("Test8");
        p.setStringPainted(true);


        p.setBounds(0, 0, width - 5, height - 5);

        currentTest.add(p);

        testpanel.add(currentTest);


        panel.add(testpanel, BorderLayout.CENTER);

        setTitle("ConTest: testing...");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    public final void UIwithProgressBars()
    {

        int startX = 20;
        int startY = 10;
        int width = 120;
        int height = 35;
        int stepX = 140;
        int stepY = 30;

        JPanel panel = new JPanel();
        getContentPane().add(panel);

//        panel.setLayout(null);
        panel.setLayout(new BorderLayout());

        JButton quitButton = new JButton("Pause Testing");
        quitButton.setBounds(40, 80, 120, 30);
        quitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(@Nullable ActionEvent event)
            {
                System.exit(0);
            }
        });

        panel.add(quitButton, BorderLayout.PAGE_END);

        JPanel testpanel = new JPanel();
        testpanel.setLayout(null);

        JProgressBar p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(15);
        p.setString("Test1");
        p.setStringPainted(true);

        p.setBounds(startX, startY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(15);
        p.setString("Test2");
        p.setStringPainted(true);

        p.setBounds(startX, startY + stepY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(7);
        p.setString("Test3");
        p.setStringPainted(true);

        p.setBounds(startX, startY + 2 * stepY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test4");
        p.setStringPainted(true);

        p.setBounds(startX, startY + 3 * stepY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test5");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test6");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY + stepY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test7");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY + 2 * stepY, width, height);
        testpanel.add(p);


        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test8");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY + 3 * stepY, width, height);
        testpanel.add(p);


        panel.add(testpanel, BorderLayout.CENTER);

        setTitle("ConTest: testing...");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    public final void UIfailed()
    {

        int startX = 20;
        int startY = 10;
        int width = 120;
        int height = 35;
        int stepX = 140;
        int stepY = 30;

        JPanel panel = new JPanel();
        getContentPane().add(panel);

//    panel.setLayout(null);
        panel.setLayout(new BorderLayout());

        JButton quitButton = new JButton("Pause Testing");
        quitButton.setBounds(40, 80, 120, 30);
        quitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(@Nullable ActionEvent event)
            {
                System.exit(0);
            }
        });

        panel.add(quitButton, BorderLayout.PAGE_END);

        JPanel testpanel = new JPanel();
        testpanel.setLayout(null);

        JProgressBar p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(15);
        p.setString("Test1");
        p.setStringPainted(true);

        p.setBounds(startX, startY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(15);
        p.setString("Test2");
        p.setStringPainted(true);

        p.setBounds(startX, startY + stepY, width, height);
        testpanel.add(p);

        UIManager.put("ProgressBar.selectionForeground", Color.RED);
        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test3: failed");

        p.setStringPainted(true);

        p.setBounds(startX, startY + 2 * stepY, width, height);
        testpanel.add(p);

        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(3);
        p.setString("Test4");
        p.setStringPainted(true);

        p.setBounds(startX, startY + 3 * stepY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test5");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test6");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY + stepY, width, height);
        testpanel.add(p);

        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test7");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY + 2 * stepY, width, height);
        testpanel.add(p);


        p = new JProgressBar(0, 15);
        p.setBorderPainted(true);
        p.setValue(0);
        p.setString("Test8");
        p.setStringPainted(true);

        p.setBounds(startX + stepX, startY + 3 * stepY, width, height);
        testpanel.add(p);


        panel.add(testpanel, BorderLayout.CENTER);

        setTitle("ConTest: testing...");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                MockUp ex = new MockUp(3);
                ex.setVisible(true);
            }
        });
    }
}
