package readbiomed.bmip.dataset.NCBITaxonomy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import readbiomed.bmip.dataset.utils.Utils;

/**
 * Prepare collection data set based on information previously collected from
 * NCBI taxonomy
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class BuildDataset {

	public static Map<String, Set<String>> readPathogenEntries(String folderName)
			throws JAXBException, FileNotFoundException {

		Map<String, Set<String>> pathogenEntries = new HashMap<>();

		for (File file : new File(folderName).listFiles()) {
			if (file.getName().endsWith(".xml")) {
				JAXBContext context = JAXBContext.newInstance(NCBIEntry.class);
				Unmarshaller um = context.createUnmarshaller();
				NCBIEntry entry = (NCBIEntry) um.unmarshal(new FileReader(file));
				System.out.println(entry.getScientificName());

				Set <String> pmids = new HashSet<>(entry.getMeSHPMIDs());
				
				pathogenEntries.put("pathogen-" + entry.getId(), pmids);
				
				Queue<NCBIEntry> deque = new ArrayDeque<NCBIEntry>();
				deque.add(entry);

				while (deque.size() > 0) {
					NCBIEntry e = deque.poll();
					pmids.addAll(e.getMeSHPMIDs());
					deque.addAll(e.getChildren());
				}
			}
		}

		return pathogenEntries;
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

				String root = entry.getId();

				Queue<NCBIEntry> deque = new ArrayDeque<NCBIEntry>();
				deque.add(entry);

				while (deque.size() > 0) {
					NCBIEntry e = deque.poll();
					for (String pmid : e.getMeSHPMIDs()) {
						getDocumentEntry(documentMap, pmid).getMeSHTaxon().add(root);
					}

					for (String pmid : e.getGeneBankPMIDs()) {
						getDocumentEntry(documentMap, pmid).getGeneBankTaxon().add(root);
					}

					deque.addAll(e.getChildren());
				}
			}
		}

		return documentMap;
	}

	public static Map<String, String> readRootTaxonomyMapping(String folderName)
			throws JAXBException, FileNotFoundException {

		Map<String, String> rootTaxonomyMapping = new HashMap<>();

		for (File file : new File(folderName).listFiles()) {
			if (file.getName().endsWith(".xml")) {
				JAXBContext context = JAXBContext.newInstance(NCBIEntry.class);
				Unmarshaller um = context.createUnmarshaller();
				NCBIEntry entry = (NCBIEntry) um.unmarshal(new FileReader(file));
				System.out.println(entry.getScientificName());

				String root = entry.getId();

				rootTaxonomyMapping.put(root, root);

				Queue<NCBIEntry> deque = new ArrayDeque<NCBIEntry>(entry.getChildren());

				while (deque.size() > 0) {
					NCBIEntry e = deque.poll();
					rootTaxonomyMapping.put(e.getId(), root);
					deque.addAll(e.getChildren());
				}
			}
		}

		return rootTaxonomyMapping;
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
		Utils.recoverPubMedCitations(documentMap.keySet(), outputFolder);
	}
}