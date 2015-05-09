
import java.util.BitSet;  
import java.util.ArrayList;  
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

public class FindSubset
{  
    private boolean[] start;  
    private boolean[] end;  
    private List<List<String>> subsets;
	private String[] elements;
    public FindSubset(List<String> items)
    {  
        initrun(items);
    }
	private void initrun(List<String> items)
	{
		int n = items.size();
		this.start = new boolean[n];
        this.end = new boolean[n];
        this.subsets = new ArrayList<List<String>>();
        this.elements = new String[n];
        items.toArray(elements);
	}
    public void execute(int n)
    {
        int size = elements.length;
		boolean former, last;
		boolean startBit, endBit;
		int i, j;
        this.subsets.clear();
        if(n > size)
        	return;
        for (i = 0; i < n; i++) {
			start[i] = true;
			end[i+size-n] = true;
        }  
        for (i = 0; i < size-n; i++) {  
			end[i] = false;
			start[i+n] = false;
		}      
        update();
       
        for (;Arrays.equals(start,end)==false; ) { 

			endBit = start[size - 1]; 			
            for (i = size - 1; i > 0; i--) {  
				former = start[i - 1];  
				last = start[i];
                if (former == true && last == false) {
                    break;  
                }  
            }  
			if (i == 0) {
                break;  
            } else {
				i--;
				start[i] = false;
                start[i + 1] =  true;
                if (endBit == false) {  
                    update();                    
                } else {  
                    i++;  
                    for (j = i + 1; j < size; j++) {  
                        if (start[j] == true) {                  	
                            start[j] =  false;  
                            start[i + 1] = true;
                            i++;
                            update(); 
                        }  
                    }
                }
            }
        }
    }  
    public void update() {
        List<String> temp = new ArrayList<String>();
		int i;
        for (i = 0; i < elements.length; i++) {  
            if (start[i] == true) {
                temp.add(elements[i]);
            }
        }
        if(subsets.contains(temp)==false)
        	subsets.add(temp);
    }  
    public List<List<String>> getSubsets() {  
        return this.subsets;
    }  
      
    public void clearSubsets(){
		int i;
		for(i = 0; i<subsets.size(); i++)
			subsets.get(i).clear();
        this.subsets.clear();  
    }
	
	public static void main(String[] args)
	{
		//for test, do not use it
		FindSubset sbr = null;
		ArrayList<String> ls = new ArrayList<String>();
		List<List<String>> res = null;
		String str = "abcdefghijklmnopqrstuvwxyz";
		int s = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		int i,j;
		for(i = 0; i<s; i++)
			ls.add(new String(str.substring(i,i+1)));
		sbr = new FindSubset(ls);
		sbr.execute(k);
		res = sbr.getSubsets();
		System.out.println(res.size());
		
	}
}

