package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class ScheduleBlock {
    int startTime;
    int endTime;
    int processID; // -1 for idle
    
    ScheduleBlock(int startTime, int endTime, int processID) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.processID = processID;
    }
}

class Process {
    int processID, arrivalTime, burstTime, priority;
    int waitingTime = 0, turnAroundTime = 0, responseTime = -1, completeTime = 0;

    Process(int processID, int arrivalTime, int burstTime, int priority) {
        this.processID = processID;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }
}

public class SchedulerGUI extends JFrame {

    private JTextField arrivalField, burstField, priorityField;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private ArrayList<Process> processes = new ArrayList<>();
    private int processCounter = 1;
    private List<ScheduleBlock> prioritySchedule = new ArrayList<>();
    private List<ScheduleBlock> srtfSchedule = new ArrayList<>();
    private JPanel ganttPanel;
    private DefaultTableModel priorityResultsModel;
    private DefaultTableModel srtfResultsModel;
    private JLabel priorityAvgWTLabel;
    private JLabel priorityAvgTATLabel;
    private JLabel priorityAvgRTLabel;
    private JLabel srtfAvgWTLabel;
    private JLabel srtfAvgTATLabel;
    private JLabel srtfAvgRTLabel;

    public SchedulerGUI() {
        setTitle("Priority vs SRTF Scheduling");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createInputPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createOutputPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Input Panel"));

        arrivalField = new JTextField();
        burstField = new JTextField();
        priorityField = new JTextField();

        JButton addBtn = new JButton("Add Process");
        JButton runBtn = new JButton("Run Simulation");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(this::addProcess);
        runBtn.addActionListener(this::runSimulation);
        clearBtn.addActionListener(e -> clearAll());

        panel.add(new JLabel("Arrival Time"));
        panel.add(new JLabel("Burst Time"));
        panel.add(new JLabel("Priority"));
        panel.add(new JLabel("Actions"));

        panel.add(arrivalField);
        panel.add(burstField);
        panel.add(priorityField);

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(runBtn);
        btnPanel.add(clearBtn);
        panel.add(btnPanel);

        return panel;
    }

