#Chapter 8.4 单词同现算法
##文件说明
* WholeFileInputFormat.java 用于将整个文件作为一个整体传送给一个map
* WordConcurrence.java 算法执行入口
* SingleFileNameReader.java 将文件作为整体读入
* WordPair.java 单词对
##使用方法
将文件打包成jar包后，执行以下命令：
> hadoop jar WordConcurrence.jar <input path\> <output path\> <window size\>
###输入参数说明
* input path： 输入路径，支持多文件
* output path： 输出路径
* window size： 窗口大小，只有在window size内共同出现的单词才会被认为是一次同现
###输出文件说明：
输出格式为：
一行一个单词对，后面记录该单词对同现的次数
> wordA, wordB 3
