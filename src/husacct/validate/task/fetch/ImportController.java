package husacct.validate.task.fetch;

import husacct.validate.domain.configuration.ActiveRuleType;
import husacct.validate.domain.configuration.ConfigurationServiceImpl;
import husacct.validate.domain.validation.Severity;
import husacct.validate.domain.validation.Violation;
import husacct.validate.domain.validation.ViolationHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jdom2.Element;

public class ImportController {
	private List<Severity> severities;
	private final ImportFactory importFactory;
	private final ConfigurationServiceImpl configuration;

	public ImportController(ConfigurationServiceImpl configuration) {
		this.configuration = configuration;
		this.importFactory = new ImportFactory();
		this.severities = new ArrayList<Severity>();
	}

	public void importWorkspace(Element element) throws DatatypeConfigurationException{
		importSeverties(element.getChild("severities"));
		
		importSeveritiesPerTypesPerProgrammingLanguages(element.getChild("severitiesPerTypesPerProgrammingLanguages"));
		importActiveViolationTypes(element.getChild("activeViolationTypes"));
		
		//TODO
		//Not used because of memory issues
		//importViolations(element.getChild("violations"));
		//importViolationHistory(element.getChild("violationHistories"));		
	}

	private void importSeverties(Element element) {
		this.severities = importFactory.importSeverities(element);
		configuration.setSeverities(severities);
	}

	/**
	 * @deprecated Not used because of memory issues
	 */
	private void importViolations(Element element) throws DatatypeConfigurationException{
		List<Violation> violations = importFactory.importViolations(element, severities);
		configuration.addViolations(violations);
	}

	private void importSeveritiesPerTypesPerProgrammingLanguages(Element element){
		HashMap<String, HashMap<String, Severity>> severitiesPerTypesPerProgrammingLanguage = importFactory.importSeveritiesPerTypesPerProgrammingLanguages(element, severities);
		configuration.setSeveritiesPerTypesPerProgrammingLanguages(severitiesPerTypesPerProgrammingLanguage);
	}

	/**
	 * @deprecated Not used because of memory issues
	 */
	private void importViolationHistory(Element element) {
		List<ViolationHistory> violationHistory = importFactory.importViolationHistory(element);
		configuration.setViolationHistory(violationHistory);
	}

	private void importActiveViolationTypes(Element element) {
		Map<String, List<ActiveRuleType>> activeViolationTypes = importFactory.importActiveViolationTypes(element);
		configuration.setActiveViolationTypes(activeViolationTypes);
	}
}