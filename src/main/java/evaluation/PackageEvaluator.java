package evaluation;

public class PackageEvaluator {

    private int totalClasses = 0;
    private int totalLOC = 0;
    private int totalCC = 0;

    public void addClass(int loc, int cc) {
        totalClasses++;
        totalLOC += loc;
        totalCC += cc;
    }

    public void printReport() {
        System.out.println("\nEvaluación de Paquete");
        System.out.println("-----------------------");
        System.out.println("Clases: " + totalClasses);
        System.out.println("LOC total: " + totalLOC);
        System.out.println("CC total: " + totalCC);

        if (totalCC > totalClasses * 10) {
            System.out.println("Paquete con alta complejidad");
        } else {
            System.out.println("Paquete saludable");
        }
    }
}
