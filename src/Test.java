import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Test {

    public static int[] constructCarryOver(String regex){
        int [] carryOver = new int[regex.length()];
        int cnd=0;
        carryOver[0] = -1;
        for(int i = 1; i<regex.length(); i++){
            if(regex.charAt(i) == regex.charAt(cnd))
            {
                carryOver[i] = carryOver[cnd];
            }
            else
            {
                carryOver[i] = cnd;
                while(cnd>=0 && regex.charAt(i)!=regex.charAt(cnd)){
                    cnd = carryOver[cnd];
                }
            }
            cnd++;
        }
//        carryOver[regex.length()] = cnd;
        return  carryOver;
    }

    public static void kmp_algo(String line, String regex, int [] carryOver ){
        int j=0,i=0;
        while(i < line.length() && j<regex.length()){
            if(line.charAt(i) == regex.charAt(j)) {
                j++;
                i++;
            } else {
                if (carryOver[j] == -1){
                    j = 0;
                    i++;
                }
                else{
                    j = carryOver[j];
                }
            }
        }
        if(j == regex.length()){
            System.out.println(line);
        }
    }

    public static boolean is_Word(String regex){
        for(int i =0; i<regex.length(); i++){
            switch (regex.charAt(i)) {
                case '(': {}
                case ')': {}
                case '.': {}
                case '|': {}
                case '*': {return  false;}
                default: {}
            }
        }
        return  true;
    }

    private static class StackElement{
        char ch;
        int char_pos;
        int state_num;
        int transition_num;

        StackElement(char c, int cp, int sn, int tn){
            ch = c;
            char_pos =cp;
            state_num = sn;
            transition_num = tn;
        }
    }

    public static void acceptLine(String line, Automata automata) {
        int line_length = line.length();
        for (int i = 0; i < line_length; i++) {
            int currstate = 0;
            LinkedList<StackElement> stack = new LinkedList<>();
            int j = i;
            while(j<line_length || !stack.isEmpty()){
                if (automata.getFinalStates().get(currstate)) {
                    System.out.println(line);
                    return;
                }
                if (j<line_length  && automata.getAutomata().get(currstate).containsKey(line.charAt(j))) {
                    ArrayList<Integer> transitions = automata.getAutomata().get(currstate).get(line.charAt(j));
                    if(transitions.size()!=1) {
                        stack.addFirst(new StackElement(line.charAt(j), j ,currstate,0));
                    }
                    currstate = transitions.get(0);
                } else {
                    if (stack.isEmpty()){
                        break;
                    }
                    else {
                        StackElement elt = stack.removeFirst();
                        ArrayList<Integer> transitions = automata.getAutomata().get(elt.state_num).get(elt.ch);
                        while(elt.transition_num == transitions.size()-1 && !stack.isEmpty()){
                            elt = stack.removeFirst();
                            transitions = automata.getAutomata().get(elt.state_num).get(elt.ch);
                        }
                        if(elt.transition_num != transitions.size()-1){
                            stack.addFirst(new StackElement(elt.ch, elt.char_pos, elt.state_num, elt.transition_num+1));
                            currstate = transitions.get(elt.transition_num+1);
                            j = elt.char_pos;
                        }

                    }
                }
                j++;
            }
        }
    }

    public static void acceptLines(Automata a, String filename) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(
                    filename));
            for (String line : lines) {
                acceptLine(line, a);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void acceptLinesKMP(String filename, String word, int[] carryOver) {

        try {
            List<String> lines = Files.readAllLines(Paths.get(
                    filename));
            for (String line : lines) {
                kmp_algo(line, word, carryOver);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RegExTree tree = RegEx.main(new String[0]);
        if (is_Word(RegEx.regEx)){
            int [] carryOver = constructCarryOver(RegEx.regEx);
            acceptLinesKMP("56667-0.txt",RegEx.regEx,carryOver);
        }
        else{
            Automata automata = Automata.transformToNotDeterminist(tree);
            automata = Automata.transformToDeterminist(automata);
            automata = Automata.transformToMinimalist(automata);
            acceptLines(automata,"56667-0.txt");
        }
    }

}
