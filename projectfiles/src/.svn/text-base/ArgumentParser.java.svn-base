import java.util.HashMap;
import java.util.Map;


public class ArgumentParser {
	Map<String, String> argsMap = new HashMap<String,String>();
	int numArgs; 

	public ArgumentParser(String[] args){
		numArgs = args.length;
		for(int i = 0; i < args.length; ++i){
			String flag = args[i];
			String value = "";
			if(flag.charAt(0)=='-'){
				if(i < (args.length - 1)){
					if(args[i+1].charAt(0) != '-'){
						value = args[i+1];
					}		
				}
				argsMap.put(flag, value);
			}
		}
	}
	
	public boolean hasFlag(String flag){
		return argsMap.containsKey(flag);
	}
	
	public boolean hasValue(String flag){
		return !argsMap.get(flag).isEmpty();
	}
	
	public String getValue(String flag){
		return argsMap.get(flag);
	}
	
	public int numFlags(){
		return argsMap.size();
	}
	
	public int numArguments(){
		return numArgs;
	}
	
	public void printFlags(){
		for (String flag : argsMap.keySet())
			System.out.println(flag);
	}		
}
