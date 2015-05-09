#Chapter 10.2 KNN算法
我们这里实现的KNN算法，有一个基本假设为训练集相对较小，可以整体放进每个工作节点的内存，而预测集较大，需要分而治之。
##输入文件
###训练集格式
1.0 2.2 3.3 4.4 1<br/>
2.1 5.3 7.9 8.0 0 <br/>
其中一行为一个实例，最后的属性为类标号
###预测集格式
1.0 2.2 3.3 4.4 -1<br/>
2.1 5.3 7.9 8.0 -1<br/>
##运行
参数说明：<br/>
predict set path: 需要预测的实例的路径<br/>
output path: 输出路径<br/>
trainset path: 训练集路径<br/>
neighbour num： 邻居个数<br/>
打包后运行：
> hadoop jar KNN.jar <k\> <predict set path\> <output path\> <trainset path> <neighbour num>