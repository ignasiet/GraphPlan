import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * @author ignasi
 *
 */

public class graphplanner {
	
	private ArrayList<Step> _Steps = new ArrayList<Step>();
	private ArrayList<String> _Actions_list = new ArrayList<String>();
	private Hashtable<String, AbstractAction> _Actions = new Hashtable<String, AbstractAction>();
	private Hashtable<String, Integer> _State = new Hashtable<String, Integer>();
	private ArrayList<String> _Goal = new ArrayList<String>();
	protected ArrayList<String> _Plan = new ArrayList<String>();

	public graphplanner(Hashtable<String, Integer> state, Hashtable<String, AbstractAction> Actions, ArrayList<String> goal) {
		_State = state;
		_Actions = Actions;
		_Goal = goal;
		initActionList();
		Enumeration enumerator_states = state.keys();
		Step stepInit = new Step();
		stepInit.father = null;
		while(enumerator_states.hasMoreElements()){
			String predicate = enumerator_states.nextElement().toString();
			Node no = new Node(predicate);
			stepInit.addNode(no);
		}
		_Steps.add(stepInit);
		while(!isGoal(_Steps.get(_Steps.size()-1))){
			//Expandimos o último nó
			expandStep(_Steps.get(_Steps.size()-1));
		}
		backtrackPlan();
		System.out.println("=========================================");
		System.out.println("Plano: ");
		System.out.println(_Plan.toString());
	}
	
	private void backtrackPlan() {
		Hashtable<String, Integer> achieved_preconds = new Hashtable<String, Integer>();
		Step currentStep = _Steps.get(_Steps.size()-1);
		ArrayList<String> _subgoal = new ArrayList<String>(_Goal);
		while(currentStep.father != null){
			ArrayList<String> _actualList = new ArrayList<String>();
			for(String p : _subgoal){
				Node n = currentStep.getNode(p);
				if(n.hasParent()){
					for(Node parent_node : n.getParent()){
						String new_Param = parent_node.predicate;
						if(!achieved_preconds.containsKey(new_Param)){
							_actualList.add(new_Param);
							achieved_preconds.put(new_Param, 1);
						}
					}					
				}
			}
			_subgoal.clear();
			_subgoal.addAll(_actualList);
			if((currentStep.step %2)==0){
				_Plan.add(0, _actualList.toString());
			}
			if(currentStep.father != null){
				currentStep = currentStep.father;
			}
		}
	}

	public void initActionList(){
		Enumeration enumerator_actions = _Actions.keys();
		while(enumerator_actions.hasMoreElements()){
			_Actions_list.add(enumerator_actions.nextElement().toString());
		}		
	}
	
	public boolean isGoal(Step predicates){
		for(String pred : _Goal){
			if(!predicates.Contains(pred)){
				return false;
			}
		}
		return true;
	}
	
	public void expandStep(Step predicates_list){
		//1 expand actions if possible
		Step ActionStep = new Step();
		Step PredicateStep = new Step();
		for(String action_name : _Actions_list){
			AbstractAction a = _Actions.get(action_name);
			if(isActionApplicable(a, predicates_list)){
				Node no = new Node(action_name);
				ActionStep.addNode(no);
				for(String precondition : a._precond){
					predicates_list.updateSuccessorNode(precondition, no);
					ActionStep.updateParentNode(action_name, predicates_list.getNode(precondition));
				}
				for(String effect : a._Positive_effects){
					Node node_effect = new Node(effect);
					PredicateStep.addNode(node_effect);
					ActionStep.updateSuccessorNode(action_name, node_effect);
					PredicateStep.updateParentNode(effect, no);
				}
			}
		}
		//2 Add no-ops actions and effects
		for(Node predicate : predicates_list.getIterator()){
			Node no = new Node("No-op-" + predicate.toString());
			Node node_effect_no = new Node(predicate.toString());
			ActionStep.addNode(no);
			ActionStep.updateParentNode(no.toString(), predicate);
			PredicateStep.addNode(node_effect_no);
			ActionStep.updateSuccessorNode(no.predicate, node_effect_no);
			PredicateStep.updateParentNode(node_effect_no.predicate, no);
		}
		ActionStep.father = predicates_list;
		PredicateStep.father = ActionStep;
		_Steps.add(ActionStep);
		_Steps.add(PredicateStep);
		PredicateStep.step = predicates_list.step + 2;
		ActionStep.step = ActionStep.step + 1;
	}
	
	/**Verify if the action is applicable*/
	private boolean isActionApplicable(AbstractAction a, Step s){
		for(String precondition : a._precond){
			if(!s.Contains(precondition)){
				return false;
			}
		}
		return true;
	}
}
