package HMMParallel.Demo;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain transitionMatrix copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.Collection;
import java.util.Iterator;

/**
 * Class containing several algorithms used to train transitionMatrix Hidden
 * Markov Model. The three main algorithms are: supervised learning,
 * unsupervised Viterbi and unsupervised Baum-Welch.
 */
public final class HmmTrainer {

  /**
   * No public constructor for utility classes.
   */
  HmmTrainer() {
    // nothing to do here really.
  }

  /**
   * Create an supervised initial estimate of an HMM Model based on
   * transitionMatrix sequence of observed and hidden states.
   * 
   * @param nrOfHiddenStates The total number of hidden states
   * @param nrOfOutputStates The total number of output states
   * @param observedSequence Integer array containing the observed sequence
   * @param hiddenSequence Integer array containing the hidden sequence
   * @param pseudoCount Value that is assigned to non-occurring transitions to
   *          avoid zero probabilities.
   * @return An initial model using the estimated parameters
   */
  public static HmmModel trainSupervised(int nrOfHiddenStates, int nrOfOutputStates, int[] observedSequence,
      int[] hiddenSequence, double pseudoCount) {
    // make sure the pseudo count is not zero
    pseudoCount = pseudoCount == 0 ? Double.MIN_VALUE : pseudoCount;

    // initialize the parameters
    double[][] transitionMatrix = new double[nrOfHiddenStates][nrOfHiddenStates];
    double[][] emissionMatrix = new double[nrOfHiddenStates][nrOfOutputStates];
    // assign transitionMatrix small initial probability that is larger than
    // zero, so
    // unseen states will not get transitionMatrix zero probability

    for(int p = 0; p < nrOfHiddenStates; p++){
    	for(int q = 0; q < nrOfHiddenStates; q++)
    		transitionMatrix[p][q] = pseudoCount;
    }
 
    for(int p = 0; p < nrOfHiddenStates; p++){
    	for(int q = 0; q < nrOfOutputStates; q++)
    		emissionMatrix[p][q] = pseudoCount;
    }
    // given no prior knowledge, we have to assume that all initial hidden
    // states are equally likely
    double[] initialProbabilities = new double[nrOfHiddenStates];

    for(int t = 0; t < nrOfHiddenStates; t++)
    	initialProbabilities[t] = 1.0 / (double) nrOfHiddenStates;

    // now loop over the sequences to count the number of transitions
    countTransitions(transitionMatrix, emissionMatrix, observedSequence,
        hiddenSequence);

    // make sure that probabilities are normalized
    for (int i = 0; i < nrOfHiddenStates; i++) {
      // compute sum of probabilities for current row of transition matrix
      double sum = 0;
      for (int j = 0; j < nrOfHiddenStates; j++) {
        sum += transitionMatrix[i][j];
      }
      // normalize current row of transition matrix
      for (int j = 0; j < nrOfHiddenStates; j++) {
        transitionMatrix[i][j] = transitionMatrix[i][j] / sum;
      }
      // compute sum of probabilities for current row of emission matrix
      sum = 0;
      for (int j = 0; j < nrOfOutputStates; j++) {
        sum += emissionMatrix[i][j];
      }
      // normalize current row of emission matrix
      for (int j = 0; j < nrOfOutputStates; j++) {
        emissionMatrix[i][j] = emissionMatrix[i][j] / sum;
      }
    }

    // return transitionMatrix new model using the parameter estimations
    return new HmmModel(transitionMatrix, emissionMatrix, initialProbabilities);
  }

