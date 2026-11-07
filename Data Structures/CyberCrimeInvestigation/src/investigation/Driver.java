package investigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension; 
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Driver {

    private Timer actionTimer; // Java swing timer. we use a singular timer to only allow one action at a time (in case of infinte loops)

    private static final int TIMEOUT = 10 * 1000; // 10 seconds
    private static final String path = "";

    private static final int WIDTH = 650;
    private static final int HEIGHT = 500;

    private CyberCrimeInvestigation cyberCrimeInvestigation;

    private JFrame display;
    private JPanel mainPanel;
    private HackerDatabase hackerPanel;

    // "Merge Hackers" combo boxes
    private JComboBox<String> hacker1;
    private JComboBox<String> hacker2;

    private JComboBox<String> locationBox;

    public Driver() {
        display = new JFrame(); 
        display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout()); 
 
        runStudentCode(() -> {
            cyberCrimeInvestigation = new CyberCrimeInvestigation();
        });
 
        JPanel inputPanel = createInputPanel();
        hackerPanel = new HackerDatabase();
        hackerPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(hackerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        display.add(mainPanel);  
        display.pack();
        display.setVisible(true);
    }
 
    public void refreshDriver() { 
        hacker1.removeAllItems();
        hacker2.removeAllItems();
        for (HNode node : cyberCrimeInvestigation.getHackerDirectory()) {
            while (node != null) {
                if (node.getHacker() != null) {
                    hacker1.addItem(node.getHacker().getName());
                    hacker2.addItem(node.getHacker().getName());
                }
                node = node.getNext();
            }
        }
 
        locationBox.removeAllItems(); 
        for (String s : getLocations(cyberCrimeInvestigation.getHackerDirectory())) {
            locationBox.addItem(s);
        }

        hackerPanel.updatePanel();
        hackerPanel.revalidate();
        hackerPanel.repaint();
        display.revalidate();
        display.repaint();
    }

    private JPanel createButtonPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.BLACK);
  
               
        
        JPanel mergePanel = new JPanel(); 
        mergePanel.setBackground(Color.BLACK);
        mergePanel.setLayout(new BoxLayout(mergePanel, BoxLayout.X_AXIS));
        hacker1 = new JComboBox<>();
        hacker2 = new JComboBox<>();
        for (HNode h : cyberCrimeInvestigation.getHackerDirectory()) {
            while (h != null) {
                if (h.getHacker() != null) {
                    hacker1.addItem(h.getHacker().getName());
                    hacker2.addItem(h.getHacker().getName());
                }
                h = h.getNext();
            }
        }
        JButton mergeButton = new JButton("Merge Hackers");
        mergeButton.setBackground(Color.BLACK);
        mergeButton.setForeground(Color.GREEN);
        mergeButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        mergeButton.setBorder(new LineBorder(Color.RED, 2, true));
        mergeButton.addActionListener(e -> {
            runStudentCode(() -> {
                if (hacker1.getSelectedItem() == null || hacker2.getSelectedItem() == null) { 
                    JOptionPane.showMessageDialog(null, "Select both hackers to merge!");
                    return;
                }
                if (((String)(hacker1.getSelectedItem())).equals((String)hacker2.getSelectedItem())) {
                    JOptionPane.showMessageDialog(null, "Cannot merge a hacker into themselves!");
                    return;
                } 
                boolean success = cyberCrimeInvestigation.mergeHackers((String)hacker1.getSelectedItem(),(String)hacker2.getSelectedItem());
                if (!success) { 
                    JOptionPane.showMessageDialog(null, "mergeHackers() returned false. Make sure you implement this method before clicking, and make sure you return true if successful.");
                    return;
                }
                else if (success && Driver.contains(cyberCrimeInvestigation.getHackerDirectory(), (String)hacker1.getSelectedItem())) {
                    hacker1.removeItem((String)hacker2.getSelectedItem());
                    hacker2.removeItem((String)hacker2.getSelectedItem());
                } else if (success && Driver.contains(cyberCrimeInvestigation.getHackerDirectory(), (String)hacker2.getSelectedItem())) {
                    hacker1.removeItem((String)hacker1.getSelectedItem());
                    hacker2.removeItem((String)hacker1.getSelectedItem());
                }
                refreshDriver();
            });
        });
        mergePanel.add(hacker1);
        mergePanel.add(hacker2);
        mergePanel.add(mergeButton);

        JPanel mostWantedPanel = new JPanel();
        mostWantedPanel.setLayout(new BoxLayout(mostWantedPanel, BoxLayout.X_AXIS));
        JSpinner mostWantedNum = new JSpinner(); 
        mostWantedNum.setModel(new SpinnerNumberModel(10, 10, 50, 1));
        JButton mostWantedButton = new JButton("*** View Most Wanted List ***"); 
        mostWantedButton.addActionListener(e -> {
            JFrame display = new JFrame("[CLASSIFIED] Most Wanted"); 
            
            display.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            display.setSize(400, 500);
            display.setLayout(new BorderLayout());
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.BLACK);
            mainPanel.setBorder(new LineBorder(Color.RED, 3, true));
            
            int val = (int)mostWantedNum.getValue();
            JLabel title = new JLabel(String.format("TOP %d MOST WANTED", val), SwingConstants.CENTER);
            title.setForeground(Color.RED);
            title.setFont(new Font("Monospaced", Font.BOLD, 24));
            mainPanel.add(title, BorderLayout.NORTH);

            StringBuilder sb = new StringBuilder(); 
            int index = 1;
            ArrayList<Hacker> arr = cyberCrimeInvestigation.getNMostWanted(val);
            if (arr != null) {
                for (Hacker h : arr) {
                    sb.append("Number " + index++ + ": ");
                    sb.append(h.toString() + "\n\n");
                }
            } 
            
            JTextArea incidentArea = new JTextArea();
            incidentArea.setEditable(false);
            incidentArea.setBackground(Color.BLACK);
            incidentArea.setForeground(Color.GREEN);
            incidentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            incidentArea.setLineWrap(true);
            incidentArea.setWrapStyleWord(true); 
            incidentArea.setText(sb.toString());
            
            JScrollPane scrollPane = new JScrollPane(incidentArea);
            scrollPane.setPreferredSize(new Dimension(580, 375));
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            
            display.add(mainPanel);
            display.setVisible(true);
        });
   
        mostWantedPanel.add(mostWantedNum);
        mostWantedPanel.add(mostWantedButton); 


        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));
        locationBox = new JComboBox<>();
        for (String s : getLocations(cyberCrimeInvestigation.getHackerDirectory())) {
            locationBox.addItem(s);
        }

        JButton locationButton = new JButton("Sort By Location");  
        locationButton.addActionListener(e -> {
            String location = (String)locationBox.getSelectedItem();
            if (location == null) {
                JOptionPane.showMessageDialog(null, "Select a location!");
                return;
            }
            JFrame display = new JFrame("[CLASSIFIED] Hackers by Location "); 
            
            display.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            display.setSize(400, 500);
            display.setLayout(new BorderLayout());
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.BLACK);
            mainPanel.setBorder(new LineBorder(Color.RED, 3, true));
             
            JLabel title = new JLabel("Location: " + location, SwingConstants.CENTER);
            title.setForeground(Color.RED);
            title.setFont(new Font("Monospaced", Font.BOLD, 24));
            mainPanel.add(title, BorderLayout.NORTH);

            StringBuilder sb = new StringBuilder();  
            ArrayList<Hacker> arr = cyberCrimeInvestigation.getHackersByLocation(location);
            if (arr != null) {
                for (Hacker h : arr) { 
                    sb.append("Name: " + h.getName() + "\n");
                    sb.append("Aliases: " + h.getAliases().toString() + "\n"); 
                    sb.append("Num Incidents: " + h.numIncidents() + "\n\n"); 
                }
            }
            
            JTextArea incidentArea = new JTextArea();
            incidentArea.setEditable(false);
            incidentArea.setBackground(Color.BLACK);
            incidentArea.setForeground(Color.GREEN);
            incidentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            incidentArea.setLineWrap(true);
            incidentArea.setWrapStyleWord(true); 
            incidentArea.setText(sb.toString());
            
            JScrollPane scrollPane = new JScrollPane(incidentArea);
            scrollPane.setPreferredSize(new Dimension(580, 375));
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            
            display.add(mainPanel);
            display.setVisible(true);
        });

        locationPanel.add(locationBox);
        locationPanel.add(locationButton);
   
        inputPanel.add(mergePanel);
        inputPanel.add(locationPanel);
        inputPanel.add(mostWantedPanel);
        return inputPanel;
    }

    private static boolean contains(HNode[] ht, String name) {
        HNode node = ht[Math.abs(name.hashCode())%ht.length];
        while (node != null) {
            if (node.getHacker() != null && node.getHacker().getName().equals(name)) { 
                return true;
            }
            node = node.getNext();
        } 
        return false;
    }

    private ArrayList<String> getLocations(HNode[] ht) {
        ArrayList<String> locs = new ArrayList<>();
        for (HNode h : ht) {
            while (h != null) {
                if (h.getHacker() != null) {
                    for (Incident i : h.getHacker().getIncidents()) {
                        if (!locs.contains(i.getLocation())) {
                            locs.add(i.getLocation());
                        }
                    }
                }
                h = h.getNext();
            }
        }
        Collections.sort(locs);
        return locs;
    }

    private JPanel createInputPanel() { 
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.BLACK);
 
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"hacker1.in", "hacker2.in", "hacker3.in", "hacker4.in", "hackerTest.in"}); 
        comboBox.setSelectedIndex(0);

        JButton button = new JButton("Read Input File"); 
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                runStudentCode(() -> {
                    try {
                        cyberCrimeInvestigation.initializeTable(path + (String)comboBox.getSelectedItem());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Make sure you complete the first two methods before clicking Read File.\nError reading file: " + ex.getMessage());
                        return;
                    }
                    hackerPanel.removeAll();   
                    comboBox.removeItemAt(comboBox.getSelectedIndex());
                    int i = 0;
                    for (HNode node : cyberCrimeInvestigation.getHackerDirectory()) {
                        ((HackerDatabase)hackerPanel).addIndex(i++);
                        while (node != null) {
                            Hacker hacker = node.getHacker();
                            ((HackerDatabase)hackerPanel).addHacker(hacker);
                            hacker1.addItem(hacker.getName());
                            hacker2.addItem(hacker.getName());
                            node = node.getNext();
                        }
                    }  
                    refreshDriver(); 
                }); 
            }
        });
  
        inputPanel.add(button);
        inputPanel.add(comboBox);
        return inputPanel;
    }
 

    private void runStudentCode(Runnable task) {
        this.runStudentCode(Executors.callable(task));
    }

    private <T> void runStudentCode(Callable<T> task) {  
        try {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        task.call();
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() ->  e.printStackTrace());
                    }
                    this.done();
                    actionTimer = null;
                    return null;
                } 
            }; 
            if (actionTimer != null && actionTimer.isRunning()) { 
                JOptionPane.showMessageDialog(null, "Code is still running, stopping it may interrupt the Driver or cause and error. If the code does not terminate, you may have an infinite loop, and should restart the Driver or debug your code.");
                return;
            }
            actionTimer = new Timer(TIMEOUT, e -> {
                if (!worker.isDone()) {
                    worker.cancel(true);
                    actionTimer = null;
                    int choice = JOptionPane.showConfirmDialog( null, 
                        "Solution code is taking more time than expected -- if you're not using the debugger right now, there may be an infinite loop.\nWould you like to close the Driver?", 
                        "Warning", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        display.dispose(); 
                        System.exit(0);
                    }
                }
            });
            actionTimer.start();
            worker.execute(); 
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(null, "Make sure you have completed methods before pressing their buttons. \nError: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Driver());
    }


    private class HackerDatabase extends JPanel {
        private JScrollPane scrollPanel;
        private JPanel hackersPanel;
    
        public HackerDatabase() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBackground(Color.GRAY);
            
            scrollPanel = new JScrollPane();
            hackersPanel = new JPanel();
            hackersPanel.setLayout(new BoxLayout(hackersPanel, BoxLayout.Y_AXIS));
            hackersPanel.setBackground(Color.BLACK);
            scrollPanel.setViewportView(hackersPanel);
            scrollPanel.setBorder(new LineBorder(Color.RED, 3, true));
            
            this.add(scrollPanel);
        } 

        @Override
        public void removeAll() {
            hackersPanel.removeAll();
        }
    
        public void addHacker(Hacker h) {
            for (Component component : hackersPanel.getComponents()) {
                if (component instanceof HackerPanel) {
                    HackerPanel panel = (HackerPanel) component;
                    if (panel.hacker != null && panel.hacker.equals(h)) {
                        panel.updatePanel();
                        return;
                    }
                }
            }
            
            HackerPanel hackerPanel = new HackerPanel(h);
            hackersPanel.add(hackerPanel);
        }

        public void addIndex(int i) {
            for (Component component : hackersPanel.getComponents()) {
                if (component instanceof HackerPanel) {
                    HackerPanel panel = (HackerPanel) component;
                    if (panel.hacker == null) {
                        continue;
                    }
                }
            }
            
            HackerPanel hackerPanel = new HackerPanel(i);
            hackersPanel.add(hackerPanel);
        }
        
        public void updatePanel() {
            for (Component component : hackersPanel.getComponents()) {
                if (component instanceof HackerPanel) {
                    HackerPanel panel = (HackerPanel) component;
                    if (panel.hacker != null && !Driver.contains(cyberCrimeInvestigation.getHackerDirectory(), panel.hacker.getName())) {    
                        hackersPanel.remove(component);  
                    } else {
                        panel.updatePanel();
                    }
                }
            }
            this.revalidate();
            this.repaint();
        }
    }
    
    private class HackerPanel extends JPanel {
        private Hacker hacker;
        private JLabel hackerInfo;
        private JButton incidentButton;
        private JButton deleteButton;

        private ArrayList<IncidentPanel> incidentPanels = new ArrayList<>();
    
        public HackerPanel(Hacker hacker) {
            super();
            this.hacker = hacker;
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBackground(Color.BLACK);
            this.setBorder(new LineBorder(Color.RED, 2, true));
            
            hackerInfo = new JLabel(hackerTitle());
            hackerInfo.setForeground(Color.GREEN);
            hackerInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            incidentButton = new JButton("View Incidents");
            incidentButton.setBackground(Color.BLACK);
            incidentButton.setForeground(Color.GREEN);
            incidentButton.setFont(new Font("Monospaced", Font.BOLD, 12));
            incidentButton.setBorder(new LineBorder(Color.RED, 2, true));
            incidentButton.addActionListener(e -> {
                incidentPanels.add(new IncidentPanel(hacker));
            });

            deleteButton = new JButton("Delete File");
            deleteButton.setBackground(Color.BLACK);
            deleteButton.setForeground(Color.GREEN);
            deleteButton.setFont(new Font("Monospaced", Font.BOLD, 12));
            deleteButton.setBorder(new LineBorder(Color.RED, 2, true)); 
            deleteButton.addActionListener(e -> {
                cyberCrimeInvestigation.remove(hacker.getName());
                refreshDriver();
            });
            
            this.add(hackerInfo);
            this.add(incidentButton);
            this.add(deleteButton);
        }

        public HackerPanel(int index) {
            super();
            this.hacker = null;
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBackground(Color.DARK_GRAY);
            this.setBorder(new LineBorder(Color.RED, 2, true));
            
            hackerInfo = new JLabel("<html><br/>Index " + index + "<br/></html>");
            hackerInfo.setForeground(Color.GREEN);
            hackerInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
            this.add(hackerInfo);
        }
    
        private String hackerTitle() {
            return "<html><br/>Name: " + hacker.getName() + "</html>";
        }
        
        public void updatePanel() {
            if (this.hacker == null) return; // don't update panel if it represents index
            hackerInfo.setText(hackerTitle());
            for (IncidentPanel panel : incidentPanels) {
                if (panel != null && panel.isVisible()) {
                    panel.updatePanel();
                } else {
                    incidentPanels.remove(panel);
                }
            } 
        }
    }
    

    private class IncidentPanel extends JFrame {
        private Hacker hacker;
        private JTextArea hackerInfo = new JTextArea();
        private JTextArea incidentArea = new JTextArea();

        public void updatePanel() {
            hackerInfo.setText("Suspect: " + hacker.getName() + "\n" +
            "Aliases: " + hacker.getAliases().toString() + "\n" +
            "Incidents: " + hacker.numIncidents());


            ArrayList<Incident> incidents = hacker.getIncidents();
            incidentArea.setText("");
            for (Incident inc : incidents) {
                incidentArea.append("- Date: " + inc.getDate() + "\n" +
                                    "  Location: " + inc.getLocation() + "\n" +
                                    "  IP Hash: " + inc.getIPHash() + "\n" +
                                    "  OS: " + inc.getOS() + "\n" +
                                    "  Web Server: " + inc.getWebServer() + "\n" +
                                    "  URL Hash: " + inc.getURLHash() + "\n\n");
            }

            this.revalidate();
            this.repaint();
        }
    
        public IncidentPanel(Hacker h) {
            super("[CLASSIFIED] Incident Report");
            this.hacker = h;
            
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setSize(400, 500);
            this.setLayout(new BorderLayout());
             
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.BLACK);
            mainPanel.setBorder(new LineBorder(Color.RED, 3, true));
             
            JLabel title = new JLabel("TOP SECRET", SwingConstants.CENTER);
            title.setForeground(Color.RED);
            title.setFont(new Font("Monospaced", Font.BOLD, 24));
            mainPanel.add(title, BorderLayout.NORTH);
              
            hackerInfo.setEditable(false);
            hackerInfo.setBackground(Color.BLACK);
            hackerInfo.setForeground(Color.GREEN);
            hackerInfo.setFont(new Font("Monospaced", Font.PLAIN, 14));
            hackerInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
            mainPanel.add(hackerInfo, BorderLayout.CENTER);
             
            incidentArea.setEditable(false);
            incidentArea.setBackground(Color.BLACK);
            incidentArea.setForeground(Color.GREEN);
            incidentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            incidentArea.setLineWrap(true);
            incidentArea.setWrapStyleWord(true);
             
            
            JScrollPane scrollPane = new JScrollPane(incidentArea);
            scrollPane.setPreferredSize(new Dimension(580, 325));
            mainPanel.add(scrollPane, BorderLayout.SOUTH);
            
            updatePanel();

            this.add(mainPanel);
            this.setVisible(true);
        }
    }
    
}