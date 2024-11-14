import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ContadorSqlTransaccional {

	public static void main(String[] args) throws ClassNotFoundException {
		// Prueba de concepto de transacción con bloqueo de fila para lectura
		// Sería más fácil en el propio sql poner un set cuenta=cuenta+1 pero ilustramos
		// aquí el problema de concurrencia entre varios procesos.
		// con el for update + transacción conseguimos el bloque de fila y atomicidad
		String sqlConsulta = "select nombre,cuenta from contadores where nombre='contador1' for update;";
		String sqlActualización = "update contadores set cuenta=? where nombre='contador1';";
		String crearTabla = "CREATE TABLE if not exists contadores  (nombre VARCHAR(255) PRIMARY KEY, cuenta INT NOT NULL) ;";
		// String insert = "insert into contadores values ('contador1',1000);";

		String url = "jdbc:h2:./databses/apartado2.db";
		try (Connection con = DriverManager.getConnection(url, "dam2", "asdf.1234")) {
			//	PreparedStatement insertAA=con.prepareStatement(insert);
			//	insertAA.executeUpdate();
			PreparedStatement crearTablaInicial = con.prepareStatement(crearTabla);
			crearTablaInicial.executeUpdate();
			
			PreparedStatement consulta = con.prepareStatement(sqlConsulta);
			PreparedStatement actualización = con.prepareStatement(sqlActualización);
			int cuenta = 0;

			for (int i = 0; i < 1000; i++) {
				con.setAutoCommit(false);
				ResultSet res = consulta.executeQuery();
				if (res.next()) {
					cuenta = res.getInt(2);
					cuenta++;
					actualización.setInt(1, cuenta);
					actualización.executeUpdate();
				} else
					break;
				con.commit();
				con.setAutoCommit(false);
			} // for
			System.out.println("Valor final: " + cuenta);
		} catch (Exception e) {
			e.printStackTrace();
		} // try
	} // main
} // class ContadorSql
