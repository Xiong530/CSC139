/*
* Andrew Xiong
* CSC139
* Ghassan Shobaki
* Assignment3: CPU Scheduling
* Grade Received: 83/100
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Scheduler {

    // Global Variables

    static int completedProcesses = 0;

    static int currentProcess = -1;
    static int systemClock = 0;
    static int timeQuantum = 0;
    static int queueSize = 0;
    static int processCount = 0;


    static int[][] processes = new int[4][2000];
    static int[][] finishedTimes = new int[3][2000];

    static String schedulingAlgorithm;
    static String processLine;
    //static String tempBuffer;

    // FIFI Variables
    static int[] processQueue = new int[2000];
    static int frontIndex = 0;
    static int rearIndex = 0;
    static int queueCapacity = 2000;

    public static void main(String[] args) {
        parseInput();
    }

    // Round Robin
    static void roundRobin() throws IOException {
        FileWriter outputFile = new FileWriter("./output.txt");

        timeQuantum = (int) schedulingAlgorithm.charAt(3) - 48;
        completedProcesses = 0;
        handleArrivals(-1);
        outputFile.write("RR " + timeQuantum + "\n");

        while (completedProcesses < processCount) {
            currentProcess = removeProcessFromQueue();
            outputFile.write(systemClock + " " + currentProcess + "\n");

            int burstTime = getBurstTime(currentProcess);

            // If the burst time is greater than the time quantum
            if (burstTime > timeQuantum) {
                burstTime -= timeQuantum;
                setBurstTime(currentProcess, burstTime);
                systemClock += timeQuantum;
                finishedTimes[2][currentProcess] += timeQuantum;
                handleArrivals(currentProcess);
                insertProcessIntoQueue(currentProcess);
            } else if (burstTime > 0 && burstTime <= timeQuantum) {
                handleArrivals(currentProcess);
                setBurstTime(currentProcess, -1);
                systemClock += burstTime;
                finishedTimes[2][currentProcess] += burstTime;
                finishedTimes[0][currentProcess] = systemClock;
                completedProcesses++;
            } else {
                systemClock++;
                handleArrivals(currentProcess);
            }

        }
        outputFile.write("AVG Waiting Time: " + String.format("%.2f", calculateAvgWaitTime()) + "\n");
        outputFile.close();
    }

    // Shortest Job First
    //Throwing an out-of-bounds error that im not sure how to solve :(
    static void shortestJobFirst() throws IOException {
        FileWriter outputFile = new FileWriter("./output.txt");
        sortProcessesByBurstTime();
        completedProcesses = 0;
        handleArrivals(-1);

        outputFile.write("SJF\n");

        while (completedProcesses < processCount) {

            currentProcess = removeProcessFromQueue();
            outputFile.write(systemClock + " " + currentProcess + "\n");


            if (currentProcess > 0) {
                int burstTime = getBurstTime(currentProcess);
                systemClock += burstTime;
                setBurstTime(currentProcess, 0);
                finishedTimes[0][currentProcess] = systemClock;
                finishedTimes[2][currentProcess] = burstTime;
                completedProcesses++;
            } else {
                systemClock++;
            }
            clearQueue();
            handleArrivals(currentProcess);

        }
        outputFile.write("AVG Waiting Time: " + String.format("%.2f", calculateAvgWaitTime()) + "\n"); // Last Line of output
        outputFile.close();
    }

    // Priority Scheduling without Preemption (PR_noPREMP)
    static void prioritySchedulingNoPreemption() throws IOException {
        FileWriter outputFile = new FileWriter("./output.txt");

        sortProcessesByPriority();
        completedProcesses = 0;
        handleArrivals(-1);
        outputFile.write("PR_noPREMP\n");

        while (completedProcesses < processCount) {

            currentProcess = removeProcessFromQueue();
            outputFile.write(systemClock + " " + currentProcess + "\n");

            if (currentProcess > 0) {
                int burstTime = getBurstTime(currentProcess);
                systemClock += burstTime;
                setBurstTime(currentProcess, 0);
                finishedTimes[0][currentProcess] = systemClock;
                finishedTimes[2][currentProcess] = burstTime;
                completedProcesses++;
                clearQueue();
                handleArrivals(currentProcess);
            } else {
                systemClock++;
                clearQueue();
                handleArrivals(currentProcess);
            }
        }
        outputFile.write("AVG Waiting Time: " + String.format("%.2f", calculateAvgWaitTime()) + "\n");
        outputFile.close();
    }

    // Priority Scheduling with Preemption (PR_withPREMP)
    static void prioritySchedulingWithPreemption() throws IOException {
        FileWriter outputFile = new FileWriter("./output.txt");

        sortProcessesByPriority();
        completedProcesses = 0;
        handleArrivals(-1);
        outputFile.write("PR_withPREMP\n");

        while (completedProcesses < processCount) {

            int lastProcess = currentProcess;

            currentProcess = removeProcessFromQueue();

            if (currentProcess != lastProcess) {
                outputFile.write(systemClock + " " + currentProcess + "\n");
            }

            systemClock++;

            if (currentProcess > 0) {

                int burstTime = getBurstTime(currentProcess);
                burstTime--;
                finishedTimes[2][currentProcess] += 1;
                setBurstTime(currentProcess, burstTime);

                if (burstTime < 1) {
                    finishedTimes[0][currentProcess] = systemClock;
                    completedProcesses++;
                }
            }
            clearQueue();
            handleArrivals(-1);
        }
        outputFile.write("AVG Waiting Time: " + String.format("%.2f", calculateAvgWaitTime()) + "\n");
        outputFile.close();
    }

    // Parse the Input File.
    static void parseInput() {
        try {
            BufferedReader inputFile = new BufferedReader(new FileReader("test_cases/input2.txt"));
            String buffer;

            schedulingAlgorithm = inputFile.readLine().trim();
            processLine = inputFile.readLine().trim();
            processCount = Integer.parseInt(processLine);

            for (int i = 0; i < processCount; i++) {
                buffer = inputFile.readLine();
                String[] tokens = buffer.split(" ");
                for (int j = 0; j < 4; j++) {
                    processes[j][i] = Integer.parseInt(tokens[j]);
                    if (j == 1) {
                        finishedTimes[1][processes[0][i]] = processes[j][i];
                    }
                }
            }

            // Call scheduling algorithm.
            if (schedulingAlgorithm.charAt(0) == 'R') {
                roundRobin();
            } else if (schedulingAlgorithm.charAt(0) == 'S') {
                shortestJobFirst();
            } else if (schedulingAlgorithm.charAt(0) == 'P' && schedulingAlgorithm.charAt(3) == 'n') {
                prioritySchedulingNoPreemption();
            } else if (schedulingAlgorithm.charAt(0) == 'P' && schedulingAlgorithm.charAt(3) == 'w') {
                prioritySchedulingWithPreemption();
            } else {
                System.exit(1);
            }
            inputFile.close();
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Calculate the average wait time for the given scheduling algorithm
    static float calculateAvgWaitTime() {
        float waitTime = 0;
        for (int cnt = 0; cnt < processCount; cnt++) {
            int proc = processes[0][cnt];
            int a, b, c;
            a = finishedTimes[0][proc];
            b = finishedTimes[1][proc];
            c = finishedTimes[2][proc];
            waitTime += a - b - c;
        }
        return waitTime / processCount;
    }

    static void setBurstTime(int processNumber, int burstTime) {
        for (int cnt = 0; cnt < processCount; cnt++) {
            if (processes[0][cnt] == processNumber) {
                processes[2][cnt] = burstTime;
                break;
            }
        }
    }

    static int getBurstTime(int processNumber) {
        for (int cnt = 0; cnt < processCount; cnt++) {
            if (processes[0][cnt] == processNumber) {
                return processes[2][cnt];
            }
        }
        return -1;
    }

    static int linearSearch(int element, int[] array, int start, int end) {
        for (int cnt = start; cnt <= end; cnt++) {
            if (array[cnt] == element) {
                return cnt;
            }
        }
        return -1;
    }

    static void handleArrivals(int processNumber) {
        for (int cnt = 0; cnt < processCount; cnt++) {
            if (processes[0][cnt] != processNumber && processes[2][cnt] > 0 && processes[1][cnt] <= systemClock) {
                if (linearSearch(processes[0][cnt], processQueue, frontIndex, rearIndex) < 0) {
                    insertProcessIntoQueue(processes[0][cnt]);
                }
            }
        }
    }

    static void sortProcessesByPriority() {
        for (int i = 0; i < processCount; i++) {
            for (int j = i + 1; j < processCount; ++j) {
                if (processes[3][i] > processes[3][j]) {
                    for (int cnt = 0; cnt < 4; cnt++) {
                        int temp = processes[cnt][i];
                        processes[cnt][i] = processes[cnt][j];
                        processes[cnt][j] = temp;
                    }
                }
            }
        }
        if (schedulingAlgorithm.charAt(3) == 'n' || schedulingAlgorithm.charAt(3) == 'w') {
            for (int i = 0; i < processCount; ++i) {
                for (int j = i + 1; j < processCount; ++j) {
                    if ((processes[3][i] == processes[3][j]) && (processes[1][i] >= processes[1][j])) {
                        for (int cnt = 0; cnt < 4; cnt++) {
                            int temp = processes[cnt][i];
                            processes[cnt][i] = processes[cnt][j];
                            processes[cnt][j] = temp;
                        }
                    }
                }
            }
        }
    }

    static void sortProcessesByBurstTime() {
        for (int i = 0; i < processCount; i++) {
            for (int j = i + 1; j < processCount; ++j) {
                if (processes[2][i] > processes[2][j]) {
                    for (int cnt = 0; cnt < 4; cnt++) {
                        int temp = processes[cnt][i];
                        processes[cnt][i] = processes[cnt][j];
                        processes[cnt][j] = temp;
                    }
                }
            }
        }
        if (schedulingAlgorithm.charAt(3) == 'n' || schedulingAlgorithm.charAt(3) == 'w') {
            for (int i = 0; i < processCount; ++i) {
                for (int j = i + 1; j < processCount; ++j) {
                    if ((processes[2][i] == processes[2][j]) && (processes[1][i] >= processes[1][j])) {
                        for (int cnt = 0; cnt < 4; cnt++) {
                            int temp = processes[cnt][i];
                            processes[cnt][i] = processes[cnt][j];
                            processes[cnt][j] = temp;
                        }
                    }
                }
            }
        }
    }

    static int removeProcessFromQueue() {
        if (frontIndex == rearIndex) {
            return -1;
        }
        queueSize--;
        return processQueue[frontIndex++];
    }

    static void insertProcessIntoQueue(int processNumber) {
        if (rearIndex != queueCapacity) {
            processQueue[rearIndex++] = processNumber;
            queueSize++;
        }
    }

    static void clearQueue() {
        frontIndex = 0;
        rearIndex = 0;
        Arrays.fill(processQueue, -1);
    }
}
