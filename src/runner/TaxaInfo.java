package runner;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import utils.IOTool;
import utils.PStringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Taxon	Population	PopulationName
 * tsk_0	4	E
 * tsk_1	4	E
 * tsk_2	4	E
 * tsk_3	4	E
 * tsk_4	4	E
 */
public class TaxaInfo {

    String[] taxa; // unique among all taxa
    int[] populationID; // population ID in tskit
    String[] populationName; // e.g., E, D, C

    public TaxaInfo(String taxaInfo){

        List<String> taxaList = new ArrayList<>();
        IntList populationIDList = new IntArrayList();
        List<String> populationNameList = new ArrayList<>();
        try (BufferedReader br = IOTool.getReader(taxaInfo)) {
            String line;
            List<String> temp;
            br.readLine();
            while ((line=br.readLine())!=null){
                temp = PStringUtils.fastSplit(line);
                taxaList.add(temp.get(0));
                populationIDList.add(Integer.parseInt(temp.get(1)));
                populationNameList.add(temp.get(2));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.taxa = taxaList.toArray(new String[0]);
        this.populationID = populationIDList.toIntArray();
        this.populationName = populationNameList.toArray(new String[0]);
    }

    public int getPopSampleSize(String popName){
//        return this.getPopTaxaListMap().get(popName).size();
        int count = 0;
        for (String populationName : this.populationName){
            if (populationName.equals(popName)){
                count++;
            }
        }
        return count;
    }

    public List<String> getTaxaListOf(String popName){
        List<String> taxaList = new ArrayList<>();
        for (int i = 0; i < this.populationName.length; i++) {
            if (this.populationName[i].equals(popName)){
                taxaList.add(this.taxa[i]);
            }
        }
        return taxaList;
    }

    public String getTaxonPop(String taxon){
        for (int i = 0; i < this.taxa.length; i++) {
            if (this.taxa[i].equals(taxon)){
                return this.populationName[i];
            }
        }
        return null;
    }
}
