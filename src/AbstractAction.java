import java.util.ArrayList;

/**
 * @author ignasi
 *
 */
public abstract class AbstractAction {

	/**
	 * 
	 */
	public ArrayList<String> _precond;
	public ArrayList<String> _Positive_effects;
	public ArrayList<String> _Negative_effects;
	public String Name;
	//public abstract void expand();
	public boolean IsObservation;
	public boolean deductive_action = false;
	

}
