package readbiomed.pathogens.dataset.toxins;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "toxin")
public class ToxinEntry {
	private String name = null;

	@XmlElement(name = "MeSHPMID")
	private List<String> meshPMIDs = new ArrayList<String>();

	public ToxinEntry() {
	}

	public ToxinEntry(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getMeSHPMIDs() {
		return meshPMIDs;
	}
}