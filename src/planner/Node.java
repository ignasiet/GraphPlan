package planner;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author ignasi
 *
 */
public class Node {

	/**
	 * 
	 */
	private ArrayList<Node> predecessors = new ArrayList<Node>();
	private ArrayList<Node> successors = new ArrayList<Node>();
	public String predicate;
	public int level;
	
	public Node(String pred) {
		//Constructor
		predicate = pred;
	}
	
	public String toString(){
		return predicate;
	}
	
	public void addSuccessor(Node successor){
		successors.add(successor);
	}
	
	public void addPredecessor(Node predecessor){
		predecessors.add(predecessor);
	}

	public ArrayList<Node> getParent(){
		return predecessors;
	}
	
	public boolean hasParent(){
		if(!predecessors.isEmpty()){
			return true;
		}
		else{
			return false;
		}
	}
}
