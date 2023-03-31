import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import demography.DemographicModelTools;
import evaluation.LocalAncestry;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import laidp.Source;
import runner.*;
import simulation.Simulation;
import simulation.SimulationMetadata;
import utils.IOTool;
import utils.MD5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

public class Start {

    public static void main(String[] args) {
//        MD5.checkTwoFileMD5("/Users/xudaxing/Desktop/LAIDP_development/two_way_ancient_test2/005_evaluation/contingencyTable2.txt","/Users/xudaxing/Desktop/LAIDP_development/two_way_ancient_test2/005_evaluation/contingencyTable.txt");
        String dir = "/Users/xudaxing/Desktop/LAIDP_development/two_way_ancient_test2";
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
        List<int[]>[][][] actual_tract = LocalAncestry.extractLocalAncestry_TractSize(actual_values,
                simulationMetadata, dirsFile[2].getAbsolutePath());
        DoubleList[] actual_tract_logcM = LocalAncestry.transformTractSizeTo_logcM(actual_tract,
                simulationMetadata, dirsFile[2].getAbsolutePath());
        BitSet[][][] inferredValue;
        DoubleList[] inferred_tract_logcM;

        GenotypeMetaData genotypeMetaData = new GenotypeMetaData(simulationMetadataOutFile, dirsFile[2].getAbsolutePath());
        int[][][][] software_contingencyTable = new int[SOFTWARE.length][][][];
        List<int[]>[][][][] software_inferredTract = new List[SOFTWARE.length][][][];

        double[][] klDivergence = new double[SOFTWARE.length][];

        for (int i = 0; i < SOFTWARE.length; i++) {
            switch(SOFTWARE[i]) {
                case "loter":
                    Loter_runner loterRunner = new Loter_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    loterRunner.startRun();
                    inferredValue = loterRunner.extractLocalAncestry_bitset();
                    software_contingencyTable[i]=LocalAncestry.contingencyTable_2way_bitset(inferredValue, actual_values);
                    software_inferredTract[i] = LocalAncestry.extractLocalAncestry_TractSize(inferredValue,
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    inferred_tract_logcM = LocalAncestry.transformTractSizeTo_logcM(software_inferredTract[i],
                                    simulationMetadata, dirsFile[2].getAbsolutePath());
                    klDivergence[i] = LocalAncestry.calculateKLDivergence(inferred_tract_logcM, actual_tract_logcM);
                    break;
                case "elai":
                    ELAI_runner elaiRunner = new ELAI_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    elaiRunner.startRun();
                    inferredValue = elaiRunner.extractLocalAncestry_bitset();
                    software_contingencyTable[i]=LocalAncestry.contingencyTable_2way_bitset(inferredValue, actual_values);
                    software_inferredTract[i] = LocalAncestry.extractLocalAncestry_TractSize(inferredValue,
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    inferred_tract_logcM = LocalAncestry.transformTractSizeTo_logcM(software_inferredTract[i],
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    klDivergence[i] = LocalAncestry.calculateKLDivergence(inferred_tract_logcM, actual_tract_logcM);
                    break;
                case "mosaic":
                    Mosaic_runner mosaicRunner = new Mosaic_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    mosaicRunner.startRun();
                    inferredValue = mosaicRunner.extractLocalAncestry_bitset();
                    software_contingencyTable[i]=LocalAncestry.contingencyTable_2way_bitset(inferredValue, actual_values);
                    software_inferredTract[i] = LocalAncestry.extractLocalAncestry_TractSize(inferredValue,
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    inferred_tract_logcM = LocalAncestry.transformTractSizeTo_logcM(software_inferredTract[i],
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    klDivergence[i] = LocalAncestry.calculateKLDivergence(inferred_tract_logcM, actual_tract_logcM);
                    break;
                case "laidp":
                    LAIDP_runner laidpRunner = new LAIDP_runner.Builder(genotypeMetaData, logFiles[i], softwareSubDir[i]).build();
                    laidpRunner.startRun();
                    inferredValue = laidpRunner.extractLocalAncestry_bitset();
                    software_contingencyTable[i]=LocalAncestry.contingencyTable_2way_bitset(inferredValue, actual_values);
                    software_inferredTract[i] = LocalAncestry.extractLocalAncestry_TractSize(inferredValue,
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    inferred_tract_logcM = LocalAncestry.transformTractSizeTo_logcM(software_inferredTract[i],
                            simulationMetadata, dirsFile[2].getAbsolutePath());
                    klDivergence[i] = LocalAncestry.calculateKLDivergence(inferred_tract_logcM, actual_tract_logcM);
                default:
                    break;
            }
        }

        try (BufferedWriter bw_contingencyTable = IOTool.getWriter(new File(dirsFile[5], "contingencyTable.txt").getAbsolutePath());
             BufferedWriter bw_KLDivergence = IOTool.getWriter(new File(dirsFile[5], "KLDivergence.txt"));
             BufferedWriter bw_inferredTract = IOTool.getWriter(new File(dirsFile[5], "inferredTract.txt"));
             BufferedWriter bw_simulatedTract = IOTool.getWriter(new File(dirsFile[5], "simulatedTract.txt"))) {
            bw_contingencyTable.write("DemesID\tSoftware\tAdmixedIndividual\tTruePositive" +
                    "\tFalseNegative\tFalsePositive\tTrueNegative");
            bw_contingencyTable.newLine();
            bw_KLDivergence.write("DemesID\tSoftware\tKLDivergence");
            bw_KLDivergence.newLine();
            bw_inferredTract.write("DemesID\tSoftware\tAdmixedIndividual\tIntrogressedPopulation\tStart\tEnd");
            bw_inferredTract.newLine();
            bw_simulatedTract.write("DemesID\tSoftware\tAdmixedIndividual\tIntrogressedPopulation\tStart\tEnd");
            bw_simulatedTract.newLine();
            StringBuilder sb = new StringBuilder();
            String joined, admixedPop;
            List<String> admixedTaxaList;
            int softwareNum = SOFTWARE.length;
            int runNum = simulationMetadata.getDemesID().length;
            int admixedTaxaNum, introgressedSourceNum, introgressedWindowNum;
            for (int softwareIndex = 0; softwareIndex < softwareNum; softwareIndex++) {
                for (int runIndex = 0; runIndex < runNum; runIndex++) {
                    admixedPop = simulationMetadata.getAdmixedPop()[runIndex];
                    admixedTaxaList = genotypeMetaData.getTaxaInfo(runIndex).getTaxaListOf(admixedPop);
                    admixedTaxaNum = simulationMetadata.getAdmixedPopSampleSize()[runIndex];
                    introgressedSourceNum = simulationMetadata.getIntrogressedPopSampleSize()[runIndex].size();
                    for (int admixedTaxonIndex = 0; admixedTaxonIndex < admixedTaxaNum; admixedTaxonIndex++) {
                        // contingencyTable
                        sb.setLength(0);
                        sb.append(simulationMetadata.getDemesID()[runIndex]).append("\t");
                        sb.append(SOFTWARE[softwareIndex]).append("\t");
                        sb.append(admixedTaxaList.get(admixedTaxonIndex)).append("\t");
                        joined = Joiner.on("\t").join(Ints.asList(software_contingencyTable[softwareIndex][runIndex][admixedTaxonIndex]));
                        sb.append(joined);
                        bw_contingencyTable.write(sb.toString());
                        bw_contingencyTable.newLine();

                        for (int introgressedSourceIndex = 0; introgressedSourceIndex < introgressedSourceNum; introgressedSourceIndex++) {
                            introgressedWindowNum = software_inferredTract[softwareIndex][runIndex][admixedTaxonIndex][introgressedSourceIndex].size();
                            for (int introgressedWindowIndex = 0; introgressedWindowIndex < introgressedWindowNum; introgressedWindowIndex++) {
                                // inferredTract
                                sb.setLength(0);
                                sb.append(simulationMetadata.getDemesID()[runIndex]).append("\t");
                                sb.append(SOFTWARE[softwareIndex]).append("\t");
                                sb.append(admixedTaxaList.get(admixedTaxonIndex)).append("\t");
                                sb.append(Source.getInstanceFromIndex(introgressedSourceIndex+1).get().name()).append("\t");
                                sb.append(software_inferredTract[softwareIndex][runIndex][admixedTaxonIndex][introgressedSourceIndex].get(introgressedWindowIndex)[0]).append("\t");
                                sb.append(software_inferredTract[softwareIndex][runIndex][admixedTaxonIndex][introgressedSourceIndex].get(introgressedWindowIndex)[1]);
                                bw_inferredTract.write(sb.toString());
                                bw_inferredTract.newLine();
                            }

                            if (softwareIndex != 0) continue;
                            introgressedWindowNum = actual_tract[runIndex][admixedTaxonIndex][introgressedSourceIndex].size();
                            for (int introgressedWindowIndex = 0; introgressedWindowIndex < introgressedWindowNum; introgressedWindowIndex++) {
                                // actualTract
                                sb.setLength(0);
                                sb.append(simulationMetadata.getDemesID()[runIndex]).append("\t");
                                sb.append("simulated").append("\t");
                                sb.append(admixedTaxaList.get(admixedTaxonIndex)).append("\t");
                                sb.append(Source.getInstanceFromIndex(introgressedSourceIndex+1).get().name()).append("\t");
                                sb.append(actual_tract[runIndex][admixedTaxonIndex][introgressedSourceIndex].get(introgressedWindowIndex)[0]).append("\t");
                                sb.append(actual_tract[runIndex][admixedTaxonIndex][introgressedSourceIndex].get(introgressedWindowIndex)[1]);
                                bw_simulatedTract.write(sb.toString());
                                bw_simulatedTract.newLine();
                            }

                        }
                    }
                    sb.setLength(0);
                    sb.append(simulationMetadata.getDemesID()[runIndex]).append("\t");
                    sb.append(SOFTWARE[softwareIndex]).append("\t");
                    sb.append(klDivergence[softwareIndex][runIndex]);
                    bw_KLDivergence.write(sb.toString());
                    bw_KLDivergence.newLine();
                }
            }
            bw_contingencyTable.flush();
            bw_KLDivergence.flush();
            bw_inferredTract.flush();
            bw_simulatedTract.flush();
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
        BitSet[][][] inferredValue;
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
                    inferredValue = laidpRunner.extractLocalAncestry_bitset();
                    softwareList.add(sb.toString());
//                    laidpRunner.startRun();
                    software_contingencyTable.add(laidpRunner.contingencyTable_2way_bitset(inferredValue,
                            actual_values));
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
