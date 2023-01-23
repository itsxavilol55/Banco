import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class App extends JFrame implements ActionListener {
    DefaultTableModel modelo1, modelo2;
    JTextField TField;
    JButton BtnCalcular, BtnAgregar, BtnOcultar;
    JTable tabla2;
    boolean Visible = true;
    int deno[] = { 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000 };

    public static void main(String[] args) {
        new App();
    }

    public App() {
        super("banco");
        interfaz();
        eventos();
    }

    private void interfaz() {
        setSize(800, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        BtnCalcular = new JButton("Calcular");
        BtnOcultar = new JButton("Ocultar");
        BtnAgregar = new JButton("Agregar");
        TField = new JTextField(12);
        JPanel panel4 = new JPanel();// botones
        {
            panel4.setLayout(new FlowLayout(1, 10, 10));
            panel4.add(BtnAgregar);
            panel4.add(BtnOcultar);
        }
        JPanel panel3 = new JPanel();// panel donde se lee el dato
        {
            panel3.setLayout(new FlowLayout(0, 10, 30));
            panel3.add(new JLabel("IMPORTE"));
            panel3.add(TField);
            panel3.add(BtnCalcular);
        }
        JPanel panel2 = new JPanel();// lado derecho
        {
            panel2.setLayout(new BoxLayout(panel2, 1));
            panel2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            modelo2 = new DefaultTableModel(0, 2);
            modelo2.setColumnIdentifiers(new Object[] { "Denomacion", "Cantidad" });
            tabla2 = new JTable(modelo2);
            tabla2.setModel(modelo2);
            tabla2.setRowHeight(30);
            tabla2.setEnabled(false);
            panel2.add(new JScrollPane(tabla2));
            panel2.add(panel4);
        }
        JPanel panel1 = new JPanel();// lado izquierdo
        {
            panel1.setLayout(new BoxLayout(panel1, 1));
            panel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel1.add(new JLabel("Banco de cambio"));
            panel1.add(new JLabel("Caja 1 "));
            panel1.add(panel3);
            modelo1 = new DefaultTableModel(10, 2);
            JTable tabla1 = new JTable(modelo1);
            JScrollPane sp = new JScrollPane(tabla1);
            modelo1.setColumnIdentifiers(new Object[] { "Denomacion", "Cantidad" });
            tabla1.setModel(modelo1);
            tabla1.setRowHeight(30);
            tabla1.setEnabled(false);
            panel1.add(sp);
        }
        llenarTabla();
        add(panel1);
        add(panel2);
        setVisible(true);
    }

    private void llenarTabla() {
        try {
            RandomAccessFile archivo = new RandomAccessFile("BILLETES.DAT", "rw");// en el nivel del src
            for (int i = 0; i < deno.length; i++) {
                archivo.seek((deno[i] - 1) * 8);
                modelo2.addRow(new Object[] { archivo.readInt(), archivo.readInt() });
            }
            archivo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eventos() {
        BtnCalcular.addActionListener(this);
        BtnAgregar.addActionListener(this);
        BtnOcultar.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == BtnOcultar) {
            Visible = !Visible;
            tabla2.setVisible(Visible);
            return;
        }
        if (e.getSource() == BtnAgregar) {
            Agregar();
            return;
        }
        if (e.getSource() == BtnCalcular) {
            Calcular();
            return;
        }
    }

    private void Agregar() {
        try {
            RandomAccessFile archivo = new RandomAccessFile("BILLETES.DAT", "rw");// en el nivel del src
            for (int i = 0; i < deno.length; i++) {
                int indice = ((deno[i] - 1) * 8) + 4;// pone el apuntador en el segundo valor de cada registro
                archivo.seek(indice);
                int num = archivo.readInt() + (int) (Math.random() * 10) + 10;// al leer el dato aumenta en 4 el
                                                                              // apuntador
                archivo.seek(indice);// lo regresa al valor anterior
                archivo.writeInt(num);// aumenta en 4 otra vez
                modelo2.setValueAt(num, i, 1);
            }
            archivo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Calcular() {
        try {
            RandomAccessFile archivo = new RandomAccessFile("BILLETES.DAT", "rw");// en el nivel del src
            int monto = Integer.parseInt(TField.getText());
            int montoInicial = monto;
            int cantidades[] = new int[deno.length];
            int suma = 0;
            for (int i = deno.length - 1; i >= 0; i--)// valida si la cantidad de billetes es correcta
            {
                if (monto / deno[i] < 1)
                    continue;
                archivo.seek(((deno[i] - 1) * 8) + 4);// mueve el apuntador a la cantidad de billete
                cantidades[i] = (int) monto / deno[i];// guarda la cantidad de una denominacion
                int total = archivo.readInt();
                if (cantidades[i] > total) {// en caso de que una denominacion no alcance acaba con el metodo
                    cantidades[i] = total;
                    monto = monto - (total * deno[i]);
                } else
                    monto = monto % deno[i];// calcula el monto restante
                suma += cantidades[i] * deno[i];
            }
            if (suma != montoInicial) {
                JOptionPane.showMessageDialog(null, "no hay suficientes billetes");
                return;
            }
            for (int i = 0; i < cantidades.length; i++) {
                int indice = ((deno[i] - 1) * 8) + 4;// pone el apuntador en el segundo valor de cada registro
                archivo.seek(indice);
                int num = archivo.readInt() - cantidades[i];// al leer el dato aumenta en 4 el apuntador
                archivo.seek(indice);// lo regresa al valor anterior
                archivo.writeInt(num);// aumenta en 4 otra vez
                modelo2.setValueAt(num, i, 1);
                archivo.seek(indice - 4);
                modelo1.setValueAt(deno[i], i, 0);
                modelo1.setValueAt(cantidades[i], i, 1);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Por favor ingrese un valor numerico");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
