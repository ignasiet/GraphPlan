package planner;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 
 */

/**
 * @author ignasi
 *
 */
public class Step {

	/**
	 * 
	 */
	private ArrayList<Node> listNodes = new ArrayList<Node>();
	private Hashtable<String, Node> nodesHash = new Hashtable<String, Node>();
	public Step father;
	public Step son;
	public int step;
	
	public Step() {
	}
	
	public ArrayList<Node> getIterator(){
		return listNodes;
	}
	
	public void addNode(Node n){
		listNodes.add(n);
		nodesHash.put(n.predicate, n);
	}

	public Node getNode(String p){
		return nodesHash.get(p);
	}
	
	public boolean Contains(String p){
		return nodesHash.containsKey(p);
	}
	
	public void updateParentNode(String node, Node parent){
		Node n = nodesHash.get(node);
		n.addPredecessor(parent);
	}
	
	public void updateSuccessorNode(String node, Node successor){
		if(!node.startsWith("~")){
			Node n = nodesHash.get(node);
			n.addSuccessor(successor);
		}
	}
}
