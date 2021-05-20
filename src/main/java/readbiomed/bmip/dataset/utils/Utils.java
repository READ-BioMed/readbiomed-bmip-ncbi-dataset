package readbiomed.bmip.dataset.utils;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Utils {
	public static final int SLEEP_TIME = 50;

	/*
	 * Try 10 times to make a call to the NCBI
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
}
