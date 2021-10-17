package readbiomed.pathogens.dataset.PrPSc;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.pathogens.dataset.utils.Utils;

@Command(name = "PrPScDocuments", mixinStandardHelpOptions = true, version = "PrPScDocuments 0.1", description = "Recover information about PrPSc prions from NCBI.")
public class PrPScDocuments implements Callable<Integer> {

	public static final String[][] species = { { "cattle", "cattle" }, { "cat", "cats" }, { "deer", "deer" },
			{ "elk", "elk" }, { "goat", "goats" }, { "greater kudu", "greater kudu" }, { "human", "humans" },
			{ "mink", "mink" }, { "moose", "moose" }, { "mule", "mule" }, { "nyala", "nyala" }, { "onyx", "oryx" },
			{ "ostrich", "ostrich" }, { "sheep", "sheep" } };

	private static void getPrPScDocuments(String speciesFileName, String speciesMH, String outputFolderName)
			throws IOException, InterruptedException, JAXBException {
		String query = "\"PrPSc Proteins\"[MH] AND \"" + speciesMH + "\"[MH]";
		PrPScEntry entry = new PrPScEntry(speciesFileName, query);

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

	@Parameters(index = "0", description = "Folder where the output will be stored.")
	private String outputFolderName;

	@Override
	public Integer call() throws Exception {
		for (String[] s : species) {
			System.out.println("*" + s[0] + "*");
			getPrPScDocuments(s[0], s[1], outputFolderName);
		}
		return 0;
	}

	public static void main(String[] argc) throws IOException, InterruptedException, JAXBException {
		int exitCode = new CommandLine(new PrPScDocuments()).execute(argc);
		System.exit(exitCode);
	}
}