  /**
   * Function that counts the number of state->state and state->output
   * transitions for the given observed/hidden sequence.
   *
   * @param transitionMatrix transition matrix to use.
   * @param emissionMatrix emission matrix to use for counting.
   * @param observedSequence observation sequence to use.
   * @param hiddenSequence sequence of hidden states to use.
   */
  private static void countTransitions(double[][] transitionMatrix,
                                       double[][] emissionMatrix, int[] observedSequence, int[] hiddenSequence) {
    emissionMatrix[hiddenSequence[0]][observedSequence[0]] =
        emissionMatrix[hiddenSequence[0]][observedSequence[0]] + 1;
    for (int i = 1; i < observedSequence.length; ++i) {
      transitionMatrix
          [hiddenSequence[i - 1]][hiddenSequence[i]] = transitionMatrix
              [hiddenSequence[i - 1]][hiddenSequence[i]] + 1;
      emissionMatrix[hiddenSequence[i]][observedSequence[i]] =
          emissionMatrix[hiddenSequence[i]][observedSequence[i]] + 1;
    }
  }

  /**
   * Create an supervised initial estimate of an HMM Model based on
   * transitionMatrix number of sequences of observed and hidden states.
   * 
   * @param nrOfHiddenStates The total number of hidden states
   * @param nrOfOutputStates The total number of output states
   * @param hiddenSequences Collection of hidden sequences to use for training
   * @param observedSequences Collection of observed sequences to use for
   *          training associated with hidden sequences.
   * @param pseudoCount Value that is assigned to non-occurring transitions to
   *          avoid zero probabilities.
   * @return An initial model using the estimated parameters
   */
  public static HmmModel trainSupervisedSequence(int nrOfHiddenStates,
                                                 int nrOfOutputStates, Collection<int[]> hiddenSequences,
                                                 Collection<int[]> observedSequences, double pseudoCount) {

    // make sure the pseudo count is not zero
    pseudoCount = pseudoCount == 0 ? Double.MIN_VALUE : pseudoCount;

    // initialize parameters
    double[][] transitionMatrix = new double[nrOfHiddenStates][
        nrOfHiddenStates];
    double[][] emissionMatrix = new double[nrOfHiddenStates][
        nrOfOutputStates];
    double[] initialProbabilities = new double[nrOfHiddenStates];

    // assign pseudo count to avoid zero probabilities
    for(int p = 0; p < nrOfHiddenStates; p++){
    	for(int q = 0; q < nrOfHiddenStates; q++)
    		transitionMatrix[p][q] = pseudoCount;
    }
 
    for(int p = 0; p < nrOfHiddenStates; p++){
    	for(int q = 0; q < nrOfOutputStates; q++)
    		emissionMatrix[p][q] = pseudoCount;
    }

    for(int t = 0; t < nrOfHiddenStates; t++)
    	initialProbabilities[t] = pseudoCount;
    
    // now loop over the sequences to count the number of transitions
    Iterator<int[]> hiddenSequenceIt = hiddenSequences.iterator();
    Iterator<int[]> observedSequenceIt = observedSequences.iterator();
    while (hiddenSequenceIt.hasNext() && observedSequenceIt.hasNext()) {
      // fetch the current set of sequences
      int[] hiddenSequence = hiddenSequenceIt.next();
      int[] observedSequence = observedSequenceIt.next();
      // increase the count for initial probabilities
      initialProbabilities[hiddenSequence[0]] = initialProbabilities
          [hiddenSequence[0]] + 1;
      countTransitions(transitionMatrix, emissionMatrix, observedSequence,
          hiddenSequence);
    }

    // make sure that probabilities are normalized
    double isum = 0; // sum of initial probabilities
    for (int i = 0; i < nrOfHiddenStates; i++) {
      isum += initialProbabilities[i];
      // compute sum of probabilities for current row of transition matrix
      double sum = 0;
      for (int j = 0; j < nrOfHiddenStates; j++) {
        sum += transitionMatrix[i][j];
      }
      // normalize current row of transition matrix
      for (int j = 0; j < nrOfHiddenStates; j++) {
        transitionMatrix[i][j] = transitionMatrix[i][j] / sum;
      }
      // compute sum of probabilities for current row of emission matrix
      sum = 0;
      for (int j = 0; j < nrOfOutputStates; j++) {
        sum += emissionMatrix[i][j];
      }
      // normalize current row of emission matrix
      for (int j = 0; j < nrOfOutputStates; j++) {
        emissionMatrix[i][j] = emissionMatrix[i][j] / sum;
      }
    }
    // normalize the initial probabilities
    for (int i = 0; i < nrOfHiddenStates; ++i) {
      initialProbabilities[i] = initialProbabilities[i] / isum;
    }

    // return transitionMatrix new model using the parameter estimates
    return new HmmModel(transitionMatrix, emissionMatrix, initialProbabilities);
  }

