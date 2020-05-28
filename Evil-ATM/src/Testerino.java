public class Testerino {
    public static void main(String[] args) {
        DatabaseInterfacer d = new DatabaseInterfacer("145.24.222.190", 666);

        String[] x = d.getInformation("00000002", "1234");
        for (String a : x) {
            System.out.println(a);
        }
    }
}
