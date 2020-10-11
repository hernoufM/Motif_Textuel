import java.util.*;

public class Automata {
    private ArrayList<Map<Character, ArrayList<Integer>>> automata = new ArrayList<>();
    private ArrayList<Boolean> startStates = new ArrayList<>();
    private ArrayList<Boolean> finalStates = new ArrayList<>();
    private static final Character epsilon = 'ε';


    public int numberStates() {
        return automata.size();
    }

    public void addState(boolean isStartState, boolean isFinalState) {
        automata.add(new HashMap<>());
        startStates.add(isStartState);
        finalStates.add(isFinalState);
    }

    public void addTransition(Character ch, int from_state, int to_state) {
        ArrayList<Integer> to_states = automata.get(from_state).get(ch);
        if (to_states != null) {
            to_states.add(to_state);
        } else {
            to_states = new ArrayList<>();
            to_states.add(to_state);
            automata.get(from_state).put(ch, to_states);
        }

    }

    private ArrayList<Integer> getStartStates() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < startStates.size(); i++) {
            if (startStates.get(i)) {
                result.add(i);
            }
        }
        return result;
    }

    private ArrayList<Integer> getFinalStates() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < finalStates.size(); i++) {
            if (finalStates.get(i)) {
                result.add(i);
            }
        }
        return result;
    }

    private Integer getLastStartState() {
        for (int i = startStates.size() - 1; i >= 0; i--) {
            if (startStates.get(i))
                return i;
        }
        return null;
    }

    private Integer getLastFinalState() {
        for (int i = finalStates.size() - 1; i >= 0; i--) {
            if (finalStates.get(i))
                return i;
        }
        return null;
    }

    private Integer getBeforeLastStartState() {
        boolean beforeLast = false;
        for (int i = startStates.size() - 1; i >= 0; i--) {
            if (startStates.get(i)) {
                if (beforeLast)
                    return i;
                else
                    beforeLast = true;
            }
        }
        return null;
    }

    private Integer getBeforeLastFinalState() {
        boolean beforeLast = false;
        for (int i = finalStates.size() - 1; i >= 0; i--) {
            if (finalStates.get(i)) {
                if (beforeLast)
                    return i;
                else
                    beforeLast = true;
            }
        }
        return null;
    }

    public static Automata transformToNotDeterminist(RegExTree tree) {
        Automata result = new Automata();
        transformation1(result, tree);
        return result;
    }

    private static void transformation1(Automata automata, RegExTree tree) {
        switch (tree.root) {
            case RegEx.ALTERN: {
                RegExTree sag = tree.subTrees.get(0);
                RegExTree sad = tree.subTrees.get(1);
                int start_state_num = automata.numberStates();
                automata.addState(true, false);
                transformation1(automata, sag);
                int final_state_num = automata.numberStates();
                automata.addState(false, true);
                transformation1(automata, sad);
                ArrayList<Integer> startStatesList = automata.getStartStates();
                for (Integer to_state : startStatesList) {
                    if (to_state != start_state_num) {
                        automata.addTransition(Automata.epsilon, start_state_num, to_state);
                        automata.startStates.set(to_state, false);
                    }
                }
                ArrayList<Integer> finalStatesList = automata.getFinalStates();
                for (Integer to_state : finalStatesList) {
                    if (to_state != final_state_num) {
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
                automata.addState(true, false);
                transformation1(automata, sa);
                int final_state_num = automata.numberStates();
                automata.addState(false, true);
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
                automata.addState(true, false);
                int final_state_num = automata.numberStates();
                automata.addState(false, true);
                for (char ch = 'a'; ch <= 'z'; ch++) {
                    automata.addTransition(ch, start_state_num, final_state_num);
                }
                for (char ch = 'A'; ch <= 'Z'; ch++) {
                    automata.addTransition(ch, start_state_num, final_state_num);
                }
                break;
            }
            default: {
                int start_state_num = automata.numberStates();
                automata.addState(true, false);
                int final_state_num = automata.numberStates();
                automata.addState(false, true);
                automata.addTransition((char) tree.root, start_state_num, final_state_num);
            }
        }

    }

    private Set<Integer> constructStartSet() {
        Set<Integer> result = new HashSet<>();
        ArrayList<Integer> startStatesList = new ArrayList<>();
        for (int i = 0; i < numberStates(); i++) {
            if (startStates.get(i))
                startStatesList.add(i);
        }
        for (Integer start_state : startStatesList) {
            result.addAll(reduceEpsilonTransitions(start_state));
        }
        return result;
    }

    private Set<Integer> reduceEpsilonTransitions(int start_state) {
        Set<Integer> result = new HashSet<>();
        result.add(start_state);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Integer state : result) {
                Map<Character, ArrayList<Integer>> transitions = automata.get(state);
                if (transitions.containsKey(Automata.epsilon)) {
                    for (Integer state_to_add : transitions.get(Automata.epsilon)) {
                        if (!result.contains(state_to_add)) {
                            result.add(state_to_add);
                            changed = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static int getNumberSet(ArrayList<Set<Integer>> sets, Set<Integer> set) {
        for (int i = 0; i < sets.size(); i++) {
            if (set.equals(sets.get(i)))
                return i;
        }
        return -1;
    }

    private boolean isStartSet(Set<Integer> set) {
        for (Integer state : set) {
            if (startStates.get(state))
                return true;
        }
        return false;
    }

    private boolean isFinalSet(Set<Integer> set) {
        for (Integer state : set) {
            if (finalStates.get(state))
                return true;
        }
        return false;
    }

    public static Automata transformToDeterminist(Automata nda) {
        Automata result = new Automata();
        ArrayList<Set<Integer>> sets = new ArrayList<>();
        ArrayList<Set<Integer>> workingList = new ArrayList<>();
        Set<Integer> start_set = nda.constructStartSet();
        sets.add(start_set);
        result.addState(nda.isStartSet(start_set), nda.isFinalSet(start_set));
        workingList.add(start_set);
        while (!workingList.isEmpty()) {
            Set<Integer> current_set = workingList.remove(0);
            for (Integer current_state : current_set) {
                Map<Character, ArrayList<Integer>> transitions = nda.automata.get(current_state);
                for (Map.Entry<Character, ArrayList<Integer>> entry : transitions.entrySet()) {
                    if (!entry.getKey().equals(Automata.epsilon)) {
                        char ch = entry.getKey();
                        for (Integer state_to : entry.getValue()) {
                            Set<Integer> set_to = nda.reduceEpsilonTransitions(state_to);
                            if (getNumberSet(sets, set_to) == -1) {
                                sets.add(set_to);
                                workingList.add(set_to);
                                result.addState(nda.isStartSet(set_to), nda.isFinalSet(set_to));
                            }
                            if (!result.automata.get(getNumberSet(sets, current_set)).containsValue(ch)) {
                                result.addTransition(ch, getNumberSet(sets, current_set), getNumberSet(sets, set_to));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

}
