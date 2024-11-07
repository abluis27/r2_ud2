import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static Connection conexionBBDD;
    private static final String urlBBDD = "jdbc:mysql://localhost:3306/adat7";
    private static final String usuario = "dam2";
    private static final String password = "asdf.1234";
    private static int opcion;

    public static void main(String[] args) {
        conexionBBDD = obtenerConexionBBDD(urlBBDD, usuario, password);
        GestorConcesionario.inicializarBBDD(conexionBBDD);
        opcion = 0;
        boolean salir = false;

        while(!salir) {
            mostrarMenuPrincial();
            opcion = pedirOpcionMenu(6);
            salir = operar();
        }
    }

    private static Connection obtenerConexionBBDD(String url, String usuario, String password) {
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, password);
            return conexion;
        } catch (SQLException e) {
            System.out.println("Error al iniciar conexion con la base de datos");
            return null;
        }
    }

    private static void mostrarMenuPrincial() {
        System.out.println("+-----------------------------------------------------------+");
        System.out.println("|                 GESTOR BBDD CONCESIONARIO                 |");
        System.out.println("+-----------------------------------------------------------+");
        System.out.println("""
                1) Buscar coche por matricula\
                
                2) Listar todos los coches registrados\
                
                3) Registrar un coche\
                
                4) Modificar informacion de un coche\
                
                5) Eliminar un coche\
                
                6) Salir\
                """);
    }

    private static void mostrarMenuAtributos() {
        System.out.println("[ATRIBUTOS DISPONIBLES PARA CAMBIAR]");
        System.out.println("""
                1) Marca\
                
                2) Modelo\
                
                3) Año de fabricacion\
                """);
    }

    private static int pedirOpcionMenu(int opcionMax) {
        Scanner entrada = new Scanner(System.in);
        int opcion = 0;
        while (opcion < 1 || opcion > opcionMax) {
            try {
                System.out.print("Elija una opcion: ");
                opcion = entrada.nextInt();
                if(opcion < 1 || opcion > opcionMax) {
                    System.out.println("[ERROR] La opcion debe de ser del 1 al " + opcionMax);
                }
            } catch (InputMismatchException e) {
                System.out.println("[ERROR] Debes ingresar un número válido.");
                entrada.next();  // Limpiar la entrada incorrecta del scanner
            }
        }
        return opcion;
    }

    private static boolean operar() {
        System.out.println();
        switch (opcion) {
            case 1:
                buscarCoche();
                break;
            case 2:
                listarCoches();
                break;
            case 3:
                registrarCoche();
                break;
            case 4:
                modificarCoche();
                break;
            case 5:
                eliminarCoche();
                break;
            case 6:
                System.out.println("+---------------------------------------------------+");
                System.out.println("|                  FIN DEL PROGRAMA                 |");
                System.out.println("+---------------------------------------------------+");
                return true;
        }
        return false;
    }

    private static void buscarCoche() {
        System.out.println("|-------------- [BUSQUEDA COCHE POR MATRICULA] --------------|");
        String matricula = GestorConcesionario.pedirString(
                "Introduzca la matricula del coche: ", 10);
        Coche cocheBuscado = GestorConcesionario.obtenerCoche(conexionBBDD, matricula);
        if(cocheBuscado != null){
            System.out.println("[COCHE ENCONTRADO]");
            System.out.println(cocheBuscado);
        } else {
            System.out.println("El coche con la matricula " + matricula + " no existe");
        }
    }

    private static void listarCoches() {
        System.out.println("|-------------- [LISTADO COCHES REGISTRADOS] --------------|");
        ArrayList<Coche> cochesConcesionario = GestorConcesionario.obtenerCoches(conexionBBDD);
        if(cochesConcesionario != null) {
            for (Coche coche : cochesConcesionario) {
                System.out.println(coche);
            }
        } else {
            System.out.println("No hay coches registrados en este momento");
        }
    }

    private static void registrarCoche() {
        System.out.println("|-------------- [REGISTRANDO COCHE] --------------|");
        Coche cocheNuevo = GestorConcesionario.crearCoche();
        GestorConcesionario.registrarCoche(conexionBBDD, cocheNuevo);
    }

    private static void modificarCoche() {
        System.out.println("|-------------- [MODIFICAR COCHE] --------------|");
        String matricula = GestorConcesionario.pedirString(
                "Escriba la matricula del coche que desee modificar: ", 10);
        if(cocheExiste(matricula)) {
            System.out.println("[DATOS ACTUALES DEL COCHE]");
            System.out.println(GestorConcesionario.obtenerCoche(conexionBBDD, matricula));
            mostrarMenuAtributos();
            int numeroAtributo = pedirOpcionMenu(3);
            GestorConcesionario.modificarCoche(conexionBBDD, matricula, numeroAtributo);
        } else {
            System.out.println("No hay ningun coche con la matricula " + matricula + " registrado");
        }
    }

    private static void eliminarCoche() {
        System.out.println("|-------------- [ELIMINAR COCHE] --------------|");
        String matricula = GestorConcesionario.pedirString(
                "Introduzca la matricula del coche: ", 10);
        if(cocheExiste(matricula)) {
            GestorConcesionario.eliminarCoche(conexionBBDD, matricula);
        } else {
            System.out.println("No hay ningun coche con la matricula " + matricula + " registrado");
        }
    }

    private static boolean cocheExiste(String matricula) {
        return GestorConcesionario.obtenerCoche(conexionBBDD, matricula) != null;
    }
}
