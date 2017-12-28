package core.util;
import java.io.IOException;
import java.util.ArrayList;

public class InfixToPostfix {
	private Stack theStack;
	private ArrayList<String> input;
	private ArrayList<String> output = new ArrayList<String>();

	public InfixToPostfix(ArrayList<String> in) {
		input = in;
		int stackSize = input.size();
		theStack = new Stack(stackSize);
	}

	public ArrayList<String> doTrans() {
		String pre = "";
		for (int j = 0; j < input.size(); j++) {    	   
			String ch = input.get(j);
			switch (ch) {
			case "and": 
			case "or":
			case "implies":
				gotOper(ch, 2); 
				break; 

			case "not":
				break;
				//gotOper(ch, 3);

			case "(": 
				if(pre.equals("not")) theStack.push("<");
				else theStack.push(ch);
				break;
			case ")": 
				gotParen(ch); 
				break;
			default:
				output.add(ch);
				if(pre.equals("not")) {
					output.add(pre);
				}
				break;
			}
			pre = ch;
		}
		while (!theStack.isEmpty()) {
			output.add(theStack.pop());
		}
		//System.out.println(output);
		return output; 
	}

	public void gotOper(String opThis, int prec1) {
		while (!theStack.isEmpty()) {
			String opTop = theStack.pop();
			if ((opTop.equals("(")) || (opTop.equals("<"))) {
				theStack.push(opTop);
				break;
			}
			else {
				int prec2;
				if (opTop.equals("and") || opTop.equals("or"))
					prec2 = 2;
				else 
					prec2 = 3;
				if (prec2 < prec1) { 
					theStack.push(opTop);
					break;
				}
				else
					output.add(opTop);
			}
		}
		theStack.push(opThis);
	}

	public void gotParen(String ch){ 
		while (!theStack.isEmpty()) {
			String chx = theStack.pop();
			if (chx.equals("(") ) break;
			else if(chx.equals("<")) {
				output.add("not");
				break;
			}
			else
				output.add(chx); 
		}
	}

	public static void main(String[] args) 
			throws IOException {
		ArrayList<String> input = new ArrayList<String>();
		input.add("1");
		input.add("and");
		input.add("not");
		input.add("(");
		input.add("2");
		input.add(")");
		
		InfixToPostfix theTrans = new InfixToPostfix(input);
		ArrayList<String> output = theTrans.doTrans(); 
		
		for(String s : output) {
			System.out.print(s + "|");
		}
	}

	private class Stack {
		private int maxSize;
		private String[] stackArray;
		private int top;
		public Stack(int max) {
			maxSize = max;
			stackArray = new String[maxSize];
			top = -1;
		}
		public void push(String j) {
			stackArray[++top] = j;
		}
		public String pop() {
			return stackArray[top--];
		}
		public String peek() {
			return stackArray[top];
		}
		public boolean isEmpty() {
			return (top == -1);
		}
	}
}