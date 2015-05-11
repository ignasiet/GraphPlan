import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 
 */

/**
 * @author ignasi
 *
 */
public class main {

	/**
	 * 
	 */
	public main() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Hashtable<String, Integer> state = new Hashtable<String, Integer>();
		Hashtable<String, AbstractAction> actions = new Hashtable<String, AbstractAction>();
		ArrayList<String> goal = new ArrayList<String>();
		
		//init
		ArrayList<String> _precond = new ArrayList<String>();
		ArrayList<String> _effect = new ArrayList<String>();
		_effect.add("P");
		mock_Action action = new mock_Action();
		action._precond = _precond;
		action._Positive_effects = _effect;
		action.Name = "OpP";
		
		mock_Action action2 = new mock_Action();
		action2.Name = "OpG1";
		action2._precond.add("P");
		action2._Positive_effects.add("Goal1");
		
		mock_Action action3 = new mock_Action();
		action3.Name = "OpG2";
		action3._precond.add("P");
		action3._Positive_effects.add("Goal2");
		
		actions.put("OpP", action);
		actions.put("OpG1", action2);
		actions.put("OpG2", action3);
		
		goal.add("Goal2");
		goal.add("Goal1");
		
		graphplanner gp = new graphplanner(state, actions, goal);		
	}

}
