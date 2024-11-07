import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class GestorConcesionario {
    private static Scanner entrada = new Scanner(System.in);
    private static final String FICHERO_TABLAS = "tablas.sql";
    private static final String TABLA_COCHES = "coches";

    // Creacion de coches

    public static Coche crearCoche() {
        String matricula = pedirString("Introduzca la matricula del coche: ", 10);
        String marca = pedirString("Introduzca la marca del coche: ", 50);
        String modelo = pedirString("Introduzca el modelo del coche: ", 50);
        int anyo = pedirAnyo();
        return new Coche(matricula, marca, modelo, anyo);
    }

    public static String pedirString(String mensaje, int longitudMax) {
        boolean valorValido = false;
        String valor = "";

        while(!valorValido) {
            System.out.print(mensaje);
            valor = entrada.nextLine();
            valorValido = esStringValido(valor, longitudMax);
        }
        return valor.trim();
    }

    public static int pedirAnyo() {
        boolean anyoValido = false;
        String anyoIntroducido = "";

        while(!anyoValido) {
            System.out.print("Introduzca el año de fabricacion del coche: ");
            anyoIntroducido = entrada.nextLine();
            anyoValido = esAnyoValido(anyoIntroducido);
        }

        return Integer.parseInt(anyoIntroducido);
    }

    private static boolean esAnyoValido(String anyoIntroducido) {
        if(anyoIntroducido.isEmpty()) {
            System.out.println("[ERROR] el campo no debe de estar vacio, vuelva a introducirlo");
            return false;
        }
        try {
            int anyo = Integer.parseInt(anyoIntroducido);
            if(anyo < 0) {
                System.out.println("[ERROR]El año no puede ser 0");
                return false;
            }
        }  catch (NumberFormatException e) {
            System.out.println("[ERROR] Entrada inválida. Por favor, introduzca un número.");
            return false;
        }
        return true;
    }

    private static boolean esStringValido(String valor, int longitudMax) {
        if(valor.isEmpty()) {
            System.out.println("[ERROR] el campo no debe de estar vacio, vuelva a introducirlo");
            return false;
        }
        if (valor.length() > longitudMax) {
            System.out.println("[ERROR] La longitud maxima del campo es " + longitudMax + ", vuelva a introducirlo");
            return false;
        }
        return true;
    }

    // Para inicializar las tablas
    public static void inicializarBBDD(Connection conexionBBDD) {
        String lineasSQL = leerFichero();
        try (PreparedStatement createStatement = conexionBBDD.prepareStatement(lineasSQL)) {
            createStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Busqueda de coches registrados
    public static Coche obtenerCoche(Connection conexionBBDD, String matricula) {
        Coche coche = null;
        String lineaSQL = "SELECT * FROM " + TABLA_COCHES + " WHERE matricula = ?";

        try (PreparedStatement selectStatement = conexionBBDD.prepareStatement(lineaSQL)) {
            selectStatement.setString(1, matricula);
            ResultSet resultado = selectStatement.executeQuery();
            if(!resultado.isBeforeFirst()) {
                return null;
            }
            resultado.next();
            coche = recostruirCoche(resultado);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return coche;
    }

    // Lectura de todos los coches registrados
    public static ArrayList<Coche> obtenerCoches(Connection conexionBBDD) {
        ArrayList<Coche> cochesConcesionario = new ArrayList<>();
        Coche coche = null;
        String lineaSQL = "SELECT * FROM " + TABLA_COCHES;

        try (PreparedStatement selectStatement = conexionBBDD.prepareStatement(lineaSQL);
        ResultSet resultado = selectStatement.executeQuery()) {
            if(!resultado.isBeforeFirst()) {
                return null;
            }
            while(resultado.next()) {
                coche = recostruirCoche(resultado);
                if(coche != null) {
                    cochesConcesionario.add(coche);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cochesConcesionario;
    }

    private static Coche recostruirCoche(ResultSet resultado) {
        try {
            String matricula = resultado.getString(1);
            String marca = resultado.getString(2);
            String modelo = resultado.getString(3);
            int anyo = resultado.getInt(4);
            return new Coche(matricula, marca, modelo, anyo);
        } catch (SQLException e) {
            return null;
        }
    }

    // Insertado de coches en la BBDD
    public static void registrarCoche(Connection conexionBBDD, Coche coche) {
        String lineaSQL = "INSERT INTO " + TABLA_COCHES
                + "(matricula, marca, modelo, año) VALUES(?, ?, ?, ?)";
        try (PreparedStatement insertStatement = conexionBBDD.prepareStatement(lineaSQL)) {
            insertStatement.setString(1, coche.getMatricula());
            insertStatement.setString(2, coche.getMarca());
            insertStatement.setString(3, coche.getModelo());
            insertStatement.setInt(4, coche.getAnyo());
            insertStatement.executeUpdate();
            System.out.println("El coche con la matricula "
                    + coche.getMatricula() + " ha sido registrado");
        } catch (SQLException e) {
            System.out.println("La matricula " + coche.getMatricula()
                    + " ya esta registrada en la base de datos");
        }
    }

    // Modificacion de coche
    public static void modificarCoche(Connection conexionBBDD, String matricula, int numeroAtributo) {
        String campoACambiar = obtenerCampo(numeroAtributo);
        String lineaSQL = "UPDATE " + TABLA_COCHES
                + " SET " + campoACambiar + " = ? WHERE matricula = ?";

        try (PreparedStatement updateStatement = conexionBBDD.prepareStatement(lineaSQL)) {
            asignarNuevoValor(updateStatement, numeroAtributo);
            updateStatement.setString(2, matricula);
            updateStatement.executeUpdate();
            System.out.println("El coche con la matricula "
                    + matricula + " ha sido modificado");
        } catch (SQLException e) {
            System.out.println("No se pudo modificar el coche con la matricula " + matricula);

        }
    }

    private static void asignarNuevoValor(
            PreparedStatement updateStatement, int numeroAtributo) throws SQLException {
        switch (numeroAtributo) {
            case 1:
                String marca = pedirString(
                        "Introduzca la marca del coche: ", 50);
                updateStatement.setString(1, marca);
                break;
            case 2:
                String modelo = pedirString(
                        "Introduzca el modelo del coche: ", 50);
                updateStatement.setString(1, modelo);
                break;
            case 3:
                int anyo = pedirAnyo();
                updateStatement.setInt(1, anyo);
                break;
        }
    }

    private static String obtenerCampo(int numeroAtributo) {
        String nombreCampo = "";
        switch (numeroAtributo) {
            case 1:
                nombreCampo = "marca";
                break;
            case 2:
                nombreCampo = "modelo";
                break;
            case 3:
                nombreCampo = "año";
                break;
        }
        return nombreCampo;
    }

    // Eliminar coche
    public static void eliminarCoche(Connection conexionBBDD, String matricula) {
        String lineaSQL = "DELETE FROM "
                + TABLA_COCHES + " WHERE matricula = ?";
        try (PreparedStatement deleteStatement = conexionBBDD.prepareStatement(lineaSQL)) {
            deleteStatement.setString(1, matricula);
            deleteStatement.executeUpdate();
            System.out.println("El coche con la matricula "
                    + matricula + " ha sido eliminado");
        } catch (SQLException e) {
            System.out.println("No se pudo eliminar el coche con la matricula " + matricula);
        }
    }

    private static String leerFichero() {
        StringBuilder contenidoFichero = new StringBuilder();
        try {
            File fichero = new File(FICHERO_TABLAS);
            BufferedReader lector = new BufferedReader(new FileReader(fichero));
            String linea = "";
            while ((linea = lector.readLine()) != null) {
                contenidoFichero.append(linea).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contenidoFichero.toString();
    }
}
