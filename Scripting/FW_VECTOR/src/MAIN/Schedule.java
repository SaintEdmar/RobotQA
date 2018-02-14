package MAIN;
import java.sql.SQLException;
public class Schedule {
	//public static void main(String[] args) throws SQLException {
	public static void DemonioSchedule() throws SQLException, InterruptedException {
		//Variables.ruta = "Q:/TestingQA/"; //RUTA DEL FRAMEWORK SELENIUM WEB DRIVER
		Variables.ruta = ConfiguradorQA.textRutaEVD.getText(); //RUTA DEL FRAMEWORK SELENIUM WEB DRIVER
		Variables.file = Variables.ruta + "Suite_Evidencia/"; //RUTA DE LA CARPETA EVD
		//System.out.println("Iniciando en: " + Variables.bln_demonio_escenario);
			while (Variables.bln_demonio_escenario == true){
				//System.out.println("Entrando al hilo: " + UtilsVector.getFechaActual() + " " + UtilsVector.getHoraActual());
				//UtilsVector.schedule("SQL", "MINI_FW", "172.22.1.249", "sa", "sql2015");
				UtilsVector.schedule("SQL", ConfiguradorQA.txtNameDB.getText(),ConfiguradorQA.txtNameServidor.getText(), ConfiguradorQA.txtUsuario.getText(), ConfiguradorQA.txtPassWord.getText());
				ConfiguradorQA.semaforo();
				if (Variables.blnpendiente){
					System.out.println("Existe escenario: " + UtilsVector.getFechaActual() + " " + UtilsVector.getHoraActual());
					UtilsVector.kill_procesos(Variables.web);
					UtilsVector.rastreador_casos();
					UtilsVector.orquestador();
					UtilsVector.kill_procesos(Variables.web);
					UtilsVector.desconectar_sql();
				}
				ConfiguradorQA.semaforo();
			}
	}
}
