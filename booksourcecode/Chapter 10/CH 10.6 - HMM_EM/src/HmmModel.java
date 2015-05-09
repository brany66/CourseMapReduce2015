package HMMParallel.Demo;

import java.text.DecimalFormat;

/**
 * This class implements transitionMatrix Hidden Markov Model, as well as the
 * Baum-Welch Algorithm for training HMMs.
 * 
 * @author Holger Wunsch (wunsch@sfs.nphil.uni-tuebingen.de)
 */
public class HmmModel {
  /** number of states */
  public int numStates;

  /** size of output vocabulary */
  public int sigmaSize;

  /** initial state probabilities */
  public double initialProbabilities[];

  /** transition probabilities */
  public double transitionMatrix[][];

  /** emission probabilities */
  public double emissionMatrix[][];

  /** initializes an HMM.
      @param numStates number of states
      @param sigmaSize size of output vocabulary 
  */
  public HmmModel(int numStates, int sigmaSize) {
    this.numStates = numStates;
    this.sigmaSize = sigmaSize;

    initialProbabilities = new double[numStates];
    transitionMatrix = new double[numStates][numStates];
    emissionMatrix = new double[numStates][sigmaSize];
  }

  /**
   * Generates transitionMatrix Hidden Markov model using the specified
   * parameters
   * 
   * @param transitionMatrix transition probabilities.
   * @param emissionMatrix emission probabilities.
   * @param initialProbabilities initial start probabilities.
   * @throws IllegalArgumentException If the given parameter set is invalid
   */
  public HmmModel(double[][] transitionMatrix, double[][] emissionMatrix, double[] initialProbabilities) {
    this.numStates = initialProbabilities.length;
    this.sigmaSize = emissionMatrix[0].length;
    this.transitionMatrix = transitionMatrix;
    this.emissionMatrix = emissionMatrix;
    this.initialProbabilities = initialProbabilities;
  }
  
  /** prints all the parameters of an HMM */
  public void print() {
    DecimalFormat fmt = new DecimalFormat();
    fmt.setMinimumFractionDigits(5);
    fmt.setMaximumFractionDigits(5);
    
    for (int i = 0; i < numStates; i++)
      System.out.println("initialProbabilities(" + i + ") = "
          + fmt.format(initialProbabilities[i]));
    System.out.println();

    for (int i = 0; i < numStates; i++) {
      for (int j = 0; j < numStates; j++)
        System.out.print("transitionMatrix(" + i + "," + j + ") = "
            + fmt.format(transitionMatrix[i][j]) + "  ");
      System.out.println();
    }

    System.out.println();
    for (int i = 0; i < numStates; i++) {
      for (int k = 0; k < sigmaSize; k++)
        System.out.print("emissionMatrix(" + i + "," + k + ") = "
            + fmt.format(emissionMatrix[i][k]) + "  ");
      System.out.println();
    }
  }
}