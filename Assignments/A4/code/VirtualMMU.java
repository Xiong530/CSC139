/*
* Andrew Xiong
* CSC139 Summer1
* Ghassan Shobaki
* Assignment4
*  7/2/24
* Grade Receieved: 70/100
*/
import java.io.*;

public class VirtualMMU {

    static final int maxFrames = 1000;
    static final String inputFile = "test_cases/input.txt";
    static final String outputFile = "test_cases/output.txt";

    // Global variables
    static PrintWriter writer;
    static int[] pfr = {-1, -1, -1};
    static int[] req = new int[maxFrames];

    // FIFO Variables
    static int initial = 0;
    static int end = 0;
    static int queueSize = 0;
    static int[] queue = new int[maxFrames];

    public static void main(String[] args) {
        try {
            writer = new PrintWriter(new FileWriter(outputFile));
            BufferedReader inputfile = new BufferedReader(new FileReader(inputFile));
            String temp;

            temp = inputfile.readLine();
            String[] parts = temp.split(" ");

            for (int i = 0; i < 3; i++) {
                pfr[i] = Integer.parseInt(parts[i]);
            }

            for (int i = 0; i < pfr[2]; i++) {
                temp = inputfile.readLine();
                req[i] = Integer.parseInt(temp.split(" ")[0]);
            }
            inputfile.close();

            FIFO();
            Optimal();
            LRU();

            writer.close();

        }catch (FileNotFoundException e) {
            System.err.print("Error opening input file");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            System.err.print("Error writing to output file");
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static void FIFO() {
        //int initial = 0;
        //int end = 0;
        writer.println("FIFO");

        int pageFault = 0;

        for (int i = 0; i < pfr[2]; i++) {
            int frame = searchArr(req[i], queue, initial, end) % pfr[1];

            if (frame < 0) {
                if (queueFull()) {
                    int tempPage = queue[initial];
                    frame = searchArr(tempPage, queue, initial, end) % pfr[1];
                    removePage();
                    writer.print("Page "+tempPage+" unloaded from Frame "+frame+", ");
                }
                insertNewPage(req[i]);
                frame = searchArr(req[i], queue, initial, end) % pfr[1];
                writer.print("Page "+req[i]+" loaded into Frame "+frame+"\n");
                pageFault++;
            } else {
                writer.print("Page "+req[i]+" already in Frame "+frame+"\n");
            }
        }
        writer.println(pageFault +" page faults\n");
    }

    public static void Optimal() {
        writer.println("Optimal");

        int pageFault = 0;
        int frameCount = 0;
        int[] frames = new int[maxFrames];

        for (int i = 0; i < pfr[2]; i++) {
            int frame = searchArr(req[i], frames, 0, pfr[1]);

            if (frame < 0) {
                if (frameCount >= pfr[1]) {
                    int fTemp = maxFrames;
                    int[] temp = new int[maxFrames];
                    int notFound = -1;

                    for (int j = 0; j < pfr[1]; j++) {
                        int n = searchArr(frames[j], req, i, pfr[2]);
                        if (n == notFound) {
                            fTemp = frames[j];
                            break;
                        } else if (n > i && searchArr(frames[j], temp, 0, i) < 0) {
                            fTemp = frames[j];
                            temp[j] = fTemp;
                        }
                    }

                    frame = searchArr(fTemp, frames, 0, pfr[1]);
                    writer.print("Page "+fTemp+" unloaded from Frame "+frame+", ");
                    frames[frame] = req[i];
                    writer.print("Page "+req[i]+" loaded into Frame "+frame+"\n");
                    frameCount--;
                } else {
                    frames[frameCount] = req[i];
                    writer.print("Page "+req[i]+" loaded into Frame "+frameCount+"\n");
                }
                pageFault++;
                frameCount++;
            } else {
                writer.print("Page "+req[i]+" already in Frame "+frame+"\n");
            }
        }
        writer.println(pageFault +" page faults\n");
    }

    public static void LRU() {
        writer.println("LRU");

        int pageFaults = 0;
        int frameCount = 0;
        int[] frames = new int[maxFrames];

        for (int i = 0; i < pfr[2]; i++) {
            int frame = searchArr(req[i], frames, 0, pfr[1]);

            if (frame < 0) {
                if (frameCount >= pfr[1]) {
                    int fTemp = maxFrames;
                    int[] arrTemp = new int[maxFrames];

                    for (int j = i - 1; j >= 0; j--) {
                        for (int k = 0; k < pfr[1]; k++) {
                            if (frames[k] == req[j] && searchArr(frames[k], arrTemp, 0, i) == -1) {
                                fTemp = frames[k];
                                arrTemp[k] = fTemp;
                            }
                        }
                    }

                    frame = searchArr(fTemp, frames, 0, pfr[1]);
                    writer.print("Page "+fTemp+" unloaded from Frame "+frame+", ");
                    frames[frame] = req[i];
                    writer.print("Page "+req[i]+" loaded into Frame "+frame+"\n");
                    frameCount--;
                } else {
                    frames[frameCount] = req[i];
                    writer.print("Page "+req[i]+" loaded into Frame "+frameCount+"\n");
                }
                pageFaults++;
                frameCount++;
            } else {
                writer.print("Page "+req[i]+" already in Frame "+frame+"\n");
            }
        }
        writer.println(pageFaults +" page faults\n");
    }
    public static int searchArr(int element, int[] arr, int first, int next) {
        for (int i = first; i <= next; i++) {
            if (arr[i] == element) {
                return i;
            }
        }
        return errorHandling();
    }

    public static int insertNewPage(int page) {
        //int newPage;
        //newPage = 0;

        if (!queueFull()) {
            int i = end;
            queue[end++] = page;
            queueSize++;
            return i;
        }
        return errorHandling();
    }

    public static int removePage() {
        if (initial == end) {
            return errorHandling();
        }
        queueSize--;
        return queue[initial++];
    }

    public static boolean queueFull() {
        return queueSize >= pfr[1];
    }
    public static int errorHandling(){
        return -1;
    }
}
