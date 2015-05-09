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


/**
 * Class containing implementations of the three major HMM algorithms: forward,
 * backward and Viterbi
 */
public class HmmAlgorithms {


  /**
   * No public constructors for utility classes.
   */
  private HmmAlgorithms() {
    // nothing to do here really
  }

  /**
   * External function to compute transitionMatrix matrix of alpha factors
   * 
   * @param model model to run forward algorithm for.
   * @param observations observation sequence to train on.
   * @param scaled Should log-scaled beta factors be computed?
   * @return matrix of alpha factors.
   */
  public static double[][] forwardAlgorithm(HmmModel model, int[] observations, boolean scaled) {
    double[][] alpha = new double[observations.length][model.numStates];
    forwardAlgorithm(alpha, model, observations, scaled);

    return alpha;
  }

  /**
   * Internal function to compute the alpha factors
   *
   * @param alpha        matrix to store alpha factors in.
   * @param model        model to use for alpha factor computation.
   * @param observations observation sequence seen.
   * @param scaled       set to true if log-scaled beta factors should be computed.
   */
  static void forwardAlgorithm(double[][] alpha, HmmModel model, int[] observations, boolean scaled) {

    // fetch references to the model parameters
    double[] ip = model.initialProbabilities;
    double[][] emissionMatrix = model.emissionMatrix;
    double[][] transitionMatrix = model.transitionMatrix;

    if (scaled) { // compute log scaled alpha values
      // Initialization
      for (int i = 0; i < model.numStates; i++) {
        alpha[0][i] = Math.log(ip[i] * emissionMatrix[i][observations[0]]);
      }

      // Induction
      for (int t = 1; t < observations.length; t++) {
        for (int i = 0; i < model.numStates; i++) {
          double sum = Double.NEGATIVE_INFINITY; // log(0)
          for (int j = 0; j < model.numStates; j++) {
            double tmp = alpha[t - 1][j] + Math.log(transitionMatrix[j][i]);
            if (tmp > Double.NEGATIVE_INFINITY) {
              // make sure we handle log(0) correctly
              sum = tmp + Math.log1p(Math.exp(sum - tmp));
            }
          }
          alpha[t][i] = sum + Math.log(emissionMatrix[i][observations[t]]);
        }
      }
    } else {

      // Initialization
      for (int i = 0; i < model.numStates; i++) {
        alpha[0][i] = ip[i] * emissionMatrix[i][observations[0]];
      }

      // Induction
      for (int t = 1; t < observations.length; t++) {
        for (int i = 0; i < model.numStates; i++) {
          double sum = 0.0;
          for (int j = 0; j < model.numStates; j++) {
            sum += alpha[t - 1][j] * transitionMatrix[j][i];
          }
          alpha[t][i] = sum * emissionMatrix[i][observations[t]];
        }
      }
    }
  }

  /**
   * External function to compute transitionMatrix matrix of beta factors
   * 
   * @param model model to use for estimation.
   * @param observations observation sequence seen.
   * @param scaled Set to true if log-scaled beta factors should be computed.
   * @return beta factors based on the model and observation sequence.
   */
  public static double[][] backwardAlgorithm(HmmModel model, int[] observations, boolean scaled) {
    // initialize the matrix
    double[][] beta = new double[observations.length][model.numStates];
    // compute the beta factors
    backwardAlgorithm(beta, model, observations, scaled);

    return beta;
  }

  /**
   * Internal function to compute the beta factors
   *
   * @param beta         Matrix to store resulting factors in.
   * @param model        model to use for factor estimation.
   * @param observations sequence of observations to estimate.
   * @param scaled       set to true to compute log-scaled parameters.
   */
  static void backwardAlgorithm(double[][] beta, HmmModel model, int[] observations, boolean scaled) {
    // fetch references to the model parameters
    double[][] emissionMatrix = model.emissionMatrix;
    double[][] transitionMatrix = model.transitionMatrix;

    if (scaled) { // compute log-scaled factors
      // initialization
      for (int i = 0; i < model.numStates; i++) {
        beta[observations.length - 1][i]= 0;
      }

      // induction
      for (int t = observations.length - 2; t >= 0; t--) {
        for (int i = 0; i < model.numStates; i++) {
          double sum = Double.NEGATIVE_INFINITY; // log(0)
          for (int j = 0; j < model.numStates; j++) {
            double tmp =
                beta[t + 1][j] + Math.log(transitionMatrix[i][j])
                    + Math.log(emissionMatrix[j][observations[t + 1]]);
            if (tmp > Double.NEGATIVE_INFINITY) {
              // handle log(0)
              sum = tmp + Math.log1p(Math.exp(sum - tmp));
            }
          }
          beta[t][i] = sum;
        }
      }
    } else {
      // initialization
      for (int i = 0; i < model.numStates; i++) {
        beta[observations.length - 1][i] = 1;
      }
      // induction
      for (int t = observations.length - 2; t >= 0; t--) {
        for (int i = 0; i < model.numStates; i++) {
          double sum = 0;
          for (int j = 0; j < model.numStates; j++) {
            sum +=
                beta[t + 1][j] * transitionMatrix[i][j]
                    * emissionMatrix[j][observations[t + 1]];
          }
          beta[t][i] = sum;
        }
      }
    }
  }

