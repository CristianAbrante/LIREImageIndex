package pokemon.lireapp;

import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.surf.SurfFeature;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PokemonFeatureExtractor implements LocalFeatureExtractor {
    List<SurfFeature> features = null;

    @Override
    public List<? extends LocalFeature> getFeatures() {
        return features;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return SurfFeature.class;
    }

    @Override
    public void extract(BufferedImage bufferedImage) {

    }
}
