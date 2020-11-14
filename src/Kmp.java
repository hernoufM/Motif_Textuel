/**
 * Classe qui reunit l'algorithme KMP et ses attributs et methodes auxilierre
 */
public class Kmp {

    /**
     * Carry Over
     */
    private static int[] carryOver;

    /**
     * Construit un Carry Over à partir d'un mot
     */
    public static int[] constructCarryOver(String regex){
        carryOver = new int[regex.length()];
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
        return  carryOver;
    }

    /**
     * KMP algorithme
     */
    public static void kmp_algo(String line, String regex){
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
}
