package pokemon.lireapp;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used for extracting the pokemon features.
 *
 * The main idea behind is that Pokemons usually have a predominant color,
 * so a good measure of a feature should be the amount of occurrences
 * of colors in an image.
 */
public class PokemonFeature implements GlobalFeature {

    /**
     * This variable is going to store the structured histogram of the image
     *
     * key => the key is going to be the color in format "r g b"
     * value => number of occurrences, only the ones greater than zero.
     */
    public Map<String, Integer> histogram;

    /**
     * Converts rgb array from a pixel of an image in hex format.
     * @param pixel
     * @return
     */
    public static int[] getRGBArray(int pixel) {
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{red,green,blue};
    }

    /**
     * From the string representation, which is the key of the
     * histogram, we obtain the array of ints representing r,g,b
     * values.
     *
     * @param color
     * @return
     */
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

    /**
     * Returns the string representation given an array of
     * r, g, b values.
     *
     * @param rgbArr
     * @return
     */
    public static String getStringRepresentation(int[] rgbArr) {
        return rgbArr[0] + " " + rgbArr[1] + " " + rgbArr[2];
    }

    /**
     * Tests if color is gray.
     *
     * @param rgbArr
     * @return
     */
    public static boolean isGray(int[] rgbArr) {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        if (rgDiff > tolerance || rgDiff < -tolerance)
            return rbDiff <= tolerance && rbDiff >= -tolerance;
        return true;
    }

    /**
     * Method used for extracting the features of an image.
     *
     * For our purpose this method is going to build the histogram variable.
     * The main idea is to run thought the image with a loop and count the
     * occurrences of each color. And then call an auxiliary method for sorting.
     *
     * @param bufferedImage the image when we have to extract the features from.
     */
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

    /**
     * In a lire application, it is needed to store the features using
     * a bytes array. The way to store the histogram as a byte array is as
     * it follows: first three positions are reserved for the r, g, b components
     * of the color, and then finally we store the number of occurrences of the
     * certain color.
     *
     * @return the byte array representing the histogram.
     */
    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] rawHistogram = new byte[this.histogram.values().size() * 7];
        int index = 0;
        for (Map.Entry<String, Integer> entry : this.histogram.entrySet()) {
            int[] rgbColor = getIntRepresentationFromString(entry.getKey());
            rawHistogram[index] = (byte) rgbColor[0];
            rawHistogram[index + 1] = (byte) rgbColor[1];
            rawHistogram[index + 2] = (byte) rgbColor[2];
            int value = entry.getValue();
            rawHistogram[index + 3] = (byte) (value >>> 24);
            rawHistogram[index + 4] = (byte) (value >>> 16);
            rawHistogram[index + 5] = (byte) (value >>> 8);
            rawHistogram[index + 6] = (byte) (value);

            index += 7;
        }
        return rawHistogram;
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        setByteArrayRepresentation(bytes, 0, 0);
    }

    /**
     * Given an array of bytes storing the histogram representation,
     * we have to build the actual histogram as a map. The way for doing
     * that is looping through the array in the same way that we stored it.
     *
     * @param bytes array of bytes.
     * @param i
     * @param i1
     */
    @Override
    public void setByteArrayRepresentation(byte[] bytes, int i, int i1) {
        this.histogram = new HashMap<>();
        for (int index = 0; index < bytes.length; index += 7) {
            int r = bytes[index] & 0xFF;
            int g = bytes[index + 1] & 0xFF;
            int b = bytes[index + 2] & 0xFF;
            String color = getStringRepresentation(new int[]{r, g, b});
            byte b1 = bytes[index + 3];
            byte b2 = bytes[index + 4];
            byte b3 = bytes[index + 5];
            byte b4 = bytes[index + 6];
            int value = ((0xFF & b1) << 24) | ((0xFF & b2) << 16) |
                    ((0xFF & b3) << 8) | (0xFF & b4);
            this.histogram.put(color, value);
        }
        this.sortHistogram();
    }

    /**
     * Distance function is used to give an score of how close it is
     * a feature vector from the other.
     * Here we used a threshold for selecting the first 75 elements of
     * the sorted histogram. and the make the histogram of them. The distance
     * is the size of the intersection between those subsets of histogram.
     *
     * @param lireFeature
     * @return
     */
    @Override
    public double getDistance(LireFeature lireFeature) {
        PokemonFeature feature = (PokemonFeature) lireFeature;
        int maxNumberOfElements = 100;

        String[] currentHistogramFirstColors = this.histogram.keySet().stream().limit(maxNumberOfElements).collect(Collectors.toList()).toArray(new String[0]);
        String[] otherHistogramFirstColors = feature.histogram.keySet().stream().limit(maxNumberOfElements).collect(Collectors.toList()).toArray(new String[0]);

        HashSet<String> set = new HashSet<>();
        set.addAll(Arrays.asList(currentHistogramFirstColors));
        set.retainAll(Arrays.asList(otherHistogramFirstColors));
        return set.size();
    }

    @Override
    public double[] getFeatureVector() {
        double[] rawHistogram = new double[this.histogram.values().size() * 4];
        int index = 0;
        for (Map.Entry<String, Integer> entry : this.histogram.entrySet()) {
            int[] rgbColor = getIntRepresentationFromString(entry.getKey());
            rawHistogram[index] =  rgbColor[0];
            rawHistogram[index + 1] =  rgbColor[1];
            rawHistogram[index + 2] =  rgbColor[2];
            rawHistogram[index + 3] = entry.getValue().byteValue();
            index += 4;
        }
        return rawHistogram;
    }

    /**
     * Method for sorting the histogram in the reversed order,
     * colors with more occurrences will go fist.
     */
    private void sortHistogram() {
        this.histogram = this.histogram.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