  /**
   * Viterbi algorithm to compute the most likely hidden sequence for
   * transitionMatrix given model and observed sequence
   * 
   * @param model HmmModel for which the Viterbi path should be computed
   * @param observations Sequence of observations
   * @param scaled Use log-scaled computations, this requires higher
   *          computational effort but is numerically more stable for large
   *          observation sequences
   * @return nrOfObservations 1D int array containing the most likely hidden
   *         sequence
   */
  public static int[] viterbiAlgorithm(HmmModel model, int[] observations, boolean scaled) {

    // probability that the most probable hidden states ends at state i at
    // time t
    double[][] delta = new double[observations.length][model
        .numStates];

    // previous hidden state in the most probable state leading up to state
    // i at time t
    int[][] phi = new int[observations.length - 1][model.numStates];

    // initialize the return array
    int[] sequence = new int[observations.length];

    viterbiAlgorithm(sequence, delta, phi, model, observations, scaled);

    return sequence;
  }

  /**
   * Internal version of the viterbi algorithm, allowing to reuse existing
   * arrays instead of allocating new ones
   *
   * @param sequence     NrOfObservations 1D int array for storing the viterbi sequence
   * @param delta        NrOfObservations x NrHiddenStates 2D double array for storing the
   *                     delta factors
   * @param phi          NrOfObservations-1 x NrHiddenStates 2D int array for storing the
   *                     phi values
   * @param model        HmmModel for which the viterbi path should be computed
   * @param observations Sequence of observations
   * @param scaled       Use log-scaled computations, this requires higher computational
   *                     effort but is numerically more stable for large observation
   *                     sequences
   */
  static void viterbiAlgorithm(int[] sequence, double[][] delta, int[][] phi, HmmModel model, int[] observations,
      boolean scaled) {
    // fetch references to the model parameters
    double[] ip = model.initialProbabilities;
    double[][] emissionMatrix = model.emissionMatrix;
    double[][] transitionMatrix = model.transitionMatrix;

    // Initialization
    if (scaled) {
      for (int i = 0; i < model.numStates; i++) {
        delta[0][i] = Math.log(ip[i] * emissionMatrix[i][observations[0]]);
      }
    } else {

      for (int i = 0; i < model.numStates; i++) {
        delta[0][i] = ip[i] * emissionMatrix[i][observations[0]];
      }
    }

    // Induction
    // iterate over the time
    if (scaled) {
      for (int t = 1; t < observations.length; t++) {
        // iterate over the hidden states
        for (int i = 0; i < model.numStates; i++) {
          // find the maximum probability and most likely state
          // leading up
          // to this
          int maxState = 0;
          double maxProb = delta[t - 1][0] + Math.log(transitionMatrix[0][i]);
          for (int j = 1; j < model.numStates; j++) {
            double prob = delta[t - 1][j] + Math.log(transitionMatrix[j][i]);
            if (prob > maxProb) {
              maxProb = prob;
              maxState = j;
            }
          }
          delta[t][i] = maxProb + Math.log(emissionMatrix[i][observations[t]]);
          phi[t - 1][i] = maxState;
        }
      }
    } else {
      for (int t = 1; t < observations.length; t++) {
        // iterate over the hidden states
        for (int i = 0; i < model.numStates; i++) {
          // find the maximum probability and most likely state
          // leading up
          // to this
          int maxState = 0;
          double maxProb = delta[t - 1][0] * transitionMatrix[0][i];
          for (int j = 1; j < model.numStates; j++) {
            double prob = delta[t - 1][j] * transitionMatrix[j][i];
            if (prob > maxProb) {
              maxProb = prob;
              maxState = j;
            }
          }
          delta[t][i] = maxProb * emissionMatrix[i][observations[t]];
          phi[t - 1][i] = maxState;
        }
      }
    }

    // find the most likely end state for initialization
    double maxProb;
    if (scaled) {
      maxProb = Double.NEGATIVE_INFINITY;
    } else {
      maxProb = 0.0;
    }
    for (int i = 0; i < model.numStates; i++) {
      if (delta[observations.length - 1][i] > maxProb) {
        maxProb = delta[observations.length - 1][i];
        sequence[observations.length - 1] = i;
      }
    }

    // now backtrack to find the most likely hidden sequence
    for (int t = observations.length - 2; t >= 0; t--) {
      sequence[t] = phi[t][sequence[t + 1]];
    }
  }
  
  

}