  /**
   * Iteratively train the parameters of the given initial model wrt to the
   * observed sequence using Viterbi training.
   *
   * @param initialModel     The initial model that gets iterated
   * @param observedSequence The sequence of observed states
   * @param pseudoCount      Value that is assigned to non-occurring transitions to avoid zero
   *                         probabilities.
   * @param epsilon          Convergence criteria
   * @param maxIterations    The maximum number of training iterations
   * @param scaled           Use Log-scaled implementation, this is computationally more
   *                         expensive but offers better numerical stability for large observed
   *                         sequences
   * @return The iterated model
   */
  public static HmmModel trainViterbi(HmmModel initialModel,
                                      int[] observedSequence, double pseudoCount, double epsilon,
                                      int maxIterations, boolean scaled) {

    // make sure the pseudo count is not zero
    pseudoCount = pseudoCount == 0 ? Double.MIN_VALUE : pseudoCount;

    // allocate space for iteration models
    HmmModel lastIteration = initialModel;
    HmmModel iteration = initialModel;

    // allocate space for Viterbi path calculation
    int[] viterbiPath = new int[observedSequence.length];
    int[][] phi = new int[observedSequence.length - 1][initialModel
        .numStates];
    double[][] delta = new double[observedSequence.length][initialModel
        .numStates];

    // now run the Viterbi training iteration
    for (int i = 0; i < maxIterations; ++i) {
      // compute the Viterbi path
      HmmAlgorithms.viterbiAlgorithm(viterbiPath, delta, phi, lastIteration,
          observedSequence, scaled);
      // Viterbi iteration uses the viterbi path to update
      // the probabilities
      double[][] emissionMatrix = iteration.emissionMatrix;
      double[][] transitionMatrix = iteration.transitionMatrix;

      // first, assign the pseudo count
      for(int p = 0; p < iteration.numStates; p++){
      	for(int q = 0; q < iteration.numStates; q++)
      		transitionMatrix[p][q] = pseudoCount;
      }
   
      for(int p = 0; p < iteration.numStates; p++){
      	for(int q = 0; q < iteration.sigmaSize; q++)
      		emissionMatrix[p][q] = pseudoCount;
      }
      // now count the transitions
      countTransitions(transitionMatrix, emissionMatrix, observedSequence,
          viterbiPath);

      // and normalize the probabilities
      for (int j = 0; j < iteration.numStates; ++j) {
        double sum = 0;
        // normalize the rows of the transition matrix
        for (int k = 0; k < iteration.numStates; ++k) {
          sum += transitionMatrix[j][k];
        }
        for (int k = 0; k < iteration.numStates; ++k) {
          transitionMatrix
              [j][k] = transitionMatrix[j][k] / sum;
        }
        // normalize the rows of the emission matrix
        sum = 0;
        for (int k = 0; k < iteration.sigmaSize; ++k) {
          sum += emissionMatrix[j][k];
        }
        for (int k = 0; k < iteration.sigmaSize; ++k) {
          emissionMatrix[j][k] = emissionMatrix[j][k] / sum;
        }
      }
      // check for convergence
      if (checkConvergence(lastIteration, iteration, epsilon)) {
        break;
      }
      // overwrite the last iterated model by the new iteration
      lastIteration = iteration;
    }
    // we are done :)
    return iteration;
  }

