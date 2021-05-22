package readbiomed.bmip.dataset.PrPSc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import readbiomed.bmip.dataset.utils.Utils;

public class PrPScBuildDataset {

	public static Collection<String> getPMIDList(String folderName) throws JAXBException, FileNotFoundException {

		Set<String> pmids = new HashSet<>();

		for (File file : new File(folderName).listFiles()) {
			if (file.getName().endsWith(".xml")) {
				JAXBContext context = JAXBContext.newInstance(PrPScEntry.class);
				Unmarshaller um = context.createUnmarshaller();
				PrPScEntry entry = (PrPScEntry) um.unmarshal(new FileReader(file));
				System.out.println(entry.getPubMedQuery());

				pmids.addAll(entry.getMeSHPMIDs());
			}
		}

		return pmids;
	}

	public static void main(String[] argc) throws JAXBException, IOException, InterruptedException {
		String inputFolderName = argc[0];
		String outputFolder = argc[1];

		Collection<String> pmids = getPMIDList(inputFolderName);
		System.out.println("Unique documents: " + pmids.size());
		Utils.recoverPubMedCitations(pmids, outputFolder);
	}
}