package readbiomed.pathogens.dataset.toxins;

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

@Command(name = "ToxinDocuments", mixinStandardHelpOptions = true, version = "ToxinDocuments 0.1", description = "Recover information about toxins from NCBI.")
public class ToxinDocuments implements Callable<Integer> {

	public final static String[][] toxins = { { "Abrus abrin toxin", "Abrus abrin toxin" },
			{ "Aflatoxin", "Aflatoxins" }, { "Anatoxin-A", "Anatoxin-A" }, { "Batrachotoxin", "Batrachotoxin" },
			{ "Botulinum toxin", "Botulinum toxins" }, { "Brevetoxin", "Brevetoxin" }, { "Ciguatoxin", "Ciguatoxins" },
			{ "Conotoxin", "Conotoxins" }, { "decarbamoylsaxitoxin", "decarbamoylsaxitoxin" },
			{ "Fusariotoxins (T-2)", "Fusariotoxins (T-2)" }, { "gonyautoxins", "gonyautoxins" },
			{ "Maitotoxin", "Maitotoxin" }, { "Mycotoxin", "Mycotoxin" }, { "neosaxitoxin", "neosaxitoxin" },
			{ "Palytoxin", "Palytoxin" }, { "Ricinus ricin toxin", "Ricinus ricin toxin" },
			{ "saxitoxin", "Saxitoxin" }, { "Staphylococcus enterotoxin", "Enterotoxins" },
			{ "Tetrodotoxin", "Tetrodotoxin" } };

	private static void getToxinDocuments(String fileName, String toxin, String outputFolderName)
			throws IOException, InterruptedException, JAXBException {
		ToxinEntry entry = new ToxinEntry(toxin);

		Elements esMeSHPMIDs = Utils.getIds(
				"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=\"" + toxin + "\"[MH]");
		if (esMeSHPMIDs != null) {
			for (Element eMeSHPMIDs : esMeSHPMIDs) {
				entry.getMeSHPMIDs().add(eMeSHPMIDs.text());
			}
		}

		File output = new File(outputFolderName, fileName + ".xml");
		JAXBContext context = JAXBContext.newInstance(ToxinEntry.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(entry, output);
	}

	@Parameters(index = "0", description = "Folder where the output will be stored.")
	private String outputFolderName;

	@Override
	public Integer call() throws Exception {
		for (String[] toxin : toxins) {
			System.out.println("*" + toxin[0] + "*");
			getToxinDocuments(toxin[0], toxin[1], outputFolderName);
		}
		return 0;
	}

	public static void main(String[] argc) throws IOException, InterruptedException, JAXBException {
		int exitCode = new CommandLine(new ToxinDocuments()).execute(argc);
		System.exit(exitCode);
	}
}