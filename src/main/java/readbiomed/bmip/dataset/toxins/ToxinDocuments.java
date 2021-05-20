package readbiomed.bmip.dataset.toxins;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import readbiomed.bmip.dataset.utils.Utils;

public class ToxinDocuments {

	private final static String[][] toxins = { { "Abrus abrin toxin", "Abrus abrin toxin" },
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

	public static void main(String[] argc) throws IOException, InterruptedException, JAXBException {
		String outputFolderName = argc[0];

		for (String[] toxin : toxins) {
			System.out.println("*" + toxin[0] + "*");
			getToxinDocuments(toxin[0], toxin[1], outputFolderName);
		}
	}
}