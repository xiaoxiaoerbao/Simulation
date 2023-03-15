package simulation;

import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.StringUtils;
import utils.Benchmark;
import utils.CommandUtils;
import utils.IOTool;
import utils.PStringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Simulation {

    /**
     * Python packages need to be installed
     * demes, demesdraw, matplotlib, msprime, numpy
     */
    String pythonInterpreterPath;

    SimulationMetadata simulationMetadata;

    String logFile;
    String outDir;

    int random_seed;
    int threadsNum;

    public Simulation(String pythonInterpreterPath, String simulationMetadata, String logFile,
                      String outDir, int random_seed, int threadsNum){

        this.pythonInterpreterPath=pythonInterpreterPath;
        this.simulationMetadata=new SimulationMetadata(simulationMetadata);
        this.logFile=logFile;
        this.outDir=outDir;
        this.random_seed=random_seed;
        this.threadsNum=threadsNum;
    }

    public Simulation(Builder builder){
        this.pythonInterpreterPath=builder.pythonInterpreterPath;
        this.simulationMetadata=builder.simulationMetadata;
        this.logFile=builder.logFile;
        this.outDir=builder.outDir;
        this.random_seed=builder.random_seed;
        this.threadsNum=builder.threadsNum;
    }

    public Simulation(String parameterFile){
        this.initialize(parameterFile);
        this.run_simulation();
    }

    private void initialize(String parameterFile){
        try (BufferedReader br = IOTool.getReader(parameterFile)) {
            String line;
            List<String> temp;
            while ((line=br.readLine())!=null){
                if (line.startsWith("##")) continue;
                temp = PStringUtils.fastSplit(line, ":");
                if (line.startsWith("pythonInterpreterPath")){
                    this.pythonInterpreterPath=temp.get(1);
                    continue;
                }
                if (line.startsWith("SimulationMetadata")){
                    this.simulationMetadata = new SimulationMetadata(temp.get(1));
                    continue;
                }
                if (line.startsWith("LogFilePath")){
                    this.logFile=temp.get(1);
                    continue;
                }
                if (line.startsWith("OutDir")){
                    this.outDir=temp.get(1);
                    continue;
                }
                if (line.startsWith("random_seed")){
                    this.random_seed=Integer.parseInt(temp.get(1));
                    continue;
                }
                if (line.startsWith("threadsNum")){
                    this.threadsNum=Integer.parseInt(temp.get(1));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {

        /**
         * Required parameters
         */
        private final SimulationMetadata simulationMetadata;
        private final String logFile;
        private final String outDir;

        /**
         * Optional parameters
         */
        private String pythonInterpreterPath = "/Users/xudaxing/anaconda3/envs/Msprime/bin/python";
        private int random_seed = 1;
        private int threadsNum = 2;

        public Builder(String simulationMetadata, String logFile, String outDir){
            this.simulationMetadata=new SimulationMetadata(simulationMetadata);
            this.logFile=logFile;
            this.outDir=outDir;
        }

        public Builder pythonInterpreterPath(String pythonInterpreterPath){
            this.pythonInterpreterPath=pythonInterpreterPath;
            return this;
        }

        public Builder random_seed(int random_seed){
            this.random_seed=random_seed;
            return this;
        }

        public Builder threadsNum(int threadsNum){
            this.threadsNum=threadsNum;
            return this;
        }

        public Simulation build(){
            return new Simulation(this);
        }

    }

    public int getTotalNumberOfSimulation(){
        return this.simulationMetadata.demesID.length;
    }

    public String getDemesID(int simulationIndex){
        return this.simulationMetadata.demesID[simulationIndex];
    }

    public String getDemesPath(int simulationIndex){
        return this.simulationMetadata.demesPath[simulationIndex];
    }

    public String getAdmixedPop(int simulationIndex){
        return this.simulationMetadata.admixedPop[simulationIndex];
    }

    public String getNativePop(int simulationIndex){
        return this.simulationMetadata.nativePop[simulationIndex];
    }

    public List<String> getIntrogressedPop(int simulationIndex){
        return this.simulationMetadata.introgressedPop[simulationIndex];
    }

    public int getSampleSizeOfAdmixedPop(int simulationIndex){
        return this.simulationMetadata.admixedPopSampleSize[simulationIndex];
    }

    public int getSampleSizeOfNativePop(int simulationIndex){
        return this.simulationMetadata.nativePopSampleSize[simulationIndex];
    }

    public IntList getSampleSizeOfIntrogressedPop(int simulationIndex){
        return this.simulationMetadata.introgressedPopSampleSize[simulationIndex];
    }

    public int getSeqLen(int simulationIndex){
        return this.simulationMetadata.sequenceLen[simulationIndex];
    }

    public double getRecombinationRate(int simulationIndex){
        return this.simulationMetadata.recombinationRate[simulationIndex];
    }

    public double getMutationRate(int simulationIndex){
        return this.simulationMetadata.mutationRate[simulationIndex];
    }

    public int getRandom_seed() {
        return random_seed;
    }

    public String getOutDir() {
        return outDir;
    }

    public String getLogFile() {
        return logFile;
    }

    public int getThreadsNum() {
        return threadsNum;
    }

    public void run_simulation(){
        new File(this.logFile).delete();
        long start = System.nanoTime();
        String simulationPyPath = Simulation.class.getResource("Simulation.py").getPath();
        List<Callable<Integer>> callableList = new ArrayList<>();
        for (int i = 0; i < this.getTotalNumberOfSimulation(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.setLength(0);
            sb.append(this.pythonInterpreterPath).append(" ");
            sb.append(simulationPyPath).append(" ");
            sb.append(this.getDemesPath(i)).append(" ");
            sb.append(new File(outDir, this.getDemesID(i)+".graph.pdf").getAbsolutePath()).append(" ");
            sb.append(new File(outDir, this.getDemesID(i)+".vcf")).append(" ");
            sb.append(new File(outDir, this.getDemesID(i)+".taxaInfo")).append(" ");
            sb.append(new File(outDir, this.getDemesID(i)+".recombinationMap")).append(" ");
            sb.append(new File(outDir, this.getDemesID(i)+".tract")).append(" ");
            sb.append(this.getSeqLen(i)).append(" ");
            sb.append(this.getRandom_seed()).append(" ");
            sb.append(this.getRecombinationRate(i)).append(" ");
            sb.append(this.getMutationRate(i)).append(" ");
            sb.append(this.getAdmixedPop(i)).append(",");
            sb.append(this.getNativePop(i)).append(",");
            sb.append(String.join(",", this.getIntrogressedPop(i))).append(" ");
            sb.append(this.getSampleSizeOfAdmixedPop(i)).append(",");
            sb.append(this.getSampleSizeOfNativePop(i)).append(",");
            sb.append(StringUtils.join(this.getSampleSizeOfIntrogressedPop(i), ","));
            callableList.add(()-> CommandUtils.runOneCommand(sb.toString(), this.getOutDir(),
                    new File(this.getLogFile())));
        }
        List<Integer> results = CommandUtils.run_commands(callableList, this.getThreadsNum());
        int failedCommandCount=0;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i)!=0){
                System.out.println(this.getDemesID(i)+" simulation run failed");
                failedCommandCount++;
            }
        }
        if (failedCommandCount > 0){
            System.out.println("Total "+failedCommandCount+" simulations run failed");
        }else {
            System.out.println("All simulation run successful");
        }
        System.out.println("simulation runner completed in "+ Benchmark.getTimeSpanSeconds(start)+" seconds");
    }

}
