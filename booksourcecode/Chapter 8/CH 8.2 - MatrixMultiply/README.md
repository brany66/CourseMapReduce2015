#  Chapter8.2 MatrixMultiply 说明

##使用方法
将源码打包成jar包后，在系统节点上执行如下命令：
>$ bin/hadoop jar MatrixMultiply.jar <Matrix M input path\> <Matrix N input path\> <output path\>

##矩阵生成
在CH8.2目录下，有一个生成矩阵的shell脚本文件，在Linux下，执行
>./genMatrix.sh <rows of M\> <cols of M\> <cols of N\>

三个参数分别代表矩阵M的行数、矩阵M的列数（同时也是矩阵N的行数）、矩阵N的列数，用整形Int输入，运行脚本后会得到两个矩阵的文本文件M\_$1\_$2和N\_$2\_$3，作为我们程序的两个输入文件。

**注意** 此脚本的运行效率不是很高，对于大量数据的矩阵生产，可以编译C++源码genMatrix.cpp程序运行。C++程序的三个参数同脚本文件相同，也是<rows of M\> <cols of M\> <cols of N\>