package readbiomed.bmip.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EntrezTaxonomyDocuments {
	
	private static final int SLEEP_TIME = 50;
	
	/*
	 * Try 10 times to make a call to the NCBI
	 */
	private static Document queryNCBI(String url) throws IOException, InterruptedException {
		int count = 10;
		while (count > 0) {
			try {
				Document doc = Jsoup.connect(url).timeout(300 * 1000).get();
				Thread.sleep(SLEEP_TIME);
				return doc;
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(SLEEP_TIME);
			count--;
		}

		throw new IOException("We tried too many times");
	}

	private static String getScientificName(String id) throws IOException, InterruptedException {
		Document doc = queryNCBI("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=" + id);
		return doc.getElementsByTag("TaxaSet").first().getElementsByTag("Taxon").first()
				.getElementsByTag("ScientificName").first().text();
	}

	private static String getOtherNames(String id) throws IOException, InterruptedException {
		Document doc = queryNCBI("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=" + id);
		Elements otherNames = doc.getElementsByTag("TaxaSet").first().getElementsByTag("Taxon").first()
				.getElementsByTag("OtherNames");

		if (otherNames != null) {
			Element on = otherNames.first();
			if (otherNames.first() != null) {
				return on.toString();
			}
		}

		return null;
	}

	private static Elements getIds(String url) throws IOException, InterruptedException {
		Document doc = queryNCBI(url);

		try {
			Element result = doc.getElementsByTag("eSearchResult").first();
			int count = Integer.parseInt(result.getElementsByTag("Count").first().text());
			int retMax = Integer.parseInt(result.getElementsByTag("RetMax").first().text());

			if (count > retMax) {
				doc = queryNCBI(url + "&retmax=" + count);
				result = doc.getElementsByTag("eSearchResult").first();
				count = Integer.parseInt(result.getElementsByTag("Count").first().text());
			}

			if (count > 0) {
				return result.getElementsByTag("IdList").first().getElementsByTag("Id");
			}
		} catch (Exception e) {
			System.err.println(url);
			e.printStackTrace();
		}

		return null;
	}

	private static void getSubSpecies(NCBIEntry entry) throws IOException, InterruptedException {
		// Check for children and create entry
		Elements es = getIds("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=\""
				+ entry.getScientificName() + "\"[next level]");

		if (es != null) {
			for (Element e : es) {
				String id = e.text();
				// Get scientific name
				NCBIEntry subspecies = new NCBIEntry(id, getScientificName(id));
				subspecies.setOtherNames(getOtherNames(subspecies.getId()));
				getPMCIDs(subspecies);
				getGeneBankPMIDs(subspecies);
				getMeSHIds(subspecies);

				// Add subspecies and repeat the process
				getSubSpecies(subspecies);

				entry.getChildren().add(subspecies);
			}
		}
	}

	private static String getId(String speciesName) throws IOException, InterruptedException {
		Document doc = queryNCBI("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=\""
				+ speciesName + "\"[scientific name]");

		Element result = doc.getElementsByTag("eSearchResult").first();

		int count = Integer.parseInt(result.getElementsByTag("Count").first().text());

		if (count == 1 && result.getElementsByTag("ErrorList").toString().length() == 0) {
			return result.getElementsByTag("IdList").first().getElementsByTag("Id").text();
		} else {
			// Check for any name
			Document doc2 = queryNCBI("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=\""
					+ speciesName + "\"");

			Element result2 = doc2.getElementsByTag("eSearchResult").first();

			int count2 = Integer.parseInt(result2.getElementsByTag("Count").first().text());

			if (count2 == 1 && result2.getElementsByTag("ErrorList").toString().length() == 0) {
				return result2.getElementsByTag("IdList").first().getElementsByTag("Id").text();
			}
		}

		return null;
	}

	private static void getPMCIDs(NCBIEntry entry) throws IOException, InterruptedException {
		Elements es = getIds("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pmc&term=txid"
				+ entry.getId() + "[Organism]");

		if (es != null) {
			for (Element e : es) {
				entry.getPMCIDs().add(e.text());
			}
		}
	}

	private static void getGeneBankPMIDs(NCBIEntry entry) throws IOException, InterruptedException {
		// Find the genes
		Elements esGenes = getIds(
				"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=" + entry.getId() + "[TaxID]");

		if (esGenes != null) {
			Set<String> pmids = new HashSet<>();

			// Find the PMIDs for each gene
			for (Element eGene : esGenes) {
				// Adding a longer time out in case there are many PMIDs to download
				Document doc = queryNCBI("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=gene&id="
						+ eGene.text() + "&format=xml");

				for (Element ePMID : doc.select("PubMedId")) {
					pmids.add(ePMID.text());
				}
			}

			entry.getGeneBankPMIDs().addAll(pmids);
		}
	}

	private static void getMeSHIds(NCBIEntry entry) throws IOException, InterruptedException {
		// Find the genes
		Elements esMeSH = getIds("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=mesh&term=\""
				+ entry.getScientificName() + "\"[MH]");

		if (esMeSH != null && esMeSH.size() == 1) {
			// Get MeSH Tree
			int count = 10;

			while (count > 0) {
				try {
					try (BufferedReader in = new BufferedReader(new InputStreamReader(
							new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=mesh&id="
									+ esMeSH.first().text()).openStream()))) {

						String inputLine;
						while ((inputLine = in.readLine()) != null) {
							if (inputLine.startsWith("Tree Number(s):")) {
								entry.setMeSHTree(inputLine.replaceAll("Tree Number\\(s\\):", "").trim());
							}
						}
					}

					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.sleep(SLEEP_TIME);
				count--;
			}

			if (count == 0) {
				throw new IOException("We tried too many times");
			}

			Thread.sleep(SLEEP_TIME);

			// Get PMIDs
			Elements esMeSHPMIDs = getIds("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=\""
					+ entry.getScientificName() + "\"[MH]");

			if (esMeSHPMIDs != null) {
				for (Element eMeSHPMIDs : esMeSHPMIDs) {
					entry.getMeSHPMIDs().add(eMeSHPMIDs.text());
				}
			}

		}
	}

	public static NCBIEntry taxonomyDocuments(String speciesName) throws IOException, InterruptedException {
		String id = getId(speciesName);

		if (id != null) {
			NCBIEntry entry = new NCBIEntry(id, speciesName);
			entry.setOtherNames(getOtherNames(entry.getId()));
			getPMCIDs(entry);
			getGeneBankPMIDs(entry);
			getMeSHIds(entry);

			getSubSpecies(entry);

			return entry;
		}

		// PMCID and PMID conversion?
		// https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/?tool=my_tool&email=my_email@example.com&ids=PMC3531190&versions=no

		// Do the same for the children of this entry

		return null;
	}

	public static void main(String[] argc) throws JAXBException, IOException, InterruptedException {
		// Open CSV file and read species names
		String pathogenFile = argc[0];
		String outputFolder = argc[1];

		while (true)

			try (BufferedReader b = new BufferedReader(new FileReader(pathogenFile))) {

				String line;
				boolean processed = false;
				while ((line = b.readLine()) != null) {
					String pathogenName = line.replaceAll("/", "-").trim();

					if (!new File(outputFolder, pathogenName + ".xml").exists()) {
						processed = true;
						System.out.println("*" + pathogenName + "*");
						// For each species
						NCBIEntry e = taxonomyDocuments(pathogenName);

						if (e == null) {
							e = new NCBIEntry("-1", "Not found");
						}

						File output = new File(outputFolder, pathogenName + ".xml");
						JAXBContext context = JAXBContext.newInstance(NCBIEntry.class);
						Marshaller m = context.createMarshaller();
						m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
						m.marshal(e, output);

						// Unmarshaller um = context.createUnmarshaller();
						// NCBIEntry out = (NCBIEntry) um.unmarshal(new StringReader(sw.toString()));
					}
				}

				if (!processed) {
					System.out.println("Finished processing");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
