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
    private JPanel priorityGanttChart;
    private JPanel srtfGanttChart;
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
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 240, 240)); // Light gray background
        setLayout(new BorderLayout(10, 10)); // Add gaps

        // Add padding around the content
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createInputPanel(), BorderLayout.NORTH);

        JSplitPane mainSplitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            createTablePanel(),
            createOutputPanel()
        );
        mainSplitPane.setResizeWeight(0.55);
        mainSplitPane.setDividerSize(6);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());
        add(mainSplitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Labels
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Arrival Time:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel("Burst Time:"), gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 3;
        panel.add(new JLabel("Actions:"), gbc);

        // Fields
        arrivalField = new JTextField(8);
        arrivalField.setFont(new Font("Arial", Font.PLAIN, 14));
        burstField = new JTextField(8);
        burstField.setFont(new Font("Arial", Font.PLAIN, 14));
        priorityField = new JTextField(8);
        priorityField.setFont(new Font("Arial", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(arrivalField, gbc);
        gbc.gridx = 1;
        panel.add(burstField, gbc);
        gbc.gridx = 2;
        panel.add(priorityField, gbc);

        // Buttons
        JButton addBtn = new JButton("Add Process");
        styleButton(addBtn, new Color(70, 130, 180)); // Steel blue
        JButton runBtn = new JButton("Run Simulation");
        styleButton(runBtn, new Color(34, 139, 34)); // Forest green
        JButton clearBtn = new JButton("Clear");
        styleButton(clearBtn, new Color(220, 20, 60)); // Crimson

        addBtn.addActionListener(this::addProcess);
        runBtn.addActionListener(this::runSimulation);
        clearBtn.addActionListener(e -> clearAll());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(addBtn);
        btnPanel.add(runBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 3; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    private JPanel createTablePanel() {
        tableModel = new DefaultTableModel(
                new String[]{"PID", "Arrival Time", "Burst Time", "Priority"}, 0);
        processTable = new JTable(tableModel);
        processTable.setFont(new Font("Arial", Font.PLAIN, 12));
        processTable.setRowHeight(25);
        processTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        processTable.getTableHeader().setBackground(new Color(70, 130, 180));
        processTable.getTableHeader().setForeground(Color.WHITE);

        // Set column widths to ensure all columns are visible
        processTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // PID
        processTable.getColumnModel().getColumn(0).setMinWidth(50);
        processTable.getColumnModel().getColumn(0).setMaxWidth(80);

        processTable.getColumnModel().getColumn(1).setPreferredWidth(110); // Arrival Time
        processTable.getColumnModel().getColumn(1).setMinWidth(80);
        processTable.getColumnModel().getColumn(1).setMaxWidth(150);

        processTable.getColumnModel().getColumn(2).setPreferredWidth(110); // Burst Time
        processTable.getColumnModel().getColumn(2).setMinWidth(80);
        processTable.getColumnModel().getColumn(2).setMaxWidth(150);

        processTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Priority
        processTable.getColumnModel().getColumn(3).setMinWidth(60);
        processTable.getColumnModel().getColumn(3).setMaxWidth(100);

        // Set table properties for better display
        processTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(processTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(Color.WHITE);

        // Configure scroll pane for better display
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        processTable.setPreferredScrollableViewportSize(new Dimension(600, 280));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(240, 240, 240));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JTabbedPane createOutputPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Results tab with structured layout
        JPanel resultsPanel = createResultsPanel();
        tabbedPane.addTab("Results", resultsPanel);

        // Gantt Chart tab
        JPanel ganttPanelContainer = new JPanel(new BorderLayout());
        ganttPanelContainer.setBackground(new Color(240, 240, 240));
        
        JTabbedPane ganttTabs = new JTabbedPane();
        ganttTabs.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Priority Gantt Chart
        JPanel priorityGanttPanel = new JPanel(new BorderLayout());
        priorityGanttPanel.setBackground(Color.WHITE);
        priorityGanttPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        priorityGanttChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGantt(g, prioritySchedule, "Priority Scheduling");
            }
        };
        priorityGanttChart.setBackground(Color.WHITE);
        JScrollPane priorityGanttScroll = new JScrollPane(priorityGanttChart);
        priorityGanttScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        priorityGanttScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        priorityGanttPanel.add(priorityGanttScroll, BorderLayout.CENTER);
        
        // SRTF Gantt Chart
        JPanel srtfGanttPanel = new JPanel(new BorderLayout());
        srtfGanttPanel.setBackground(Color.WHITE);
        srtfGanttPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        srtfGanttChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGantt(g, srtfSchedule, "SRTF Scheduling");
            }
        };
        srtfGanttChart.setBackground(Color.WHITE);
        JScrollPane srtfGanttScroll = new JScrollPane(srtfGanttChart);
        srtfGanttScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        srtfGanttScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        srtfGanttPanel.add(srtfGanttScroll, BorderLayout.CENTER);
        
        ganttTabs.addTab("Priority Gantt Chart", priorityGanttPanel);
        ganttTabs.addTab("SRTF Gantt Chart", srtfGanttPanel);
        
        ganttPanelContainer.add(ganttTabs, BorderLayout.CENTER);
        tabbedPane.addTab("Gantt Chart", ganttPanelContainer);

        return tabbedPane;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBackground(new Color(240, 240, 240));
        
        JTabbedPane resultsTabs = new JTabbedPane();
        resultsTabs.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Priority Results Tab
        JPanel priorityPanel = new JPanel(new BorderLayout());
        priorityPanel.setBackground(Color.WHITE);
        priorityPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        priorityResultsModel = new DefaultTableModel(
                new String[]{"Process ID", "Waiting Time", "Turnaround Time", "Response Time"}, 0);
        JTable priorityTable = new JTable(priorityResultsModel);
        styleTable(priorityTable);
        JScrollPane priorityScroll = new JScrollPane(priorityTable);
        
        JPanel priorityAvgPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        priorityAvgPanel.setBackground(Color.WHITE);
        priorityAvgPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        priorityAvgWTLabel = new JLabel("Average WT: 0.00");
        priorityAvgTATLabel = new JLabel("Average TAT: 0.00");
        priorityAvgRTLabel = new JLabel("Average RT: 0.00");
        styleLabel(priorityAvgWTLabel);
        styleLabel(priorityAvgTATLabel);
        styleLabel(priorityAvgRTLabel);
        priorityAvgPanel.add(priorityAvgWTLabel);
        priorityAvgPanel.add(priorityAvgTATLabel);
        priorityAvgPanel.add(priorityAvgRTLabel);
        
        priorityPanel.add(priorityScroll, BorderLayout.CENTER);
        priorityPanel.add(priorityAvgPanel, BorderLayout.SOUTH);
        
        // SRTF Results Tab
        JPanel srtfPanel = new JPanel(new BorderLayout());
        srtfPanel.setBackground(Color.WHITE);
        srtfPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        srtfResultsModel = new DefaultTableModel(
                new String[]{"Process ID", "Waiting Time", "Turnaround Time", "Response Time"}, 0);
        JTable srtfTable = new JTable(srtfResultsModel);
        styleTable(srtfTable);
        JScrollPane srtfScroll = new JScrollPane(srtfTable);
        
        JPanel srtfAvgPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        srtfAvgPanel.setBackground(Color.WHITE);
        srtfAvgPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        srtfAvgWTLabel = new JLabel("Average WT: 0.00");
        srtfAvgTATLabel = new JLabel("Average TAT: 0.00");
        srtfAvgRTLabel = new JLabel("Average RT: 0.00");
        styleLabel(srtfAvgWTLabel);
        styleLabel(srtfAvgTATLabel);
        styleLabel(srtfAvgRTLabel);
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

    private void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setEnabled(false);
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(50, 50, 50));
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

        priorityGanttChart.repaint();
        srtfGanttChart.repaint();
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

    private void drawGantt(Graphics g, List<ScheduleBlock> schedule, String title) {
        int width = getWidth();
        int height = getHeight();
        int barHeight = 40;
        int y = 60;

        // Title
        g.setColor(new Color(50, 50, 50));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, 30);

        drawSchedule(g, schedule, 20, y, width - 40, barHeight);
    }

    private void drawSchedule(Graphics g, List<ScheduleBlock> schedule, int x, int y, int maxWidth, int barHeight) {
        if (schedule.isEmpty()) return;

        int scale = 25; // pixels per time unit
        Map<Integer, Color> colorMap = new HashMap<>();
        int colorIndex = 0;
        Color[] colors = {new Color(255, 99, 132), new Color(54, 162, 235), new Color(75, 192, 192),
                         new Color(255, 205, 86), new Color(153, 102, 255), new Color(255, 159, 64)};

        int maxTime = 0;

        // Calculate max time
        for (ScheduleBlock block : schedule) {
            maxTime = Math.max(maxTime, block.endTime);
        }

        // Adjust scale if needed to fit
        if (maxTime * scale > maxWidth) {
            scale = maxWidth / maxTime;
            if (scale < 15) scale = 15; // minimum scale
        }

        // Draw blocks
        for (ScheduleBlock block : schedule) {
            int blockWidth = (block.endTime - block.startTime) * scale;
            int blockX = x + block.startTime * scale;

            if (block.processID == -1) {
                // Idle
                g.setColor(new Color(220, 220, 220));
                g.fillRect(blockX, y, blockWidth, barHeight);
                g.setColor(new Color(150, 150, 150));
                g.drawRect(blockX, y, blockWidth, barHeight);
                g.setColor(new Color(100, 100, 100));
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth("Idle");
                g.drawString("Idle", blockX + (blockWidth - textWidth) / 2, y + barHeight / 2 + 5);
            } else {
                // Process
                if (!colorMap.containsKey(block.processID)) {
                    colorMap.put(block.processID, colors[colorIndex % colors.length]);
                    colorIndex++;
                }
                Color blockColor = colorMap.get(block.processID);
                g.setColor(blockColor);
                g.fillRect(blockX, y, blockWidth, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(blockX, y, blockWidth, barHeight);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                String processText = "P" + block.processID;
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(processText);
                g.drawString(processText, blockX + (blockWidth - textWidth) / 2, y + barHeight / 2 + 5);
            }
        }

        // Draw time axis
        g.setColor(new Color(50, 50, 50));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int t = 0; t <= maxTime; t++) {
            int tx = x + t * scale;
            g.drawLine(tx, y + barHeight, tx, y + barHeight + 15);
            String timeStr = String.valueOf(t);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(timeStr);
            g.drawString(timeStr, tx - textWidth / 2, y + barHeight + 30);
        }

        // Draw legend
        int legendY = y + barHeight + 60;
        int legendX = x;
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Legend:", legendX, legendY);
        legendY += 20;

        for (Map.Entry<Integer, Color> entry : colorMap.entrySet()) {
            g.setColor(entry.getValue());
            g.fillRect(legendX, legendY - 10, 15, 15);
            g.setColor(Color.BLACK);
            g.drawRect(legendX, legendY - 10, 15, 15);
            g.drawString("P" + entry.getKey(), legendX + 20, legendY);
            legendX += 60;
            if (legendX > maxWidth - 60) {
                legendX = x;
                legendY += 20;
            }
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
