import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Test {

//    private static int longestProperSuffixPreffix(String word){
//        int i = 0;
//        int j = word.length()-1;
//        int size = 0;
//        while(j>0){
//            if(word.substring(0,i+1).equals(word.substring(j))){
//                size = i+1;
//            }
//            i++;
//            j--;
//        }
//        return size;
//    }
//
//    public static int[] constructCarryOver(String regex){
//        int [] carryOver = new int[regex.length()];
//        carryOver[0] = -1;
//        for(int i = 1; i<regex.length(); i++){
//            carryOver[i] = longestProperSuffixPreffix(regex.substring(0,i));
//        }
//        for(int i = 1; i<regex.length(); i++){
//            if(carryOver[i]==0 && regex.charAt(i) == regex.charAt(0)){
//                carryOver[i] = -1;
//            }
//        }
//        for(int i = 1; i<regex.length(); i++){
//            if(carryOver[i]>0 && carryOver[carryOver[i]]==-1){
//                carryOver[i] = -1;
//            }
//        }
//        return  carryOver;
//    }

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

    public static void main(String[] args) {
        RegExTree tree = RegEx.main(new String[0]);
        Automata automata = Automata.transformToNotDeterminist(tree);
        automata = Automata.transformToDeterminist(automata);
        automata = Automata.transformToMinimalist(automata);
        acceptLines(automata,"56667-0.txt");
//        int[] carryOver = constructCarryOver("abacababc");
//        for (int i = 0; i<carryOver.length;i++){
//            System.out.println(carryOver[i]);
//        }
    }

}
