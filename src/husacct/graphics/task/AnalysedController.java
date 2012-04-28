package husacct.graphics.task;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.AbstractDTO;
import husacct.common.dto.AnalysedModuleDTO;
import husacct.common.dto.DependencyDTO;
import husacct.common.dto.ViolationDTO;
import husacct.control.IControlService;
import husacct.control.ILocaleChangeListener;
import husacct.graphics.presentation.decorators.DTODecorator;
import husacct.graphics.presentation.decorators.Decorator;
import husacct.graphics.presentation.decorators.DependenciesDecorator;
import husacct.graphics.presentation.decorators.ViolationsDecorator;
import husacct.graphics.presentation.figures.BaseFigure;
import husacct.validate.IValidateService;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.jhotdraw.draw.ConnectionFigure;

public class AnalysedController extends BaseController {

	private IControlService controlService;
	private IAnalyseService analyseService;
	private IValidateService validateService;
	private Logger logger = Logger.getLogger(AnalysedController.class);

	public AnalysedController() {
		super();
		analyseService = ServiceProvider.getInstance().getAnalyseService();
		validateService = ServiceProvider.getInstance().getValidateService();
		controlService = ServiceProvider.getInstance().getControlService();

		controlService.addLocaleChangeListener(new ILocaleChangeListener() {
			@Override
			public void update(Locale newLocale) {
				getAndDrawModulesIn(currentPath);
				if(violationsAreShown()){
					drawViolationsForShownModules();
				}
			}
		});
	}

	public void drawArchitecture(DrawingDetail detail) {
		AbstractDTO[] modules = analyseService.getRootModules();
		this.resetCurrentPath();
		this.drawModules(modules);
		
		if(detail == DrawingDetail.WITH_VIOLATIONS){
			this.showViolations();
		}
		this.drawLinesBasedOnSetting();
	}

	protected void drawModules(AbstractDTO[] modules) {
		super.drawModules(modules);
	}
	
	// Dependencies
	
	private void drawDependenciesForShownModules(){
		for (BaseFigure figureFrom : this.drawing.getShownModules()) {
			for (BaseFigure figureTo : this.drawing.getShownModules()) {
				getAndDrawDependencyBetween(figureFrom, figureTo);
			}
		}
	}
	
	private void getAndDrawDependencyBetween(BaseFigure figureFrom, BaseFigure figureTo){
		AnalysedModuleDTO dtoFrom = (AnalysedModuleDTO) this.figureDTOMap.get(figureFrom);
		AnalysedModuleDTO dtoTo = (AnalysedModuleDTO) this.figureDTOMap.get(figureTo);
		
		DependencyDTO[] dependencies = getDependenciesBetween(dtoFrom.uniqueName, dtoTo.uniqueName);
		
		try{
			BaseFigure dependencyFigure = this.figureFactory.createFigure(dependencies);
			this.connectionStrategy.connect((ConnectionFigure) ((Decorator) dependencyFigure).getDecorator(), figureFrom, figureTo);
			drawing.add(dependencyFigure);
		} catch (RuntimeException e) {
			logger.debug(e.getMessage() + " " + dtoFrom.uniqueName + " -> " + dtoTo.uniqueName);
		}
	}
	
	private DependencyDTO[] getDependenciesBetween(String from, String to) {
		return analyseService.getDependencies(from, to);
	}
	
	// Violations
	
	public void drawViolationsForShownModules() {
		for (BaseFigure figureFrom : this.drawing.getShownModules()) {
			for (BaseFigure figureTo : this.drawing.getShownModules()) {
				getAndDrawViolationBetween(figureFrom, figureTo);
			}
		}
	}
	
	private void getAndDrawViolationBetween(BaseFigure figureFrom, BaseFigure figureTo){
		AnalysedModuleDTO dtoFrom = (AnalysedModuleDTO) this.figureDTOMap.get(figureFrom);
		AnalysedModuleDTO dtoTo = (AnalysedModuleDTO) this.figureDTOMap.get(figureTo);
		
		ViolationDTO[] dependencies = getViolationsBetween(dtoFrom.uniqueName, dtoTo.uniqueName);
		
		try{
			BaseFigure violationFigure = this.figureFactory.createFigure(dependencies);
			this.connectionStrategy.connect((ConnectionFigure) ((Decorator) violationFigure).getDecorator(), figureFrom, figureTo);
			drawing.add(violationFigure);
		} catch (RuntimeException e) {
			logger.debug(e.getMessage() + " " + dtoFrom.uniqueName + " -> " + dtoTo.uniqueName);
		}
	}
	
	private ViolationDTO[] getViolationsBetween(String from, String to) {
		return validateService.getViolationsByPhysicalPath(from, to);
	}
	
	// Listener methods

	@Override
	public void moduleZoom(BaseFigure figure) {
		if (isZoomable(figure)) {
			AbstractDTO dto = FigureResolver.resolveDTO(figure);
	
			if (dto.getClass().getSimpleName().equals("AnalysedModuleDTO")) {
	
				AnalysedModuleDTO analysedDTO = ((AnalysedModuleDTO) dto);
				this.setCurrentPath(analysedDTO.uniqueName);
				getAndDrawModulesIn(analysedDTO.uniqueName);
			}
		}
	}
	
	private boolean isZoomable(BaseFigure figure) {
		// FIXME: This code probably doesn't belong in the Controller. This should be made a discussion.
		if (figure instanceof DependenciesDecorator)
			return false;
		else if (figure instanceof DTODecorator)
			return true;
		else if (figure instanceof ViolationsDecorator)
			return true;
			
		return false;
	}

	@Override
	public void moduleZoomOut() {
		AnalysedModuleDTO parentDTO = analyseService.getParentModuleForModule(this.getCurrentPath());
		if (parentDTO != null) {
			this.setCurrentPath(parentDTO.uniqueName);
			this.getAndDrawModulesIn(parentDTO.uniqueName);
		} else {
			logger.debug("Tried to zoom out from " + this.getCurrentPath() + ", but it has no parent.");
			logger.debug("Reverting to the root of the application.");
			drawArchitecture(DrawingDetail.WITHOUT_VIOLATIONS);
		}
	}

	private void getAndDrawModulesIn(String parentName) {
		AnalysedModuleDTO[] children = analyseService.getChildModulesInModule(parentName);
		if (children.length > 0) {
			this.drawModules(children);
			this.drawLinesBasedOnSetting();
		} else {
			logger.debug("Tried to draw modules for " + parentName + ", but it has no children.");
		}
	}

	@Override
	public void exportToImage() {
		// TODO Auto-generated method stub
		System.out.println("Option triggered: Export to image");
	}

	@Override
	public void toggleViolations() {
		super.toggleViolations();
		System.out.println("Option triggered: Toggle violations visiblity");
		this.drawLinesBasedOnSetting();
	}
	
	private void drawLinesBasedOnSetting(){
		if(violationsAreShown()){
			System.out.println("violations");
			this.drawViolationsForShownModules();
			// TODO
			// Loop through all the figures/dtos
			// Request found violations between all combinations
			// Create a relationFigure for the violations
			// Clear dependency lines
			// Add the violation lines to the drawing
			// validateService.getViolationsByPhysicalPath(physicalpathFrom, physicalpathTo);
		}else{
			System.out.println("dep");
			this.drawDependenciesForShownModules();
			// TODO
			// Loop through all the figures/dtos
			// Request found dependencies between all combinations
			// Create a relationFigure for the dependencies
			// Clear violation lines
			// Add the dependency lines to the drawing
			// this.drawDependencies(modules); //Where to get the modules? We do
			// not save them!
		}
	}
}
