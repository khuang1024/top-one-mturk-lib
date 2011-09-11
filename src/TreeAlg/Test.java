package TreeAlg;

import java.util.*;

public class Test {
	public static void parseToMap(HashMap hm, String str){
		String[] key_value = str.split("&");
		for(String kv: key_value){
			String[] s = kv.split("=");
			hm.put(s[0], s[1]);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap hm = new HashMap();
		String str = "imgfile=https://users.soe.ucsc.edu/~carlliu/ext/imagenet/189022.jpg&qnumber=4&q0=12707&q1=13645&q2=13381&q3=12076";
		System.out.println(str);
		
		parseToMap(hm, str);
		
		System.out.println(hm.get("imgfile"));
		System.out.println(hm.get("qnumber"));
		for(int i = 0; i < Integer.parseInt(hm.get("qnumber").toString()); i++){
			System.out.println(hm.get("q"+i));
		}
	}

}
