package evaluation;


import java.util.Arrays;

/**
 * Kullback–Leibler divergence
 */
public class KLDivergence {

    /**
     *
     * @param px expected
     * @param qx inferred
     * @return Kullback–Leibler divergence
     */
    private static double calculateKLDivergence(double[] px, double[] qx){
        assert px.length == qx.length : "The length px and qx must equal";
        int binBum = px.length;
        double divergence = 0;
        for (int i = 0; i < binBum; i++) {
            divergence += px[i] * Math.log(px[i]/qx[i]);
        }
        return divergence;
    }

    public static double[] calculatePDF(double[] data, int numBins) {
        double[] pdf = new double[numBins+1];
        double min = Arrays.stream(data).min().getAsDouble();
        double max = Arrays.stream(data).max().getAsDouble();
        double binWidth = (max - min) / numBins;
        for (double datum : data) {
            int binIndex = (int) ((datum - min) / binWidth);
            pdf[binIndex]++;
        }
        int zero_count = 0;
        for (double v : pdf) {
            if (v == 0) {
                zero_count++;
            }
        }
        for (int i = 0; i < pdf.length; i++) {
            if (pdf[i] == 0){
                pdf[i] = 1;
            }
            pdf[i] = pdf[i]/(data.length+zero_count);
        }
        return pdf;
    }

    public static double calculate_KLDivergence(double[] expData, double[] obsData, int numBins){
        double[] px = calculatePDF(expData, numBins);
        double[] qx = calculatePDF(obsData, numBins);
        return calculateKLDivergence(px,qx);
    }

}
