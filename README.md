# FSE2017
This is the code archive for the paper 'Finding Near-optimal Configuraitons in Product Lines by Random Sampling' published in FSE/ESEC 2017. The paper can be found at http://www.cs.utexas.edu/ftp/predator/17FSE.pdf

This application implements the Statistical Recursive Searching (SRS) described in the paper. SRS is an approach to find near-optimal configurations of a highly configurable system regarding a given performance measure. 

From a given feature model, Binary Deciaion Diagram (BDD) is used to count all valid configuraitons and random sample the configurations from a uniform distribution. SRS samples configuraitons in a recursive manner, identifying feature decisions that are certain to improve the performance at each recursive step and refine the search space.

For more details on the algorithm, please refer to the paper.

## Dependencies ##
This application is built based on JAVA 1.8
This applicaiton uses following third party libraries:
* JDD (https://bitbucket.org/vahidi/jdd/wiki/Home) to run BDD. Its print functionality is slightly modified to convert BDD into CBDD.
* Apach Commons Math (http://commons.apache.org/proper/commons-math/) for statistical analyses.

## How to Run ##
Build and run FSE2017/src/core/main.java

This applicaiton accepts following inputs in order as run parameters:
* Target: name of the target SPL
* Feature model: feature model of the SPL. Its format is described in https://www.cs.utexas.edu/ftp/predator/splc05.pdf. The files used in the paper are in FSE2017/FeatureModel
* Benchmark data: performance data of all valid configurations of the SPL. It uses the xml data generated from http://www.infosun.fim.uni-passau.de/se/projects/splconqueror/icse2012.php. The files used in the paper are in FSE2017/SiegmundData
* Output file name: the file name and location of the output .csv file to be generated.
* Maximum number of recursions: For data used in the paper, they do not go beyond 10.
* Number of configurations to sample at initial recursion
* Number of configurations to sample at subsequent recursions
* Number of experiments to repeat

The resulting .csv file contains following data per experiment:
* delta_x (%): true X-axis accuracy of SRS when it terminates
* delta_x_est (%): estimated X-axis accuracy of SRS when it terminates
* delta_x_nrs (%): estimated X-axis when same number of samples are used without recursion
* delta_y (%): performance difference to the actual best configuration
* delta_y_est (%): estimated performance difference to the actual best configuration
* sampleSize: total number of configurations sampled throughout all recursive steps
* noteworthy (%): percentage of identified feature decisions that are common with the actual best configuraiton
* recursion: number of recursive steps taken
* features: total number of features decisions throughout all recursive steps

