import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import demography.DemographicModelTools;
import evaluation.LocalAncestry;
import runner.*;
import simulation.Simulation;
import simulation.SimulationMetadata;
import utils.IOTool;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

public class Start {

    public static void main(String[] args) {
//        MD5.checkTwoFileMD5("/Users/xudaxing/Desktop/LAIDP_development/twoWay_ancient_test/005_evaluation/evaluation" +
//                ".txt","/Users/xudaxing/Desktop/LAIDP_development/twoWay_ancient_test/005_evaluation/evaluation_0.txt");
        String dir = "/Users/xudaxing/Desktop/LAIDP_development/twoWay_100M";
        evaluate_contingencyTable(DemographicModelTools.N_way.TWO_WAY, dir);
    }

    /**
     * pearsonCorrelationData meanDeviationData contingencyTableData
     * @param nWay
     * @param outDir
     */
    public static void evaluate_pearsonCorrelation_meanDeviation_contingencyTable(DemographicModelTools.N_way nWay, String outDir){

        final String[] DIRS = {"001_parameterFile","002_demes","003_simulation","004_runner","log",
                "005_evaluation"};
        final String[] SOFTWARE = {"loter","elai","mosaic","laidp"};

        File[] dirsFile = new File[DIRS.length];
        for (int i = 0; i < dirsFile.length; i++) {
            dirsFile[i] = new File(outDir, DIRS[i]);
            dirsFile[i].mkdir();
        }

        String[] logFiles = new String[SOFTWARE.length];
        String[] softwareSubDir = new String[SOFTWARE.length];
        for (int i = 0; i < SOFTWARE.length; i++) {
            File file = new File(dirsFile[3], SOFTWARE[i]);
            file.mkdir();
            softwareSubDir[i] = file.getAbsolutePath();
            logFiles[i] = new File(dirsFile[4], SOFTWARE[i]+".log").getAbsolutePath();
        }

        String simulationMetadataOutFile = new File(dirsFile[0], "simulationMetadata.txt").getAbsolutePath();
        String simulationLogFile = new File(dirsFile[4], "simulation.log").getAbsolutePath();
        DemographicModelTools.batchRun(nWay, simulationMetadataOutFile, dirsFile[1].getAbsolutePath());
        Simulation simulation = new Simulation.Builder(simulationMetadataOutFile, simulationLogFile,
                dirsFile[2].getAbsolutePath()).build();
        simulation.run_simulation();

        GenotypeMetaData genotypeMetaData = new GenotypeMetaData(simulationMetadataOutFile, dirsFile[2].getAbsolutePath());

        List<double[][][][]> software_localAncestry = new ArrayList<>();
        List<String> software = new ArrayList<>();

//         使用 Java 8 Streams API
        IntStream.range(0, SOFTWARE.length).forEach(i -> {
            switch(SOFTWARE[i]) {
                case "loter":
                    Loter_runner loterRunner = new Loter_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    loterRunner.startRun();
                    software_localAncestry.add(loterRunner.extractLocalAncestry());
                    software.add(SOFTWARE[i]);
                    break;
                case "elai":
                    ELAI_runner elaiRunner = new ELAI_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    elaiRunner.startRun();
                    software_localAncestry.add(elaiRunner.extractLocalAncestry());
                    software.add(SOFTWARE[i]);
                    break;
                case "mosaic":
                    Mosaic_runner mosaicRunner = new Mosaic_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    mosaicRunner.startRun();
                    software_localAncestry.add(mosaicRunner.extractLocalAncestry());
                    software.add(SOFTWARE[i]);
                    break;
                case "laidp":
                    LAIDP_runner laidpRunner = new LAIDP_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    laidpRunner.startRun();
                    software_localAncestry.add(laidpRunner.extractLocalAncestry());
                    software.add(SOFTWARE[i]);
                default:
                    break;
            }
        });

        LocalAncestry.write_RobustnessData(simulationMetadataOutFile, dirsFile[2].getAbsolutePath(),
                software_localAncestry, software, new File(dirsFile[5], "evaluation.txt").getAbsolutePath());
    }

