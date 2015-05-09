package HMMParallel.Demo;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * The HMMEvaluator class offers several methods to evaluate an HMM Model. The
 * following use-cases are covered: 1) Generate a sequence of output states from
 * a given model (prediction). 2) Compute the likelihood that a given model
 * generated a given sequence of output states (model likelihood). 3) Compute
 * the most likely hidden sequence for a given model and a given observed
 * sequence (decoding).
 */
public final class HmmEvaluator {

  /**
   * No constructor for utility classes.
   */
  private HmmEvaluator() {
    // Nothing to do here.
  }

  /**
   * Returns the likelihood that a given output sequence was produced by the
   * given model. Internally, this function calls the forward algorithm to
   * compute the alpha values and then uses the overloaded function to compute
   * the actual model likelihood.
   *
   * @param model          Model to base the likelihood on.
   * @param outputSequence Sequence to compute likelihood for.
   * @param scaled         Use log-scaled parameters for computation. This is computationally
   *                       more expensive, but offers better numerically stability in case of
   *                       long output sequences
   * @return Likelihood that the given model produced the given sequence
   */
  public static double modelLikelihood(HmmModel model, int[] outputSequence,
                                       boolean scaled) {
    return modelLikelihood(HmmAlgorithms.forwardAlgorithm(model, outputSequence, scaled), scaled);
  }

  /**
   * Computes the likelihood that a given output sequence was computed by a
   * given model using the alpha values computed by the forward algorithm.
   * // TODO I am a bit confused here - where is the output sequence referenced in the comment above in the code?
   * @param alpha  Matrix of alpha values
   * @param scaled Set to true if the alpha values are log-scaled.
   * @return model likelihood.
   */
  public static double modelLikelihood(double[][] alpha, boolean scaled) {
    double likelihood = 0;
    if (scaled) {
      for (int i = 0; i < alpha[0].length; ++i) {
        likelihood += Math.exp(alpha[alpha.length - 1][i]);
      }
    } else {
      for (int i = 0; i < alpha[0].length; ++i) {
        likelihood += alpha[alpha.length - 1][i];
      }
    }
    return likelihood;
  }

  /**
   * Computes the likelihood that a given output sequence was computed by a
   * given model.
   *
   * @param model model to compute sequence likelihood for.
   * @param outputSequence sequence to base computation on.
   * @param beta beta parameters.
   * @param scaled     set to true if betas are log-scaled.
   * @return likelihood of the outputSequence given the model.
   */
  public static double modelLikelihood(HmmModel model, int[] outputSequence, double[][] beta, boolean scaled) {
    double likelihood = 0;
    // fetch the emission probabilities
    double[][] e = model.emissionMatrix;
    double[] initialProbabilities = model.initialProbabilities;
    int firstOutput = outputSequence[0];
    if (scaled) {
      for (int i = 0; i < model.numStates; ++i) {
        likelihood +=
            initialProbabilities[i] * Math.exp(beta[0][i]) * e[i][firstOutput];
      }
    } else {
      for (int i = 0; i < model.numStates; ++i) {
        likelihood += initialProbabilities[i] * beta[0][i] * e[i][firstOutput];
      }
    }
    return likelihood;
  }

  /**
   * Returns the most likely sequence of hidden states for the given model and
   * observation
   *
   * @param model model to use for decoding.
   * @param observations integer Array containing a sequence of observed state IDs
   * @param scaled       Use log-scaled computations, this requires higher computational
   *                     effort but is numerically more stable for large observation
   *                     sequences
   * @return integer array containing the most likely sequence of hidden state
   * IDs
   */
  public static int[] decode(HmmModel model, int[] observations, boolean scaled) {
    return HmmAlgorithms.viterbiAlgorithm(model, observations, scaled);
  }

}
