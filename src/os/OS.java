package os;

import java.util.Scanner;

class Process {
    int processID, arrivalTime, burstTime, pirority;
    int watingTime = 0, turnAroundTime = 0, responseTime = -1, completeTime = 0;

    Process(int processID, int arrivalTime, int burstTime, int pirority) {
        this.processID = processID;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.pirority = pirority;
    }
}

public class OS {

    static void priorityScheduling(Process[] p, int n) {

    int[] remaining = new int[n]; 
    int completed = 0, time = 0;

    for (int i = 0; i < n; i++) {  // bnstore el burst time
        remaining[i] = p[i].burstTime;
    }

    System.out.println("\n===== Priority Scheduling =====");
    System.out.print("Gantt Chart: | ");

    while (completed < n) {

        int index = -1;
        int bestPriority = Integer.MAX_VALUE;

        // bn5tar a3la pirority
        for (int i = 0; i < n; i++) {
            if (p[i].arrivalTime <= time && remaining[i] > 0) {
                if (p[i].pirority < bestPriority) {
                    bestPriority = p[i].pirority;
                    index = i;
                }
            }
        }

        // law mafesh process gdeda
        if (index == -1) {
            time++;
            continue;
        }

        // awel mara save response time
        if (p[index].responseTime == -1) {
            p[index].responseTime = time - p[index].arrivalTime;
        }

        System.out.print("P" + p[index].processID + " | ");

        remaining[index]--; 
        time++;

        // if finished
        if (remaining[index] == 0) {
            completed++;
            p[index].completeTime = time;
            p[index].turnAroundTime = p[index].completeTime - p[index].arrivalTime;
            p[index].watingTime = p[index].turnAroundTime - p[index].burstTime;

            if (p[index].watingTime < 0)
                p[index].watingTime = 0;
        }
    }

    double avgWT = 0, avgTAT = 0, avgRT = 0;

    System.out.println("\n");
    System.out.println("ID\tWT\tTAT\tRT");

    for (int i = 0; i < n; i++) {
        System.out.println("P" + p[i].processID + "\t" + p[i].watingTime + "\t" + p[i].turnAroundTime + "\t" + p[i].responseTime);

        avgWT += p[i].watingTime;
        avgTAT += p[i].turnAroundTime;
        avgRT += p[i].responseTime;
    }

    System.out.printf("\nAverage WT = %.2f\n", avgWT / n);
    System.out.printf("Average TAT = %.2f\n", avgTAT / n);
    System.out.printf("Average RT = %.2f\n", avgRT / n);
}

    static void srtfScheduling(Process[] p, int n) {

        int[] remaining = new int[n]; 
        int completed = 0, time = 0;

        for (int i = 0; i < n; i++) { // store the burst time 
            remaining[i] = p[i].burstTime;
        }

        System.out.println("\n===== SRTF Scheduling =====");
        System.out.print("Gantt Chart: | ");

        while (completed < n) {

            int index = -1;
            int min = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (p[i].arrivalTime <= time && remaining[i] > 0 && remaining[i] < min) {
                    min = remaining[i];  // burst time of first prosses
                    index = i;
                }
            }

            if (index == -1) {
                time++;
                continue;
            }

            if (p[index].responseTime == -1) {  // fe awel kol process
                p[index].responseTime = time - p[index].arrivalTime; 
            }

            System.out.print("P" + p[index].processID + " | ");

            remaining[index]--; // burst time - 1 (ba2y time ad eh ya3ni)
            time++; 

            if (remaining[index] == 0) { // if finished 
                completed++;
                p[index].completeTime = time;
                p[index].turnAroundTime = p[index].completeTime - p[index].arrivalTime; 
                p[index].watingTime = p[index].turnAroundTime - p[index].burstTime; 

                if (p[index].watingTime < 0)
                    p[index].watingTime = 0;
            }
        }

        double avgWT = 0, avgTAT = 0, avgRT = 0;

        System.out.println("\n");
        System.out.println("ID\tWT\tTAT\tRT");

        for (int i = 0; i < n; i++) {
            System.out.println("P" + p[i].processID + "\t" + p[i].watingTime + "\t" 
            + p[i].turnAroundTime + "\t" + p[i].responseTime);

            avgWT += p[i].watingTime;
            avgTAT += p[i].turnAroundTime;
            avgRT += p[i].responseTime;
        }

        System.out.printf("\nAverage WT = %.2f\n", avgWT / n);
        System.out.printf("Average TAT = %.2f\n", avgTAT / n);
        System.out.printf("Average RT = %.2f\n", avgRT / n);
    }

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = input.nextInt();

        Process[] p1 = new Process[n];
        Process[] p2 = new Process[n];

        for (int i = 0; i < n; i++) {

            System.out.println("\nProcess " + (i + 1));

            System.out.print("Arrival Time: ");
            int arrivalTime = input.nextInt();

            System.out.print("Burst Time: ");
            int burstTime = input.nextInt();
            System.out.print("Priority: ");
            int pirority = input.nextInt();

            p1[i] = new Process(i + 1, arrivalTime, burstTime, pirority);
            p2[i] = new Process(i + 1, arrivalTime, burstTime, pirority);
        }

        priorityScheduling(p1, n);
        srtfScheduling(p2, n);
    }
}