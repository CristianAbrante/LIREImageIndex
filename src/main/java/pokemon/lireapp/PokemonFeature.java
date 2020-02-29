package pokemon.lireapp;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PokemonFeature implements GlobalFeature {

    public Map<String, Integer> histogram;

    @Override
    public void extract(BufferedImage bufferedImage) {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        this.histogram = new HashMap<>();
        for(int i=0; i < width; i++) {
            for(int j=0; j < height; j++) {
                int rgb = bufferedImage.getRGB(i, j);
                int[] rgbArr = getRGBArray(rgb);
                String rgbRepresentation = getStringRepresentation(rgbArr);

                // Filter out grays
                if (!isGray(rgbArr)) {
                    Integer counter = this.histogram.get(rgbRepresentation);
                    this.histogram.put(rgbRepresentation, counter == null ? 1 : counter + 1);
                }
            }
        }

       this.sortHistogram();

    }

    @Override
    public String getFeatureName() {
        return "PokemonFeature";
    }

    @Override
    public String getFieldName() {
        return "PokemonField";
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this.histogram);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(byteIn);
            this.histogram = (Map<String, Integer>) in.readObject();
            System.out.println("inside " + this.histogram);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int i, int i1) {

    }

    @Override
    public double getDistance(LireFeature lireFeature) {
        PokemonFeature feature = (PokemonFeature) lireFeature;
        int maxNumberOfElements = 20;
        System.out.println(feature.histogram);
        System.out.println();
        System.out.println(this.histogram);

        String[] currentHistogramFirstColors = this.histogram.keySet().stream().limit(maxNumberOfElements).collect(Collectors.toList()).toArray(new String[0]);
        String[] otherHistogramFirstColors = feature.histogram.keySet().stream().limit(maxNumberOfElements).collect(Collectors.toList()).toArray(new String[0]);

        HashSet<String> set = new HashSet<>();
        set.addAll(Arrays.asList(currentHistogramFirstColors));
        set.retainAll(Arrays.asList(otherHistogramFirstColors));
        return set.size();
    }

    @Override
    public double[] getFeatureVector() {
        double[] itemsArray = new double[this.histogram.values().size()];
        for (int i =0  ; i<  this.histogram.values().size(); i++) {
            itemsArray[i] = this.histogram.values().toArray(new Double[0])[i];
        }
        return itemsArray;
    }

    public static int[] getRGBArray(int pixel) {
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{red,green,blue};
    }

    public static String getStringRepresentation(int rgb) {
        return getStringRepresentation(getRGBArray(rgb));
    }

    public static String getStringRepresentation(int[] rgbArr) {
        return rgbArr[0] + " " + rgbArr[1] + " " + rgbArr[2];
    }

    private void sortHistogram() {
        this.histogram = this.histogram.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }


    public static boolean isGray(int[] rgbArr) {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        if (rgDiff > tolerance || rgDiff < -tolerance)
            return rbDiff <= tolerance && rbDiff >= -tolerance;
        return true;
    }
}
