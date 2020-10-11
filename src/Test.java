public class Test {

    public static void main(String[] args) {
        RegExTree tree = RegEx.main(new String[0]);
        Automata automata = Automata.transformToNotDeterminist(tree);
        automata = Automata.transformToDeterminist(automata);
        automata = Automata.transformToMinimalist(automata);
    }
}
