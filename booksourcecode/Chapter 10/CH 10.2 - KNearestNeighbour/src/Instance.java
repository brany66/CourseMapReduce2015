
public class Instance {
	private double[] attributeValue;
    private double lable;
    
    /**
     * a line of form a1 a2 ...an lable
     * @param line
     */
    public Instance(String line){
    	String[] value = line.split(" ");
    	attributeValue = new double[value.length - 1];
    	for(int i = 0;i < attributeValue.length;i++){
    		attributeValue[i] = Double.parseDouble(value[i]);
    	}
    	lable = Double.parseDouble(value[value.length - 1]);
    }
    
    public double[] getAtrributeValue(){
    	return attributeValue;
    }
    
    public double getLable(){
    	return lable;
    }
}
