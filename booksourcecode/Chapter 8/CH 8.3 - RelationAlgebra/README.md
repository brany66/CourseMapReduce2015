#Chapter 8.3 关系代数算法
关系代数算法下一共设置的5个子算法，选择，投影，自然连接，交，差。
这5个子算法需要分别打包，独立运行。
这里我们用普通文本文件作为输入文件，展示关系代数算法的流程，我们用作实验的关系有两个RelationA和RelationB。他们用以记录学生信息。<br/>
RelationA关系的格式为：<br/>
id name age weight<br/>
1  tom  18  60<br/>
3  lily 17  58</br>
RelationB关系文件的格式为:<br/>
id genger height<br/>
1  1      178<br/>
3  0      155<br/>
##运行说明
* Selection<br>
选择以RelationA关系作为输入文件<br/>
输入参数：<br/>
id： 选择的属性号<br/>
value： 属性值
> hadoop jar Selection.jar <input path\> <output path\> <id\> <value\>

* Projection
投影以RelationA作为关系输入文件
主要输入参数解释：
col id: 投影的列号
> hadoop jar Projection.jar <input path\> <output path\> <col id\>

* Intersection
交以RelationA作为输入,输入路径下放置两个RelationA关系文件，求交集
> hadoop jar Intersection.jar <input path\> <output path\> 

* Difference
差以RelationA作为输入文件，这里一关系A的两个输入文件R1.txt和R2.txt来举例
<br/>
打包后运行:<br>
> hadoop jar Difference.jar <input path\> <output path\>
> <relation name\>    

   其中relationname表示被减的关系文件的名称,例如可以运行:
> hadoop jar Difference.jar /input/ /output/ R1.txt

* NaturalJoin
自然连接以RelationA和RelationB作为输入关系
打包后运行：
> hadoop jar NaturalJoin.jar <input path\> <output path\>
> <join col id\> <relation name>

例如有两个关系的输入文件Ra.txt 和 Rb.txt。分别为关系A和关系B,现在要在列号0上进行自然连接,那么join col id就为0，relation name为Ra.txt或者Rb.txt
  