  /**
   * Iteratively train the parameters of the given initial model wrt the
   * observed sequence using Baum-Welch training.
   *
   * @param initialModel     The initial model that gets iterated
   * @param observedSequence The sequence of observed states
   * @param epsilon          Convergence criteria
   * @param maxIterations    The maximum number of training iterations
   * @param scaled           Use log-scaled implementations of forward/backward algorithm. This
   *                         is computationally more expensive, but offers better numerical
   *                         stability for long output sequences.
   * @return The iterated model
   */
  public static HmmModel trainBaumWelch(HmmModel initialModel,
                                        int[] observedSequence, double epsilon, int maxIterations, boolean scaled) {
    // allocate space for the iterations
    HmmModel lastIteration = initialModel;
    HmmModel iteration = initialModel;

    // allocate space for baum-welch factors
    int hiddenCount = initialModel.numStates;
    int visibleCount = observedSequence.length;
    double[][] alpha = new double[visibleCount][hiddenCount];
    double[][] beta = new double[visibleCount][hiddenCount];

    // now run the baum Welch training iteration
    for (int it = 0; it < maxIterations; ++it) {
      // fetch emission and transition matrix of current iteration
      double[] initialProbabilities = iteration.initialProbabilities;
      double[][] emissionMatrix = iteration.emissionMatrix;
      double[][] transitionMatrix = iteration.transitionMatrix;

      // compute forward and backward factors
      HmmAlgorithms.forwardAlgorithm(alpha, iteration, observedSequence, scaled);
      HmmAlgorithms.backwardAlgorithm(beta, iteration, observedSequence, scaled);

      if (scaled) {
        logScaledBaumWelch(observedSequence, iteration, alpha, beta);
      } else {
        unscaledBaumWelch(observedSequence, iteration, alpha, beta);
      }
      // normalize transition/emission probabilities
      // and normalize the probabilities
      double isum = 0;
      for (int j = 0; j < iteration.numStates; ++j) {
        double sum = 0;
        // normalize the rows of the transition matrix
        for (int k = 0; k < iteration.numStates; ++k) {
          sum += transitionMatrix[j][k];
        }
        for (int k = 0; k < iteration.numStates; ++k) {
          transitionMatrix
              [j][k] = transitionMatrix[j][k] / sum;
        }
        // normalize the rows of the emission matrix
        sum = 0;
        for (int k = 0; k < iteration.sigmaSize; ++k) {
          sum += emissionMatrix[j][k];
        }
        for (int k = 0; k < iteration.sigmaSize; ++k) {
          emissionMatrix[j][k] = emissionMatrix[j][k] / sum;
        }
        // normalization parameter for initial probabilities
        isum += initialProbabilities[j];
      }
      // normalize initial probabilities
      for (int i = 0; i < iteration.numStates; ++i) {
        initialProbabilities[i] = initialProbabilities[i]
            / isum;
      }
      // check for convergence
      if (checkConvergence(lastIteration, iteration, epsilon)) {
        break;
      }
      // overwrite the last iterated model by the new iteration
      lastIteration = iteration;
    }
    // we are done :)
    return iteration;
  }

  private static void unscaledBaumWelch(int[] observedSequence, HmmModel iteration, double[][] alpha, double[][] beta) {
    double[] initialProbabilities = iteration.initialProbabilities;
    double[][] emissionMatrix = iteration.emissionMatrix;
    double[][] transitionMatrix = iteration.transitionMatrix;
    double modelLikelihood = HmmEvaluator.modelLikelihood(alpha, false);

    for (int i = 0; i < iteration.numStates; ++i) {
      initialProbabilities[i] = alpha[0][i]
          * beta[0][i];
    }

    // recompute transition probabilities
    for (int i = 0; i < iteration.numStates; ++i) {
      for (int j = 0; j < iteration.numStates; ++j) {
        double temp = 0;
        for (int t = 0; t < observedSequence.length - 1; ++t) {
          temp += alpha[t][i]
              * emissionMatrix[j][observedSequence[t + 1]]
              * beta[t + 1][j];
        }
        transitionMatrix[i][j] = transitionMatrix[i][j]
            * temp / modelLikelihood;
      }
    }
    // recompute emission probabilities
    for (int i = 0; i < iteration.numStates; ++i) {
      for (int j = 0; j < iteration.sigmaSize; ++j) {
        double temp = 0;
        for (int t = 0; t < observedSequence.length; ++t) {
          // delta tensor
          if (observedSequence[t] == j) {
            temp += alpha[t][i] * beta[t][i];
          }
        }
        emissionMatrix[i][j] = temp / modelLikelihood;
      }
    }
  }

