package pokemon.lireapp;

import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class PokemonIndexSearcher {
    static String imageKey = "-p";
    static String helpMessage = "$> PokemonIndexSearcher " + imageKey + " <directory>\n" +
            "\n" +
            "Build a lire indexer given a directory of images dataset.\n" +
            "Directory is going to be build in the index/ and index.config/ directory.\n" +
            "Custom feature extractor is using for indexing.\n" +
            "\n" +
            "Options\n" +
            "=======\n" +
            imageKey + " ... the directory with the images, files with .jpg and .png are read. \n";

    public static void main(String[] args) throws IOException {
        // Using for extracting command line options
        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[]{imageKey});

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(PokemonDatasetIndexer.indexDirectory)));

        // make sure that this matches what you used for indexing (see below) ...
        ImageSearcher imgSearcher = new GenericFastImageSearcher(20, PokemonFeature.class);

        // just a static example with a given image.
        ImageSearchHits hits = imgSearcher.search(ImageIO.read(new File(p.getProperty(imageKey))), reader);
        for (int i = 0; i < hits.length(); i++) {
            System.out.printf("%.2f: (%d) %s\n", hits.score(i), hits.documentID(i), reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        }
    }
}
