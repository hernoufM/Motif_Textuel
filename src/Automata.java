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
                transformation1(automata, sad);
                int final_state_num = automata.numberStates();
                automata.addState(false, true);

                ArrayList<Integer> startStatesList = new ArrayList<>();
                startStatesList.add(automata.getBeforeLastStartState());
                startStatesList.add(automata.getLastStartState());
                for (Integer to_state : startStatesList) {
                    if (to_state != start_state_num) {
                        automata.addTransition(Automata.epsilon, start_state_num, to_state);
                        automata.startStates.set(to_state, false);
                    }
                }
                ArrayList<Integer> finalStatesList = new ArrayList<>();
                int i = automata.numberStates() - 1;
                while (finalStatesList.size() < 2) {
                    if (automata.finalStates.get(i) && i != automata.getLastFinalState()) {
                        finalStatesList.add(i);
                    }
                    i--;
                }
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

    // TODO: working list
    private Set<Integer> reduceEpsilonTransitions(int start_state) {
        Set<Integer> result = new HashSet<>();
        result.add(start_state);
        boolean changed = true;
        while (changed) {
            Set<Integer> set = new HashSet<>(result);
            changed = false;
            for (Integer state : result) {
                Map<Character, ArrayList<Integer>> transitions = automata.get(state);
                if (transitions.containsKey(Automata.epsilon)) {
                    for (Integer state_to_add : transitions.get(Automata.epsilon)) {
                        if (!result.contains(state_to_add)) {
                            set.add(state_to_add);
                            changed = true;
                        }
                    }
                }
            }
            result.addAll(set);

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

    public static Automata transformToDeterminist(Automata ndfa) {
        Automata result = new Automata();
        ArrayList<Set<Integer>> sets = new ArrayList<>();
        ArrayList<Set<Integer>> workingList = new ArrayList<>();
        Set<Integer> start_set = ndfa.constructStartSet();
        sets.add(start_set);
        result.addState(ndfa.isStartSet(start_set), ndfa.isFinalSet(start_set));
        workingList.add(start_set);
        while (!workingList.isEmpty()) {
            Set<Integer> current_set = workingList.remove(0);
            for (Integer current_state : current_set) {
                Map<Character, ArrayList<Integer>> transitions = ndfa.automata.get(current_state);
                for (Map.Entry<Character, ArrayList<Integer>> entry : transitions.entrySet()) {
                    if (!entry.getKey().equals(Automata.epsilon)) {
                        char ch = entry.getKey();
                        for (Integer state_to : entry.getValue()) {
                            Set<Integer> set_to = ndfa.reduceEpsilonTransitions(state_to);
                            if (getNumberSet(sets, set_to) == -1) {
                                sets.add(set_to);
                                workingList.add(set_to);
                                result.addState(ndfa.isStartSet(set_to), ndfa.isFinalSet(set_to));
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

    private static int getSetOfState(int state, ArrayList<Set<Integer>> sets) {
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).contains(state))
                return i;
        }
        return -1;
    }

    private boolean isEquivalentStates(int state1, int state2, ArrayList<Set<Integer>> sets) {
        Map<Character, ArrayList<Integer>> transitions1 = automata.get(state1);
        Map<Character, ArrayList<Integer>> transitions2 = automata.get(state2);
        if (!transitions1.keySet().equals(transitions2.keySet()))
            return false;
        for (Map.Entry<Character, ArrayList<Integer>> entry : transitions1.entrySet()) {
            int state_to1 = entry.getValue().get(0);
            int state_to2 = transitions2.get(entry.getKey()).get(0);
            if (!(getSetOfState(state_to1, sets) == getSetOfState(state_to2, sets))) {
                return false;
            }
        }
        return true;
    }

    public static Automata transformToMinimalist(Automata dfa) {
        Automata result = new Automata();
        ArrayList<Set<Integer>> sets = new ArrayList<>();
        Set<Integer> start_set1 = new HashSet<>();
        Set<Integer> start_set2 = new HashSet<>();
        for (int i = 0; i < dfa.numberStates(); i++) {
            if (!dfa.finalStates.get(i))
                start_set1.add(i);
            else
                start_set2.add(i);
        }
        sets.add(start_set1);
        sets.add(start_set2);
        ArrayList<Set<Integer>> new_sets = new ArrayList<>();
        for (Set<Integer> current_set : sets) {
            for (Integer state1 : current_set) {
                if (getSetOfState(state1, new_sets) == -1) {
                    Set<Integer> new_set = new HashSet<>();
                    new_set.add(state1);
                    for (Integer state2 : current_set) {
                        if (state1 != state2 && dfa.isEquivalentStates(state1, state2, sets) && getSetOfState(state2, new_sets) == -1) {
                            new_set.add(state2);
                        }
                    }
                    new_sets.add(new_set);
                    result.addState(dfa.isStartSet(new_set), dfa.isFinalSet(new_set));
                }
            }
        }
        for (int i = 0; i < new_sets.size(); i++) {
            Set<Integer> current_set = new_sets.get(i);
            Object state_obj = current_set.toArray()[0];
            Integer state = (Integer) state_obj;
            Map<Character, ArrayList<Integer>> transitions = dfa.automata.get(state);
            for (Map.Entry<Character, ArrayList<Integer>> entry : transitions.entrySet()) {
                result.addTransition(entry.getKey(), i, getSetOfState(entry.getValue().get(0), new_sets));
            }

        }
        return result;
    }

}