  private static void logScaledBaumWelch(int[] observedSequence, HmmModel iteration, double[][] alpha, double[][] beta) {
    double[] initialProbabilities = iteration.initialProbabilities;
    double[][] emissionMatrix = iteration.emissionMatrix;
    double[][] transitionMatrix = iteration.transitionMatrix;
    double modelLikelihood = HmmEvaluator.modelLikelihood(alpha, true);

    for (int i = 0; i < iteration.numStates; ++i) {
      initialProbabilities[i] = Math.exp(alpha[0][i] + beta[0][i]);
    }

    // recompute transition probabilities
    for (int i = 0; i < iteration.numStates; ++i) {
      for (int j = 0; j < iteration.numStates; ++j) {
        double sum = Double.NEGATIVE_INFINITY; // log(0)
        for (int t = 0; t < observedSequence.length - 1; ++t) {
          double temp = alpha[t][i]
              + Math.log(emissionMatrix[j][observedSequence[t + 1]])
              + beta[t + 1][j];
          if (temp > Double.NEGATIVE_INFINITY) {
            // handle 0-probabilities
            sum = temp + Math.log1p(Math.exp(sum - temp));
          }
        }
        transitionMatrix[i][j] = transitionMatrix[i][j]
            * Math.exp(sum - modelLikelihood);
      }
    }
    // recompute emission probabilities
    for (int i = 0; i < iteration.numStates; ++i) {
      for (int j = 0; j < iteration.sigmaSize; ++j) {
        double sum = Double.NEGATIVE_INFINITY; // log(0)
        for (int t = 0; t < observedSequence.length; ++t) {
          // delta tensor
          if (observedSequence[t] == j) {
            double temp = alpha[t][i] + beta[t][i];
            if (temp > Double.NEGATIVE_INFINITY) {
              // handle 0-probabilities
              sum = temp + Math.log1p(Math.exp(sum - temp));
            }
          }
        }
        emissionMatrix[i][j] = Math.exp(sum - modelLikelihood);
      }
    }
  }

  /**
   * Check convergence of two HMM models by computing transitionMatrix simple
   * distance between emission / transition matrices
   * 
   * @param oldModel Old HMM Model
   * @param newModel New HMM Model
   * @param epsilon Convergence Factor
   * @return true if training converged to transitionMatrix stable state.
   */
  private static boolean checkConvergence(HmmModel oldModel, HmmModel newModel,
                                          double epsilon) {
    // check convergence of transitionProbabilities
    double[][] oldTransitionMatrix = oldModel.transitionMatrix;
    double[][] newTransitionMatrix = newModel.transitionMatrix;
    double diff = 0;
    for (int i = 0; i < oldModel.numStates; ++i) {
      for (int j = 0; j < oldModel.numStates; ++j) {
        double tmp = oldTransitionMatrix[i][j]
            - newTransitionMatrix[i][j];
        diff += tmp * tmp;
      }
    }
    double norm = Math.sqrt(diff);
    diff = 0;
    // check convergence of emissionProbabilities
    double[][] oldEmissionMatrix = oldModel.emissionMatrix;
    double[][] newEmissionMatrix = newModel.emissionMatrix;
    for (int i = 0; i < oldModel.numStates; i++) {
      for (int j = 0; j < oldModel.sigmaSize; j++) {

        double tmp = oldEmissionMatrix[i][j]
            - newEmissionMatrix[i][j];
        diff += tmp * tmp;
      }
    }
    norm += Math.sqrt(diff);
    // iteration has converged :)
    return norm < epsilon;
  }

}
