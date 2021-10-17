package readbiomed.pathogens.dataset.toxins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.pathogens.dataset.utils.Utils;

@Command(name = "ToxinBuildDataset", mixinStandardHelpOptions = true, version = "ToxinBuildDataset 0.1", description = "Builds a collection of MEDLINE citations about toxins based on information obtained using MeSH.")
public class ToxinBuildDataset implements Callable <Integer> {
	
	public static Map<String, Set<String>> readToxinEntries(String folderName)
			throws JAXBException, FileNotFoundException {

		Map<String, Set<String>> toxinEntries = new HashMap<>();

		for (File file : new File(folderName).listFiles()) {
			if (file.getName().endsWith(".xml")) {
				JAXBContext context = JAXBContext.newInstance(ToxinEntry.class);
				Unmarshaller um = context.createUnmarshaller();
				ToxinEntry entry = (ToxinEntry) um.unmarshal(new FileReader(file));
				System.out.println(entry.getName());

				toxinEntries.put("toxin-" + entry.getName().toLowerCase(), new HashSet<>(entry.getMeSHPMIDs()));
			}
		}

		return toxinEntries;
	}
	
	public static Collection <String> getPMIDList(String folderName)
			throws JAXBException, FileNotFoundException {

		Set <String> pmids = new HashSet<>();

		for (File file : new File(folderName).listFiles()) {
			if (file.getName().endsWith(".xml")) {
				JAXBContext context = JAXBContext.newInstance(ToxinEntry.class);
				Unmarshaller um = context.createUnmarshaller();
				ToxinEntry entry = (ToxinEntry) um.unmarshal(new FileReader(file));
				System.out.println(entry.getName());

				pmids.addAll(entry.getMeSHPMIDs());
			}
		}

		return pmids;
	}

	@Parameters(index = "0", description = "Folder where the XML of the recovered pathogens from NCBI are placed.")
	private String inputFolderName;

	@Parameters(index = "1", description = "The folder there the MEDLINE citations will be stored in.")
	private String outputFolderName;
	
	@Override
	public Integer call() throws Exception {
		Collection <String> pmids = getPMIDList(inputFolderName);
		System.out.println("Unique documents: " + pmids.size());
		Utils.recoverPubMedCitations(pmids, outputFolderName);
		return 0;
	}
	
	public static void main(String[] argc) throws JAXBException, IOException, InterruptedException {
		int exitCode = new CommandLine(new ToxinBuildDataset()).execute(argc);
        System.exit(exitCode);
	}
}