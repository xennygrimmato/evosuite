package org.evosuite.ga.seeding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;

public class TestBIMethodSeedingTestSuiteChromosomeFactory extends SystemTest {

	ChromosomeSampleFactory defaultFactory = new ChromosomeSampleFactory();
	TestSuiteChromosome bestIndividual;
	GeneticAlgorithm<TestSuiteChromosome> ga;

	@Before
	public void setup() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Class1.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		ga = (GeneticAlgorithm<TestSuiteChromosome>) getGAFromResult(result);
		bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();
	}

	@Test
	public void testNotSeed() {
		Properties.SEED_PROBABILITY = 0;
		BIMethodSeedingTestSuiteChromosomeFactory bicf = new BIMethodSeedingTestSuiteChromosomeFactory(
				defaultFactory, bestIndividual);
		TestSuiteChromosome chromosome = bicf.getChromosome();
		
		boolean containsSeededMethod = false;
		for (int i = 0; i < chromosome.getTests().size(); i++){
			if (!chromosome.getTests().get(i).equals(ChromosomeSampleFactory.CHROMOSOME.getTests().get(i))){
				containsSeededMethod = true;
			}
		}
		assertFalse(containsSeededMethod);
	}

	@Test
	public void testBIMethod() {
		//probability is SEED_PROBABILITY/test cases, so 10 guarentees a seed
		Properties.SEED_PROBABILITY = 10;
		BIMethodSeedingTestSuiteChromosomeFactory factory = new BIMethodSeedingTestSuiteChromosomeFactory(
				defaultFactory, bestIndividual);
		TestSuiteChromosome chromosome = factory.getChromosome();
		boolean containsSeededMethod = false;
		for (TestCase t : chromosome.getTests()) {
			for (TestCase t2 : bestIndividual.getTests()) {
				if (t.equals(t2)) {
					containsSeededMethod = true;
				}
			}
		}
		assertTrue(containsSeededMethod);
	}

}