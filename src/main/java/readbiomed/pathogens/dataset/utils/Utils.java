package readbiomed.pathogens.dataset.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Utils {
	public static final int SLEEP_TIME = 100;
	
	public static final int TRY_TIMES = 20;

	/*
	 * Try n times to make a call to the NCBI
	 */
	public static Document queryNCBI(String url) throws IOException, InterruptedException {
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

	public static Elements getIds(String url) throws IOException, InterruptedException {
		int ecount = 10;

		while (ecount > 0) {
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
				return null;
			} catch (Exception e) {
				System.err.println(url);
				e.printStackTrace();
			}
			
			ecount--;
			Thread.sleep(SLEEP_TIME);
		}

		throw new IOException("We tried too many times");
	}

	private static final int maxEFetchPMIDs = 400;

	public static void urlStreamToFile(String queryURL, String fileName) throws InterruptedException, IOException {
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

			Thread.sleep(SLEEP_TIME);
			count--;
		}

		if (count == 0) {
			throw new IOException("We tried too many times");
		}

		Thread.sleep(SLEEP_TIME);
	}

	public static void recoverPubMedCitations(Collection<String> pmids, String folderName)
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
}
