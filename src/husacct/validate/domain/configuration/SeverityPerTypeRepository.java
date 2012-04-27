package husacct.validate.domain.configuration;

import husacct.validate.domain.exception.ProgrammingLanguageNotFound;
import husacct.validate.domain.exception.SeverityNotFoundException;
import husacct.validate.domain.validation.Severity;

import java.util.HashMap;
import java.util.Map.Entry;

public class SeverityPerTypeRepository {
	private HashMap<String, HashMap<String, Severity>> severitiesPerTypePerProgrammingLanguage;

	public SeverityPerTypeRepository(){
		severitiesPerTypePerProgrammingLanguage = new HashMap<String, HashMap<String, Severity>>();
		//TODO delete test data
		severitiesPerTypePerProgrammingLanguage.put("java", new HashMap<String, Severity>());
	}

	public HashMap<String, HashMap<String, Severity>> getSeveritiesPerTypePerProgrammingLanguage() {
		return severitiesPerTypePerProgrammingLanguage;
	}

	public Severity getSeverity(String language, String key){
		HashMap<String, Severity> severityPerType = severitiesPerTypePerProgrammingLanguage.get(language);
		if(severityPerType == null){
			throw new SeverityNotFoundException();
		}
		else{
			Severity severity = severityPerType.get(key);
			if(severity == null){
				throw new SeverityNotFoundException();
			}
			else{
				return severity;
			}
		}
	}

	public void restoreDefaultSeverity(String language, String key){
		//TODO
	}

	public void restoreAllToDefault(String language){
		//TODO
	}

	public void setSeverityMap(HashMap<String, HashMap<String, Severity>> severitiesPerTypePerProgrammingLanguage){
		this.severitiesPerTypePerProgrammingLanguage = severitiesPerTypePerProgrammingLanguage;
	}

	public void setSeverityMap(String language, HashMap<String, Severity> severityMap) {
		HashMap<String, Severity> local = severitiesPerTypePerProgrammingLanguage.get(language);
		for(Entry<String, Severity> entry : severityMap.entrySet()){
			if(local.containsKey(entry.getKey())){
				local.remove(entry.getKey());
			}
			local.put(entry.getKey(), entry.getValue());
		}
		severitiesPerTypePerProgrammingLanguage.remove(language);
		severitiesPerTypePerProgrammingLanguage.put(language, local);
	}
}