package readbiomed.bmip.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Prepare collection data set based on information previously collected from
 * NCBI taxonomy
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class BuildDataset {

	private static final int maxEFetchPMIDs = 400;

	private static void urlStreamToFile(String queryURL, String fileName) throws InterruptedException, IOException {
		int count = 10;

		while (count > 0) {
			try {
				try (InputStream in = new URL(queryURL).openStream();
						OutputStream out = new GZIPOutputStream(new FileOutputStream(fileName))) {
					in.transferTo(out);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Thread.sleep(250);
			count--;
		}

		if (count == 0) {
			throw new IOException("We tried too many times");
		}

		Thread.sleep(250);
	}

	private static void recoverPubMedCitations(Collection<String> pmids, String folderName)
			throws IOException, InterruptedException {
		// If folder does not exist, create it
		Path path = Files.createDirectories(Paths.get(folderName + "/PubMed"));

		StringBuilder pmidList = new StringBuilder();

		int fileCount = 0;
		int pmidCount = 0;
		int recovered = 0;

		System.out.println("Recovering " + pmids.size() + " MEDLINE citations");
		for (String pmid : pmids) {
			if (pmidCount == maxEFetchPMIDs) {
				String queryURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="
						+ pmidList.toString().substring(1) + "&retmode=xml";

				urlStreamToFile(queryURL, path.toString() + "/pmids" + fileCount + ".xml.gz");

				fileCount++;
				pmidCount = 0;
				pmidList.setLength(0);
				System.out.println("Recovered " + recovered + " citations.");
			}

			pmidList.append(",").append(pmid);
			pmidCount++;
			recovered++;
		}

		if (pmidList.length() > 0) {
			String queryURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="
					+ pmidList.toString().substring(1) + "&retmode=xml";

			urlStreamToFile(queryURL, path.toString() + "/pmids" + fileCount + ".xml.gz");
		}

		System.out.println("Finished recovering.");
	}

	private static DocumentEntry getDocumentEntry(Map<String, DocumentEntry> documentMap, String pmid) {
		DocumentEntry entry = documentMap.get(pmid);

		if (entry == null) {
			entry = new DocumentEntry(pmid);
			documentMap.put(pmid, entry);
		}

		return entry;
	}

	public static Map<String, DocumentEntry> readDocumentEntries(String folderName)
			throws JAXBException, FileNotFoundException {

		Map<String, DocumentEntry> documentMap = new HashMap<>();

		for (File file : new File(folderName).listFiles()) {
			if (file.getName().endsWith(".xml")) {
				JAXBContext context = JAXBContext.newInstance(NCBIEntry.class);
				Unmarshaller um = context.createUnmarshaller();
				NCBIEntry entry = (NCBIEntry) um.unmarshal(new FileReader(file));
				System.out.println(entry.getScientificName());

				for (String pmid : entry.getMeSHPMIDs()) {
					getDocumentEntry(documentMap, pmid).getMeSHTaxon().add(entry.getId());
				}

				for (String pmid : entry.getGeneBankPMIDs()) {
					getDocumentEntry(documentMap, pmid).getGeneBankTaxon().add(entry.getId());
				}
			}
		}
		
		return documentMap;
	}

	// To consider
	// 1. A given citation or full text document might be related to more than one
	// pathogen
	// 2. A given pathogen might appear in several citations or documents, but
	// sources might not agree, e.g. a pathogen might appear in a full text article
	// from PMC but might not appear in a PubMed citation identified by MeSH
	// indexing
	// 3. Processing is going to be done based on citation/document
	// 4. PubMed citations might not contain an abstract
	// 5. Some PubMed Central documents might not be in the Open Access set
	public static void main(String[] argc) throws JAXBException, IOException, InterruptedException {
		String inputFolderName = argc[0];
		String outputFolder = argc[1];
		
		Map<String, DocumentEntry> documentMap = readDocumentEntries(inputFolderName);

		System.out.println("Unique documents: " + documentMap.size());

		// Collect documents in independent files. One folder for PMIDs and another one
		// for PMCIDs
		recoverPubMedCitations(documentMap.keySet(), outputFolder);
	}
}