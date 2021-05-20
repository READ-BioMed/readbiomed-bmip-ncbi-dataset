package readbiomed.bmip.dataset.NCBITaxonomy;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "document")
public class DocumentEntry {
	private String pmid = null;
	private String pmcid = null;

	@XmlElement(name = "PMCTaxon")
	private Set<String> pmcTaxon = new HashSet<String>();

	@XmlElement(name = "GeneBankTaxon")
	private Set<String> geneBankTaxon = new HashSet<String>();

	@XmlElement(name = "MeSHTaxon")
	private Set<String> meshTaxon = new HashSet<String>();

	public DocumentEntry() {
	}

	public DocumentEntry(String pmid) {
		this.pmid = pmid;
	}
	
	public DocumentEntry(String pmid, String pmcid) {
		this.pmid = pmid;
		this.pmcid = pmcid;
	}	

	public String getPMID() {
		return pmid;
	}

	public void setPMID(String pmid) {
		this.pmid = pmid;
	}

	public String getPMCId() {
		return pmcid;
	}

	public void setPMCId(String pmcid) {
		this.pmcid = pmcid;
	}

	public Set<String> getPMCTaxon() {
		return pmcTaxon;
	}

	public Set<String> getGeneBankTaxon() {
		return geneBankTaxon;
	}

	public Set<String> getMeSHTaxon() {
		return meshTaxon;
	}
}