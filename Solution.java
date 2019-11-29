import java.util.*;
import java.io.File;

public class Solution {

    static int machineSize;
    static int pageSize;
    static int processSize;
    static int jobMix;
    static int numReferences;
    static String evictAlgo;
    static List<Frame> frameList;
    static List<Process> processList;
    static int numProcesses;
    static int nextFrame;
    static int frameCapacity;
    static int time = 0;

    static Scanner randomScanner;



    public static void main(String [] args){
      randomScanner = getRandomFile();
      frameList = new ArrayList<>();
      processList = new ArrayList<>();

      machineSize = Integer.parseInt(args[0]);
      pageSize = Integer.parseInt(args[1]);
      processSize = Integer.parseInt(args[2]);
      jobMix =  Integer.parseInt(args[3]);
      numReferences =  Integer.parseInt(args[4]);
      evictAlgo = args[5];
      frameCapacity = machineSize / pageSize;

      if(jobMix == 1){
        numProcesses = 1;
      }else{
        numProcesses = 4;
      }

      for(int i = 0; i < frameCapacity; i ++){
        frameList.add(new Frame());
      }


      for(int i = 0 ; i < numProcesses; i ++){
        double probA = 0.0;
        double probB = 0.0;
        double probC = 0.0;


        if(jobMix == 1 || jobMix == 2){
          probA = 1.0;
        }else if(jobMix == 4){
          if(i == 0){
            probA = .75;
            probB = .25;
          }else if(i == 1){
            probA = .75;
            probC = .25;
          }else if(i == 2){
            probA = .75;
            probB = .125;
            probC = .125;
          }else{
            probA = .5;
            probB = .125;
            probC = .125;
          }
        }

        processList.add(new Process(i, numReferences, probA, probB, probC));

      }

      boolean continueLoop = true;
      while(continueLoop){
        continueLoop = false;
        for(int i = 0; i < numProcesses; i ++){
            // do the referencing
            Process currProcess = processList.get(i);
            for(int j = 0 ; j < 3; j ++){  // 3 references

                if(currProcess.referencesLeft > 0){

                  time += 1;
                  continueLoop = true;
                  // generate the next refernce
                  int wordNumber = currProcess.currentWord;
                  if(currProcess.generate){
                    wordNumber = getWordNumber(i);
                    currProcess.currentWord = wordNumber;
                  }
                  currProcess.generate = true;
                  int pageNumber = getPage(wordNumber);
                  currProcess.pageList.get(pageNumber).lastUsed = time;
                  System.out.print("Process " + (i+1) + " references word  " + wordNumber + "(page " + pageNumber + ") " + "at time " + time  + " references Left " + currProcess.referencesLeft );
                  if(isPageLoaded(currProcess, pageNumber)){
                    currProcess.referencesLeft  -= 1;
                    continue;
                  }else{
                    // if(currProcess.processNumber == 0){
                    //   System.out.println("TEST");
                    // }
                    System.out.print("Fault " );
                    currProcess.pageFaults += 1;
                    loadPage(currProcess, pageNumber);
                  }

                  currProcess.referencesLeft  -= 1;

                }else{
                  break;
                }
            }

            int wordNumber = getWordNumber(i);
            currProcess.currentWord = wordNumber;
            currProcess.generate = false;
        }
      }
      printInputs();
      printStats();
      //System.out.println("TEST " + (-1 % 40));
    }

    public static void printInputs(){
      System.out.println("The machine size is " + machineSize);
      System.out.println("The page size is " + pageSize);
      System.out.println("The process size is " + processSize );
      System.out.println("The job mix number is " + jobMix);
      System.out.println("The number of references per process is " + numReferences);
      System.out.println("The replacement algorithm is " + evictAlgo);

    }
    public static void printStats(){

      int faultsTotal = 0;
      double residencyTotal = 0.0;
      double evictionsTotal = 0.0;
      for(Process p : processList){
        double residencyProcess = 0.0;
        double evictionsProcess = 0.0;
        for(Page pg : p.pageList){
          residencyProcess += pg.residencySum;
          evictionsProcess += pg.numberEvictions;
        }
        System.out.println();
        faultsTotal += p.pageFaults;
        residencyTotal += residencyProcess;
        evictionsTotal += evictionsProcess;
        if(evictionsProcess == 0 ){
          System.out.println("Process " + (p.processNumber + 1) + " had " + p.pageFaults + " faults");
          System.out.println("With no evictions, the average residence is undefined");
        }else{
          System.out.println("Process " + (p.processNumber + 1) + " had " + p.pageFaults + " faults and " + (residencyProcess / evictionsProcess) + " average residency time "  + evictionsProcess + " total evictions" + " residency total " + residencyProcess );
        }
      }
      System.out.println();
      if(evictionsTotal == 0 ){
          System.out.println("The total number of faults is " + faultsTotal);
          System.out.println("With no evictions, the overall average residence is undefined");
      }else{
        System.out.println("The total number of faults is " + faultsTotal + " and the average overall residency is " + (residencyTotal / evictionsTotal) );
        //System.out.println("The total number of evictions is "+ evictionsTotal );
      }
    }