    private JScrollPane createTablePanel() {
        tableModel = new DefaultTableModel(
                new String[]{"PID", "Arrival Time", "Burst Time", "Priority"}, 0);
        processTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(processTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Process Table"));
        return scrollPane;
    }

    private JTabbedPane createOutputPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Results tab with structured layout
        JPanel resultsPanel = createResultsPanel();
        tabbedPane.addTab("Results", resultsPanel);

        // Gantt Chart tab
        ganttPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGantt(g);
            }
        };
        ganttPanel.setPreferredSize(new Dimension(800, 200));
        JScrollPane chartScroll = new JScrollPane(ganttPanel);
        tabbedPane.addTab("Gantt Chart", chartScroll);

        return tabbedPane;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());
        
        JTabbedPane resultsTabs = new JTabbedPane();
        
        // Priority Results Tab
        JPanel priorityPanel = new JPanel(new BorderLayout());
        priorityResultsModel = new DefaultTableModel(
                new String[]{"Process ID", "Waiting Time", "Turnaround Time", "Response Time"}, 0);
        JTable priorityTable = new JTable(priorityResultsModel);
        priorityTable.setEnabled(false);
        JScrollPane priorityScroll = new JScrollPane(priorityTable);
        
        JPanel priorityAvgPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        priorityAvgPanel.setBorder(BorderFactory.createTitledBorder("Averages"));
        priorityAvgWTLabel = new JLabel("Average WT: 0.00");
        priorityAvgTATLabel = new JLabel("Average TAT: 0.00");
        priorityAvgRTLabel = new JLabel("Average RT: 0.00");
        priorityAvgPanel.add(priorityAvgWTLabel);
        priorityAvgPanel.add(priorityAvgTATLabel);
        priorityAvgPanel.add(priorityAvgRTLabel);
        
        priorityPanel.add(priorityScroll, BorderLayout.CENTER);
        priorityPanel.add(priorityAvgPanel, BorderLayout.SOUTH);
        
        // SRTF Results Tab
        JPanel srtfPanel = new JPanel(new BorderLayout());
        srtfResultsModel = new DefaultTableModel(
                new String[]{"Process ID", "Waiting Time", "Turnaround Time", "Response Time"}, 0);
        JTable srtfTable = new JTable(srtfResultsModel);
        srtfTable.setEnabled(false);
        JScrollPane srtfScroll = new JScrollPane(srtfTable);
        
        JPanel srtfAvgPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        srtfAvgPanel.setBorder(BorderFactory.createTitledBorder("Averages"));
        srtfAvgWTLabel = new JLabel("Average WT: 0.00");
        srtfAvgTATLabel = new JLabel("Average TAT: 0.00");
        srtfAvgRTLabel = new JLabel("Average RT: 0.00");
        srtfAvgPanel.add(srtfAvgWTLabel);
        srtfAvgPanel.add(srtfAvgTATLabel);
        srtfAvgPanel.add(srtfAvgRTLabel);
        
        srtfPanel.add(srtfScroll, BorderLayout.CENTER);
        srtfPanel.add(srtfAvgPanel, BorderLayout.SOUTH);
        
        resultsTabs.addTab("Priority Scheduling", priorityPanel);
        resultsTabs.addTab("SRTF Scheduling", srtfPanel);
        
        resultsPanel.add(resultsTabs, BorderLayout.CENTER);
        return resultsPanel;
    }

    private void addProcess(ActionEvent e) {
        try {
            int at = Integer.parseInt(arrivalField.getText());
            int bt = Integer.parseInt(burstField.getText());
            int pr = Integer.parseInt(priorityField.getText());

            if (at < 0 || bt <= 0 || pr < 0) {
                JOptionPane.showMessageDialog(this, "Invalid input values");
                return;
            }

            Process p = new Process(processCounter, at, bt, pr);
            processes.add(p);

            tableModel.addRow(new Object[]{
                    processCounter, at, bt, pr
            });

            processCounter++;
            arrivalField.setText("");
            burstField.setText("");
            priorityField.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers");
        }
    }

    private void runSimulation(ActionEvent e) {
        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add processes first");
            return;
        }

        // Create copies for each scheduling
        ArrayList<Process> priorityProcesses = new ArrayList<>();
        ArrayList<Process> srtfProcesses = new ArrayList<>();
        for (Process p : processes) {
            priorityProcesses.add(new Process(p.processID, p.arrivalTime, p.burstTime, p.priority));
            srtfProcesses.add(new Process(p.processID, p.arrivalTime, p.burstTime, p.priority));
        }

        // Priority Scheduling
        priorityScheduling(priorityProcesses);
        updateResultsTable(priorityProcesses, priorityResultsModel, 
                          priorityAvgWTLabel, priorityAvgTATLabel, priorityAvgRTLabel);

        // SRTF Scheduling
        srtfScheduling(srtfProcesses);
        updateResultsTable(srtfProcesses, srtfResultsModel,
                          srtfAvgWTLabel, srtfAvgTATLabel, srtfAvgRTLabel);

        ganttPanel.repaint();
    }

    private void updateResultsTable(ArrayList<Process> processes, DefaultTableModel tableModel,
                                     JLabel avgWTLabel, JLabel avgTATLabel, JLabel avgRTLabel) {
        // Clear table
        tableModel.setRowCount(0);
        
        double totalWT = 0, totalTAT = 0, totalRT = 0;
        
        // Add rows
        for (Process p : processes) {
            tableModel.addRow(new Object[]{
                    "P" + p.processID,
                    p.waitingTime,
                    p.turnAroundTime,
                    p.responseTime
            });
            totalWT += p.waitingTime;
            totalTAT += p.turnAroundTime;
            totalRT += p.responseTime;
        }
        
        // Update average labels
        double avgWT = totalWT / processes.size();
        double avgTAT = totalTAT / processes.size();
        double avgRT = totalRT / processes.size();
        
        avgWTLabel.setText(String.format("Average WT: %.2f", avgWT));
        avgTATLabel.setText(String.format("Average TAT: %.2f", avgTAT));
        avgRTLabel.setText(String.format("Average RT: %.2f", avgRT));
    }

    private void priorityScheduling(ArrayList<Process> processes) {
        int n = processes.size();
        int[] remaining = new int[n];
        int completed = 0, time = 0;
        prioritySchedule.clear();

        for (int i = 0; i < n; i++) {
            remaining[i] = processes.get(i).burstTime;
        }

        // Find minimum arrival time
        int minArrival = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            minArrival = Math.min(minArrival, processes.get(i).arrivalTime);
        }

        // Add idle time before first process arrives
        if (minArrival > 0) {
            prioritySchedule.add(new ScheduleBlock(0, minArrival, -1));
            time = minArrival;
        }

        int currentProcessID = -1; // -1 means no process currently executing
        int processStartTime = time;

        while (completed < n) {
            int index = -1;
            int bestPriority = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (processes.get(i).arrivalTime <= time && remaining[i] > 0) {
                    if (processes.get(i).priority < bestPriority) {
                        bestPriority = processes.get(i).priority;
                        index = i;
                    }
                }
            }

            if (index == -1) {
                // No process ready, finalize current if any
                if (currentProcessID >= 0) {
                    prioritySchedule.add(new ScheduleBlock(processStartTime, time, currentProcessID));
                    currentProcessID = -1;
                }

                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (remaining[i] > 0) {
                        nextArrival = Math.min(nextArrival, processes.get(i).arrivalTime);
                    }
                }
                if (nextArrival != Integer.MAX_VALUE && nextArrival > time) {
                    prioritySchedule.add(new ScheduleBlock(time, nextArrival, -1));
                    time = nextArrival;
                    processStartTime = nextArrival;
                }
                continue;
            }

            // Check if switching processes
            if (index != currentProcessID) {
                if (currentProcessID >= 0) {
                    prioritySchedule.add(new ScheduleBlock(processStartTime, time, currentProcessID));
                }
                currentProcessID = index;
                processStartTime = time;
            }

            if (processes.get(index).responseTime == -1) {
                processes.get(index).responseTime = time - processes.get(index).arrivalTime;
            }

            remaining[index]--;
            time++;

            if (remaining[index] == 0) {
                completed++;
                processes.get(index).completeTime = time;
                processes.get(index).turnAroundTime = processes.get(index).completeTime - processes.get(index).arrivalTime;
                processes.get(index).waitingTime = processes.get(index).turnAroundTime - processes.get(index).burstTime;

                if (processes.get(index).waitingTime < 0) {
                    processes.get(index).waitingTime = 0;
                }
            }
        }

        // Finalize last process
        if (currentProcessID >= 0) {
            prioritySchedule.add(new ScheduleBlock(processStartTime, time, currentProcessID));
        }
    }

    private void srtfScheduling(ArrayList<Process> processes) {
        int n = processes.size();
        int[] remaining = new int[n];
        int completed = 0, time = 0;
        srtfSchedule.clear();

        for (int i = 0; i < n; i++) {
            remaining[i] = processes.get(i).burstTime;
        }

        // Find minimum arrival time
        int minArrival = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            minArrival = Math.min(minArrival, processes.get(i).arrivalTime);
        }

        // Add idle time before first process arrives
        if (minArrival > 0) {
            srtfSchedule.add(new ScheduleBlock(0, minArrival, -1));
            time = minArrival;
        }

        int currentProcessID = -1; // -1 means no process currently executing
        int processStartTime = time;

        while (completed < n) {
            int index = -1;
            int min = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (processes.get(i).arrivalTime <= time && remaining[i] > 0 && remaining[i] < min) {
                    min = remaining[i];
                    index = i;
                }
            }

            if (index == -1) {
                // No process ready, finalize current if any
                if (currentProcessID >= 0) {
                    srtfSchedule.add(new ScheduleBlock(processStartTime, time, currentProcessID));
                    currentProcessID = -1;
                }

                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (remaining[i] > 0) {
                        nextArrival = Math.min(nextArrival, processes.get(i).arrivalTime);
                    }
                }
                if (nextArrival != Integer.MAX_VALUE && nextArrival > time) {
                    srtfSchedule.add(new ScheduleBlock(time, nextArrival, -1));
                    time = nextArrival;
                    processStartTime = nextArrival;
                }
                continue;
            }

            // Check if switching processes
            if (index != currentProcessID) {
                if (currentProcessID >= 0) {
                    srtfSchedule.add(new ScheduleBlock(processStartTime, time, currentProcessID));
                }
                currentProcessID = index;
                processStartTime = time;
            }

            if (processes.get(index).responseTime == -1) {
                processes.get(index).responseTime = time - processes.get(index).arrivalTime;
            }

            remaining[index]--;
            time++;

            if (remaining[index] == 0) {
                completed++;
                processes.get(index).completeTime = time;
                processes.get(index).turnAroundTime = processes.get(index).completeTime - processes.get(index).arrivalTime;
                processes.get(index).waitingTime = processes.get(index).turnAroundTime - processes.get(index).burstTime;

                if (processes.get(index).waitingTime < 0) {
                    processes.get(index).waitingTime = 0;
                }
            }
        }

        // Finalize last process
        if (currentProcessID >= 0) {
            srtfSchedule.add(new ScheduleBlock(processStartTime, time, currentProcessID));
        }
    }

    private void drawGantt(Graphics g) {
        int width = ganttPanel.getWidth();
        int barHeight = 30;
        int y = 50;

        // Priority
        g.setColor(Color.BLACK);
        g.drawString("Priority Scheduling", 10, 20);
        drawSchedule(g, prioritySchedule, 10, y, width / 2 - 20, barHeight);

        // SRTF
        g.drawString("SRTF Scheduling", width / 2 + 10, 20);
        drawSchedule(g, srtfSchedule, width / 2 + 10, y, width / 2 - 20, barHeight);
    }

    private void drawSchedule(Graphics g, List<ScheduleBlock> schedule, int x, int y, int maxWidth, int barHeight) {
        if (schedule.isEmpty()) return;

        int scale = 20; // pixels per time unit
        Map<Integer, Color> colorMap = new HashMap<>();
        int colorIndex = 0;
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PINK};

        int maxTime = 0;

        // Draw blocks
        for (ScheduleBlock block : schedule) {
            int blockWidth = (block.endTime - block.startTime) * scale;
            int blockX = x + block.startTime * scale;

            if (block.processID == -1) {
                // Idle
                g.setColor(new Color(200, 200, 200));
                g.fillRect(blockX, y, blockWidth, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(blockX, y, blockWidth, barHeight);
                g.drawString("Idle", blockX + 5, y + barHeight / 2 + 5);
            } else {
                // Process
                if (!colorMap.containsKey(block.processID)) {
                    colorMap.put(block.processID, colors[colorIndex % colors.length]);
                    colorIndex++;
                }
                g.setColor(colorMap.get(block.processID));
                g.fillRect(blockX, y, blockWidth, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(blockX, y, blockWidth, barHeight);
                g.drawString("P" + block.processID, blockX + 5, y + barHeight / 2 + 5);
            }

            maxTime = Math.max(maxTime, block.endTime);
        }

        // Draw time axis
        g.setColor(Color.BLACK);
        for (int t = 0; t <= maxTime; t++) {
            int tx = x + t * scale;
            g.drawLine(tx, y + barHeight, tx, y + barHeight + 10);
            g.drawString(String.valueOf(t), tx - 5, y + barHeight + 25);
        }
    }

    private void clearAll() {
        processes.clear();
        tableModel.setRowCount(0);
        priorityResultsModel.setRowCount(0);
        srtfResultsModel.setRowCount(0);
        priorityAvgWTLabel.setText("Average WT: 0.00");
        priorityAvgTATLabel.setText("Average TAT: 0.00");
        priorityAvgRTLabel.setText("Average RT: 0.00");
        srtfAvgWTLabel.setText("Average WT: 0.00");
        srtfAvgTATLabel.setText("Average TAT: 0.00");
        srtfAvgRTLabel.setText("Average RT: 0.00");
        processCounter = 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SchedulerGUI::new);
    }
}
