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
        byte[] rawHistogram = new byte[(int) Math.pow(256, 3)];
        Arrays.fill(rawHistogram, (byte) 0);

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
        byte[] rawHistogram = new byte[this.histogram.values().size() * 4];
        int index = 0;
        for (Map.Entry<String, Integer> entry : this.histogram.entrySet()) {
            int[] rgbColor = getIntRepresentationFromString(entry.getKey());
            rawHistogram[index] = (byte) rgbColor[0];
            rawHistogram[index + 1] = (byte) rgbColor[1];
            rawHistogram[index + 2] = (byte) rgbColor[2];
            rawHistogram[index + 3] = entry.getValue().byteValue();
            index += 4;
        }
        return rawHistogram;
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        setByteArrayRepresentation(bytes, 0, 0);
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int i, int i1) {
        System.out.println("method executed!");
        this.histogram = new HashMap<>();
        for (int index = 0; index < bytes.length; index += 4) {
            int r = bytes[index] & 0xFF;
            int g = bytes[index + 1] & 0xFF;
            int b = bytes[index + 2] & 0xFF;
            String color = getStringRepresentation(new int[]{r, g, b});
            this.histogram.put(color, bytes[index + 3] & 0xFF);
        }
        this.sortHistogram();
    }

    @Override
    public double getDistance(LireFeature lireFeature) {
        PokemonFeature feature = (PokemonFeature) lireFeature;
        int maxNumberOfElements = 75;

        String[] currentHistogramFirstColors = this.histogram.keySet().stream().limit(maxNumberOfElements).collect(Collectors.toList()).toArray(new String[0]);
        String[] otherHistogramFirstColors = feature.histogram.keySet().stream().limit(maxNumberOfElements).collect(Collectors.toList()).toArray(new String[0]);

        System.out.println(Arrays.toString(currentHistogramFirstColors));
        System.out.println();
        System.out.println(Arrays.toString(otherHistogramFirstColors));

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

    public static int[] getIntRepresentationFromString(String color) {
        String[] splitColor = color.split(" ");
        int[] result = new int[3];
        result[0] = Integer.parseInt(splitColor[0]);
        result[1] = Integer.parseInt(splitColor[1]);
        result[2] = Integer.parseInt(splitColor[2]);
        return result;
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
