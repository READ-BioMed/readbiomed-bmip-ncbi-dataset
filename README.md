# readbiomed-ncbi-pathogen-dataset-generation

This package has been used for the generation of datasets with information about pathogens and MEDLINE citations associated to them using resources from NCBI (National Center for Biotechnology Information).

This package has code to generate pathogen information in XML format and to build corpora from those XML files for taxonomic pathogens (e.g. bacteria and viruses), PrPSc prions and toxins. 

An example of generated data set is available [here](https://github.com/READ-BioMed/readbiomed-pathogens-dataset).

# Installation

The package has been tested with Java 11 and Maven 3.6.3.

To install it run `mvn install` after cloning this github repository and moved to the local cloned directory.

# Data sets generation

In the following sections, the generation of data sets for the different pathogens is explained.

## Taxonimic pathogens

From the cloned folder, in order to create the files for the taxonomic pathogens using NCBI resources, there are two steps.
In the first one, a set of XML files are generated from a list of pathogens in a text file, one pathogen per line.
An output folder needs to be specified as well.

```
mvn exec:java -Dexec.mainClass="readbiomed.pathogens.dataset.NCBITaxonomy.EntrezTaxonomyDocuments" -Dexec.args="[File_with_list_of_pathogens] [Output_folder]"
```

In the second set, using the files generated in the previous step as input, citations are collected from MEDLINE and placed in the output folder.

```
mvn exec:java -Dexec.mainClass="readbiomed.pathogens.dataset.NCBITaxonomy.BuildDataset" -Dexec.args="[Input_folder] [Output_folder]"
```

## PrPSc prions

From the cloned folder, in order to create the files for the PrPSc prions using NCBI resources, there are two steps.
In the first one, based on a predefined list of prions, a set of XML files is generated in the specified output folder.

```
mvn exec:java -Dexec.mainClass="readbiomed.pathogens.dataset.PrPSc.PrPScDocuments" -Dexec.args="[Output_folder]"
```

In the second step, the XML files are used to recover MEDLINE citations. The XML files are in the input folder, while the MEDLINE citations will be stored in the specified output folder.

```
mvn exec:java -Dexec.mainClass="readbiomed.pathogens.dataset.PrPSc.PrPScBuildDataset" -Dexec.args="[Input_folder] [Output_folder]"
```

## Toxins


From the cloned folder, in order to create the files for the toxins using NCBI resources, there are two steps.
In the first one, based on a predefined list of toxins, a set of XML files is generated in the specified output folder.

```
mvn exec:java -Dexec.mainClass="readbiomed.pathogens.dataset.toxins.ToxinDocuments" -Dexec.args="[Output_folder]"
```

In the second step, the XML files are used to recover MEDLINE citations. The XML files are in the input folder, while the MEDLINE citations will be stored in the specified output folder.

```
mvn exec:java -Dexec.mainClass="readbiomed.pathogens.dataset.toxins.ToxinBuildDataset" -Dexec.args="[Input_folder] [Output_folder]"
```

# References

If you use this work in your research, remember to cite it. More information about our is available in the following paper.

```
@article{jimeno2022classifying,
  title={Classifying literature mentions of biological pathogens as experimentally studied using natural language processing},
  author={Jimeno Yepes, Antonio and Verspoor, Karin},
  journal={Journal of Biomedical Semantics},
  year={2022}
}
```
