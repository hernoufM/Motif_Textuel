import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe contenant methode main et des methodes permetant 'matcher' regex avec le texte et afficher le resultat de matching (comme la commande egrep)
 */
public class Motif_textuel {

    /**
     * Appelle algorithme de matching sur chaque ligne
     */
    public static void acceptLines(Automata a, String filename) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(
                    filename));
            for (String line : lines) {
                acceptLine(line, a);
            }
        } catch (IOException e) {
            System.err.println("ERROR : File not exists");
            System.exit(1);
        }
    }

    /**
     * Appelle KMP algorithme sur chaque ligne
     */
    public static void acceptLinesKMP(String filename, String word) {

        try {
            List<String> lines = Files.readAllLines(Paths.get(
                    filename));
            for (String line : lines) {
                Kmp.kmp_algo(line, word);
            }
        } catch (IOException e) {
            System.err.println("ERROR : File not exists");
            System.exit(1);
        }
    }

    /**
     * Indique si le regex contient que des concatenations
     */
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

    /**
     * Classe qui represente un element de la pile, qui permet de stocker l'information sur le derniere choix du chemin de matching
     */
    private static class StackElement{
        /**
         * Caractere de matching
         */
        char ch;
        /**
         * Position dans la ligne
         */
        int char_pos;
        /**
         * Numero d'etat au moment de matching
         */
        int state_num;
        /**
         * Le numero du transition choisi
         */
        int transition_num;

        /**
         * Constructeur
         */
        StackElement(char c, int cp, int sn, int tn){
            ch = c;
            char_pos =cp;
            state_num = sn;
            transition_num = tn;
        }
    }

    /**
     * Recherche mot dans une ligne de texte
     */
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

    /**
     * Methode main
     */
    public static void main(String[] args) {
        if (args.length !=2){
            System.err.println("Correct use : motif-textuel [regex] [filename]");
            return;
        }
        String regex = args[0];
        String filename = args[1];
        RegExTree tree;
        if (regex.length() == 0){
            System.err.println("ERROR : Regex shouldn't be empty");
            return;
        }
        try {
            tree = RegEx.parse_main(regex);
        } catch (Exception e) {
            System.err.println("ERROR: Syntax error for regex \"" + regex + "\".");
            return;
        }
        if (is_Word(regex)){
            Kmp.constructCarryOver(regex);
            acceptLinesKMP(filename,regex);
        }
        else{
            Automata automata = Automata.transformToNotDeterminist(tree);
            automata = Automata.transformToDeterminist(automata);
            automata = Automata.transformToMinimalist(automata);
            acceptLines(automata,filename);
        }
    }

}
