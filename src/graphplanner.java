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
	protected Hashtable<Integer, ArrayList<String>> _ActionPlan = new Hashtable<Integer, ArrayList<String>>();
	protected Integer last_layer = 0;
	protected boolean fail = false;

	public graphplanner(Hashtable<String, Integer> state, Hashtable<String, AbstractAction> Actions, ArrayList<String> goal) {
		_State = state;
		_Actions = Actions;
		_Goal = goal;
		Integer num_MAX_iter = 0;
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
		while(!isGoal(_Steps.get(_Steps.size()-1)) && num_MAX_iter < _Actions.size()){
			//Expandimos o último nó
			expandStep(_Steps.get(_Steps.size()-1));
			num_MAX_iter++;
		}
		if(num_MAX_iter == _Actions.size()){
			System.out.println("FAIL! no plan found");
			fail = true;
		}else{
			backtrackPlan();
			/*System.out.println("=========================================");
			System.out.println("Plano parcialmente ordenado (incluindo no-ops): ");
			System.out.println(_Plan.toString());*/
		}
	}
	
	private void backtrackPlan() {
		Hashtable<String, Integer> achieved_preconds = new Hashtable<String, Integer>();
		Step currentStep = _Steps.get(_Steps.size()-1);
		ArrayList<String> _subgoal = new ArrayList<String>(_Goal);
		while(currentStep.father != null){
			ArrayList<String> _actualList = new ArrayList<String>();
			ArrayList<String> _actions_Partial_ordered = new ArrayList<String>();
			for(String p : _subgoal){
				Node n = currentStep.getNode(p);
				if(n.hasParent()){
					for(Node parent_node : n.getParent()){
						String new_Param = parent_node.predicate;
						if(!achieved_preconds.containsKey(new_Param)){
							_actualList.add(new_Param);
							if(!new_Param.startsWith("No-op")){
								_actions_Partial_ordered.add(new_Param);
							}
							achieved_preconds.put(new_Param, 1);
						}
					}					
				}
			}
			_subgoal.clear();
			_subgoal.addAll(_actualList);
			if((currentStep.step %2)==0){
				_Plan.add(0, _actualList.toString());
				_ActionPlan.put(currentStep.step -1 , _actions_Partial_ordered);
			}
			if(currentStep.father != null){
				currentStep = currentStep.father;
			}
		}
	}
	
	public Integer heuristicValue(){
		int hValue = 0;
		Enumeration e = _ActionPlan.keys();
		while(e.hasMoreElements()){
			Integer Key_Plan = Integer.parseInt(e.nextElement().toString());
			ArrayList<String> partialActions = _ActionPlan.get(Key_Plan);
			hValue += partialActions.size();
		}
		return hValue;
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
					if(!precondition.contains("^")){						
						predicates_list.updateSuccessorNode(precondition, no);
						ActionStep.updateParentNode(action_name, predicates_list.getNode(precondition));
					}
					else{
						//Action with or precondition
						String[] orPrecond = precondition.split("\\^");
						for(String orP : orPrecond){
							if(predicates_list.Contains(orP)){
								predicates_list.updateSuccessorNode(orP, no);
								ActionStep.updateParentNode(action_name, predicates_list.getNode(orP));
							}
						}
					}
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
		ActionStep.step = predicates_list.step + 1;
		last_layer = ActionStep.step;
	}
	
	/**Verify if the action is applicable*/
	private boolean isActionApplicable(AbstractAction a, Step s){
		for(String precondition : a._precond){
			if(!precondition.contains("^")){
				if(!s.Contains(precondition)){
					return false;
				}
			}else{
				boolean flag = false;
				String[] orPrecond = precondition.split("\\^");
				for(String orP : orPrecond){
					if(s.Contains(orP)){
						flag = true;
						break;
					}
				}
				if(!flag){
					return false;
				}
			}
		}
		return true;
	}
}
