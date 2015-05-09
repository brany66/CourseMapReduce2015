#include <iostream>
#include <string>
#include <time.h>
#include <stdlib.h>
#include <sstream>
using namespace std;

//usage: argv[1] is the rows of the matrix M, argv[2] is the columns of the matrix M, argv[3] is the columns of the matrix N
int main(int argc, char* argv[])
{
	int row,column,columnB;
	sscanf(argv[1],"%d",&row);
	sscanf(argv[2],"%d",&column);
	sscanf(argv[3],"%d",&columnB);
	FILE *fp;
	stringstream ss;
	ss <<  "M_";
	ss << row;
	ss << "_";
	ss <<column;
	fp = fopen(ss.str().c_str(),"w");
	printf("matrix M %d,%d \n",row,column);
	srand(time(NULL));
	for(int  i =1; i <= row; i++)
	{
		
		for(int j = 1; j <= column; j++)
		{
		    fprintf(fp,"%d%s%d%s%d%s",i,",",j,"\t",rand()%100,"\n");
		}
		
	}
	fclose(fp);
	FILE *fp2;
	stringstream ss2;
	ss2 <<  "N_";
	ss2 <<column;
	ss2 << "_";
	ss2 <<columnB;
	fp2 = fopen(ss2.str().c_str(),"w");
	printf("matrix N %d,%d \n",column,columnB);
	srand(time(NULL));
	for(int  i =1; i <= column; i++)
	{
		
		for(int j = 1; j <= columnB; j++)
		{
		    fprintf(fp2,"%d%s%d%s%d%s",i,",",j,"\t",rand()%100,"\n");
		}
		
	}
	fclose(fp2);
	return 0;
}
