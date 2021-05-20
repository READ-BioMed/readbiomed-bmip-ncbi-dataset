package readbiomed.bmip.dataset.PrPSc;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import readbiomed.bmip.dataset.utils.Utils;

public class PrPScDocuments {

	private static final String[][] species = { { "cattle", "cattle" }, { "cat", "cats" }, { "deer", "deer" },
			{ "elk", "elk" }, { "goat", "goats" }, { "greater kudu", "greater kudu" }, { "human", "humans" },
			{ "mink", "mink" }, { "moose", "moose" }, { "mule", "mule" }, { "nyala", "nyala" }, { "onyx", "oryx" }, { "ostrich", "ostrich" },
			{ "sheep", "sheep" } };

	private static void getPrPScDocuments(String speciesFileName, String speciesMH, String outputFolderName)
			throws IOException, InterruptedException, JAXBException {
		String query = "\"PrPSc Proteins\"[MH] AND \"" + speciesMH + "\"[MH]";
		PrPScEntry entry = new PrPScEntry(speciesMH, query);

		Elements esMeSHPMIDs = Utils
				.getIds("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + query);
		if (esMeSHPMIDs != null) {
			for (Element eMeSHPMIDs : esMeSHPMIDs) {
				entry.getMeSHPMIDs().add(eMeSHPMIDs.text());
			}
		}

		File output = new File(outputFolderName, "Sc (" + speciesFileName + ").xml");
		JAXBContext context = JAXBContext.newInstance(PrPScEntry.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(entry, output);
	}

	public static void main(String[] argc) throws IOException, InterruptedException, JAXBException {
		String outputFolderName = argc[0];

		for (String [] s : species) {
			System.out.println("*" + s[0] + "*");
			getPrPScDocuments(s[0], s[1], outputFolderName);
		}
	}
}