    public static void evaluate_contingencyTable(DemographicModelTools.N_way nWay, String outDir){

        final String[] DIRS = {"001_parameterFile","002_demes","003_simulation","004_runner","log",
                "005_evaluation"};
        final String[] SOFTWARE = {"loter","elai","mosaic","laidp"};

        File[] dirsFile = new File[DIRS.length];
        for (int i = 0; i < dirsFile.length; i++) {
            dirsFile[i] = new File(outDir, DIRS[i]);
            dirsFile[i].mkdir();
        }

        String[] logFiles = new String[SOFTWARE.length];
        String[] softwareSubDir = new String[SOFTWARE.length];
        for (int i = 0; i < SOFTWARE.length; i++) {
            File file = new File(dirsFile[3], SOFTWARE[i]);
            file.mkdir();
            softwareSubDir[i] = file.getAbsolutePath();
            logFiles[i] = new File(dirsFile[4], SOFTWARE[i]+".log").getAbsolutePath();
        }

        String simulationMetadataOutFile = new File(dirsFile[0], "simulationMetadata.txt").getAbsolutePath();

        String simulationLogFile = new File(dirsFile[4], "simulation.log").getAbsolutePath();
        DemographicModelTools.batchRun(nWay, simulationMetadataOutFile, dirsFile[1].getAbsolutePath());
        Simulation simulation = new Simulation.Builder(simulationMetadataOutFile, simulationLogFile,
                dirsFile[2].getAbsolutePath()).build();
        simulation.run_simulation();

        SimulationMetadata simulationMetadata = new SimulationMetadata(simulationMetadataOutFile);
//        double[][][][] actual_values = LocalAncestry.extractLocalAncestry_actualValue(simulationMetadata, dirsFile[2].getAbsolutePath());
        BitSet[][][] actual_values = LocalAncestry.extractLocalAncestry_actualValue_bitset(simulationMetadata,
                dirsFile[2].getAbsolutePath());

        GenotypeMetaData genotypeMetaData = new GenotypeMetaData(simulationMetadataOutFile, dirsFile[2].getAbsolutePath());
        int[][][][] software_contingencyTable = new int[SOFTWARE.length][][][];


//         使用 Java 8 Streams API
        IntStream.range(0, SOFTWARE.length).forEach(i -> {
            switch(SOFTWARE[i]) {
                case "loter":
                    Loter_runner loterRunner = new Loter_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    loterRunner.startRun();
                    software_contingencyTable[i]=loterRunner.contingencyTable_2way_bitset(actual_values);
                    break;
                case "elai":
                    ELAI_runner elaiRunner = new ELAI_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    elaiRunner.startRun();
                    software_contingencyTable[i]=elaiRunner.contingencyTable_2way_bitset(actual_values);
                    break;
                case "mosaic":
                    Mosaic_runner mosaicRunner = new Mosaic_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    mosaicRunner.startRun();
                    software_contingencyTable[i] = mosaicRunner.contingencyTable_2way_bitset(actual_values);
                    break;
                case "laidp":
                    LAIDP_runner laidpRunner = new LAIDP_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    laidpRunner.startRun();
                    software_contingencyTable[i] = laidpRunner.contingencyTable_2way_bitset(actual_values);
                default:
                    break;
            }
        });

        try (BufferedWriter bw = IOTool.getWriter(new File(dirsFile[5], "evaluation.txt").getAbsolutePath())) {
            bw.write("DemesID\tSoftware\tAdmixedIndividual\tTruePositive" +
                    "\tFalseNegative\tFalsePositive\tTrueNegative");

            bw.newLine();
            StringBuilder sb = new StringBuilder();
            String joined;
            for (int i = 0; i < software_contingencyTable.length; i++) {
                for (int j = 0; j < software_contingencyTable[i].length; j++) {
                    for (int k = 0; k < software_contingencyTable[i][j].length; k++) {
                        sb.setLength(0);
                        sb.append(simulationMetadata.getDemesID()[j]).append("\t");
                        sb.append(SOFTWARE[i]).append("\t");
                        sb.append("tsk_").append(k).append("\t");
                        joined = Joiner.on("\t").join(Ints.asList(software_contingencyTable[i][j][k]));
                        sb.append(joined);
                        bw.write(sb.toString());
                        bw.newLine();
                    }
                }
            }
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Grid search
     * @param nWay
     * @param outDir
     */
    public static void evaluate_contingencyTable_gridSearch(DemographicModelTools.N_way nWay, String outDir){

        final String[] DIRS = {"001_parameterFile","002_demes","003_simulation","004_runner","log",
                "005_evaluation"};
        final int[] conjunctionNum = {0,1,2,3,4,5};
        final double[] switchCostScore = {1.5,2.5,3.5};
        final int[] maxSolutionCount = {1,2,4,8,16,32,64,128};


        File[] dirsFile = new File[DIRS.length];
        for (int i = 0; i < dirsFile.length; i++) {
            dirsFile[i] = new File(outDir, DIRS[i]);
            dirsFile[i].mkdir();
        }

        String simulationMetadataOutFile = new File(dirsFile[0], "simulationMetadata.txt").getAbsolutePath();

//        String simulationLogFile = new File(dirsFile[4], "simulation.log").getAbsolutePath();
//        DemographicModelTools.batchRun(nWay, simulationMetadataOutFile, dirsFile[1].getAbsolutePath());
//        Simulation simulation = new Simulation.Builder(simulationMetadataOutFile, simulationLogFile,
//                dirsFile[2].getAbsolutePath()).build();
//        simulation.run_simulation();

        SimulationMetadata simulationMetadata = new SimulationMetadata(simulationMetadataOutFile);
//        double[][][][] actual_values = LocalAncestry.extractLocalAncestry_actualValue(simulationMetadata, dirsFile[2].getAbsolutePath());
        BitSet[][][] actual_values = LocalAncestry.extractLocalAncestry_actualValue_bitset(simulationMetadata,
                dirsFile[2].getAbsolutePath());

        GenotypeMetaData genotypeMetaData = new GenotypeMetaData(simulationMetadataOutFile, dirsFile[2].getAbsolutePath());


        List<int[][][]> software_contingencyTable = new ArrayList<>();
        List<String> softwareList = new ArrayList<>();
        File file;
        LAIDP_runner laidpRunner;
        String logFile, softwareSubDir;
        StringBuilder sb = new StringBuilder();
        for (int value : conjunctionNum) {
            for (double v : switchCostScore) {
                for (int i : maxSolutionCount) {
                    sb.setLength(0);
                    sb.append("c").append("_").append(value).append("_");
                    sb.append("s").append("_").append(v).append("_");
                    sb.append("m").append("_").append(i);
                    logFile = new File(dirsFile[4], sb + ".log").getAbsolutePath();
                    file = new File(dirsFile[3], sb.toString());
                    file.mkdir();
                    softwareSubDir = file.getAbsolutePath();
                    laidpRunner = new LAIDP_runner.Builder(genotypeMetaData, logFile, softwareSubDir)
                            .conjunctionNum(value).switchCostScore(v).maxSolutionCount(i).build();
                    softwareList.add(sb.toString());
//                    laidpRunner.startRun();
                    software_contingencyTable.add(laidpRunner.contingencyTable_2way_bitset(actual_values));
                }
            }
        }

        try (BufferedWriter bw = IOTool.getWriter(new File(dirsFile[5], "evaluation.txt").getAbsolutePath())) {
            bw.write("DemesID\tSoftware\tAdmixedIndividual\tTruePositive" +
                    "\tFalseNegative\tFalsePositive\tTrueNegative");

            bw.newLine();
            String joined, software;
            for (int i = 0; i < software_contingencyTable.size(); i++) {
                for (int j = 0; j < software_contingencyTable.get(i).length; j++) {
                    for (int k = 0; k < software_contingencyTable.get(i)[j].length; k++) {
                        software = softwareList.get(i);
                        sb.setLength(0);
                        sb.append(simulationMetadata.getDemesID()[j]).append("\t");
                        sb.append(software).append("\t");
                        sb.append("tsk_").append(k).append("\t");
                        joined = Joiner.on("\t").join(Ints.asList(software_contingencyTable.get(i)[j][k]));
                        sb.append(joined);
                        bw.write(sb.toString());
                        bw.newLine();
                    }
                }

            }
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
