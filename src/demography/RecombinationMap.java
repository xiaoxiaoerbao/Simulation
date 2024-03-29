package demography;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import utils.IOTool;
import utils.PStringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RecombinationMap {
    int[] pos;
    double[] recombinationRate; // cM/Mb
    double[] map; // cM

    double meanRate;

    public RecombinationMap(File recombinationMapFile){
        IntList posList = new IntArrayList();
        DoubleList rateList = new DoubleArrayList();
        DoubleList mapList = new DoubleArrayList();
        try (BufferedReader br = IOTool.getReader(recombinationMapFile)) {
            String line;
            List<String> temp;
            br.readLine();
            while ((line=br.readLine())!=null){
                temp = PStringUtils.fastSplit(line);
                posList.add(Integer.parseInt(temp.get(0)));
                rateList.add(Double.parseDouble(temp.get(1)));
                mapList.add(Double.parseDouble(temp.get(2)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.pos = posList.toIntArray();
        this.recombinationRate = rateList.toDoubleArray();
        this.map = mapList.toDoubleArray();
        this.meanRate=this.getMeanRate();
    }

    private double getMeanRate(){
        int posNum = this.map.length;
        double geneticsDistance = map[posNum-1] - map[0];
        double bp = pos[posNum-1] - pos[0];
        return geneticsDistance/bp;
    }

    /**
     *
     * @param startPos inclusive
     * @param endPos inclusive
     * @return map (cM)
     */
    public double getDistance(int startPos, int endPos){
        int maxPos = pos[pos.length-1];
        endPos = Math.min(endPos, maxPos);
        int startHit = Arrays.binarySearch(pos, startPos);
        int endHit = Arrays.binarySearch(pos, endPos);
        int startIndex = startHit < 0 ? -startHit-1 : startHit;
        int endIndex = endHit < 0 ? -endHit-1 : endHit;
        return Math.max(this.meanRate, map[endIndex] - map[startIndex]);
    }
}
