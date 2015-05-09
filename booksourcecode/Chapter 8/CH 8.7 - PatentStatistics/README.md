#Chapter 8.7 专利文献分析算法
##文件说明
* CitationBy.java 按照特定属性进行统计的算法
* CitationCount.java 统计每个专利被引用的次数
* CitationCountDistribution.java 统计专利被引用次数的分布情况
* PatentCitation.java 构建专利引用关系列表
##运行说明
专利文献下一共有四个算法，四个算法需要分别打包成四个jar包后再运行
其中CitationBy, CitationCount, PatentCitation的输入文件为http://www.nber.org/patents/.这里不再赘述其文件格式。CitationCountDistribution的输入文件为CitationCount的输出文件。
###运行
####CitationCount 
打包后运行
> hadoop jar CitationCount.jar <input path\> <output path\>
####CitationBy
打包后运行
其中col No为属性的列号
> hadoop jar CitationBy.jar <input path\> <output path\> <col No>
####CitationCountDistribution
注意，输入文件为CitationCount的输出文件
打包后运行
> hadoop jar CitationCountDistribution.jar <input path\> <output path\>
####PatentCitation
打包后直接运行
> hadoop jar PatentCitation.jar <input path\> <output path\>


