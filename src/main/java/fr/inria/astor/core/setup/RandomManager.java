package fr.inria.astor.core.setup;

import java.util.Random;

/**
 * @author Claire Le Goues (CLG), clegoues@cs.cmu.edu
 *         <p>
 *         RandomManager is a static provider of randomness to all interested classes.
 *         This allows the experiment to specify a random seed at configuration time
 *         that can then be used to reproduce particular runs.
 *         <p>
 *         Note that this feels a bit heavyweight to CLG, as she was trying to just
 *         replace the calls to new Random() in place, but that solution appeared a bit
 *         scattered.
 *         <p>
 *         If accepted as a solution to the reproducible randomness problem, the project
 *         moving forward should stringently disallow calls to new java.util.Random, and
 *         all randomness requirements should be mediated through this class.
 */
public class RandomManager {

    private static Random randomGenerator4IngredientInt = null;
    private static Random randomGenerator4IngredientDouble = null;
    private static Random randomGenerator4OperationInt = null;
    private static Random randomGenerator4OperationDouble = null;
    private static Random randomGenerator4MutationInt = null;
    private static Random randomGenerator4MutationDouble = null;

    public static int ingCounter=0;
    public static int opeCounter=0;
    public static int mutCounter=0;

    public static void initialize() {
        if (ConfigurationProperties.hasProperty("seed")) {
            Integer seed = ConfigurationProperties.getPropertyInt("seed");
            randomGenerator4IngredientInt = new Random(seed);
            randomGenerator4IngredientDouble = new Random(seed);
            randomGenerator4OperationInt = new Random(seed);
            randomGenerator4OperationDouble = new Random(seed);
            randomGenerator4MutationInt = new Random(seed);
            randomGenerator4MutationDouble = new Random(seed);
        } else {
            // by default Astor is deterministic
            ConfigurationProperties.properties.setProperty("seed", "0");
            randomGenerator4IngredientInt = new Random(0);
            randomGenerator4IngredientDouble = new Random(0);
            randomGenerator4OperationInt = new Random(0);
            randomGenerator4OperationDouble = new Random(0);
            randomGenerator4MutationInt = new Random(0);
            randomGenerator4MutationDouble = new Random(0);
        }
        ingCounter=0;
        opeCounter=0;
        mutCounter=0;
    }

    /*public static Integer nextInt(int bound) {
        return randomGenerator4Ingredient.nextInt(bound);
    }
    public static Double nextDouble() {
        return randomGenerator4Ingredient.nextDouble();
    }*/

    public static Integer nextInt4Ingredient(int bound) {
        ingCounter++;
        return randomGenerator4IngredientInt.nextInt(bound);
    }

    public static Double nextDouble4Ingredient() {
        ingCounter++;
        return randomGenerator4IngredientDouble.nextDouble();
    }

    public static Integer nextInt4Operaion(int bound) {
        opeCounter++;
        return randomGenerator4OperationInt.nextInt(bound);
    }

    public static Double nextDouble4Operation() {
        opeCounter++;
        return randomGenerator4OperationDouble.nextDouble();
    }

    public static Integer nextInt4Mutation(int bound) {
        mutCounter++;
        return randomGenerator4MutationInt.nextInt(bound);
    }

    public static Double nextDouble4Mutation() {
        mutCounter++;
        return randomGenerator4MutationDouble.nextDouble();
    }

}
