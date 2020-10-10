import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Automata {
    private ArrayList<Map<Character, ArrayList<Integer>>> automata = new ArrayList<>();
    private ArrayList<Boolean> startStates = new ArrayList<>();
    private ArrayList<Boolean> finalStates = new ArrayList<>();
    private static final Character epsilon = 'ε';


    public int numberStates(){
        return automata.size();
    }

    public void addState(boolean isStartState, boolean isFinalState){
        automata.add(new HashMap<>());
        startStates.add(isStartState);
        finalStates.add(isFinalState);
    }

    public void addTransition(Character ch, int from_state, int to_state){
        ArrayList<Integer> to_states = automata.get(from_state).get(ch);
        if (to_states != null){
            to_states.add(to_state);
        }
        else{
            to_states = new ArrayList<>();
            to_states.add(to_state);
            automata.get(from_state).put(ch, to_states);
        }

    }

    private ArrayList<Integer> getStartStates(){
        ArrayList<Integer> result = new ArrayList<>();
        for(int i =0; i<startStates.size(); i++){
            if (startStates.get(i)){
                result.add(i);
            }
        }
        return result;
    }

    private ArrayList<Integer> getFinalStates(){
        ArrayList<Integer> result = new ArrayList<>();
        for(int i =0; i<finalStates.size(); i++){
            if (finalStates.get(i)){
                result.add(i);
            }
        }
        return result;
    }

    private Integer getLastStartState(){
        for(int i = startStates.size()-1; i>=0; i--){
            if(startStates.get(i))
                return i;
        }
        return null;
    }

    private Integer getLastFinalState(){
        for(int i = finalStates.size()-1; i>=0; i--){
            if(finalStates.get(i))
                return i;
        }
        return null;
    }

    private Integer getBeforeLastStartState(){
        boolean beforeLast = false;
        for(int i = startStates.size()-1; i>=0; i--){
            if (startStates.get(i)) {
                if (beforeLast)
                    return i;
                else
                    beforeLast = true;
            }
        }
        return null;
    }

    private Integer getBeforeLastFinalState(){
        boolean beforeLast = false;
        for(int i = finalStates.size()-1; i>=0; i--){
            if (finalStates.get(i)) {
                if (beforeLast)
                    return i;
                else
                    beforeLast = true;
            }
        }
        return null;
    }

    public static Automata transformToNotDeterminist(RegExTree tree){
        Automata result = new Automata();
        transformation1(result, tree);
        return result;
    }

    private static void transformation1(Automata automata, RegExTree tree) {
        switch (tree.root){
            case RegEx.ALTERN: {
                RegExTree sag = tree.subTrees.get(0);
                RegExTree sad = tree.subTrees.get(1);
                int start_state_num = automata.numberStates();
                automata.addState(true,false);
                transformation1(automata, sag);
                int final_state_num = automata.numberStates();
                automata.addState(false,true);
                transformation1(automata, sad);
                ArrayList<Integer> startStatesList = automata.getStartStates();
                for(Integer to_state : startStatesList){
                    if(to_state != start_state_num){
                        automata.addTransition(Automata.epsilon, start_state_num, to_state);
                        automata.startStates.set(to_state, false);
                    }
                }
                ArrayList<Integer> finalStatesList = automata.getFinalStates();
                for(Integer to_state : finalStatesList){
                    if(to_state != final_state_num){
                        automata.addTransition(Automata.epsilon, to_state, final_state_num);
                        automata.finalStates.set(to_state, false);
                    }
                }
                break;
            }
            case RegEx.CONCAT: {
                RegExTree sag = tree.subTrees.get(0);
                RegExTree sad = tree.subTrees.get(1);
                transformation1(automata, sag);
                transformation1(automata, sad);
                automata.addTransition(Automata.epsilon, automata.getBeforeLastFinalState(), automata.getLastStartState());
                automata.finalStates.set(automata.getBeforeLastFinalState(), false);
                automata.startStates.set(automata.getLastStartState(), false);
                break;
            }
            case RegEx.ETOILE: {
                RegExTree sa = tree.subTrees.get(0);
                int start_state_num = automata.numberStates();
                automata.addState(true,false);
                transformation1(automata,sa);
                int final_state_num = automata.numberStates();
                automata.addState(false,true);
                automata.addTransition(Automata.epsilon, automata.getBeforeLastFinalState(), automata.getLastStartState());
                automata.addTransition(Automata.epsilon, automata.getBeforeLastStartState(), automata.getLastStartState());
                automata.addTransition(Automata.epsilon, automata.getBeforeLastFinalState(), automata.getLastFinalState());
                automata.addTransition(Automata.epsilon, automata.getBeforeLastStartState(), automata.getLastFinalState());
                automata.startStates.set(automata.getLastStartState(), false);
                automata.finalStates.set(automata.getBeforeLastFinalState(), false);
                break;
            }
            case RegEx.DOT: {
                int start_state_num = automata.numberStates();
                automata.addState(true,false);
                int final_state_num = automata.numberStates();
                automata.addState(false,true);
                for(char ch = 'a'; ch <= 'z'; ch++){
                    automata.addTransition(ch,start_state_num,final_state_num);
                }
                for(char ch = 'A'; ch <= 'Z'; ch++){
                    automata.addTransition(ch,start_state_num,final_state_num);
                }
                break;
            }
            default: {
                int start_state_num = automata.numberStates();
                automata.addState(true,false);
                int final_state_num = automata.numberStates();
                automata.addState(false,true);
                automata.addTransition((char) tree.root,start_state_num,final_state_num);
            }
        }

    }


}
