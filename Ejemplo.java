public class Ejemplo {

    // Método BUENO (control)
    public int sumar(int a, int b) {
        return a + b;
    }

    // Método REGULAR (largo + complejidad moderada)
    public double calcularDescuento(
            double precio,
            boolean esClienteVIP,
            boolean tieneCupon,
            boolean esFestivo,
            int cantidad
    ) {

        double descuento = 0;

        if (precio > 100) {
            descuento += 0.05;
        } else {
            descuento += 0.02;
        }

        if (esClienteVIP) {
            descuento += 0.10;
        } else {
            descuento += 0.03;
        }

        if (tieneCupon) {
            descuento += 0.07;
        } else {
            descuento += 0.01;
        }

        if (esFestivo) {
            descuento += 0.04;
        }

        if (cantidad > 10) {
            descuento += 0.06;
        }

        return precio * descuento;
    }

    // Método MALO (muy largo + loops + condicionales)
    public void procesarPedido(
            int tipoPedido,
            boolean pagado,
            boolean enviado,
            boolean entregado,
            int intentos
    ) {

        if (tipoPedido == 1) {
            System.out.println("Pedido normal");
        } else {
            System.out.println("Pedido especial");
        }

        if (pagado) {
            System.out.println("Pago confirmado");
        } else {
            System.out.println("Pago pendiente");
        }

        if (!enviado) {
            for (int i = 0; i < intentos; i++) {
                System.out.println("Reintentando envío...");
            }
        }

        if (entregado) {
            System.out.println("Pedido entregado");
        } else {
            System.out.println("Pedido no entregado");
        }

        for (int i = 0; i < 3; i++) {
            if (i % 2 == 0) {
                System.out.println("Verificación " + i);
            }
        }
    }

    // 🔥 MÉTODOS EXTRA PARA FORZAR GOD CLASS
    public void log(String msg) {
        System.out.println(msg);
    }

    public void validarEstado(int estado) {
        if (estado == 1) log("OK");
        else log("ERROR");
    }

    public void guardarBD() {
        System.out.println("Guardando en base de datos...");
    }

    public void enviarCorreo() {
        System.out.println("Enviando correo...");
    }

    public void generarFactura() {
        System.out.println("Generando factura...");
    }
}
