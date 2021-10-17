package readbiomed.pathogens.dataset.PrPSc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PrPSc")
public class PrPScEntry {
	private String species = null;
	private String PubMedQuery = null;

	@XmlElement(name = "MeSHPMID")
	private List<String> meshPMIDs = new ArrayList<String>();

	public PrPScEntry() {
	}

	public PrPScEntry(String species, String PubMedQuery) {
		this.species = species;
		this.PubMedQuery = PubMedQuery;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getPubMedQuery() {
		return PubMedQuery;
	}
	
	public void setPubMedQuery(String PubMedQuery) {
		this.PubMedQuery = PubMedQuery;
	}

	public List<String> getMeSHPMIDs() {
		return meshPMIDs;
	}
}