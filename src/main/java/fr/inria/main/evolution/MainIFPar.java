package fr.inria.main.evolution;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.loop.evolutionary.ParRepair;
import fr.inria.astor.core.loop.evolutionary.population.FitnessPopulationController;
import fr.inria.astor.core.loop.evolutionary.population.ProgramVariantFactory;
import fr.inria.astor.core.loop.evolutionary.spaces.implementation.ParUniformRandomRepairOperatorSpace;
import fr.inria.astor.core.loop.evolutionary.spaces.implementation.spoon.processor.AbstractFixSpaceProcessor;
import fr.inria.astor.core.loop.evolutionary.spaces.implementation.spoon.processor.IFConditionFixSpaceProcessor;
import fr.inria.astor.core.loop.evolutionary.spaces.implementation.spoon.processor.IFExpressionFixSpaceProcessor;
import fr.inria.astor.core.loop.evolutionary.spaces.implementation.spoon.processor.LoopExpressionFixSpaceProcessor;
import fr.inria.astor.core.loop.evolutionary.spaces.ingredients.BasicFixSpace;
import fr.inria.astor.core.loop.evolutionary.spaces.ingredients.UniformRandomFixSpace;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.TransformationProperties;
import fr.inria.astor.core.validation.validators.ProcessValidator;
import fr.inria.main.AbstractMain;

/**
 *  Main for version of PAR that repairs If conditions
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class MainIFPar extends AbstractMain {

	/**
	 * @param args
	 * @throws Exception
	 * @throws ParseException
	 */
	public static void main(String[] args) throws Exception {

		MainIFPar m = new MainIFPar();
		m.execute(args);

	}

	@Override
	public void run(String location, String projectName, String dependencies, String packageToMine,
			double thfl, String failing) throws Exception {
		String method = this.getClass().getSimpleName();

		if (thfl > 0)
			TransformationProperties.THRESHOLD_SUSPECTNESS = thfl;

		List<String> failingList = Arrays.asList(new String[] { failing });

		rep = getProject(location, projectName, method, failing, failingList, dependencies, false);

		rep.getProperties().setExperimentName(this.getClass().getSimpleName());

		rep.init(ProgramVariant.DEFAULT_ORIGINAL_VARIANT);
		
		MutationSupporter mutSupporter = new MutationSupporter(getFactory());
				
		ParRepair parloop = new ParRepair(mutSupporter, rep);
		//parloop.setCurrentStat(currentStat);
		
		List<AbstractFixSpaceProcessor<?>> suspiciousProcessor = new ArrayList<AbstractFixSpaceProcessor<?>>();
		suspiciousProcessor.add(new IFConditionFixSpaceProcessor());
		parloop.setVariantFactory(new ProgramVariantFactory(suspiciousProcessor));
				
		List<AbstractFixSpaceProcessor<?>> ingredientsProcessors = new ArrayList<AbstractFixSpaceProcessor<?>>();
		ingredientsProcessors.add(new LoopExpressionFixSpaceProcessor());
		ingredientsProcessors.add(new IFExpressionFixSpaceProcessor());
		
		// We include the new location fix space
		parloop.setFixspace(new BasicFixSpace(ingredientsProcessors));
		parloop.setRepairActionSpace(new ParUniformRandomRepairOperatorSpace());
		parloop.setPopulationControler(new FitnessPopulationController());
				
		List<SuspiciousCode> candidates = rep.getSuspicious(rep.getProperties().getPackageToInstrument(),
				ProgramVariant.DEFAULT_ORIGINAL_VARIANT);
		List<SuspiciousCode> filtercandidates = new ArrayList<SuspiciousCode>();

		parloop.setProgramValidator(new ProcessValidator());
		//

		for (SuspiciousCode suspiciousCode : candidates) {
			if (!suspiciousCode.getClassName().endsWith("Exception")) {
				filtercandidates.add(suspiciousCode);
			}
		}
	//	currentStat.fl_size = filtercandidates.size();
	//	currentStat.fl_threshold = TransformationProperties.THRESHOLD_SUSPECTNESS;

		assertNotNull(candidates);
		assertTrue(candidates.size() > 0);
		try {
			parloop.start(filtercandidates);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// currentStat.printStats();

	}

	@Override
	public void run(String location, String projectName, String dependencies,  String packageToMine)
			throws Exception {
		throw new IllegalArgumentException("Functionality not provided");

	}

}
