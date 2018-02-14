package MAIN;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.GregorianCalendar;
import java.sql.*;

import org.apache.bcel.generic.SWITCH;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.*;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
@SuppressWarnings("unused")
public class UtilsVector {
public static void coneccion_db (String Str_BasedeDatos,String Str_Datapool,String Str_Servidor, String Str_Usuario ,String Str_Password) {
	try {
	switch (Str_BasedeDatos.toLowerCase())
	{
	 case "sql":
		 	SQLServerDataSource sqlDs = new SQLServerDataSource();
		 	sqlDs.setIntegratedSecurity(false);
		 	sqlDs.setServerName(Str_Servidor); // Instancia
		 	sqlDs.setDatabaseName(Str_Datapool);  //BD       
		 	sqlDs.setUser(Str_Usuario);//usuario
		 	sqlDs.setPassword(Str_Password);//clave usuario
		 	sqlDs.setPortNumber(1433);
	        try {
	        	Variables.Cnn = sqlDs.getConnection();
	        }
	        catch (Exception e){
	    		System.out.println(e.getMessage());
	    	}
	        break;
	 case "as400":
		 	AS400 CAS400 = new AS400();	
		 	CAS400.setGuiAvailable(false);
		 	DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
		 	Connection Cnn400;
		 	Cnn400 = DriverManager.getConnection("jdbc:as400://" + Str_Servidor + ";prompt=false;user=" + Str_Usuario + ";password=" + Str_Password + ";naming=system;"); 
	        DatabaseMetaData dmd = Cnn400.getMetaData(); 
	        Statement Sentencia = Cnn400.createStatement(); 
	        //ejecutamos la consulta y la almacenamos en rs 
	        //ResultSet rs = Sentencia.executeQuery("SELECT * FROM KSM");
		 	break;
	 case "oracle":
		 	//180.181.106.24:1667/ebusines
		 	Class.forName("oracle.jdbc.driver.OracleDriver");
		    Variables.CnnOracle = DriverManager.getConnection("jdbc:oracle:thin:@" + Str_Servidor, Str_Usuario, Str_Password);
		    if (Variables.CnnOracle != null) {
		    	System.out.println("Conectado Oracle Exitosa");
		    } else {
		    	System.out.println("Fallo Conectado Oracle");
		    }

	}
	}
	catch (Exception e){
		System.out.println(e.getMessage());
	}
}
public static void schedule(String Str_BasedeDatos,String Str_Datapool,String Str_Servidor, String Str_Usuario ,String Str_Password){
	Variables.blnpendiente = false;
	try {
	java.net.InetAddress InfoSistema = java.net.InetAddress.getLocalHost();
	Variables.P_Nombre_Maquina = InfoSistema.getHostName();
	coneccion_db(Str_BasedeDatos, Str_Datapool, Str_Servidor, Str_Usuario, Str_Password);
	Variables.Rst_Escenarios_Pendientes = Variables.Cnn.prepareCall("SELECT * FROM TBL_SCHEDULE WHERE S_EJECUTAR = 'TRUE' AND S_EJECUTADO = 'FALSE' AND upper(S_MAQUINA)='" + Variables.P_Nombre_Maquina.trim().toUpperCase() + "' ORDER BY S_FECHA_EJECUTAR, S_HORA_EJECUTAR, S_INDICE").executeQuery();
	if (Variables.Rst_Escenarios_Pendientes.next()==true){
		if (DateCompare(new Date(),Variables.Rst_Escenarios_Pendientes.getString("S_FECHA_EJECUTAR") + " " + Variables.Rst_Escenarios_Pendientes.getString("S_HORA_EJECUTAR")) == false){
			Variables.blnpendiente = true;
			Variables.dtFechaInicio = getFechaActual() + " " + getHoraActual();
			Variables.Cnn.prepareCall("UPDATE TBL_SCHEDULE SET S_EJECUTADO = 'TRUE', S_FECHA_INICIO_EJECUTADO = '" + Variables.dtFechaInicio + "' WHERE S_INDICE = " + Variables.Rst_Escenarios_Pendientes.getString("S_INDICE")).execute();
			//FUNCION PARA RETORNAR CASOS SELECCIONADOS DEL DATADRIVEN [FALTA IMPLEMENTAR]
			LimpiarVariables();
			Variables.str_escenario = Variables.Rst_Escenarios_Pendientes.getString("S_ESCENARIO");
			Variables.S_Casos_Ejecutar = RetornarCasosEjecutarSchedule(Variables.Rst_Escenarios_Pendientes.getString("S_CASOS_EJECUTAR"), ",", "-");
			Variables.web=Variables.Rst_Escenarios_Pendientes.getString("S_BROWSER");
			Variables.S_Version = Variables.Rst_Escenarios_Pendientes.getString("S_VERSION");
			Variables.S_Corrida = Variables.Rst_Escenarios_Pendientes.getString("S_CORRIDA");
			Variables.S_Arquitecto_QA = Variables.Rst_Escenarios_Pendientes.getString("S_ARQUITECTO_QA");
			Variables.bln_Captura_Total = Variables.Rst_Escenarios_Pendientes.getBoolean("S_CAPTURA_COMPLETA");
			Variables.bln_Solo_Evidencia = Variables.Rst_Escenarios_Pendientes.getBoolean("S_SOLO_EVIDENCIA");
		}else{
			Variables.blnpendiente = false;
		}
	}else{
		Variables.blnpendiente = false;
	}
	}
	catch (Exception ex){
		Variables.blnpendiente = false;
		System.out.println("Error en Schedule: " + ex.getMessage());
	}
}
public static void LimpiarVariables () {
	Variables.str_escenario = "";
	Variables.S_Casos_Ejecutar = "";
	Variables.web = "";
	Variables.S_Version = "";
	Variables.S_Corrida = "";
	Variables.S_Arquitecto_QA = "";
	Variables.bln_Captura_Total = false;
	Variables.bln_Solo_Evidencia = false;
	Variables.bln_cerrar_browser = true;
}
public static void Inicializar_datadriven(){
	if (!Variables.S_Casos_Ejecutar.equals("")) 
    {
		Variables.casos_ejecutar = " AND ID_CASO IN (" + Variables.S_Casos_Ejecutar + ") ";
    }
}
public static void func_identificar_Script (String escenario) throws SQLException{
	try {
		Variables.Rst_Pasos_Read =  Variables.Cnn.prepareCall("SELECT * FROM " + escenario + "_P ORDER BY ID_PASO").executeQuery();
	}
	catch (Exception e){
		System.out.println("Error al cargar la tabla de pasos" + e.getMessage());
	}
}
public static void func_identificar_DataDriven (String escenario) throws SQLException{
	try {
		Variables.Rst_Datos_Read =  Variables.Cnn.prepareCall("SELECT * FROM " + escenario + "_D WHERE upper(ESTADO_CASO) = 'Activo'" + Variables.casos_ejecutar + " ORDER BY ID_CASO").executeQuery();
	}
	catch (Exception e){
		System.out.println("Error al cargar la tabla de casos" + e.getMessage());
	}
}
public static void func_identificar_Casos_DataDriven (){
	try {
		Variables.Rst_Datos =  Variables.Cnn.prepareCall("SELECT * FROM " + Variables.str_escenario + "_D WHERE upper(ESTADO_CASO) = 'Activo' ORDER BY ID_CASO").executeQuery();
	}
	catch (Exception e){
		System.out.println("Error al cargar la tabla de casos del datadriven para generar evidencia" + e.getMessage());
	}
}
public static void limpia_log_evidencia(String escenario, String caso){
	try {
		Variables.Cnn.prepareCall("DELETE FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + escenario + "' AND L_VERSION ='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_ID_CASO='" + caso + "'").execute();
	}catch (Exception e) {
	        System.out.println("Fallo la eliminaci�n en la tabla TBL_LOG_EVIDENCIA " + e.getMessage());
	}
}
public static String RetornarCasosEjecutarSchedule (String cadenaCasos, String separador_1, String separador_2){
	String[] strAuxDato ;
	String[] strAuxDatoSep2;
	int x, y;
	String tmpCasos = "";
	String datoResult = "";
	datoResult = cadenaCasos.trim();
	if (datoResult.length() > 0)
	{
		strAuxDato = cadenaCasos.split(separador_1);
		//Se recorre array principal ---> cadena separada por el separador 1
		for(x = 0 ; x < strAuxDato.length ; x++)
		{
			//S� se detecta el separador 2 se realiza generaci�n de los �ndices de acuerdo al rango que est� separado por el separador 2
			if (strAuxDato[x].indexOf(separador_2) != -1 )
			{
				strAuxDatoSep2 = strAuxDato[x].trim().split(separador_2);
				tmpCasos = "";
					if (Integer.parseInt(strAuxDatoSep2[0]) <= Integer.parseInt(strAuxDatoSep2[1]) )   //Se valida que el rango superior no sea menor que el rango inferior.
					//Se generan los �ndices puntuales que conforman el rango
					{
						for ( y = Integer.parseInt(strAuxDatoSep2[0]); y <= Integer.parseInt(strAuxDatoSep2[1]); y++)
						{
							tmpCasos = tmpCasos + "," + Integer.toString(y);
						}
                    	tmpCasos = tmpCasos.substring(1, tmpCasos.length());
					}	
                    else
                    {
                    		tmpCasos = "0";
                    }
					datoResult = tmpCasos;
			}
            else {
                    //Se valida que el caso separado no sea vac�o.
                    if ( strAuxDato[x].trim().length() == 0 ) 
                    	datoResult = "0";
             }
		}    	
	}
	return datoResult;
}
public static void func_actualizar_estado_caso (String escenario, String caso){
	try {
		Variables.dtFechaInicioCaso = getFechaActual() + " " + getHoraActual();
		Variables.Cnn.prepareStatement("UPDATE " + escenario + "_D SET ESTADO_EJECUCION='PROCESANDO', RESULTADO='', TIEMPO_EJECUCION='',FECHA_HORA_EJECUCION='" + Variables.dtFechaInicioCaso + "' WHERE upper(ESTADO_CASO) = 'Activo'  AND ID_CASO='" + caso + "'").executeUpdate();
	}
	catch (Exception e){
		System.out.println(e.getMessage());
	}
}
public static void trace_vector(String Escenario, String Casos_Ejecutados, String Id_Caso, String Resultado_Caso, String Version, String Corrida, String Usuario, String Maquina_Exec, String Ruta_Evd, String dtFechaInicio, String Tiempo_Ejecucion){
	try {
		Variables.Cnn.prepareCall("INSERT INTO TBL_TRACE (T_ESCENARIO,T_CASOS_EJECUTAR,T_ID_CASO,T_RESULTADO_CASO,T_VERSION,T_CORRIDA,T_USUARIO,T_MAQUINA_EXEC,T_RUTA_EVD,T_FECHA_HORA_EXEC,T_TIEMPO_EJEC) VALUES ('" + Escenario + "','" + Casos_Ejecutados + "','" + Id_Caso + "','" + Resultado_Caso + "','" + Version + "','" + Corrida + "','" + Usuario + "','" + Maquina_Exec + "','" + Ruta_Evd + "','" + dtFechaInicio + "','" + Tiempo_Ejecucion + "')").execute();
	} catch (Exception e) {
		System.out.println(e.getMessage());
	}
}
public static void orquestador(){
	try {
		Variables.Str_Tipo_Ejecucion = 1;
		Inicializar_datadriven();
		func_identificar_DataDriven(Variables.str_escenario);		  	
     if (!Variables.bln_Solo_Evidencia==true){
    	 while (Variables.Rst_Datos_Read.next()==true){
    		Variables.D_ID_CASO = Variables.Rst_Datos_Read.getString("ID_CASO");
	    	limpia_log_evidencia(Variables.str_escenario,Variables.D_ID_CASO);
	    	func_identificar_Script(Variables.str_escenario);
	    	System.out.println("Iniciando caso [" + Variables.D_ID_CASO + "]");
	    	Cabecera_Casos(Variables.D_ID_CASO);
	    	func_actualizar_estado_caso(Variables.str_escenario,Variables.D_ID_CASO);
	    	while (Variables.Rst_Pasos_Read.next()==true) { //INICIA LOS PASOS
	    		Variables.P_ID_PASO = Variables.Rst_Pasos_Read.getString("ID_PASO");
	    		/*
	    		if (Variables.P_ID_PASO.equals("46")) {
	    			System.out.println("paremos a debuguear");
	    		}
	    		*/
	    		System.out.println("Iniciando paso [" + Variables.P_ID_PASO + "]");	
	    		Variables.P_Tipo_Paso="";
	    		Variables.P_Paso="";
	    		Variables.P_Object="";
	    		Variables.P_Parametro_Adicional="";
	    		Variables.P_Automatico = "";
	    		Variables.P_Tipo_Paso = Variables.Rst_Pasos_Read.getString("TIPO_PASO");
		    	Variables.P_Paso = Variables.Rst_Pasos_Read.getString("PASO");
		    	Variables.P_Object = Variables.Rst_Pasos_Read.getString("OBJECT");
		    	Variables.P_Parametro_Adicional = Variables.Rst_Pasos_Read.getString("PARAMETRO_ADICIONAL");
		    	Variables.P_Automatico = Variables.Rst_Pasos_Read.getString("AUTOMATICO");
		    	if (Variables.P_Automatico.equals("1") == true){ //Verificar si existe un paso como 1 para no cerrar el browser
		    		Variables.bln_cerrar_browser = false; //no cerrar browser
		    	}
		    	if (Integer.parseInt(Variables.P_Automatico) >= Variables.Str_Tipo_Ejecucion){
			    	if (!Variables.P_Object.equals("") == true){
			    		if (!Variables.P_Object.startsWith("dto_") == true){ 
			    				func_object_sql(Variables.str_escenario,Variables.P_Object);
					    		Variables.O_Object_Name="";
					    		Variables.O_Object_Ident="";
					    		Variables.O_Tipo_Object_Ident="";
					    		if (Variables.Rst_Objetos.next() == true){
					    			Variables.O_Object_Name = Variables.Rst_Objetos.getString("O_OBJECT_NAME");
					    			Variables.O_Object_Ident = Variables.Rst_Objetos.getString("O_OBJECT_IDENT");
					    			Variables.O_Tipo_Object_Ident = Variables.Rst_Objetos.getString("O_TIPO_OBJECT_IDENT");
					    		}
			    		}
			    	}
		    		if (!Variables.P_Object.equals("") == true){
		    			if (!Variables.P_Object.startsWith("dto_") == true){
		    				if (!Variables.P_Object.startsWith("dt_") == true) {
		    					if(!Variables.P_Object.startsWith("EXTERNO") == true){
		    						if(!Variables.P_Object.startsWith("M_") == true){//REVISAR PARA OPTIMIZAR
			    						func_datadriven_sql(Variables.str_escenario, Variables.P_Object, Variables.D_ID_CASO);   		
			    						Variables.D_Dato="";
			    						if (Variables.Rst_Datos.next() == true){
			    							Variables.D_Dato = Variables.Rst_Datos.getString(Variables.P_Object);
			    						}
		    						}
		    					}
		    				}
		    			}
		    		}
		    		Cabecera_Pasos(Variables.P_ID_PASO);
		    		System.out.println("Ingresando al Keyword");
		    		keyword_Web(Variables.P_Tipo_Paso, Variables.P_Object, Variables.O_Object_Ident, Variables.P_Paso, Variables.O_Tipo_Object_Ident ,Variables.P_Parametro_Adicional,Variables.D_Dato, Variables.file, Variables.str_escenario,Variables.S_Version, Variables.S_Corrida, Variables.D_ID_CASO, Variables.P_ID_PASO, Variables.web, Variables.ruta);	    		
		    		if (Variables.bln_Captura_Total==true){
		    			captura_evidencia(Variables.file, Variables.str_escenario,Variables.S_Version, Variables.S_Corrida, Variables.D_ID_CASO, Variables.P_ID_PASO);
		    		}
			    	System.out.println("Saliendo del Keyword");
			    	System.out.println("VERIFICANDO SI EL PASO ES CORRECTO?");
		    		if (Variables.valida_caso == false) {
		    			System.out.println("El paso es INCORRECTO");
		    			break;
		    		}else{
		    			System.out.println("El paso es CORRECTO");
		    		}   				
		    		System.out.println("Finalizando paso [" + Variables.P_ID_PASO + "]");
		    	}
	    	}
	    Variables.Str_Tipo_Ejecucion = 2;
	    System.out.println("Finalizando caso [" + Variables.D_ID_CASO + "]");
	    Func_Valida_Caso(Variables.str_escenario, Variables.D_ID_CASO , Variables.P_ID_PASO);
	    System.out.println("Valida Caso:" + Variables.valida_caso);
	    generar_evidencia_caso(Variables.file, Variables.str_escenario, Variables.D_ID_CASO);
	    Variables.Rst_Pasos_Read.close(); //cerramos el recorset de pasos
	    if (Variables.bln_cerrar_browser ==true){
	    	kill_procesos(Variables.web); // se adecua como paso del caso
	    }
	    }
  	   } //FIN DEPURAR EVD
     	kill_procesos(Variables.web);
	    generar_reporte_evidencia(Variables.file, Variables.str_escenario);
	}
	catch (Exception e) {	
		System.out.println(e.getMessage());	
	}
}
private static void func_guardar_contenido(String Origen, String Destino, String caso){
	// INPUT:
	// OBJECT = ORIGEN
	// PARAMETRO_ADICIONAL = DESTINO
	// 1 EJEMPLO: ACCION GUARDAR_CONTENIDO dto_NUMERO_COTIZACION    dt_NUMERO_COTIZACION
	// 2 EJEMPLO: ACCION GUARDAR_CONTENIDO dt_NUMERO_COTIZACION     dt_NUMERO_COTIZACION_FINAL
	// 3 EJEMPLO: EXTERNO:ESCENARIO:dt_NUMERO_COTIZACION_ORIGEN		dt_NUMERO_COTIZACION_DESTINO
	try{
		WebElement StrWebElement;
		String AuxDatoDestino="";
		String AuxDatoOrigen="";
		String strSQLaux="";
		ResultSet Rst_ColumnaInicial;
		ResultSet Rst_ColumnaFinal;
		String strColumnaInicial= "";
		String strColumnaFinal= "";
		String dataColumnaInicial = "";
		String dataColumnaDestino = "";
		String strTablaOrigen = "";
		String strTablaDestino = "";
		String StrTipo_Guardar_Contenido="";
		if (Origen.toLowerCase().indexOf("externo") != -1){
			StrTipo_Guardar_Contenido="Externo_Mover_Dato";
		}
		if (Origen.toLowerCase().startsWith("dt_") == true){
			StrTipo_Guardar_Contenido="Mover_Dato";
		}
		if (Origen.toLowerCase().startsWith("dto_") == true){
			StrTipo_Guardar_Contenido="Capturar_Dato";
		}
		switch (StrTipo_Guardar_Contenido){
			case "Externo_Mover_Dato":
				strTablaOrigen = Origen.split(":")[1];
				strTablaDestino = Variables.str_escenario;
				if (Origen.toLowerCase().split(":")[2].startsWith("dt_")==true){
					strColumnaInicial = Origen.split(":")[2].split("dt_")[1];
					strSQLaux = "SELECT " + strColumnaInicial + " FROM " + strTablaOrigen + "_D WHERE ID_CASO='" + caso + "'";
					Rst_ColumnaInicial = Variables.Cnn.prepareCall(strSQLaux).executeQuery();
					if (Rst_ColumnaInicial.next()==true){
						AuxDatoOrigen = Rst_ColumnaInicial.getString(strColumnaInicial).toString();
					}
					if (Destino.toLowerCase().startsWith("dt_")==true){
						strColumnaFinal = Destino.split("dt_")[1];
						strSQLaux = "UPDATE " + strTablaDestino + "_D SET " + strColumnaFinal + " = '" + AuxDatoOrigen + "' WHERE ID_CASO='" + caso + "'";
						Variables.Cnn.prepareCall(strSQLaux).execute();
					}
				}
				break;
			case "Mover_Dato":
				strTablaDestino = Variables.str_escenario;
				strColumnaInicial = Origen.split("dt_")[1];
				strColumnaFinal = Destino.split("dt_")[1];
				strSQLaux = "SELECT " + strColumnaInicial + " FROM " + strTablaDestino + "_D WHERE ID_CASO='" + caso + "'";
				Rst_ColumnaInicial = Variables.Cnn.prepareCall(strSQLaux).executeQuery();
				if (Rst_ColumnaInicial.next()==true){
					AuxDatoOrigen=Rst_ColumnaInicial.getString(strColumnaInicial).toString();
				}
				if (Destino.toLowerCase().startsWith("dt_")==true)	{
						strSQLaux = "UPDATE " + strTablaDestino + "_D SET " + strColumnaFinal + " = '" + AuxDatoOrigen + "' WHERE ID_CASO='" + caso + "'";
						Variables.Cnn.prepareCall(strSQLaux).execute();
				}
				break;
			case "Capturar_Dato":
				strTablaDestino = Variables.str_escenario;
				strColumnaInicial = Origen.split("dto_")[1];
				strColumnaFinal = Destino.split("dt_")[1];
				func_object_sql(strTablaDestino,strColumnaInicial);
				Variables.O_Object_Name="";
				Variables.O_Object_Ident="";
				Variables.O_Tipo_Object_Ident="";
	    		if (Variables.Rst_Objetos.next() == true){
	    			Variables.O_Object_Name = Variables.Rst_Objetos.getString("O_OBJECT_NAME");
	    			Variables.O_Object_Ident = Variables.Rst_Objetos.getString("O_OBJECT_IDENT");
	    			Variables.O_Tipo_Object_Ident = Variables.Rst_Objetos.getString("O_TIPO_OBJECT_IDENT");
	    		}
				StrWebElement = Variables.driver.findElement(obtenerObjeto(Variables.O_Object_Ident, Variables.O_Tipo_Object_Ident));
				highlightElement(StrWebElement);
				if(StrWebElement.getAttribute("readonly") != null && StrWebElement.getAttribute("readonly").equalsIgnoreCase("readonly")){
					setAttribute("arguments[0].removeAttribute(arguments[1])",StrWebElement, "ReadOnly", "");
					AuxDatoOrigen = StrWebElement.getAttribute("value");
					setAttribute("arguments[0].setAttribute(arguments[1], arguments[2])",StrWebElement ,"ReadOnly", "readonly");
				}else if (StrWebElement.isEnabled()==false){
					AuxDatoOrigen = StrWebElement.getAttribute("value");
				}else{
					AuxDatoOrigen = StrWebElement.getText();
					AuxDatoOrigen = StrWebElement.getAttribute("value");
				}
				if (Destino.toLowerCase().startsWith("dt_")==true){		
					strSQLaux = "UPDATE " + strTablaDestino + "_D SET " + strColumnaFinal + " = '" + AuxDatoOrigen + "' WHERE ID_CASO='" + caso + "'";
					Variables.Cnn.prepareCall(strSQLaux).execute();
				}
				break;
			default:
				break;
		}
	}
	catch (Exception e){
		System.out.println(e.getMessage());	
	}
}
static void setAttribute(String Accion,WebElement element, String attributeName, String value) {
    	JavascriptExecutor js = (JavascriptExecutor) Variables.driver;
    	//Para hacer un Set a un atributo
        //js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2])", element, attributeName, value);
    	//Para eliminar un atributo x completo
    	if (value =="")
    		js.executeScript(Accion, element, attributeName);
    	else
    		js.executeScript(Accion, element, attributeName, value);
 }
private static boolean DateCompare(Date date, String string) throws ParseException {
	Date Date_Now = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    Date Date_Ejecucion = date;
    Date_Now = (Date)formatter.parse(string);
    return Date_Now.after(Date_Ejecucion);
}

public static void func_object_sql (String escenario, String objeto) throws SQLException{
	try {
	Variables.Rst_Objetos =  Variables.Cnn.prepareCall("SELECT * FROM TBL_OBJETOS WHERE O_ESCENARIO = '" + escenario + "' AND O_OBJECT_NAME = '" + objeto + "'").executeQuery();
	}
	catch (Exception e){
		System.out.println(e.getMessage());
	}
}
public static void func_datadriven_sql (String escenario, String objeto, String caso) throws SQLException{
	try{
	Variables.Rst_Datos =  Variables.Cnn.prepareCall("SELECT " + objeto + " FROM " + escenario + "_D" + " WHERE ID_CASO='" + caso + "' AND upper(ESTADO_CASO) = 'Activo'").executeQuery();
	}
	catch (Exception e){
		System.out.println(e.getMessage());
	}
}
public static void keyword_Web (String str_tipo_paso, String str_object, String mapeo_objeto, String str_paso, String mapeo_tipo,String adicional,  String valor, String file, String escenario, String version, String corrida, String i, String j,String web, String ruta) {
	try {
	Thread.sleep(1000);
	switch (str_tipo_paso.toUpperCase()) {
	case "ACCION":
			func_accion(str_object, mapeo_objeto, str_paso, mapeo_tipo, valor, adicional);
			break;
	case "REVISION":
			func_revision(str_object,mapeo_objeto,str_paso, mapeo_tipo, valor, adicional);
			break;
	case "VECTOR":
			func_funciones_vector(str_object,mapeo_objeto,str_paso, mapeo_tipo, valor, adicional);
			break;
	case "GMG":
			func_GMG(str_object,mapeo_objeto,str_paso, mapeo_tipo, valor, adicional);
		break;
	case "BSP":
			func_BSP(str_object,mapeo_objeto,str_paso, mapeo_tipo, valor, adicional);
			break;
	default:
			break;
	}
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
		return;	
	}
}
public static void func_GMG(String Objeto,String mapeo_objeto,String tipo_accion, String mapeo_tipo, String valor, String adicional){
	try{
		Variables.valida_caso=false;
		switch (tipo_accion.toUpperCase()) {
			case "COMBOBOX_GMG":
				highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));
				Variables.driver.findElement(obtenerObjeto(mapeo_objeto + "/*[contains(text(),'" + valor + "')]" ,mapeo_tipo)).click();
				Variables.valida_caso=true;
				Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
				break;
			case "CHECK_GMG":
				break;
			case "CLOSE_POPUP_GMG":
				break;
			default:
				break;
		}	
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
	}
}
public static void func_teclado_proveedor (CharSequence TeclasData) throws InterruptedException{
	int length = TeclasData.length();
    for (int i = 0; i < length; i++) {
            char character = TeclasData.charAt(i);
            func_teclas_proveedor(character);
    }
}
public static void func_teclado_usuario (CharSequence TeclasData) throws InterruptedException{
	int length = TeclasData.length();
    for (int i = 0; i < length; i++) {
            char character = TeclasData.charAt(i);
            func_teclas_usuario(character);
    }
}
public static void func_teclas_proveedor (char caracter) throws InterruptedException{
	switch (caracter) {
	case '1':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='1']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='1']")).click();
		break;
	case '2':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='2']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='2']")).click();
		break;
	case '3':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='3']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='3']")).click();
		break;
	case '4':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='4']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='4']")).click();
		break;
	case '5':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='5']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='5']")).click();
		break;
	case '6':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='6']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='6']")).click();
		break;
	case '7':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='7']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='7']")).click();
		break;
	case '8':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='8']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='8']")).click();
		break;
	case '9':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='9']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='9']")).click();
		break;
	case '0':
		highlightElement(Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='0']")));
		Variables.driver.findElement(By.xpath("//BUTTON[@type='button'][text()='0']")).click();
		break;
	}
}
public static void func_teclas_usuario (char caracter) throws InterruptedException{
	switch (caracter) {
	case '1':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[2]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[2]/span")).click();
		break;
	case '2':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[3]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[3]/span")).click();
		break;
	case '3':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[4]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[4]/span")).click();
		break;
	case '4':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[5]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[5]/span")).click();
		break;
	case '5':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[6]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[6]/span")).click();
		break;
	case '6':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[7]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[7]/span")).click();
		break;
	case '7':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[8]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[8]/span")).click();
		break;
	case '8':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[9]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[9]/span")).click();
		break;
	case '9':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[10]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[10]/span")).click();
		break;
	case '0':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[11]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[11]/span")).click();
		break;
	case 'q':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[15]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[15]/span")).click();
		break;
	case 'w':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[16]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[16]/span")).click();
		break;
	case 'e':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[17]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[17]/span")).click();
		break;
	case 'r':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[18]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[18]/span")).click();
		break;
	case 't':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[19]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[19]/span")).click();
		break;
	case 'y':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[20]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[20]/span")).click();
		break;
	case 'u':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[21]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[21]/span")).click();
		break;
	case 'i':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[22]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[22]/span")).click();
		break;
	case 'o':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[23]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[23]/span")).click();
		break;
	case 'p':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[24]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[24]/span")).click();
		break;
	case 'a':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[28]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[28]/span")).click();
		break;
	case 's':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[29]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[29]/span")).click();
		break;
	case 'd':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[30]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[30]/span")).click();
		break;
	case 'f':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[31]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[31]/span")).click();
		break;
	case 'g':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[32]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[32]/span")).click();
		break;
	case 'h':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[33]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[33]/span")).click();
		break;
	case 'j':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[34]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[34]/span")).click();
		break;
	case 'k':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[35]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[35]/span")).click();
		break;
	case 'l':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[36]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[36]/span")).click();
		break;
	case 'z':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[40]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[40]/span")).click();
		break;
	case 'x':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[41]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[41]/span")).click();
		break;
	case 'c':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[42]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[42]/span")).click();
		break;
	case 'v':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[43]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[43]/span")).click();
		break;
	case 'b':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[44]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[44]/span")).click();
		break;
	case 'n':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[45]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[45]/span")).click();
		break;
	case 'm':
		highlightElement(Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[46]/span")));
		Variables.driver.findElement(By.xpath("//*[@id='campo_password_keyboard']/div[1]/button[46]/span")).click();
		break;
	}
}
public static void func_BSP(String Objeto,String mapeo_objeto,String tipo_accion, String mapeo_tipo, String valor, String adicional){
	try{	
		Variables.valida_caso=false;
		switch (tipo_accion.toUpperCase()) {
			case "TECLADO_PROVEEDOR":
				func_teclado_proveedor(valor);
				Variables.valida_caso=true;
				Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
				break;
			case "TECLADO_USUARIO":
				highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));
				Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).click();
				func_teclado_usuario(valor);
				Variables.valida_caso=true;
				Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
				break;
			case "IMPORTAR_LOTE":
				Robot robot = new java.awt.Robot();
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_TAB);
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_TAB);
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_TAB);
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_RIGHT);
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_TAB);
				Thread.sleep(1000);
				//robot.keyPress(KeyEvent.VK_ENTER);
				Mouse_Click(351,381);
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_ENTER);
				type(valor);//F:\Lotes\CTS\Pago_CTS_06122017.txt
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_TAB);
				Thread.sleep(1000);
				robot.keyPress(KeyEvent.VK_ENTER);
				Thread.sleep(2000);
				Variables.valida_caso=true;
			case "CAPTCHA":
				coneccion_db("Oracle", "", "180.181.106.24:1667/ebusines", "ADMEBU", "prueba99");
				String indice = Variables.driver.findElement(By.xpath("//*[@id='idCaptcha']")).getAttribute("value");
				String cap_valor = "";
				Variables.Rst_Indice = Variables.CnnOracle.prepareCall("SELECT * FROM CAPTCHA WHERE CAP_CODIGO='" + indice + "'").executeQuery();
				if (Variables.Rst_Indice.next()==true){
					cap_valor = Variables.Rst_Indice.getString("CAP_VALOR");
				}
				highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));
				Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).click();
				Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).sendKeys(cap_valor);
				Variables.valida_caso=true;
				Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
				break;
			default:
				break;
		}	
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
		Variables.cadena_html = Variables.cadena_html + "<font color='#FF0000'>ERROR EN EL PASO: " + "FUNCIONES VECTOR: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "| MENSAJE ERROR: " + e.getMessage() + "</font><br>";
		Variables.valida_caso = false;
	}
}
public static void func_espera_cargar (int parar) 
{
	try {
		Thread.sleep(parar*1000);
		} catch(InterruptedException e) {
		}
	System.out.println("Esperamos: " + parar +" .seg");
}
public static void func_funciones_vector(String Objeto,String mapeo_objeto,String tipo_accion, String mapeo_tipo, String valor, String adicional){
	try{
		Variables.blnpendiente=false;
		switch (tipo_accion.toUpperCase()) {
		case "OPEN_WINDOWS":
			func_open_windows(Variables.web, Variables.ruta, valor);
			Variables.cadena_html = Variables.cadena_html + "en la ruta: " + valor + "<br>";
			break;
		case "TOMAR_FOTO":
			captura_evidencia(Variables.file,Variables.str_escenario,Variables.S_Version,Variables.S_Corrida,Variables.D_ID_CASO,Variables.P_ID_PASO);
			Variables.cadena_html = Variables.cadena_html + tipo_accion + " en la pantalla<br>";
			break;
		case "ESPERA_CARGAR":
			func_espera_cargar(Integer.parseInt(adicional));
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Esperando ... [" + Integer.parseInt(adicional) + " seg.]" + "|<br>";
			break;
		case "ESPERA_OBJETO":
			func_espera_elemento(mapeo_tipo, mapeo_objeto, adicional, Variables.driver);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Esperando el objeto [" + Objeto + "] con tipo de mapeo [" + mapeo_tipo + "] e identificador [" + mapeo_objeto + "]|<br>";
			break;
		case "ESPERA_TEXTO":
			func_espera_texto(mapeo_objeto, adicional, Variables.driver);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Esperando el texto [" + Objeto + "] con tipo de mapeo [" + mapeo_tipo + "] e identificador [" + mapeo_objeto + "]|<br>";
			break;
		case "VISUALIZA_TEXTO":
			func_visualizar_texto(mapeo_objeto, adicional, Variables.driver);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se visualiza el texto [" + Objeto + "] con tipo de mapeo [" + mapeo_tipo + "] e identificador [" + mapeo_objeto + "]|<br>";
			break;
		case "ESPERAR_HABILITADO":
			func_esperar_habilitado(mapeo_objeto,adicional,Variables.driver);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se espera el objeto [" + Objeto + "] con tipo de mapeo [" + mapeo_tipo + "] e identificador [" + mapeo_objeto + "]|<br>";
			break;
		case "TECLA":
			WebElement ElementoTecla = Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo));
			func_tecla_windows(ElementoTecla,adicional);
			break;
		case "GUARDAR_CONTENIDO":
			func_guardar_contenido(Objeto,adicional,Variables.D_ID_CASO);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se guarda el contenido del Objeto [" + Objeto + "] en el destino [" + adicional + "]|<br>";
			break;
		case "CERRAR_WINDOWS":
			kill_procesos(Variables.web);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se cierra el aplicativo del Browser " + Variables.web + "|<br>";
			break;
		case "MOUSE_CLICK":
			Mouse_Click(Integer.parseInt(Variables.P_Parametro_Adicional.split(",")[0]), Integer.parseInt(Variables.P_Parametro_Adicional.split(",")[1]));
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se da click en las coordenadas: [ " + Variables.P_Parametro_Adicional.split(",")[0] + ", " + Variables.P_Parametro_Adicional.split(",")[1] + "]" + "|<br>";
			break;
		case "MOUSE_COORDENADA":
			Mouse_Move(Integer.parseInt(Variables.P_Parametro_Adicional.split(",")[0]), Integer.parseInt(Variables.P_Parametro_Adicional.split(",")[1]));;
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se da mueve el puntero del mouse en las coordenadas: [ " + Variables.P_Parametro_Adicional.split(",")[0] + ", " + Variables.P_Parametro_Adicional.split(",")[1] + "]" + "|<br>";
			break;
		case "PRESS_KEY":
			type(valor);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se presiona las teclas [ " + valor + "]|<br>";
			break;
		case "PRESS_KEY_TECLA":
			typeTecla(valor);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se presiona las teclas [ " + valor + "]|<br>";
			break;
		case "PRESS_ARRIBA":
			TeclaArriba();
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se presiona la tecla Arriba |<br>";
			break;
		case "PRESS_ABAJO":
			TeclaAbajo();
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se presiona la tecla Abajo |<br>";
			break;
		case "PRESS_DERECHA":
			TeclaDerecha();
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se presiona la tecla Derecha |<br>";
			break;
		case "PRESS_IZQUIERDA":
			TeclaIzquierda();
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + "Se presiona la tecla Izquierda |<br>";
			break;
		}
	}
	catch (Exception e){
		System.out.println(e.getMessage());
		Variables.cadena_html = Variables.cadena_html + "<font color='#FF0000'>ERROR EN EL PASO: " + "FUNCIONES VECTOR: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "| MENSAJE ERROR: " + e.getMessage() + "</font><br>";
		Variables.valida_caso = false;
	}
}
private static void func_tecla_windows(WebElement element,String Tecla) throws AWTException, InterruptedException {
	switch (Tecla.toUpperCase()){
		case "[ENTER]":
			element.sendKeys(Keys.ENTER);
			Thread.sleep(1000);
			Variables.cadena_html = Variables.cadena_html + "TECLA " + Tecla.toUpperCase() + "|<br>";
			break;
		case "[TAB]":
			element.sendKeys(Keys.TAB);
			Thread.sleep(1000);
			Variables.cadena_html = Variables.cadena_html + "TECLA " + Tecla.toUpperCase() + "|<br>";
			break;
		default:
			break;
	}
}
public static void func_revision (String Objeto, String mapeo_objeto, String tipo_accion, String mapeo_tipo,  String valor, String adicional) {
	try {
		Variables.valida_caso = false;
	switch (tipo_accion.toUpperCase()) {
		case "HABILITADO":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO		
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).isEnabled();
			Variables.cadena_html = Variables.cadena_html + "REVISI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso = true;
			break;
		case "CONTIENE_TEXTO":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO		
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).getText().trim().contentEquals(valor);
			Variables.cadena_html = Variables.cadena_html + "REVISI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso = true;
			break;
		case "TEXTO":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO		
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).getText().trim().contains(valor);
			Variables.cadena_html = Variables.cadena_html + "REVISI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso = true;
			break;
		case "VISIBLE_OBJETO":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO		
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).isDisplayed();
			Variables.cadena_html = Variables.cadena_html + "REVISI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso = true;
			break;
		default:
			break;
		}
	}
	catch (Exception e) {
		System.out.println(e.getMessage());		
		Variables.cadena_html = Variables.cadena_html + "<font color='#FF0000'>ERROR EN EL PASO: " + "REVISI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "| MENSAJE ERROR: " + e.getMessage() + "</font><br>";
		Variables.valida_caso = false;	
	}
}
public static void func_accion (String Objeto, String mapeo_objeto, String tipo_accion, String mapeo_tipo,  String valor, String adicional) {
	try {
		Variables.valida_caso=false;
	switch (tipo_accion.toUpperCase()) {
	case "CLICK":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO		
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).click();
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "ESCRIBIR":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			//Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).clear();
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).sendKeys(valor);
			if ((adicional!=null)){
				if (adicional.equals("[ENTER]")){
					Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).sendKeys(Keys.ENTER);
				}
				if (adicional.equals("[TAB]")){
					Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).sendKeys(Keys.TAB);
				}
			}
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "DOBLECLICK":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			WebElement ee = Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo));
		    Actions action = new Actions(Variables.driver);
		    action.doubleClick(ee).perform();
		    Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
		    Variables.valida_caso=true;
		    break;
	case "LIMPIAR":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).clear();
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "SELECCIONAR_TEXT":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			//Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).click();	
			Select droplist = new Select(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));   
			droplist.selectByVisibleText(valor);
			//dato = true;
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "SELECCIONAR_VALOR":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			//Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).click();	
			Select droplist_v = new Select(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));   
			droplist_v.selectByValue(valor);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " el valor '" + valor  + "' en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "SELECCIONAR_OPCION":
			//Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)).click();
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			Thread.sleep(2000);
			WebElement mySelectElm = Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo));
			Select droplist1 = new Select(mySelectElm);	
			Integer x = Integer.valueOf(valor);
			droplist1.selectByIndex(x);
			//dato = true;
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " en la posici�n [" + valor  + "] en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "SELECCIONAR_FRAME":
			highlightElement(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));//SE RESALTA EL OBJETO
			Variables.driver.switchTo().frame(Variables.driver.findElement(obtenerObjeto(mapeo_objeto,mapeo_tipo)));
			Thread.sleep(2000);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	case "SELECCIONAR_WINDOW": //pendiente de estabilizar
            //Variables.driver.switchTo().window(mapeo_objeto);
			 for (String handle : Variables.driver.getWindowHandles()) {			 
			    Variables.driver.switchTo().window(handle);}
			//Variables.driver.manage().timeouts().wait(15);
			//Variables.driver.manage().timeouts().pageLoadTimeout(Integer.parseInt(Objeto), TimeUnit.SECONDS);
			Variables.cadena_html = Variables.cadena_html + "ACCI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "|<br>";
			Variables.valida_caso=true;
			break;
	default:
			break;
	}
	}
	catch (Exception e) {
		System.out.println(e.getMessage());		
		Variables.cadena_html = Variables.cadena_html + "<font color='#FF0000'>ERROR EN EL PASO: " + "ACCI�N: " + tipo_accion + " en el objeto " + Objeto + " con tipo de atributo |" + mapeo_tipo + "| y su valor |" + mapeo_objeto + "| MENSAJE ERROR: " + e.getMessage() + "</font><br>";
		Variables.valida_caso = false;	
	}
}
public static void func_esperar_habilitado (String mapeo_objeto, String tiempo, WebDriver driver) throws InterruptedException{
	int cont=0;
	while (driver.findElement(By.xpath(mapeo_objeto)).isEnabled()==false){
		Thread.sleep(1000);
		cont++;
		System.out.println("Esperando: " + cont);
		if (cont>Integer.parseInt(tiempo)){
			System.out.println("Objeto NO visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg");
			Variables.cadena_html = Variables.cadena_html + "Objeto NO visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg" + "|<br>";
			Variables.valida_caso=false;
			break;
		}
	}
	System.out.println("Objeto visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , por un per�odo de: " + cont + " .seg");
	Variables.cadena_html = Variables.cadena_html + "Objeto visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + cont + " .seg" + "|<br>";
	Variables.valida_caso=true;
	Thread.sleep(500);
}
public static void Mouse_Click(int x, int y) throws AWTException, InterruptedException
{
	Robot MouseRobot = new java.awt.Robot();
	MouseRobot.mouseMove(x,y);
	MouseRobot.mousePress(InputEvent.BUTTON1_MASK);
	MouseRobot.mouseRelease(InputEvent.BUTTON1_MASK);
	Point punto=MouseInfo.getPointerInfo().getLocation();
	int x1=punto.x;
	int y1=punto.y;
	System.out.println("Posicion X: " + x1 + " - Posicion Y: " + y1);
	Thread.sleep(1000);
	Variables.valida_caso=true;
}
private static void doType(int[] keyCodes, int offset, int length) 
{
    if (length == 0) {
            return;
    }
    try
    {
    Robot robot = new java.awt.Robot();
    robot.keyPress(keyCodes[offset]);
    doType(keyCodes, offset + 1, length - 1);
    robot.keyRelease(keyCodes[offset]);
    }catch(Exception ex)
    {}        	
}
public static void type(CharSequence characters) throws InterruptedException {
    int length = characters.length();
    for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            type(character);
    }
    Variables.valida_caso=true;
}
public static void typeTecla(String cadena) throws InterruptedException, AWTException {
	Robot robot = new java.awt.Robot();
	switch (cadena) {
	case "[TAB]": robot.keyPress(KeyEvent.VK_TAB); robot.keyRelease(KeyEvent.VK_TAB);break;
	case "[ENTER]": robot.keyPress(KeyEvent.VK_ENTER);robot.keyRelease(KeyEvent.VK_ENTER); break;
	case "[SPACE]": robot.keyPress(KeyEvent.VK_SPACE);robot.keyRelease(KeyEvent.VK_SPACE); break;
	}
    Variables.valida_caso=true;
}
private static void doType(int... keyCodes) {
    doType(keyCodes, 0, keyCodes.length);
}
public static void type (char caracter){
	switch (caracter) {
    case 'a': doType(KeyEvent.VK_A); break;
    case 'b': doType(KeyEvent.VK_B); break;
    case 'c': doType(KeyEvent.VK_C); break;
    case 'd': doType(KeyEvent.VK_D); break;
    case 'e': doType(KeyEvent.VK_E); break;
    case 'f': doType(KeyEvent.VK_F); break;
    case 'g': doType(KeyEvent.VK_G); break;
    case 'h': doType(KeyEvent.VK_H); break;
    case 'i': doType(KeyEvent.VK_I); break;
    case 'j': doType(KeyEvent.VK_J); break;
    case 'k': doType(KeyEvent.VK_K); break;
    case 'l': doType(KeyEvent.VK_L); break;
    case 'm': doType(KeyEvent.VK_M); break;
    case 'n': doType(KeyEvent.VK_N); break;
    case 'o': doType(KeyEvent.VK_O); break;
    case 'p': doType(KeyEvent.VK_P); break;
    case 'q': doType(KeyEvent.VK_Q); break;
    case 'r': doType(KeyEvent.VK_R); break;
    case 's': doType(KeyEvent.VK_S); break;
    case 't': doType(KeyEvent.VK_T); break;
    case 'u': doType(KeyEvent.VK_U); break;
    case 'v': doType(KeyEvent.VK_V); break;
    case 'w': doType(KeyEvent.VK_W); break;
    case 'x': doType(KeyEvent.VK_X); break;
    case 'y': doType(KeyEvent.VK_Y); break;
    case 'z': doType(KeyEvent.VK_Z); break;
    case 'A': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
    case 'B': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
    case 'C': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
    case 'D': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
    case 'E': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
    case 'F': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
    case 'G': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
    case 'H': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
    case 'I': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
    case 'J': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
    case 'K': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
    case 'L': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
    case 'M': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
    case 'N': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
    case 'O': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
    case 'P': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
    case 'Q': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
    case 'R': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
    case 'S': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
    case 'T': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
    case 'U': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
    case 'V': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
    case 'W': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
    case 'X': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
    case 'Y': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
    case 'Z': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
    case '`': doType(KeyEvent.VK_BACK_QUOTE); break;
    case '0': doType(KeyEvent.VK_0); break;
    case '1': doType(KeyEvent.VK_1); break;
    case '2': doType(KeyEvent.VK_2); break;
    case '3': doType(KeyEvent.VK_3); break;
    case '4': doType(KeyEvent.VK_4); break;
    case '5': doType(KeyEvent.VK_5); break;
    case '6': doType(KeyEvent.VK_6); break;
    case '7': doType(KeyEvent.VK_7); break;
    case '8': doType(KeyEvent.VK_8); break;
    case '9': doType(KeyEvent.VK_9); break;
    case '-': doType(KeyEvent.VK_MINUS); break;
    case '=': doType(KeyEvent.VK_EQUALS); break;
    case '~': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
    case '!': doType(KeyEvent.VK_EXCLAMATION_MARK); break;
    case '@': doType(KeyEvent.VK_AT); break;
    case '#': doType(KeyEvent.VK_NUMBER_SIGN); break;
    case '$': doType(KeyEvent.VK_DOLLAR); break;
    case '%': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
    case '^': doType(KeyEvent.VK_CIRCUMFLEX); break;
    case '&': doType(KeyEvent.VK_AMPERSAND); break;
    case '*': doType(KeyEvent.VK_ASTERISK); break;
    case '(': doType(KeyEvent.VK_LEFT_PARENTHESIS); break;
    case ')': doType(KeyEvent.VK_RIGHT_PARENTHESIS); break;
    case '_': doType(KeyEvent.VK_UNDERSCORE); break;
    case '+': doType(KeyEvent.VK_PLUS); break;
    case '\t': doType(KeyEvent.VK_TAB); break;
    case '\n': doType(KeyEvent.VK_ENTER); break;
    case '[': doType(KeyEvent.VK_OPEN_BRACKET); break;
    case ']': doType(KeyEvent.VK_CLOSE_BRACKET); break;
    case '\'': doType(KeyEvent.VK_QUOTE); break;
    //case '\\': doType(KeyEvent.VK_BACK_SLASH); break;
    case '\\': doType(KeyEvent.VK_ALT, KeyEvent.VK_NUMPAD9, KeyEvent.VK_NUMPAD2); break;
    case '{': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
    case '}': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
    case '|': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
    case ';': doType(KeyEvent.VK_SEMICOLON); break;
    //case ':': doType(KeyEvent.VK_COLON); break;
    case ':': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON); break;
    case '"': doType(KeyEvent.VK_QUOTEDBL); break;
    case ',': doType(KeyEvent.VK_COMMA); break;
    case '<': doType(KeyEvent.VK_LESS); break;
    case '.': doType(KeyEvent.VK_PERIOD); break;
    case '>': doType(KeyEvent.VK_GREATER); break;
    //case '/': doType(KeyEvent.VK_SLASH); break;
    case '/': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_7); break;
    case '?': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
    case ' ': doType(KeyEvent.VK_SPACE); break;
    default:
            throw new IllegalArgumentException("Cannot type character " + caracter);
    }
}
public static void Mouse_Move(int x, int y) throws AWTException
{
	Robot MouseRobot = new java.awt.Robot();
	MouseRobot.mouseMove(x,y);
	Variables.valida_caso=true;
}
public static void TeclaAbajo(){
	doType(KeyEvent.VK_DOWN);
	Variables.valida_caso=true;
}
public static void TeclaArriba(){
	doType(KeyEvent.VK_UP);
	Variables.valida_caso=true;
}
public static void TeclaDerecha(){
	doType(KeyEvent.VK_RIGHT);
	Variables.valida_caso=true;
}
public static void TeclaIzquierda(){
	doType(KeyEvent.VK_LEFT);
	Variables.valida_caso=true;
}
public static void func_espera_texto (String mapeo_objeto, String tiempo, WebDriver driver){
	//"xpath=//*[contains(text(),'" + ValorParametro1 + "')]"'
	//P[text()='Cargando cotizador... Espere por favor...']
	try{
    	Thread.sleep(2000);
		int intentos = 0;    		
		if(tiempo.equalsIgnoreCase(""))
			tiempo="60";  	
		System.out.println("Inicio buscando texto [" + mapeo_objeto + "]: " + getHoraActual());
		Variables.cadena_html = Variables.cadena_html + "Inicio buscando texto [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
		while(true){
			if(mapeo_objeto != null){
					try{
		    					driver.findElement(By.xpath("//*[contains(text(),'" + mapeo_objeto + "')]"));
		    					System.out.println("Texto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
		    					Variables.cadena_html = Variables.cadena_html + "Texto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
		    					Variables.valida_caso = true;			
					}//return;	//el objeto ya est� en la pantalla 
	        		catch(Throwable e){
	    	    			//no ha aparecido, sigue validando
	    	    		}
	    			}
			if (Variables.valida_caso == true) {Thread.sleep(2000);break;};
			intentos++;
			System.out.println("Esperando: " + intentos);
			if(intentos >  Integer.parseInt(tiempo)){
				System.out.println("Texto NO encontrado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg");
				Variables.cadena_html = Variables.cadena_html + "Texto NO encontrado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg" + "|<br>";
				Variables.valida_caso = false;
			}
			try{Thread.sleep(1000);}catch(Throwable e){}	//espera un segundo
		} 
	}
	catch (Exception ex){
		
	}
}
public static void func_visualizar_texto (String mapeo_objeto, String tiempo, WebDriver driver) throws InterruptedException{
	//tiene que ser un XPATH
try {
	int cont=0;
	Thread.sleep(2000);
	while (driver.findElement(By.xpath(mapeo_objeto)).isDisplayed()){
		Thread.sleep(1000);
		cont++;
		System.out.println("Esperando: " + cont);
		if (cont>Integer.parseInt(tiempo)){
			System.out.println("Texto NO visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg");
		    //cadena_html = cadena_html + "Texto NO visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg" + "|<br>";
			//Variables.valida_caso=false;
			break;
		}
	}
	System.out.println("Texto visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , por un per�odo de: " + cont + " .seg");
	Variables.cadena_html = Variables.cadena_html + "Texto visualizado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + cont + " .seg" + "|<br>";
	Variables.valida_caso=true;
}
catch (Exception e) {
		System.out.println(e.getMessage());
		Thread.sleep(2000);
}
}
@SuppressWarnings("null")
public static void func_espera_elemento (String mapeo_tipo,String mapeo_objeto, String tiempo, WebDriver driver){
	try{
		Variables.valida_caso = false;
    	int intentos = 0;    		
    	if(tiempo.equalsIgnoreCase(""))
    		tiempo="60";
    	Thread.sleep(2000);
    	System.out.println("Inicio buscando Objeto [" + mapeo_objeto + "]: " + getHoraActual());
    	Variables.cadena_html = Variables.cadena_html + "Inicio buscando Objeto [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	while(true){
    		if(mapeo_tipo != null){
    			
    			if(driver.getPageSource().toLowerCase().contains(mapeo_objeto.toLowerCase()))
    			{
    				System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
					Variables.valida_caso = true;
    			}else
    			{
    				try{
    	    			switch(mapeo_tipo.toLowerCase()){
    	    				case "xpath":
    	    					driver.findElement(By.xpath(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "name":
    	    					driver.findElement(By.name(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "classname":
    	    					driver.findElement(By.className(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "css":
    	    					driver.findElement(By.cssSelector(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "id":
    	    					driver.findElement(By.id(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "text":
    	    					driver.findElement(By.linkText(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "parcialtext":
    	    					driver.findElement(By.partialLinkText(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true;
    	    					break;
    	    				case "tagname":
    	    					driver.findElement(By.tagName(mapeo_objeto));
    	    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    	    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    	    					Variables.valida_caso = true; 
    	    					break;
    	    			}
    	    			//return;	//el objeto ya est� en la pantalla 
        			}catch(Throwable e){
    	    			//no ha aparecido, sigue validando
    	    		}
    			}
    		}else{
    			try{
    				switch(mapeo_tipo.toLowerCase()){
    				case "xpath":
    					driver.findElement(By.xpath(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "name":
    					driver.findElement(By.name(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "classname":
    					driver.findElement(By.className(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "css":
    					driver.findElement(By.cssSelector(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "id":
    					driver.findElement(By.id(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "text":
    					driver.findElement(By.linkText(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "parcialtext":
    					driver.findElement(By.partialLinkText(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;
    					break;
    				case "tagname":
    					driver.findElement(By.tagName(mapeo_objeto));
    					System.out.println("Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual());
    					Variables.cadena_html = Variables.cadena_html + "Objeto encontrado [" + mapeo_objeto + "]: " + getHoraActual() + "|<br>";
    					Variables.valida_caso = true;   
    					break;
    					}//return;	//el objeto ya est� en la pantalla 
            		}catch(Throwable e){
        	    			//no ha aparecido, sigue validando
        	    		}
        			}
    		if (Variables.valida_caso == true) {break;};
    		intentos++;
    		System.out.println ("Esperando: " + intentos);
    		if(intentos >  Integer.parseInt(tiempo)){
    		System.out.println("Objeto NO encontrado [" + mapeo_objeto + "]: " + getHoraActual()+" , en un m�ximo de: " + tiempo + " .seg");
			Variables.cadena_html = Variables.cadena_html + "Objeto NO encontrado [" + mapeo_objeto + "]: " + getHoraActual() +" , en un m�ximo de: " + tiempo + " .seg" + "|<br>";
			Variables.valida_caso = false;
			break;
    		}
    		try{Thread.sleep(1000);}catch(Throwable e){}	//espera un segundo
    	}
	}
	catch (Exception ex){
		
	}
 }
public static By obtenerObjeto(String mapeo_objeto, String mapeo_tipo){
	By by = null;
	switch (mapeo_tipo.toUpperCase()) {
	case "ID":
		by= new By.ById(mapeo_objeto);
		break;
	case "XPATH":
		by= new By.ByXPath(mapeo_objeto);
		break;
	case "NAME":
		by= new By.ByName(mapeo_objeto);
		break;
	case "TEXT":
		by= new By.ByLinkText(mapeo_objeto);
		break;
	case "CLASSNAME":
		by= new By.ByClassName(mapeo_objeto);
		break;
	case "CSS":
		by= new By.ByCssSelector(mapeo_objeto);
		break;
	}
	 return by;
}
public static void Cabecera_Casos (String i) {
	Variables.hora_inicio = getHoraActual();;
	func_descripcion_caso(i,Variables.str_escenario);
	Variables.cadena_html="";
	Variables.cadena_html = Variables.cadena_html + "<TR><TD>N� " + i + "</TD><TD>" + Variables.cadena_html_descripcion + "</TD><TD>[Hora inicio: " + Variables.hora_inicio + "]<br>";	
}
public static void func_descripcion_caso (String i, String escenario) {
	try {
		Variables.cadena_html_descripcion = "";
		Variables.Rst_descripcion_caso =  Variables.Cnn.prepareCall("SELECT DESCRIPCION FROM " + escenario + "_D WHERE ID_CASO='" + i + "'").executeQuery();
		if (Variables.Rst_descripcion_caso.next()==true){
			Variables.cadena_html_descripcion = Variables.Rst_descripcion_caso.getString("DESCRIPCION");
		}
		} 
	catch (Exception e) {
		System.out.println(e.getMessage());
	}
}
public static void Cabecera_Pasos (String j) {
	Variables.cadena_html = Variables.cadena_html + "'" + j + "' ";	
}
public static void rastreador_casos(){
	trace_vector(Variables.str_escenario,Variables.S_Casos_Ejecutar,"999999","INICIO_ESCENARIO",Variables.S_Version,Variables.S_Corrida,Variables.S_Arquitecto_QA,Variables.P_Nombre_Maquina,Variables.ruta,Variables.dtFechaInicio.toString(),"-");
}
public static void Func_Valida_Caso (String escenario, String caso, String paso) throws IOException {
	try {
	String Str_Resultado_Caso="";
	Variables.dtFechaFinal = getFechaActual() + " " + getHoraActual();
	Variables.hora_final = getHoraActual();
	Thread.sleep(2000);
	if (Variables.valida_caso == true) {
		Str_Resultado_Caso="OK";
	}else{
		Str_Resultado_Caso="ERROR";
	}
	String ruta_detalle = "(DETALLE)_PRUEBAS_" + escenario + "_" + Variables.S_Version + "_" + Variables.S_Corrida + "_" + Variables.web + "_" + caso + ".html";
	if (Str_Resultado_Caso == "OK") {
		Variables.cadena_html = Variables.cadena_html + "[Hora Final: " + Variables.hora_final + "]<br></TD><TD><font color='#3366FF'><b><a href='"+ ruta_detalle + "'>[OK]</a></b></font><br></TD><TD>" + tiempo_caso(Variables.hora_final,Variables.hora_inicio) + "</TD></TR>";
		Variables.Cnn.prepareStatement("UPDATE " + escenario + "_D SET RESULTADO='" + Str_Resultado_Caso + "', ESTADO_EJECUCION='FINALIZADO', TIEMPO_EJECUCION='" + Variables.total_seg + "' WHERE ID_CASO='" + caso + "'").executeUpdate();
	}
	else {
		Variables.cadena_html = Variables.cadena_html + "[Hora Final: " + Variables.hora_final + "]<br></TD><TD><font color='#FF0000'><b><a href='"+ ruta_detalle + "'>[ERROR]</a></b></font><br></TD><TD>" + tiempo_caso(Variables.hora_final,Variables.hora_inicio) + "</TD></TR>";
		Variables.Cnn.prepareStatement("UPDATE " + escenario + "_D SET RESULTADO='" + Str_Resultado_Caso + "', ESTADO_EJECUCION='FINALIZADO', TIEMPO_EJECUCION='" + Variables.total_seg + "' WHERE ID_CASO='" + caso + "'").executeUpdate();
	}
	trace_vector(Variables.str_escenario,Variables.S_Casos_Ejecutar,Variables.D_ID_CASO,Str_Resultado_Caso,Variables.S_Version,Variables.S_Corrida,Variables.S_Arquitecto_QA,Variables.P_Nombre_Maquina,Variables.ruta,Variables.dtFechaFinal,Variables.total_seg.toString());
	captura_evidencia(Variables.file ,escenario, Variables.S_Version, Variables.S_Corrida, caso, paso);
	}
	catch (Exception e) {
    	System.out.println(e.getMessage());
        System.out.println("Fallo el valida caso");
    }
}
public static String tiempo_caso (String fin, String ini) {
	String[] h_fin  = fin.split(":");
	String[] h_ini = ini.split(":");
	Variables.total_seg = (long) ((Integer.valueOf(h_fin[0])*3600 + Integer.valueOf(h_fin[1])*60 + Integer.valueOf(h_fin[2])) - (Integer.valueOf(h_ini[0])*3600 + Integer.valueOf(h_ini[1])*60 + Integer.valueOf(h_ini[2])));
	Variables.tiempo_ejecu = (Variables.total_seg/3600 + "h" + (Variables.total_seg % 3600)/60 + "m" + (Variables.total_seg % 3600) % 60 + "s").toString();
	return Variables.tiempo_ejecu;	
}
public static void highlightElement(WebElement element) throws InterruptedException {
	JavascriptExecutor js = (JavascriptExecutor) Variables.driver;
	js.executeScript("arguments[0].setAttribute('style', arguments[1]);",element, "color: yellow; border: 5px solid #00FF00; background: #CECEF6;");
	Thread.sleep(50);
	js.executeScript("arguments[0].setAttribute('style', arguments[1]);",element, "");
}
public static void captura_evidencia (String file , String escenario, String version, String corrida, String caso, String paso){
	String str_Tipo_Result="";
	String Archivo="";
	String FechaHoraCapturaEvidencia = getFechaActual() + " " + getHoraActual();
	String VRB_Tiempo;
	try {
		Thread.sleep(1000);
		if (Variables.valida_caso==true){
			str_Tipo_Result="OK";
		}else{
			str_Tipo_Result="ERROR";
		}
		File scrFile = ((TakesScreenshot) Variables.driver).getScreenshotAs(OutputType.FILE);
		VRB_Tiempo = FechaHoraCapturaEvidencia.toString().replace("/","_").replace(" ","_").replace(":","_").replace("#","");
		Archivo = file + "CapturePNG\\" + escenario + "-" + version + "-" + corrida + "-" + caso + "-" +  paso + "_" + VRB_Tiempo + ".png";
		FileUtils.copyFile(scrFile, new File(Archivo));
		Variables.Cnn.prepareCall("INSERT INTO TBL_LOG_EVIDENCIA (L_ESCENARIO,L_VERSION,L_CORRIDA,L_CADENA_HTML,L_ID_CASO,L_IMAGENES,L_TIPO_RESULTADO,L_FECHA_HORA_EJECUCION) VALUES ('" + escenario + "','" + version + "','" + corrida + "','" + Variables.cadena_html.replaceAll("'", "") + "','" + caso + "','" + Archivo + "','" + str_Tipo_Result + "','" + FechaHoraCapturaEvidencia + "')").execute();
	}
    catch (Exception ini) {
        try {
        	VRB_Tiempo = FechaHoraCapturaEvidencia.toString().replace("/","_").replace(" ","_").replace(":","_").replace("#","");
    		Archivo = file + "CapturePNG\\" + escenario + "-" + version + "-" + corrida + "-" + caso + "-" +  paso + "_" + VRB_Tiempo + ".png";
        	CapturaPantallaJava(file + "CapturePNG\\",escenario + "-" + version + "-" + corrida + "-" + caso + "-" +  paso + "_" + VRB_Tiempo + ".png");
        	Variables.Cnn.prepareCall("INSERT INTO TBL_LOG_EVIDENCIA (L_ESCENARIO,L_VERSION,L_CORRIDA,L_CADENA_HTML,L_ID_CASO,L_IMAGENES,L_TIPO_RESULTADO,L_FECHA_HORA_EJECUCION) VALUES ('" + escenario + "','" + version + "','" + corrida + "','" + Variables.cadena_html.replaceAll("'", "") + "','" + caso + "','" + Archivo + "','" + str_Tipo_Result + "','" + FechaHoraCapturaEvidencia + "')").execute();
        }
        catch (Exception e){
        	System.out.println("Fallo la captura de la pantalla: " + e.getMessage());
            try {
    			Variables.Cnn.prepareCall("INSERT INTO TBL_LOG_EVIDENCIA (L_ESCENARIO,L_VERSION,L_CORRIDA,L_CADENA_HTML,L_ID_CASO,L_IMAGENES,L_TIPO_RESULTADO,L_FECHA_HORA_EJECUCION) VALUES ('" + escenario + "','" + version + "','" + corrida + "','" + Variables.cadena_html.replaceAll("'", "") + "','" + caso + "','" + "Sin Evidencia" + "','" + str_Tipo_Result + "','" + FechaHoraCapturaEvidencia + "')").execute();
    		} catch (Exception e1) {
    			System.out.println(e1.getMessage());
    		}
            System.out.println (e.getMessage());
            Variables.valida_caso = false;
        }
    }
}
public static void CapturaPantallaJava(String Ruta, String NombreImagen) 
{
	try {
	    Robot robot = new Robot();  
	    BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));  
	    ImageIO.write(image, "png", new File(Ruta + "//" + NombreImagen));  
	} catch (Exception e) 
	{
		System.out.println("Error tomando la captura de la pantalla");
		Variables.valida_caso = false;
	}
}
public static void kill_procesos (String tipo_browser){
	try {
	switch (tipo_browser.toUpperCase()) {
	case "CHROME":
		Thread.sleep(2000);
    	Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
    	Variables.driver.quit();
    	//Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
	break;
	case "FIREFOX":
		Thread.sleep(2000);
   		Runtime.getRuntime().exec("taskkill /F /IM firefox.exe");
   		Variables.driver.quit();
	break;
	case "IEXPLORER":
		Thread.sleep(2000);
    	Runtime.getRuntime().exec("taskkill /F /IM iexplore.exe");
    	Variables.driver.quit();
	break;
	}
	}
	catch (Exception e) {
		try {
		switch (tipo_browser.toUpperCase()) {
		case "CHROME":
			Thread.sleep(2000);
	    	Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
	    	Variables.driver.quit();
	    	//Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
		break;
		case "FIREFOX":
			Thread.sleep(2000);
	   		Runtime.getRuntime().exec("taskkill /F /IM firefox.exe");
	   		Variables.driver.quit();
		break;
		case "IEXPLORER":
			Thread.sleep(2000);
	    	Runtime.getRuntime().exec("taskkill /F /IM iexplore.exe");
	    	Variables.driver.quit();
		break;
		}
		}
		catch (Exception e1){
			System.out.println(e1.getMessage());
		}
    }
}
public static void leer_cadena_html_final (String caso, String Tipo_Resultado){
	try {
		Variables.Rst_Leer_Cadena_Html_Final =  Variables.Cnn.prepareCall("SELECT L_CADENA_HTML FROM TBL_LOG_EVIDENCIA WHERE L_ID_LOG IN (SELECT MAX(L_ID_LOG) FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_TIPO_RESULTADO='" + Tipo_Resultado + "' AND L_ID_CASO='" + caso + "')").executeQuery();
		Variables.str_Leer_Cadena_Html_Final = "";
		if (Variables.Rst_Leer_Cadena_Html_Final.next()==true){
			Variables.str_Leer_Cadena_Html_Final=Variables.Rst_Leer_Cadena_Html_Final.getString("L_CADENA_HTML");
		}
	} catch (SQLException e) {
		System.out.println(e.getMessage());
	}
}
@SuppressWarnings("null")
public static void generar_evidencia_caso (String file, String escenario, String caso) throws IOException {
	try{
		   //Escribimos la ruta y el nombre del fichero
		   String ruta = file;
		   String nombre = "(DETALLE)_PRUEBAS_" + escenario + "_" + Variables.S_Version + "_" + Variables.S_Corrida + "_" + Variables.web + "_" + caso + ".html";	   
		   //Creamos un objeto con los valores anteriores
		   File archivo = new File(ruta, nombre);
		    //Creamos el archivo en el disco
		   archivo.createNewFile();
		   //Creamos los objetos para escribir en el archivo creado
		   FileWriter fw = new FileWriter(archivo);
		   PrintWriter pw = new PrintWriter(fw);   	   
		   pw.println("<meta http-equiv='Content-Type' content='' text/html; charset=utf-8''/>");
		   pw.println("<html>");
		   pw.println("<title>AUTOMATIZACION DE PRUEBAS - VECTOR SOFTWARE FACTORY</title>");
		   pw.println("<CENTER><TABLE border='1' style='border-color: #000 #999 #999 #000' style='font-family:verdana;font-size:11'  cellpadding='5' cellspacing='0'>");
		   pw.println("<TABLE class = 'altrowstable'>");
	        pw.println("<TR ALIGN=CENTER><TD COLSPAN=2><IMG SRC='" + file +"logo_ch.jpg'></TD></TR>");
	        pw.println("<TR ALIGN=CENTER><TD COLSPAN=2>EVIDENCIA PRUEBAS FUNCIONALES CASO N� " + caso +  "</CENTER></TD></TR>");
	        pw.println("<TR><TD><b>Proyecto:</b> </TD><TD>" + Variables.str_escenario + "</TD></TR>");
	        pw.println("<TR><TD><b>Version: </b> </TD><TD>" + Variables.S_Version + "</TD></TR>");
	        pw.println("<TR><TD><b>Corrida: </b> </TD><TD>" + Variables.S_Corrida + "</TD></TR>");
	        pw.println("<TR><TD><b>Hora y fecha del reporte: </b> </TD><TD>" + getFechaActual() + " " + getHoraActual() + "</TD></TR>");
	        pw.println("<TR><TD><b>Responsable Automatizaci�n :</b> </TD><TD>" + Variables.S_Arquitecto_QA + "</TD></TR>");
	        pw.println("<TR><TD><b>Descripci�n :</b> </TD><TD>" + Variables.cadena_html_descripcion + "</TD></TR>");
	        pw.println("</TABLE></CENTER>");
	        pw.println("<br><br>");      
	        pw.println("<!-- CSS goes in the document HEAD or added to your external stylesheet -->");
	        pw.println("<style type='text/css'>");
	        pw.println("table.altrowstable {");
	        pw.println(" font-family: verdana,arial,sans-serif;");
	        pw.println(" font-size:11px;");
	        pw.println(" color:#333333;");
	        pw.println(" border-width: 1px;");
	        pw.println(" border-color: #a9c6c9;");
	        pw.println(" border-collapse: collapse;");
	        pw.println("}");
	        pw.println("table.altrowstable th {");
	        pw.println(" border-width: 1px;");
	        pw.println(" padding: 8px;");
	        pw.println(" border-style: solid;");
	        pw.println(" border-color: #a9c6c9;");
	        pw.println("}");
	        pw.println("table.altrowstable td {");
	        pw.println(" border-width: 1px;");
	        pw.println(" padding: 8px;");
	        pw.println(" border-style: solid;");
	        pw.println(" border-color: #a9c6c9;");
	        pw.println("}");
	        pw.println(".oddrowcolor{");
	        pw.println(" background-color:#d4e3e5;");
	        pw.println("}");
	        pw.println(".evenrowcolor{");
	        pw.println(" background-color:#c3dde0;");
	        pw.println("}");
	        pw.println("</style>");
	        pw.println(" <script language='JavaScript1.2'>");
	        pw.println("    var zoomfactor=0.5");
	        pw.println("    function zoomhelper(){");
	        pw.println("    whatcache.style.width=1300");
	        pw.println("whatcache.style.height=1100");
	        pw.println("    }");
	        pw.println("    function zoom(originalW, originalH, what, state)");
	        pw.println("   {");
	        pw.println("    if (!document.all&&document.getElementById)");
	        pw.println("    return");
	        pw.println("    whatcache=eval('document.images.'+what)");
	        pw.println("    prefix=(state=='in')? 1 : -1");
	        pw.println("    if (whatcache.style.width==''''||state=='restore')");
	        pw.println("    {");
	        pw.println("    whatcache.style.width=originalW + 111");
	        pw.println("    whatcache.style.height=originalH + 100");
	        pw.println("    if (state=='restore')");
	        pw.println("    return");
	        pw.println("    }");
	        pw.println("    else");
	        pw.println("    {");
	        pw.println("    zoomhelper()");
	        pw.println("    }");
	        pw.println("    beginzoom=setInterval('zoomhelper()',10)");
	        pw.println("    }");
	        pw.println("    function clearzoom(){");
	        pw.println("    if (window.beginzoom)");
	        pw.println("    clearInterval(beginzoom)");
	        pw.println("    }");
	        pw.println("</script>");
	        pw.println("      <script> ");
	        pw.println("        function MostrarTImagenes(recolocar) ");
	        pw.println("       { ");
	        pw.println("        for (x=1;x<document.images.length;x++) ");
	        pw.println("            { ");
	        pw.println("                  document.images[x].height='623' ");
	        pw.println("                  document.images[x].width='1366' ");
	        pw.println("            } ");
	        pw.println("        } ");
	        pw.println("        function ImagenesP(recolocar) ");
	        pw.println("       { ");
	        pw.println("        for (x=1;x<document.images.length;x++) ");
	        pw.println("            { ");
	        pw.println("                  document.images[x].height='310' ");
	        pw.println("                  document.images[x].width='608' ");
	        pw.println("            } ");
	        pw.println("        } ");
	        pw.println("      </script> ");
	        pw.println("<CENTER><table class='altrowstable'>");
	        pw.println("<TR ALIGN=CENTER><TD COLSPAN=2><b>");
	        pw.println("<TABLE border='1'>");
	        pw.println("<TD><a href='#' onClick='MostrarTImagenes(true);'>Ampliar Vista</a></TD>");
	        pw.println("<TD><a href='#' onClick='ImagenesP(true);'>Vista Normal</a></TD>");
	        pw.println("</TABLE></b></TR>");
	        pw.println("<br><br>");
	        pw.println("<table class='altrowstable'>");
	        pw.println("<b><TR><TD><b>Evidencia</b></TD></TR></b>");
	        int contador = 0;
	        Variables.Rst_ScreenShoot_PNG = Variables.Cnn.prepareCall("SELECT L_IMAGENES FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_ID_CASO='" + caso + "' ORDER BY L_ID_LOG ASC").executeQuery();
	        while  (Variables.Rst_ScreenShoot_PNG.next()==true){
	        	Variables.str_ScreenShoot_PNG = Variables.Rst_ScreenShoot_PNG.getString("L_IMAGENES");
	            pw.println("<TR><TD><table border='0'  cellpadding='5' cellspacing='0' valign='top'>");
	            pw.println("<p align='center'>");         
	            pw.println("<TD><img name='a" + contador + "' src='" + Variables.str_ScreenShoot_PNG + "' align='left' border='1' hspace='0' vspace='0' width='608' height='310'></p>");
	            pw.println("</table>"); 
	            contador = contador + 1;
            }
    		pw.println("</body>");
    		pw.println("</html>");		   
    		pw.close();
    		fw.close();
		  } catch(Exception e) {
			  System.out.println(e.getMessage());
			  FileInputStream pw = null;
			pw.close();
			  FileInputStream fw = null;
			fw.close();
		  }
}

@SuppressWarnings("null")
public static void generar_reporte_evidencia(String file,String escenario) throws IOException {
	try{
		   //Escribimos la ruta y el nombre del fichero
		   String ruta = file;
		   String nombre = "EVIDENCIA_AUTOMATIZACION_PRUEBAS_" + escenario + "_" + Variables.S_Version + "_" + Variables.S_Corrida + "_" + Variables.web + ".html";
		   //Creamos un objeto con los valores anteriores
		   File archivo = new File(ruta, nombre);
		    //Creamos el archivo en el disco
		   archivo.createNewFile();
		   //Creamos los objetos para escribir en el archivo creado
		   FileWriter fw = new FileWriter(archivo);
		   PrintWriter pw = new PrintWriter(fw);   	   
		   pw.println("<meta http-equiv='Content-Type' content='' text/html; charset=utf-8''/>");
		   pw.println("<html>");
		   pw.println("<body><title>AUTOMATIZACION DE PRUEBAS - VECTOR SOFTWARE FACTORY</title>");
		   pw.println("<CENTER><TABLE border='1' style='border-color: #000 #999 #999 #000' style='font-family:verdana;font-size:11'  cellpadding='5' cellspacing='0'>");
		   pw.println("<TABLE class = 'altrowstable'>");
           pw.println("<TR ALIGN=CENTER><TD COLSPAN=2><IMG SRC='" + file + "logo_ch.jpg'></TD></TR>");
           pw.println("<TR ALIGN=CENTER><TD COLSPAN=2>EVIDENCIA PRUEBAS FUNCIONALES ITERACI�N N� " + Variables.S_Corrida +  "</CENTER></TD></TR>");
           pw.println("<TR><TD><b>Proyecto:</b> </TD><TD>" + Variables.str_escenario + "</TD></TR>");
           pw.println("<TR><TD><b>Version: </b> </TD><TD>" + Variables.S_Version + "</TD></TR>");
           pw.println("<TR><TD><b>Hora y fecha del reporte: </b> </TD><TD>" + getFechaActual() + " " + getHoraActual() + "</TD></TR>");
           pw.println("<TR><TD><b>Responsable Automatizaci�n :</b> </TD><TD>" + Variables.S_Arquitecto_QA + "</TD></TR>");
           pw.println("</TABLE></CENTER>");
           ///////RESUMEN DE CASOS
           func_conteo_resultados();
           pw.println("<CENTER><TABLE border='1' style='border-color: #000 #999 #999 #000' style='font-family:verdana;font-size:11'  cellpadding='5' cellspacing='0'>");
		   pw.println("<TABLE class = 'altrowstable'>");
           pw.println("<TR ALIGN=center><TD COLSPAN=2><b><a name='MENU'>RESUMEN EJECUCI�N</a></b></TD></TR>");
           if (Variables.Rst_Total.next()==true){
        	   pw.println("<TR><TD><b>TOTAL CASOS EJECUTADOS:</b> </TD><TD>" + Variables.Rst_Total.getString("TOTAL_CASOS") + "</TD></TR>");
           }else{
        	   pw.println("<TR><TD><b>TOTAL CASOS EJECUTADOS:</b> </TD><TD>" + "-" + "</TD></TR>");   
           }
           if (Variables.Rst_OK.next()==true){
        	   pw.println("<TR><TD><b>TOTAL <a href='#CASOS OK'> CASOS OK</a></TD><TD>" + Variables.Rst_OK.getString("COUNT_OK") + "</b></TD></TR>"); 
           }else{
        	   pw.println("<TR><TD><b>TOTAL <a href='#CASOS OK'> CASOS OK</a></TD><TD>" + "0" + "</b></TD></TR>"); 
           }
           if (Variables.Rst_ERROR.next()==true){
        	   pw.println("<TR><TD><b>TOTAL <a href='#CASOS ERROR'> CASOS ERROR</a></TD><TD>" + Variables.Rst_ERROR.getString("COUNT_ERROR") + "</b></TD></TR>");
           }else{
        	   pw.println("<TR><TD><b>TOTAL <a href='#CASOS ERROR'> CASOS ERROR</a></TD><TD>" + "0" + "</b></TD></TR>");
           }
           pw.println("</TABLE></CENTER>");
           //////FIN RESUMEN CASOS
           pw.println("<br><br>");
           pw.println("<script type='text/javascript'>");
           pw.println("function altRows(id){");
           pw.println(" if(document.getElementsByTagName){  ");
           pw.println(" var table = document.getElementById(id);  ");
           pw.println(" var rows = table.getElementsByTagName('tr'); ");
           pw.println(" for(i = 0; i < rows.length; i++){          ");
           pw.println(" if(i % 2 == 0){");
           pw.println(" rows[i].className = 'evenrowcolor';");
           pw.println(" }else{");
           pw.println(" rows[i].className = 'oddrowcolor';");
           pw.println(" }      ");
           pw.println(" }");
           pw.println(" }");
           pw.println("}");
           pw.println("window.onload=function(){");
           pw.println(" altRows('alternatecolor');");
           pw.println("}");
           pw.println("</script>");
           pw.println("<!-- CSS goes in the document HEAD or added to your external stylesheet -->");
           pw.println("<style type='text/css'>");
           pw.println("table.altrowstable {");
           pw.println(" font-family: verdana,arial,sans-serif;");
           pw.println(" font-size:11px;");
           pw.println(" color:#333333;");
           pw.println(" border-width: 1px;");
           pw.println(" border-color: #a9c6c9;");
           pw.println(" border-collapse: collapse;");
           pw.println("}");
           pw.println("table.altrowstable th {");
           pw.println(" border-width: 1px;");
           pw.println(" padding: 8px;");
           pw.println(" border-style: solid;");
           pw.println(" border-color: #a9c6c9;");
           pw.println("}");
           pw.println("table.altrowstable td {");
           pw.println(" border-width: 1px;");
           pw.println(" padding: 8px;");
           pw.println(" border-style: solid;");
           pw.println(" border-color: #a9c6c9;");
           pw.println("}");
           pw.println(".oddrowcolor{");
           pw.println(" background-color:#d4e3e5;");
           pw.println("}");
           pw.println(".evenrowcolor{");
           pw.println(" background-color:#c3dde0;");
           pw.println("}");
           pw.println("</style>");           
           pw.println("<TABLE  WIDTH='100%'><TR WIDTH='100%' > <TD WIDTH='100%' VALIGN='CENTER' ALIGN='CENTER' BGCOLOR='#5F9EA0'><H3><FONT FACE='verdana' ><a name='CASOS OK'>CASOS OK</a></FONT></H3></TD></TR></TABLE><BR>");
           pw.println("<CENTER><table class='altrowstable'>");
           pw.println("<b><TR><TD><b>ID. Caso</b></TD><TD><b>Descripci�n Caso</b></TD><TD><b>Pasos Ejecuci�n</b></TD><TD><b>Resultado Caso</b></TD><TD><b>Tiempo</b></TD></TR></b>");
           func_identificar_Casos_DataDriven();
           while (Variables.Rst_Datos.next()==true){
        	   leer_cadena_html_final(Variables.Rst_Datos.getString("ID_CASO"), "OK");
        	   if (!Variables.str_Leer_Cadena_Html_Final.equals("")){
        		   pw.println(Variables.str_Leer_Cadena_Html_Final);
        	   }
           }
           Variables.Rst_Datos.close();
           pw.println("<TABLE  WIDTH='100%'><TR WIDTH='100%' > <TD WIDTH='100%' VALIGN='CENTER' ALIGN='CENTER' BGCOLOR='#B22222'><H3><FONT FACE='verdana' ><a name='CASOS ERROR'>CASOS ERROR</a></FONT></H3></TD></TR></TABLE><BR>");
           pw.println("<CENTER><table class='altrowstable'>");
           pw.println("<b><TR><TD><b>ID. Caso</b></TD><TD><b>Descripci�n Caso</b></TD><TD><b>Pasos Ejecuci�n</b></TD><TD><b>Resultado Caso</b></TD><TD><b>Tiempo</b></TD></TR></b>");
           func_identificar_Casos_DataDriven();
           while (Variables.Rst_Datos.next()==true){
        	   leer_cadena_html_final(Variables.Rst_Datos.getString("ID_CASO"), "ERROR");
        	   if (!Variables.str_Leer_Cadena_Html_Final.equals("")){
        		   pw.println(Variables.str_Leer_Cadena_Html_Final);
        	   }
           }   
		   pw.println("</table></CENTER></body>");
		   pw.println("</html>");		   
		   pw.close();
		   fw.close();
		  } catch(Exception e) {
			  System.out.println(e.getMessage());
			  FileInputStream pw = null;
			pw.close();
			  FileInputStream fw = null;
			fw.close();
		  }
}
public static void func_conteo_resultados (){
	try {
		Variables.Rst_Total = Variables.Cnn.prepareCall("SELECT COUNT(DISTINCT(L_ID_CASO)) AS TOTAL_CASOS FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "'").executeQuery();
		Variables.Rst_OK = Variables.Cnn.prepareCall("SELECT COUNT(DISTINCT(L_ID_CASO)) AS COUNT_OK FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_ID_CASO NOT IN (SELECT L_ID_CASO FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_TIPO_RESULTADO='ERROR')").executeQuery();
		Variables.Rst_ERROR = Variables.Cnn.prepareCall("SELECT COUNT(DISTINCT(L_ID_CASO)) AS COUNT_ERROR FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_ID_CASO IN (SELECT L_ID_CASO FROM TBL_LOG_EVIDENCIA WHERE L_ESCENARIO='" + Variables.str_escenario + "' AND L_VERSION='" + Variables.S_Version + "' AND L_CORRIDA='" + Variables.S_Corrida + "' AND L_TIPO_RESULTADO='ERROR')").executeQuery();
	} catch (SQLException e) {
		System.out.println(e.getMessage());;
	}
}
public static void func_open_windows(String web, String ruta, String valor) {
	try {	 
			File file;
		  switch (web.toUpperCase()) {
		  	case "IEXPLORER":
		  		file = new File(ruta + "LibWebDriver_v2/IEDriverServer32.exe");	  		
		  		System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
		  		System.setProperty("webdriver.ie.logfile",ruta + "LibWebDriver_v2/IEDriverServer.log");
		  		DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
		  	    //caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
		  	    caps.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
		  	    caps.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			  	caps.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "accept");
			  	caps.setCapability("ignoreProtectedModeSettings", true);
			  	caps.setCapability("disable-popup-blocking", true);
			  	caps.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
		  		Variables.driver = new InternetExplorerDriver(caps);
		  		Variables.driver.manage().window().maximize();
		  		Variables.driver.get(valor);
		  		Variables.driver.findElement(By.id("overridelink")).click();
		  		Thread.sleep(2000);
		  		Variables.cadena_html = Variables.cadena_html + "Inicio Browser [" + web.toUpperCase() + "] ";
		  		Variables.valida_caso=true;
		  		break;
		  	case "CHROME":
		  		file = new File(ruta + "LibWebDriver_v2/ChromeDriver.exe");
		  		System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
		  		System.setProperty("webdriver.chrome.logfile", ruta + "LibWebDriver_v2/ChromeDriver.log");
		  		ChromeOptions options = new ChromeOptions();
		  		options.addArguments("test-type");
		  		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		  		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		  		Variables.driver = new ChromeDriver(capabilities);
		  		Variables.driver.manage().window().maximize();
		  		Variables.driver.get(valor);
		  		Variables.cadena_html = Variables.cadena_html + "Inicio Browser [" + web.toUpperCase() + "] ";
		  		Variables.valida_caso=true;
		  		Variables.driver.manage().timeouts().pageLoadTimeout(60,TimeUnit.SECONDS);
		  		break;
		  	case "FIREFOX":
		  		file = new File(ruta + "LibWebDriver_v2/geckodriver32.exe");	  		
		  		System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
		  		DesiredCapabilities capabilitiesf=DesiredCapabilities.firefox();
		        capabilitiesf.setCapability("marionette", true);
		  		Variables.driver = new FirefoxDriver(capabilitiesf);
		  		Variables.driver.get(valor);
		  		/*
		  		file = new File(ruta + "LibWebDriver_v2/geckodriver.exe");	  		
		  		System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
		  		DesiredCapabilities capabilitiesF = DesiredCapabilities.firefox();
		  	    capabilitiesF.setCapability("marionette", true);
		  		Variables.driver = new FirefoxDriver(capabilitiesF);
		  		*/
		  		Variables.cadena_html = Variables.cadena_html + "Inicio Browser [" + web.toUpperCase() + "] ";
		  		Variables.valida_caso=true;
		  		Variables.driver.manage().timeouts().pageLoadTimeout(60,TimeUnit.SECONDS);
		  		break;
		  default:
			  	  Variables.cadena_html = Variables.cadena_html + "No se indic� el Browser en la tabla Schedule ... ";
			  	  Variables.valida_caso=false;
			  	  break;
		  }   
		    //Variables.driver.manage().timeouts().implicitlyWait(1,TimeUnit.SECONDS);
		    //Variables.driver.manage().window().maximize();
		    //Variables.driver.manage().timeouts().pageLoadTimeout(60,TimeUnit.SECONDS);
		    
		    Thread.sleep(2000);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			Variables.valida_caso = false;
		}
}
public static void desconectar_sql() throws SQLException{
	try
	{	
		try{
			Variables.driver.quit();
			Variables.Cnn.close();
			if (Variables.CnnOracle.isReadOnly()==true) Variables.CnnOracle.close();
			Variables.Rst_Objetos.close();
			Variables.Rst_Datos.close();
			Variables.Rst_Pasos_Read.close();
			Variables.Rst_Datos_Read.close();
			Variables.Rst_descripcion_caso.close();
			Variables.Rst_Escenarios_Pendientes.close();
			Variables.Rst_Leer_Cadena_Html_Final.close();
			Variables.Rst_ScreenShoot_PNG.close();
			Variables.Rst_Total.close();
			Variables.Rst_OK.close();
			Variables.Rst_ERROR.close();
		}catch(Exception e){}
			Variables.Cnn.close();
			Variables.Rst_Objetos.close();
			Variables.Rst_Datos.close();
			Variables.Rst_Pasos_Read.close();
			Variables.Rst_Datos_Read.close();
			Variables.Rst_descripcion_caso.close();
			Variables.Rst_Escenarios_Pendientes.close();
			Variables.Rst_Leer_Cadena_Html_Final.close();
			Variables.Rst_ScreenShoot_PNG.close();
			Variables.Rst_Total.close();
			Variables.Rst_OK.close();
			Variables.Rst_ERROR.close();
	}catch(Exception e){System.out.println(e.getMessage());}	
}
public static String getFechaActual() {
    Date ahora = new Date();
    SimpleDateFormat formateador = new SimpleDateFormat("dd-MM-yyyy");
    return formateador.format(ahora);
}
public static String getHoraActual() {
    Date ahora = new Date();
    SimpleDateFormat formateador = new SimpleDateFormat("hh:mm:ss");
    return formateador.format(ahora);
}
}

