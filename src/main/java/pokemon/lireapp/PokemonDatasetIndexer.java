package pokemon.lireapp;

import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.indexers.parallel.ImagePreprocessor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import net.semanticmetadata.lire.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Properties;

public class PokemonDatasetIndexer {
    static String indexDirectory = "index";
    static String datasetKey = "-i";
    static String helpMessage = "$> PokemonDatasetIndexer -i <directory>\n" +
            "\n" +
            "Build a lire indexer given a directory of images dataset.\n" +
            "Directory is going to be build in the index/ and index.config/ directory.\n" +
            "Custom feature extractor is using for indexing.\n" +
            "\n" +
            "Options\n" +
            "=======\n" +
            datasetKey + " ... the directory with the images, files with .jpg and .png are read. \n";

    public static void main(String[] args) {
        // Using for extracting command line options
        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[]{datasetKey});

        // input directory
        File dir = new File(p.getProperty(datasetKey));
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println(p.getProperty(datasetKey) + " is not a directory. You should specify a directory");
            System.err.println(helpMessage);
            System.exit(1);
        };

        // use ParallelIndexer to index all photos from args[0] into "index".
        int numOfDocsForVocabulary = 500;
        Class<? extends AbstractAggregator> aggregator = BOVW.class;
        int[] numOfClusters = new int[] {128};

        ParallelIndexer indexer = new ParallelIndexer(
                DocumentBuilder.NUM_OF_THREADS,
                indexDirectory,
                p.getProperty(datasetKey),
                numOfClusters,
                numOfDocsForVocabulary,
                aggregator);

        indexer.setImagePreprocessor(new ImagePreprocessor() {
            @Override
            public BufferedImage process(BufferedImage image) {
                return ImageUtils.createWorkingCopy(image);
            }
        });

        //TODO: Use our own extractor.
        indexer.addExtractor(CvSurfExtractor.class);
        indexer.addExtractor(PokemonFeature.class);

        System.out.println("Starting indexing with custom extractor.");
        indexer.run();
        System.out.println("Indexing finished.");
    }
}
