package readbiomed.bmip.dataset;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "pathogen")
public class NCBIEntry {
	private String id = null;
	private String scientificName = null;
	private String otherNames = null;

	@XmlElement(name = "subSpecies")
	private List<NCBIEntry> children = new ArrayList<NCBIEntry>();

	@XmlElement(name = "PMCID")
	private List<String> pmcids = new ArrayList<String>();

	@XmlElement(name = "GeneBankPMID")
	private List<String> geneBankPMIDs = new ArrayList<String>();

	private String meshTree = null;

	@XmlElement(name = "MeSHPMID")
	private List<String> meshPMIDs = new ArrayList<String>();

	public NCBIEntry() {
	}

	public NCBIEntry(String id, String scientificName) {
		this.id = id;
		this.scientificName = scientificName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public List<NCBIEntry> getChildren() {
		return children;
	}

	public List<String> getPMCIDs() {
		return pmcids;
	}

	public List<String> getGeneBankPMIDs() {
		return geneBankPMIDs;
	}

	public String getMeSHTree() {
		return meshTree;
	}

	public void setMeSHTree(String meshTree) {
		this.meshTree = meshTree;
	}

	public List<String> getMeSHPMIDs() {
		return meshPMIDs;
	}
}