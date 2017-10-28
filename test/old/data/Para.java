package old.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Para<T> extends HashMap<String, T>{
	private static final long serialVersionUID = 1L;
	
	protected Map<String, T> parameters = new LinkedHashMap<String, T>(16);
	
	public Para(int i) {
		super(i);
	}

	public void putPara(String pName, T value) {
		parameters.put(pName, value);
	}
	
	public T getPara(String pName) {
		return parameters.get(pName);
	}
	
	public T removePara(String pName) {
		return parameters.remove(pName);
	}
	
	public Set<Entry<String, T>> paraSet() {
		return parameters.entrySet();
	}
	
	public void setParas(Map<String, T> paras) {
		parameters.putAll(paras);
	}
	
	public Map<String, T> getParas() {
		return parameters;
	}
	
	public void removeParas() {
		parameters.clear();
	}
	
	public int paraSize() {
		return parameters.size();
	}
}
