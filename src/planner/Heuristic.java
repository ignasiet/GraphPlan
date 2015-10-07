/**
 * 
 */
package planner;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import pddlElements.Action;
import pddlElements.Effect;

/**
 * @author ignasi
 *
 */
public class Heuristic {
	
	private ArrayList<String> _Goal = new ArrayList<String>();
	private ArrayList<String> _Actions_list = new ArrayList<String>();
	private Hashtable<String, Action> _Actions = new Hashtable<String, Action>();
	private Hashtable<String, Integer> _State = new Hashtable<String, Integer>();
	private Hashtable<String, Integer> _Invariants = new Hashtable<String, Integer>();
	public int heuristicValue = 100000000;
	
	
	/**Constructor*/
	public Heuristic(Hashtable<String, Integer> state, Hashtable<String, Action> Actions, ArrayList<String> goal, Hashtable<String, Integer> invariants){
		_State = state;
		_Actions = Actions;
		_Goal = goal;
		_Invariants = invariants;
		initActionList();
		//cleanProblem();
		heuristicValue = heuristicGraphPlan();
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
	
	private boolean isInvariant(String p) {
		String[] pSplitted = p.split("_");
		if(_Invariants.containsKey(pSplitted[0])){
			return true;
		}else{
			return false;
		}		
	}
	
	private void cleanProblem(){
		//1 - clean goal
		ArrayList<String> newGoal = new ArrayList<String>();
		Hashtable<String, Integer> newState = new Hashtable<String, Integer>();
		for(String predicate : _Goal){
			if(!isInvariant(predicate)){
				newGoal.add(predicate);
			}
		}
		_Goal = newGoal;
		//2 - clean state
		Enumeration e = _State.keys();
		while(e.hasMoreElements()){
			String predicate = e.nextElement().toString();
			if(!isInvariant(predicate)){
				newState.put(predicate, 1);
			}
		}
		_State = newState;
		//3 - clean actions
		for(String action_name : _Actions_list){
			ArrayList<String> newPrecond = new ArrayList<String>();
			Action action = _Actions.get(action_name);
			for(String precond : action._precond){
				if(!isInvariant(precond)){
					newPrecond.add(precond);
				}
			}
			action._precond = newPrecond;
		}
	}
	
	private int heuristicGraphPlan(){
		//Scheduled actions variable
	    ArrayList<String> scheduledActions = new ArrayList<String>();
	    //Current Layer
	    int i=0;
	    Step layerMembershipFacts = new Step();
	    Step layerMembershipActions = new Step();
	    Enumeration e = _State.keys();
	    while(e.hasMoreElements()){
	    	String p = e.nextElement().toString();
	    	Node n = new Node(p);
	    	n.level = 0;
	    	layerMembershipFacts.addNode(n);
	    }
	    
	    while(!isGoal(layerMembershipFacts) && i < _Actions.size()){
	    	ArrayList<String> stateApplicableActions = getApplicableActions(layerMembershipFacts);
	    	stateApplicableActions.removeAll(scheduledActions);
	    	ArrayList<String> scheduledNextActions = new ArrayList<String>();
	    	//Finds Actions that have not been selected and with all preconditions fulfilled
	    	for(String action : stateApplicableActions){
	    		Node NodeAction = new Node(action);
	    		NodeAction.level= i;
	    		scheduledNextActions.add(action);
		    	layerMembershipActions.addNode(NodeAction);
		    	//System.out.println(action);
	    	}
	    	i++;
	    	for(String action : scheduledNextActions){
	    		//Updating current state
	    		Action a = _Actions.get(action);
	    		for(String eff : a._Positive_effects){
	    			Node effNode = new Node(eff);
	    			//Updating LayerMembership for facts
		            if(!layerMembershipFacts.Contains(eff)){
		            	effNode.level = i;
		            	//System.out.println(effNode);
		            	layerMembershipFacts.addNode(effNode);
		            }
	    		}
	    		//Updating LayerMembership for conditional effect-facts
	    		for(Effect effect : a._Effects){
	    			if(isEffectApplicable(effect, layerMembershipFacts)){
	    				for(String eff : effect._Effects){
		    				if(!layerMembershipFacts.Contains(eff)){
		    					Node effNode = new Node(eff);
				            	effNode.level = i;
				            	//System.out.println(effNode);
				            	layerMembershipFacts.addNode(effNode);
				            }
	    				}
	    			}	    			
	    		}
	    	}
	    	scheduledActions.addAll(scheduledNextActions);
	    }
	    //End of Graph extraction... beginning of plan extraction
	    int last = i;
	    //Build list of goals achieved in each layer
	    ArrayList<Hashtable<String, Integer>> goalLayer = initGoalLayer(i);
	    int num_selected_actions = 0;
	    Hashtable<String, Integer> markedTrue = new Hashtable<String, Integer>();
	    int layer = 0;
	    for(String goalPred : _Goal){
	    	Node nodeGoal = layerMembershipFacts.getNode(goalPred);
	    	layer = nodeGoal.level;
	    	goalLayer.get(layer).put(goalPred, 1);
	    }
	    for(int iter = last; iter>=0;iter--){
	    	Hashtable<String, Integer> gIter = goalLayer.get(iter);
	    	Enumeration en = gIter.keys();
	    	while(en.hasMoreElements()){
	    		String kGoal = en.nextElement().toString();
	    		if((!markedTrue.containsKey(kGoal)) || (markedTrue.get(kGoal) > iter)){
	    			Action a = _Actions.get(chooseActionFF(kGoal, iter, layerMembershipActions, layerMembershipFacts));
	    			if(a != null){
		    			num_selected_actions++;
		    			for(String pr : a._precond){
		    				if(layerMembershipFacts.getNode(pr).level!=0){
		    					layer = layerMembershipFacts.getNode(pr).level;
		    					goalLayer.get(layer).put(pr, 1);
		    				}
		    			}
		    			for(String eff : a._Positive_effects){
		    				markedTrue.put(eff, iter);
		    				markedTrue.put(eff, iter-1);
		    			}
	    			}
		    	}
	    	}	    	
	    }
	    //after all computations, The heuristic is the number of selected actions
	    return num_selected_actions;
	}
	
	private ArrayList<Hashtable<String, Integer>> initGoalLayer(int m){
		ArrayList<Hashtable<String, Integer>> returnList = new ArrayList<Hashtable<String, Integer>>();
		for(int iter = 0; iter<=m;iter++){
			Hashtable<String, Integer> tableList = new Hashtable<String, Integer>();
			returnList.add(iter, tableList);
		}
		return returnList;		
	}
	
	private ArrayList<String> getApplicableActions(Step predicates_list){
		ArrayList<String> _actions = new ArrayList<>();
		for(String action_name : _Actions_list){
			Action a = _Actions.get(action_name);
			/*if(action_name.contains("move_p5-3_")){
				System.out.println("Checked");
			}*/
			if(isActionApplicable(a, predicates_list)){
				_actions.add(action_name);
			}
		}
		return _actions;
	}
	
	/**Find action with g in add(o), difficulty minimal
	 * @param layerMembershipFacts 
	 * @param layerMembershipActions 
	 * @param iter 
	 * @param kGoal */
	private String chooseActionFF(String kGoal, int iter, Step layerMembershipActions, Step layerMembershipFacts){
		ArrayList<String> candidates = new ArrayList<>();
		for(Node n : layerMembershipActions.getIterator()){
			Action a = _Actions.get(n.predicate);
			if((n.level == iter-1) && (a._Positive_effects.contains(kGoal))){
				candidates.add(n.predicate);
			}
			for(Effect eff : a._Effects){
				if(eff._Effects.contains(kGoal)){
					candidates.add(n.predicate);
				}
			}
		}
		int difficulty = Integer.MAX_VALUE;
		String best = "";
		if(!candidates.isEmpty()){
			best = candidates.get(0);
			for(String act : candidates){
				Action a = _Actions.get(act);
				int new_difficulty = 0;
				for(String pre : a._precond){
					new_difficulty += layerMembershipFacts.getNode(pre).level;
				}
				if(new_difficulty < difficulty){
					best = act;
					difficulty = new_difficulty;
				}
			}
		}		
		return best;
	}
	
	/**Verify if the conditional effect is applied*/
	private boolean isEffectApplicable(Effect e, Step s){
		for(String precondition : e._Condition){
			if(!precondition.startsWith("~")){
				if(!s.Contains(precondition)){
					//System.out.println(a.Name);
					return false;
				}
			}else {
				if(s.Contains(precondition.substring(1))){
					return false;
				}
			}
		}
		return true;
	}
	
	/**Verify if the action is applicable*/
	private boolean isActionApplicable(Action a, Step s){
		for(String precondition : a._precond){
			if(!precondition.contains("^")){
				if(!precondition.startsWith("~")){
					if(!s.Contains(precondition)){
						//System.out.println(a.Name);
						return false;
					}
				}else {
					if(s.Contains(precondition.substring(1))){
						return false;
					}
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
