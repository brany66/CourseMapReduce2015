#  Chapter8.5 InvertedIndexer 说明

##使用方法

本程序共有两个实现，分别是简单版本的SimpleInvertedIndex和InvertedIndexer，执行命令如下：

对于SimpleInvertedIndex版本
`$ bin/hadoop jar SimpleInvertedIndex.jar <files input path\>  <file output path\>`

对于InvertedIndexer版本
`$ bin/hadoop jar InvertedIndexer.jar <stop-words file path\> <files input path\>  <file output path\>` 

##输入

`<stop-words file path\>` 停词表

` <files input path\>` 含有多个文本文件的文档目录, 例如该目录下有doc1、doc2、doc3…… 
