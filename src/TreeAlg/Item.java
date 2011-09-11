package TreeAlg;

import java.util.*;

public class Item {
	private Object question;
	private int level;
	private int tag;
	
	Item(Object question, int level, int tag){
		this.question = question;
		this.level = level;
		this.tag = tag;
	}
	
	protected Object getQuestion(){
		return this.question;
	}
	
	protected int getLevel(){
		return this.level;
	}
	
	protected int getTag(){
		return this.tag;
	}
	
	protected void setTag(int newTag){
		this.tag = newTag;
	}
	
	public static void main(String[] args){
		ArrayList<Object> list = new ArrayList<Object>();
		
		String str = "hello";
		Integer num = new Integer(50);
		Item item1 = new Item(str, 1, 1);
		
		list.add(str);
		list.add(num);
		list.add(item1);
		
		for(int i = 0; i < list.size(); i++){
			System.out.println("list="+list.get(i));
			System.out.println("type="+list.get(i).getClass());
			System.out.println();
		}
		
		Object[] t = new Object[3];
		t[0] = num;
		t[1] = item1;
		t[2] = str;
		for(int i = 0; i < t.length; i++){
			System.out.println("\n"+t[i]);
		}
		
		Object x = "Old";
		Object y = x;
		x = "New";
		System.out.print(y);
		
		String[] strx = new String[5];
		int[] count = new int [5];
		for(int i = 0; i < 5;  i++){
			System.out.println(strx[i]);
			System.out.println(count[i]);
		}
		
		String des1 = "desc_identifier:this is a good thing";
		String des2 = "desc_identifier:I dont beleebebe";
		String des3 = des1.substring(16);
		String des4 = des2.substring(16);
		System.out.println(des3);
		System.out.println(des4);
		
		Object[] a1 = {"Hello", "OK"};
		ArrayList<Object> a2 = new ArrayList<Object>();
		a2.add("Hello");
		System.out.println(a1[0]==a2.get(0));
		System.out.println(a1[0].equals(a2.get(0)));
	}
}
