package MAIN;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import java.awt.Color;

import javax.swing.JPasswordField;

import java.awt.Canvas;

@SuppressWarnings("serial")
public class ConfiguradorQA extends JFrame {

	private JPanel contentPane;
	public static JTextField txtNameDB;
	public static JTextField txtNameServidor;
	public static JTextField txtUsuario;
	public static JTextField txtPassWord;
	public static JTextField textRutaEVD;
	public static Canvas canvas = new Canvas();
	public static Canvas canvas_green = new Canvas();
	public static Canvas canvas_red = new Canvas();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				ConfiguradorQA frame = new ConfiguradorQA();
				frame.setVisible(true);
				TextEditable();
			}
		});
		
	}
	public void iniciar() throws SQLException{
		Variables.bln_demonio_escenario=true;
		Hilo mihilo = new Hilo();
		mihilo.start();	
	}
	public void pausar() throws SQLException{
		Variables.bln_demonio_escenario=false;
	}
	public void detener(){
		System.exit(0);
	}
	public static void semaforo() {
	if (Variables.blnpendiente){
		ConfiguradorQA.canvas_green.setBackground(new Color(0, 255, 0)); //verde
		ConfiguradorQA.canvas_red.setBackground(new Color(211, 211, 211));
	}else{
		ConfiguradorQA.canvas_red.setBackground(new Color(255, 0, 0)); //rojo
		ConfiguradorQA.canvas_green.setBackground(new Color(211, 211, 211));
	}
	}
	public static void TextNoEditable(){
		txtNameDB.setEditable(false);
		txtNameServidor.setEditable(false);
		txtUsuario.setEditable(false);
		txtPassWord.setEditable(false);
		textRutaEVD.setEditable(false);
	}
	public static void TextEditable(){
		txtNameDB.setEditable(true);
		txtNameServidor.setEditable(true);
		txtUsuario.setEditable(true);
		txtPassWord.setEditable(true);
		textRutaEVD.setEditable(true);
	}
	/**
	 * Create the frame.
	 */
	ConfiguradorQA() {
		setTitle("CONFIGURACIÓN ROBOT VECTOR");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 548, 255);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNombreDB = new JLabel("Nombre DB:");
		lblNombreDB.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNombreDB.setBounds(10, 34, 121, 14);
		contentPane.add(lblNombreDB);
		
		JLabel lblNombreServidor = new JLabel("Nombre Servidor:");
		lblNombreServidor.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNombreServidor.setBounds(10, 71, 121, 14);
		contentPane.add(lblNombreServidor);
		
		JLabel lblUsuario = new JLabel("Usuario:");
		lblUsuario.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblUsuario.setBounds(10, 107, 121, 14);
		contentPane.add(lblUsuario);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPassword.setBounds(10, 143, 121, 14);
		contentPane.add(lblPassword);
		
		txtNameDB = new JTextField();
		txtNameDB.setText("FW_ROBOTQA");
		txtNameDB.setBounds(120, 31, 135, 20);
		contentPane.add(txtNameDB);
		txtNameDB.setColumns(10);
		
		txtNameServidor = new JTextField();
		txtNameServidor.setText("172.22.1.123");
		txtNameServidor.setBounds(120, 68, 135, 20);
		contentPane.add(txtNameServidor);
		txtNameServidor.setColumns(10);
		
		txtUsuario = new JTextField();
		txtUsuario.setText("sa");
		txtUsuario.setBounds(120, 104, 135, 20);
		contentPane.add(txtUsuario);
		txtUsuario.setColumns(10);
		
		txtPassWord = new JPasswordField();
		txtPassWord.setText("sql2015");
		txtPassWord.setBounds(120, 140, 135, 20);
		contentPane.add(txtPassWord);
		txtPassWord.setColumns(10);
		
		textRutaEVD = new JTextField();
		textRutaEVD.setText("\\\\172.22.1.123\\EvidenciaQA\\");
		textRutaEVD.setBounds(120, 175, 135, 20);
		contentPane.add(textRutaEVD);
		textRutaEVD.setColumns(10);
		
		JButton btnActivar = new JButton("ACTIVAR");
		btnActivar.setForeground(new Color(0, 128, 0));
		JButton btnCancelar = new JButton("CANCELAR");
		btnCancelar.setForeground(new Color(255, 0, 0));
		JButton btnPausar = new JButton("PAUSAR");
		btnPausar.setForeground(new Color(0, 0, 128));
		/**/
		btnActivar.setEnabled(true);
		btnPausar.setEnabled(false);
		btnCancelar.setEnabled(false);
		/**/
		
		btnActivar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!txtNameDB.getText().equals("")
						&& !txtNameServidor.getText().equals("") 
						&& !txtUsuario.getText().equals("")
						&& !txtPassWord.getText().equals("") 
						&& !textRutaEVD.getText().equals("")){
					btnActivar.setEnabled(false);
					btnPausar.setEnabled(true);
					btnCancelar.setEnabled(true);
					try {
						TextNoEditable();
						iniciar();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				else{
					JOptionPane.showMessageDialog(null, "Registrar la configuración, faltan variables !!!","MENSAJE", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		btnActivar.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnActivar.setBounds(413, 34, 97, 40);
		contentPane.add(btnActivar);
		
		btnCancelar.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnCancelar.setEnabled(false);
				detener();
				btnActivar.setEnabled(true);
				btnPausar.setEnabled(true);
			}
		});
		btnCancelar.setBounds(413, 140, 97, 40);
		contentPane.add(btnCancelar);
		
		JLabel lblRutaEvd = new JLabel("Ruta EVD:");
		lblRutaEvd.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblRutaEvd.setBounds(10, 178, 121, 14);
		contentPane.add(lblRutaEvd);
		
		btnPausar.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnPausar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPausar.setEnabled(false);
				TextEditable();
				try {
					pausar();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				btnActivar.setEnabled(true);
				btnCancelar.setEnabled(true);
			}
		});
		btnPausar.setBounds(413, 85, 97, 40);
		contentPane.add(btnPausar);
		canvas_green.setBackground(new Color(211, 211, 211));
		canvas_green.setBounds(306, 43, 71, 61);
		contentPane.add(canvas_green);
		canvas_red.setBackground(new Color(211, 211, 211));
		canvas_red.setBounds(306, 109, 71, 61);
		contentPane.add(canvas_red);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(211, 211, 211));
		panel.setBounds(401, 11, 119, 194);
		contentPane.add(panel);
		canvas.setBackground(new Color(255, 215, 0));
		
		canvas.setBounds(301, 34, 81, 146); //blanco
		contentPane.add(canvas);
	}
}