    public static int getWordNumber(int processNumber){
      Process currProcess = processList.get(processNumber);
      int wordNumber = -1;
      if(time == 1){
        System.out.println("TEST " + currProcess.referencesLeft + " " + numReferences );
      }
      if(currProcess.referencesLeft == numReferences){
           wordNumber =  getFirstWordNumber(processNumber);
      }else{
        double random = randomScanner.nextInt();
        System.out.println(random);
        double y = random / Integer.MAX_VALUE;
        //System.out.println("TEST " + y);
        if(y < currProcess.probA){
           wordNumber =  ((currProcess.currentWord + 1) % processSize);
        }else if(y < currProcess.probA + currProcess.probB){
          wordNumber =  ((currProcess.currentWord - 5 + processSize) % processSize);
        }else if(y < currProcess.probA + currProcess.probB + currProcess.probC){
          wordNumber = ((currProcess.currentWord + 4) % processSize);
        }else{
          wordNumber = randomScanner.nextInt() % processSize;
        }
      }
      //currProcess.referencesLeft  -= 1;
      currProcess.currentWord = wordNumber;
      return (wordNumber);
    }

    public static int getFirstWordNumber(int processNumber){
      System.out.println("TEST " + processNumber + 1);
      return ((111 * (processNumber + 1)) % processSize);
     }
    public static int getPage(int wordNumber){
        return wordNumber / pageSize;
    }

    public static boolean isPageLoaded(Process p, int pageNumber ){
        for(int i = 0 ; i < frameList.size(); i ++){
          Frame f = frameList.get(i);
          if(f.currProcess != null && f.currProcess.equals(p) && f.pageNumber == pageNumber){
            System.out.print("Hit in frame " + i + " ");
            return true;
          }
        }
        return false;
    }

    public static void loadPage(Process currProcess, int loadPage){

      int evictFrameIndex = -1;
      for(int i = frameList.size() -1 ; i >= 0; i --){
        if(frameList.get(i) == null || frameList.get(i).currProcess == null){
          System.out.print(" using free frame " + i + " ");
          evictFrameIndex = i;
          break;
        }
      }
      if(evictFrameIndex == -1 ){
        evictFrameIndex = findEvictFrame();
        System.out.print( " evicting from frame " + evictFrameIndex + " ");
        // System.out.println(evictFrameIndex);
        // System.out.println(frameList.size());
        //System.out.println(frameList.get(evictFrameIndex));
        Page evictedPage = (frameList.get(evictFrameIndex)).getPage();
        evictedPage.numberEvictions += 1;
        //System.out.println("TEST " + (time - evictedPage.lastLoadingTime));
        evictedPage.residencySum += (time - evictedPage.lastLoadingTime);
      }

      Page newPage = currProcess.pageList.get(loadPage);
      newPage.lastLoadingTime = time;
      frameList.get(evictFrameIndex).currProcess = currProcess;
      frameList.get(evictFrameIndex).pageNumber = loadPage;

      // TODO set eviction times of evict page, and new page number
    }

    public static int findEvictFrame(){
        // TODO based on policy find page to evict // and set new page in that spot
        if(evictAlgo.equals("random")){
          int randomIndex = (randomScanner.nextInt() % processSize);
          return (randomIndex % frameCapacity);
        }else{
          Collections.sort(frameList);
        }
        return 0;
    }

    public static Scanner getRandomFile(){
        try {
            File file = new File("random-numbers.txt");
            return new Scanner(file);
        }catch(Exception e){
            return null;
        }
    }



}


class Frame implements Comparable<Frame>{

  Process currProcess;
  int pageNumber;


  public int compareTo(Frame f){
    if(Solution.evictAlgo.equals("lifo")){
        return f.getPage().lastLoadingTime - this.getPage().lastLoadingTime;
    }else if(Solution.evictAlgo.equals("lru")){
        return this.getPage().lastUsed - f.getPage().lastUsed;
    }
    return 0;
  }

  public Page getPage(){
  //  System.out.println(pageNumber);
    //System.out.println(currProcess.pageList.size());
    return currProcess.pageList.get(pageNumber);
  }

}


class Process {
  int referencesLeft;
  int currentWord;
  int processNumber;
  int pageFaults;

  List<Page> pageList;
  boolean generate;

  double probA;
  double probB;
  double probC;

  public Process(int processNumber, int references, double probA, double probB, double probC){
    generate = true;
    this.processNumber = processNumber;
    this.referencesLeft = references;
    pageList = new ArrayList<>();
    this.probA = probA;
    this.probB = probB;
    this.probC = probC;
    int numPages = Solution.processSize / Solution.pageSize;
    for(int i = 0; i <= numPages; i ++ ){
      pageList.add(new Page());
    }
  }
}

class Page {
  int numberEvictions = 0;
  int residencySum = 0;
  int lastUsed = 0;

  int lastLoadingTime = -1;

}
