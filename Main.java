/*
 * MATEMATIC CALCULATOR ZERO - versión en un solo archivo.
 *
 * Compilar:  javac Main.java
 * Ejecutar:  java Main            -> abre la ventana con el menú gráfico
 *            java Main consola    -> usa el menú de texto en la consola
 *
 * Clases incluidas: Main, InterfazGrafica (ventana), EvaluadorAritmetico, SolucionadorAlgebraico,
 * SolucionadorPolinomico, DesarrolladorPolinomios, FactorizadorPolinomio, AnalizadorPolinomio,
 * Polinomio, Monomio, Fraccion, Formato, Ayuda y Ejemplos.
 */
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

// ======================================================================
// Clase: Main
// ======================================================================
/**
 * Interfaz de consola (CLI) de Matematic Calculator Zero.
 * Solo se encarga de la interacción; la lógica vive en las otras clases.
 */
public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private final EvaluadorAritmetico evaluador = new EvaluadorAritmetico();
    private final SolucionadorAlgebraico solucionadorLineal = new SolucionadorAlgebraico();
    private final SolucionadorPolinomico solucionadorPolinomico = new SolucionadorPolinomico();
    private final DesarrolladorPolinomios desarrollador = new DesarrolladorPolinomios();
    private final FactorizadorPolinomio factorizador = new FactorizadorPolinomio();

    /**
     * Sin argumentos abre la ventana gráfica (menú con todas las opciones).
     * Con el argumento "consola" usa el menú de texto:   java Main consola
     */
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Matematic Calculator Zero");
        boolean soloConsola = args.length > 0 && args[0].equalsIgnoreCase("consola");
        if (!soloConsola && !GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new InterfazGrafica().mostrar();
                }
            });
            return;
        }
        new Main().ejecutar();
    }

    // ------------------------------------------------------------ menú principal

    private void ejecutar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = leer("Elige una opción: ");
            if (opcion == null) break; // fin de la entrada

            switch (opcion.trim()) {
                case "1":
                    modo("Módulo aritmético (fracciones exactas y variables)",
                            ejemplos(Ejemplos.aritmetica())
                          + "\nComandos: variables  (ver las guardadas)   |   borrar  (quitarlas todas)",
                            "Expresión > ", evaluador::procesar);
                    break;
                case "2":
                    modo("Ecuaciones lineales y sistemas (x, y, z...)",
                            ejemplos(Ejemplos.lineales())
                          + "\nVarias ecuaciones se pueden separar con  ;   ,   o la palabra  y",
                            "Ecuación > ", solucionadorLineal::resolver);
                    break;
                case "3":
                    modo("Ecuaciones de 2.º grado (binomios y trinomios) y binomios al cubo",
                            ejemplos(Ejemplos.resolver()),
                            "Ecuación > ", solucionadorPolinomico::resolver);
                    break;
                case "4":
                    modo("Desarrollar binomios y trinomios (al cuadrado y al cubo)",
                            ejemplos(Ejemplos.desarrollar()),
                            "Expresión > ", desarrollador::desarrollar);
                    break;
                case "5":
                    modo("Factorizar binomios, trinomios y polinomios",
                            ejemplos(Ejemplos.factorizar()),
                            "Expresión > ", factorizador::factorizar);
                    break;
                case "6": menuAyuda(); break;
                case "7": salir = true; break;
                default: System.out.println("Opción no válida. Escribe un número del 1 al 7.");
            }
        }
        System.out.println("¡Hasta pronto!");
        scanner.close();
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("============ MATEMATIC CALCULATOR ZERO ============");
        System.out.println(" 1. Evaluar expresión aritmética (fracciones, variables)");
        System.out.println(" 2. Ecuaciones lineales y sistemas (x, y, z...)");
        System.out.println(" 3. Resolver binomios y trinomios (2.º grado, binomio al cubo)");
        System.out.println(" 4. Desarrollar binomios y trinomios (al cuadrado y al cubo)");
        System.out.println(" 5. Factorizar binomios, trinomios y polinomios");
        System.out.println(" 6. Ayuda (pasos de cada método)");
        System.out.println(" 7. Salir");
        System.out.println("====================================================");
    }

    /** Ciclo genérico de un módulo: lee, resuelve y maneja errores sin cerrar el programa. */
    private void modo(String titulo, String ejemplos, String prompt, Function<String, String> accion) {
        System.out.println("\n--- " + titulo + " --- (escribe 'menu' para volver)");
        System.out.println(ejemplos);
        while (true) {
            String entrada = leer(prompt);
            if (entrada == null || entrada.trim().equalsIgnoreCase("menu")) return;
            try {
                System.out.println(accion.apply(entrada));
            } catch (IllegalArgumentException e) {
                System.out.println("Error de sintaxis: " + e.getMessage());
            } catch (ArithmeticException e) {
                System.out.println("Error matemático: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------- ayuda

    private void menuAyuda() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--------------- AYUDA ---------------");
            System.out.println(" 1. Expresiones aritméticas (Shunting Yard, fracciones, variables)");
            System.out.println(" 2. Ecuaciones lineales y sistemas");
            System.out.println(" 3. Resolver binomios y trinomios (2.º grado y binomio al cubo)");
            System.out.println(" 4. Desarrollar binomios y trinomios (fórmulas)");
            System.out.println(" 5. Factorizar");
            System.out.println(" 6. Volver al menú principal");
            String op = leer("Elige un tema: ");
            if (op == null) return;
            switch (op.trim()) {
                case "1": ayudaAritmetica(); break;
                case "2": ayudaLineal(); break;
                case "3": ayudaResolver(); break;
                case "4": ayudaDesarrollar(); break;
                case "5": ayudaFactorizar(); break;
                case "6": volver = true; break;
                default: System.out.println("Opción no válida. Escribe un número del 1 al 6.");
            }
        }
    }

    private void ayudaAritmetica() {
        System.out.println(Ayuda.aritmetica());
    }

    private void ayudaLineal() {
        System.out.println(Ayuda.lineal());
    }

    private void ayudaResolver() {
        System.out.println(Ayuda.resolver());
    }

    private void ayudaDesarrollar() {
        System.out.println(Ayuda.desarrollar());
    }

    private void ayudaFactorizar() {
        System.out.println(Ayuda.factorizar());
    }

    // -------------------------------------------------------------- utilidades

    /** Encabezado y lista de ejemplos con su tipo, para mostrar al entrar a un módulo. */
    private static String ejemplos(String[][] lista) {
        return "Ejemplos (escríbelos tal cual):\n" + Ejemplos.lista(lista);
    }

    /** Lee una línea; devuelve null si ya no hay más entrada (Ctrl+D / Ctrl+Z). */
    private String leer(String mensaje) {
        System.out.print(mensaje);
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }
}

// ======================================================================
// Clase: InterfazGrafica
// ======================================================================
/**
 * Ventana gráfica de la calculadora: menú lateral con todas las opciones, pantalla de inicio con
 * tarjetas y un panel por módulo (ejemplos con un clic, caja de entrada y zona de resultados).
 * Usa exactamente las mismas clases de lógica que la versión de consola.
 */
class InterfazGrafica {

    // ------------------------------------------------------------------ paleta
    private static final Color FONDO = new Color(0xF1F5F9);
    private static final Color BARRA = new Color(0x0F172A);
    private static final Color BARRA_SEL = new Color(0x1E293B);
    private static final Color BARRA_HOVER = new Color(0x172033);
    private static final Color TEXTO = new Color(0x0F172A);
    private static final Color TEXTO_SUAVE = new Color(0x64748B);
    private static final Color BORDE = new Color(0xE2E8F0);
    private static final Color BLANCO = Color.WHITE;
    private static final Color ROJO = new Color(0xDC2626);

    private static final Color AZUL = new Color(0x2563EB);
    private static final Color VERDE = new Color(0x059669);
    private static final Color MORADO = new Color(0x7C3AED);
    private static final Color NARANJA = new Color(0xEA580C);
    private static final Color ROSA = new Color(0xDB2777);
    private static final Color GRIS = new Color(0x475569);

    private static final String SANS = "SansSerif";
    private static final String MONO = "Monospaced";

    // ------------------------------------------------------------ datos de módulos
    private static final class Modulo {
        final String id;
        final String insignia;
        final String titulo;
        final String subtitulo;
        final String descripcion;
        final String pista;
        final Color color;
        final String[][] ejemplos;
        final Function<String, String> accion;

        Modulo(String id, String insignia, String titulo, String subtitulo, String descripcion, String pista,
               Color color, String[][] ejemplos, Function<String, String> accion) {
            this.id = id;
            this.insignia = insignia;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.descripcion = descripcion;
            this.pista = pista;
            this.color = color;
            this.ejemplos = ejemplos;
            this.accion = accion;
        }

        /** Resumen corto para la tarjeta de inicio. */
        String descripcionCorta() {
            switch (id) {
                case "aritmetica": return "Fracciones exactas, raíces, potencias y variables guardadas.";
                case "lineales":   return "Ecuaciones y sistemas con x, y, z. Se separan con ; , o la palabra y.";
                case "resolver":   return "Ecuaciones de 2.º grado y binomios al cubo, con todos los pasos.";
                case "desarrollar": return "Binomio y trinomio al cuadrado y al cubo, con su fórmula.";
                case "factorizar": return "Factor común, cubos, cuadrados, trinomios y agrupación.";
                default:           return descripcion;
            }
        }
    }

    private final EvaluadorAritmetico evaluador = new EvaluadorAritmetico();
    private final SolucionadorAlgebraico lineal = new SolucionadorAlgebraico();
    private final SolucionadorPolinomico polinomico = new SolucionadorPolinomico();
    private final DesarrolladorPolinomios desarrollador = new DesarrolladorPolinomios();
    private final FactorizadorPolinomio factorizador = new FactorizadorPolinomio();

    private final List<Modulo> modulos = new ArrayList<>();
    private final List<ItemMenu> items = new ArrayList<>();
    private final Map<String, PanelModulo> paneles = new HashMap<>();
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);

    public InterfazGrafica() {
        modulos.add(new Modulo("aritmetica", "1/2", "Aritmética", "Fracciones y variables",
                "Escribe una expresión como (5+3)*4-sqrt(9) o 1/2+3/4. Los resultados salen en fracciones exactas. "
                + "Guarda variables con x = 3 y úsalas después. Comandos: variables, borrar.",
                "Expresión, por ejemplo 1/2+3/4  o  x = 3", AZUL,
                Ejemplos.aritmetica(),
                evaluador::procesar));
        modulos.add(new Modulo("lineales", "x,y", "Ecuaciones lineales", "Sistemas con x, y, z",
                "Una o varias incógnitas. Separa varias ecuaciones con  ;  ,  o la palabra  y.  "
                + "Con una sola ecuación y varias letras, usa  para y  para elegir qué letra despejar.",
                "Ecuación o sistema, por ejemplo 2x + y = 7 y x - y = 2", VERDE,
                Ejemplos.lineales(),
                lineal::resolver));
        modulos.add(new Modulo("resolver", "x\u00B2", "Binomios y trinomios", "2.º grado y binomio al cubo",
                "Resuelve ecuaciones de 2.º grado (monomio, binomio y trinomio) con sus pasos, y binomios al cubo "
                + "como x^3 - 8 = 0 o (x+1)^3 = 27. Si omites el signo =, se asume = 0.",
                "Ecuación, por ejemplo x^2 - 5x + 6 = 0", MORADO,
                Ejemplos.resolver(),
                polinomico::resolver));
        modulos.add(new Modulo("desarrollar", "(a+b)\u00B3", "Desarrollar", "Cuadrado y cubo",
                "Desarrolla binomios y trinomios al cuadrado y al cubo con la fórmula del producto notable, "
                + "paso a paso. Los términos pueden tener varias letras.",
                "Expresión, por ejemplo (2x-3y)^2", NARANJA,
                Ejemplos.desarrollar(),
                desarrollador::desarrollar));
        modulos.add(new Modulo("factorizar", "f(x)", "Factorizar", "Binomios, trinomios y más",
                "Factor común, cuadrados y cubos perfectos, diferencia de cuadrados, suma y diferencia de cubos, "
                + "trinomios y agrupación. Al final se comprueba el resultado.",
                "Expresión, por ejemplo x^2 + 5x + 6", ROSA,
                Ejemplos.factorizar(),
                factorizador::factorizar));
    }

    // ------------------------------------------------------------------ arranque

    /** Abre la ventana. */
    public void mostrar() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // se usa el aspecto por defecto
        }
        JFrame ventana = new JFrame("Matematic Calculator Zero");
        ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ventana.setIconImages(iconos());
        iconoBarraTareas();
        ventana.setContentPane(crearRaiz());
        ventana.setSize(1120, 740);
        ventana.setMinimumSize(new Dimension(940, 640));
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    /** Icono de la aplicación: el signo "=" en blanco sobre un cuadrado azul, en varios tamaños. */
    static List<Image> iconos() {
        List<Image> lista = new ArrayList<>();
        for (int t : new int[]{16, 32, 48, 64, 128, 256}) {
            lista.add(dibujarIcono(t));
        }
        return lista;
    }

    /** Icono del Dock (macOS) y de la barra de tareas con la clase Taskbar (Java 9+). En Java 8 se omite. */
    private static void iconoBarraTareas() {
        try {
            Class<?> tb = Class.forName("java.awt.Taskbar");
            Object barra = tb.getMethod("getTaskbar").invoke(null);
            tb.getMethod("setIconImage", Image.class).invoke(barra, dibujarIcono(256));
        } catch (Throwable e) {
            // sin soporte en este sistema: queda el icono de la ventana
        }
    }

    static BufferedImage dibujarIcono(int t) {
        BufferedImage img = new BufferedImage(t, t, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(AZUL);
        g.fillRoundRect(0, 0, t, t, t * 22 / 100, t * 22 / 100);
        g.setColor(BLANCO);
        int ancho = t * 56 / 100;
        int alto = Math.max(2, t * 11 / 100);
        int x = (t - ancho) / 2;
        g.fillRoundRect(x, t * 36 / 100, ancho, alto, alto, alto);
        g.fillRoundRect(x, t * 56 / 100, ancho, alto, alto, alto);
        g.dispose();
        return img;
    }

    /** Construye todo el contenido de la ventana. */
    public JComponent crearRaiz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearBarraLateral(), BorderLayout.WEST);

        contenido.setBackground(FONDO);
        contenido.add(new PanelInicio(), "inicio");
        for (Modulo m : modulos) {
            PanelModulo p = new PanelModulo(m);
            paneles.put(m.id, p);
            contenido.add(p, m.id);
        }
        contenido.add(new PanelAyuda(), "ayuda");
        raiz.add(contenido, BorderLayout.CENTER);
        irA("inicio");
        return raiz;
    }

    /** Cambia de pantalla y marca la opción elegida en el menú. */
    public void irA(String id) {
        for (ItemMenu it : items) {
            it.seleccionado = it.id.equals(id);
            it.repaint();
        }
        cartas.show(contenido, id);
        final PanelModulo p = paneles.get(id);
        if (p != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    p.campo.requestFocusInWindow();
                }
            });
        }
    }

    /** Escribe un texto en un módulo y lo calcula (se usa también para los ejemplos). */
    public void ejecutarEn(String id, String texto) {
        PanelModulo p = paneles.get(id);
        if (p != null) p.ejecutarTexto(texto);
    }

    // ----------------------------------------------------------------- utilidades

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static void textoCentrado(Graphics2D g, String texto, int x, int y, int ancho, int alto) {
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (ancho - fm.stringWidth(texto)) / 2;
        int ty = y + (alto - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(texto, tx, ty);
    }

    private static String recortar(Graphics2D g, String texto, int ancho) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(texto) <= ancho) return texto;
        String r = texto;
        while (r.length() > 1 && fm.stringWidth(r + "\u2026") > ancho) r = r.substring(0, r.length() - 1);
        return r + "\u2026";
    }

    private static int textoEnvuelto(Graphics2D g, String texto, int x, int y, int ancho, int interlinea) {
        FontMetrics fm = g.getFontMetrics();
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
            if (fm.stringWidth(prueba) > ancho && linea.length() > 0) {
                g.drawString(linea.toString(), x, y);
                y += interlinea;
                linea = new StringBuilder(palabra);
            } else {
                linea = new StringBuilder(prueba);
            }
        }
        if (linea.length() > 0) {
            g.drawString(linea.toString(), x, y);
            y += interlinea;
        }
        return y;
    }

    // ------------------------------------------------------------- barra lateral

    private JComponent crearBarraLateral() {
        JPanel p = new JPanel();
        p.setBackground(BARRA);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(272, 0));
        p.setBorder(BorderFactory.createEmptyBorder(22, 16, 18, 16));

        p.add(new Logo());
        p.add(Box.createVerticalStrut(24));
        JLabel menu = new JLabel("MENÚ");
        menu.setFont(new Font(SANS, Font.BOLD, 11));
        menu.setForeground(new Color(0x64748B));
        menu.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 0));
        menu.setAlignmentX(0f);
        p.add(menu);

        agregarItem(p, new ItemMenu("inicio", "0", "Inicio", "Todas las opciones", AZUL, "inicio"));
        for (int i = 0; i < modulos.size(); i++) {
            Modulo m = modulos.get(i);
            agregarItem(p, new ItemMenu(m.id, String.valueOf(i + 1), m.titulo, m.subtitulo, m.color, m.id));
        }
        agregarItem(p, new ItemMenu("ayuda", "6", "Ayuda", "Pasos de cada método", GRIS, "ayuda"));

        p.add(Box.createVerticalGlue());
        ItemMenu salir = new ItemMenu("salir", "7", "Salir", "Cerrar la calculadora", ROJO, null);
        salir.setAccionPersonalizada(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        });
        items.add(salir);
        salir.setAlignmentX(0f);
        p.add(salir);
        return p;
    }

    private void agregarItem(JPanel p, ItemMenu it) {
        items.add(it);
        it.setAlignmentX(0f);
        p.add(it);
        p.add(Box.createVerticalStrut(4));
    }

    /** Logotipo de la parte superior de la barra. */
    private static final class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(240, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            setAlignmentX(0f);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            g.setColor(AZUL);
            g.fillRoundRect(6, 4, 44, 44, 14, 14);
            g.setColor(BLANCO);
            g.setFont(new Font(SANS, Font.BOLD, 26));
            textoCentrado(g, "=", 6, 4, 44, 44);
            g.setFont(new Font(SANS, Font.BOLD, 18));
            g.drawString("Matematic", 62, 24);
            g.setFont(new Font(SANS, Font.PLAIN, 13));
            g.setColor(new Color(0x94A3B8));
            g.drawString("Calculator ", 62, 43);
            int xZero = 62 + g.getFontMetrics().stringWidth("Calculator ");
            g.setFont(new Font(SANS, Font.BOLD, 13));
            g.setColor(new Color(0x60A5FA));
            g.drawString("Zero", xZero, 43);
            g.dispose();
        }
    }

    /** Opción del menú lateral. */
    private final class ItemMenu extends JComponent {
        final String id;
        final String insignia;
        final String titulo;
        final String sub;
        final Color color;
        boolean seleccionado;
        boolean encima;
        private Runnable accion;

        ItemMenu(String id, String insignia, String titulo, String sub, Color color, final String destino) {
            this.id = id;
            this.insignia = insignia;
            this.titulo = titulo;
            this.sub = sub;
            this.color = color;
            if (destino != null) {
                this.accion = new Runnable() {
                    @Override
                    public void run() {
                        irA(destino);
                    }
                };
            }
            setPreferredSize(new Dimension(240, 56));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (ItemMenu.this.accion != null) ItemMenu.this.accion.run();
                }
            });
        }

        void setAccionPersonalizada(Runnable r) {
            this.accion = r;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            int w = getWidth();
            int h = getHeight();
            if (seleccionado) {
                g.setColor(BARRA_SEL);
                g.fillRoundRect(0, 0, w, h, 16, 16);
                g.setColor(color);
                g.fillRoundRect(0, 12, 4, h - 24, 4, 4);
            } else if (encima) {
                g.setColor(BARRA_HOVER);
                g.fillRoundRect(0, 0, w, h, 16, 16);
            }
            g.setColor(color);
            g.fillOval(14, (h - 32) / 2, 32, 32);
            g.setColor(BLANCO);
            g.setFont(new Font(SANS, Font.BOLD, 14));
            textoCentrado(g, insignia, 14, (h - 32) / 2, 32, 32);

            g.setFont(new Font(SANS, Font.BOLD, 14));
            g.setColor(seleccionado ? BLANCO : new Color(0xE2E8F0));
            g.drawString(titulo, 58, h / 2 - 3);
            g.setFont(new Font(SANS, Font.PLAIN, 11));
            g.setColor(new Color(0x94A3B8));
            g.drawString(sub, 58, h / 2 + 13);
            g.dispose();
        }
    }

    // ------------------------------------------------------------ pantalla de inicio

    private final class PanelInicio extends JPanel {
        PanelInicio() {
            super(new BorderLayout(0, 22));
            setBackground(FONDO);
            setBorder(BorderFactory.createEmptyBorder(34, 38, 30, 38));

            JPanel cab = new JPanel();
            cab.setOpaque(false);
            cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
            JLabel t = new JLabel("Matematic Calculator Zero");
            t.setFont(new Font(SANS, Font.BOLD, 30));
            t.setForeground(TEXTO);
            t.setAlignmentX(0f);
            JLabel s = new JLabel("Elige una opción del menú o toca una tarjeta para empezar.");
            s.setFont(new Font(SANS, Font.PLAIN, 15));
            s.setForeground(TEXTO_SUAVE);
            s.setAlignmentX(0f);
            cab.add(t);
            cab.add(Box.createVerticalStrut(6));
            cab.add(s);
            add(cab, BorderLayout.NORTH);

            JPanel rejilla = new JPanel(new GridLayout(2, 3, 20, 20));
            rejilla.setOpaque(false);
            for (Modulo m : modulos) {
                rejilla.add(new Tarjeta(m.id, m.insignia, m.titulo, m.descripcionCorta(), m.ejemplos[1][0], m.ejemplos[1][1], m.color));
            }
            rejilla.add(new Tarjeta("ayuda", "?", "Ayuda", "Pasos y fórmulas de cada método: Shunting Yard, "
                    + "Gauss-Jordan, discriminante, productos notables y más.", "", "", GRIS));
            add(rejilla, BorderLayout.CENTER);
        }
    }

    /** Tarjeta grande de la pantalla de inicio. */
    private final class Tarjeta extends JComponent {
        private final String destino;
        private final String insignia;
        private final String titulo;
        private final String texto;
        private final String ejemplo;
        private final String tipo;
        private final Color color;
        private boolean encima;

        Tarjeta(String destino, String insignia, String titulo, String texto, String ejemplo, String tipo, Color color) {
            this.destino = destino;
            this.insignia = insignia;
            this.titulo = titulo;
            this.texto = texto;
            this.ejemplo = ejemplo;
            this.tipo = tipo;
            this.color = color;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    irA(Tarjeta.this.destino);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            int w = getWidth() - 6;
            int h = getHeight() - 6;
            g.setColor(new Color(15, 23, 42, 14));
            g.fillRoundRect(2, 4, w, h, 24, 24);
            g.setColor(BLANCO);
            g.fillRoundRect(0, 0, w, h, 24, 24);
            g.setColor(encima ? color : BORDE);
            g.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);
            if (encima) g.drawRoundRect(1, 1, w - 3, h - 3, 22, 22);

            g.setColor(color);
            g.fillRoundRect(24, 24, 68, 68, 20, 20);
            g.setColor(BLANCO);
            g.setFont(new Font(SANS, Font.BOLD, insignia.length() > 4 ? 15 : 20));
            textoCentrado(g, insignia, 24, 24, 68, 68);

            g.setColor(TEXTO);
            g.setFont(new Font(SANS, Font.BOLD, 17));
            g.drawString(titulo, 24, 126);

            g.setColor(TEXTO_SUAVE);
            g.setFont(new Font(SANS, Font.PLAIN, 13));
            textoEnvuelto(g, texto, 24, 150, w - 48, 18);

            if (!ejemplo.isEmpty() && h >= 250) {
                int cajaAncho = w - 48;
                g.setColor(new Color(0xF1F5F9));
                g.fillRoundRect(24, h - 80, cajaAncho, 44, 10, 10);
                g.setColor(TEXTO);
                g.setFont(new Font(MONO, Font.PLAIN, 12));
                g.drawString(recortar(g, ejemplo, cajaAncho - 20), 34, h - 62);
                g.setColor(color);
                g.setFont(new Font(SANS, Font.BOLD, 11));
                g.drawString(recortar(g, tipo, cajaAncho - 20), 34, h - 45);
            }
            g.setColor(color);
            g.setFont(new Font(SANS, Font.BOLD, 13));
            g.drawString("Abrir  \u203A", 24, h - 16);
            g.dispose();
        }
    }

    // ------------------------------------------------------------ componentes sueltos

    /** Botón redondeado. */
    private static final class Boton extends JButton {
        private final Color fondo;
        private final boolean contorno;
        private boolean encima;

        Boton(String texto, Color fondo, boolean contorno) {
            super(texto);
            this.fondo = fondo;
            this.contorno = contorno;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(contorno ? fondo : BLANCO);
            setFont(new Font(SANS, Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            int w = getWidth();
            int h = getHeight();
            if (contorno) {
                g.setColor(encima ? new Color(0xF8FAFC) : BLANCO);
                g.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g.setColor(fondo);
                g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            } else {
                g.setColor(encima ? fondo.darker() : fondo);
                g.fillRoundRect(0, 0, w, h, 14, 14);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Ejemplo en que se puede hacer clic: arriba la expresión y abajo su tipo. */
    private static final class Chip extends JButton {
        private boolean encima;
        private final String expresion;
        private final String tipo;
        private final Color color;

        Chip(String expresion, String tipo, Color color) {
            super("");
            this.expresion = expresion;
            this.tipo = tipo;
            this.color = color;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setToolTipText(tipo);
            setPreferredSize(new Dimension(100, 50));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            int w = getWidth();
            int h = getHeight();
            g.setColor(encima ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 22) : BLANCO);
            g.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g.setColor(encima ? color : BORDE);
            g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g.setColor(color);
            g.fillRoundRect(0, 10, 4, h - 20, 4, 4);
            g.setColor(TEXTO);
            g.setFont(new Font(MONO, Font.PLAIN, 13));
            g.drawString(expresion, 16, 21);
            g.setColor(color);
            g.setFont(new Font(SANS, Font.BOLD, 11));
            g.drawString(tipo, 16, 38);
            g.dispose();
        }
    }

    /** Panel blanco de esquinas redondeadas. */
    private static final class Tablero extends JPanel {
        Tablero(BorderLayout l) {
            super(l);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            g.setColor(BLANCO);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g.setColor(BORDE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Campo de texto con un texto de pista cuando está vacío. */
    private static final class Campo extends JTextField {
        private final String pista;

        Campo(String pista) {
            this.pista = pista;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 10));
            setFont(new Font(MONO, Font.PLAIN, 18));
            setForeground(TEXTO);
            setCaretColor(TEXTO);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (getText().isEmpty()) {
                Graphics2D g = suavizar(g0);
                g.setColor(new Color(0x94A3B8));
                g.setFont(new Font(SANS, Font.PLAIN, 15));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(pista, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        }
    }

    /** Insignia cuadrada de color con un símbolo. */
    private static final class Insignia extends JComponent {
        private final String texto;
        private final Color color;

        Insignia(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            setPreferredSize(new Dimension(64, 64));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = suavizar(g0);
            g.setColor(color);
            g.fillRoundRect(0, 0, 62, 62, 18, 18);
            g.setColor(BLANCO);
            g.setFont(new Font(SANS, Font.BOLD, texto.length() > 4 ? 15 : 20));
            textoCentrado(g, texto, 0, 0, 62, 62);
            g.dispose();
        }
    }

    /** Barra de desplazamiento delgada. */
    private static final class BarraFina extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0xCBD5E1);
            trackColor = BLANCO;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return vacio();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return vacio();
        }

        private JButton vacio() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    private static JScrollPane desplazable(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BLANCO);
        sp.getVerticalScrollBar().setUI(new BarraFina());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    private static JPanel cabecera(String texto, String descripcion, Color color, String insignia) {
        JPanel cab = new JPanel(new BorderLayout(16, 0));
        cab.setOpaque(false);
        cab.setAlignmentX(0f);
        cab.add(new Insignia(insignia, color), BorderLayout.WEST);
        JPanel tx = new JPanel();
        tx.setOpaque(false);
        tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(texto);
        t.setFont(new Font(SANS, Font.BOLD, 27));
        t.setForeground(TEXTO);
        t.setAlignmentX(0f);
        JLabel d = new JLabel("<html><body style='width:500px'>" + descripcion + "</body></html>");
        d.setFont(new Font(SANS, Font.PLAIN, 14));
        d.setForeground(TEXTO_SUAVE);
        d.setAlignmentX(0f);
        tx.add(t);
        tx.add(Box.createVerticalStrut(4));
        tx.add(d);
        cab.add(tx, BorderLayout.CENTER);
        return cab;
    }

    // --------------------------------------------------------------- panel de módulo

    private final class PanelModulo extends JPanel {
        final Modulo modulo;
        final Campo campo;
        private final JTextPane salida = new JTextPane();
        private final StyledDocument doc = salida.getStyledDocument();
        private final Style sEntrada;
        private final Style sResultado;
        private final Style sError;
        private final Style sSuave;
        private boolean vacio = true;
        private final List<String> historial = new ArrayList<>();
        private int posicion = 0;

        PanelModulo(final Modulo m) {
            super(new BorderLayout(0, 16));
            this.modulo = m;
            setBackground(FONDO);
            setBorder(BorderFactory.createEmptyBorder(26, 34, 24, 34));

            JPanel arriba = new JPanel();
            arriba.setOpaque(false);
            arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
            arriba.add(cabecera(m.titulo, m.descripcion, m.color, m.insignia));
            arriba.add(Box.createVerticalStrut(18));

            JLabel et = new JLabel("Ejemplos con su tipo (haz clic en uno para probarlo):");
            et.setFont(new Font(SANS, Font.BOLD, 12));
            et.setForeground(TEXTO_SUAVE);
            et.setAlignmentX(0f);
            arriba.add(et);
            arriba.add(Box.createVerticalStrut(8));

            JPanel chips = new JPanel(new GridLayout(0, 2, 10, 8));
            chips.setOpaque(false);
            chips.setAlignmentX(0f);
            for (final String[] ej : m.ejemplos) {
                Chip c = new Chip(ej[0], ej[1], m.color);
                c.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ejecutarTexto(ej[0]);
                    }
                });
                chips.add(c);
            }
            arriba.add(chips);
            arriba.add(Box.createVerticalStrut(18));

            // caja de entrada
            Tablero entrada = new Tablero(new BorderLayout(10, 0));
            entrada.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 10));
            entrada.setAlignmentX(0f);
            campo = new Campo(m.pista);
            entrada.add(campo, BorderLayout.CENTER);
            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            botones.setOpaque(false);
            Boton calcular = new Boton("Calcular", m.color, false);
            Boton limpiar = new Boton("Limpiar", GRIS, true);
            botones.add(limpiar);
            botones.add(calcular);
            entrada.add(botones, BorderLayout.EAST);
            entrada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
            entrada.setPreferredSize(new Dimension(100, 62));
            arriba.add(entrada);
            add(arriba, BorderLayout.NORTH);

            calcular.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ejecutarTexto(campo.getText());
                }
            });
            campo.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ejecutarTexto(campo.getText());
                }
            });
            limpiar.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    campo.setText("");
                    campo.requestFocusInWindow();
                }
            });
            campo.getInputMap().put(KeyStroke.getKeyStroke("UP"), "anterior");
            campo.getActionMap().put("anterior", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (historial.isEmpty()) return;
                    posicion = Math.max(0, posicion - 1);
                    campo.setText(historial.get(posicion));
                }
            });
            campo.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "siguiente");
            campo.getActionMap().put("siguiente", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (historial.isEmpty()) return;
                    posicion = Math.min(historial.size(), posicion + 1);
                    campo.setText(posicion >= historial.size() ? "" : historial.get(posicion));
                }
            });

            // zona de resultados
            Tablero resultados = new Tablero(new BorderLayout());
            resultados.setBorder(BorderFactory.createEmptyBorder(0, 2, 12, 2));
            JPanel encab = new JPanel(new BorderLayout());
            encab.setOpaque(false);
            encab.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 12));
            JLabel rt = new JLabel("Resultados");
            rt.setFont(new Font(SANS, Font.BOLD, 14));
            rt.setForeground(TEXTO);
            encab.add(rt, BorderLayout.WEST);
            Boton borrar = new Boton("Borrar historial", GRIS, true);
            borrar.setFont(new Font(SANS, Font.BOLD, 12));
            borrar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            encab.add(borrar, BorderLayout.EAST);
            resultados.add(encab, BorderLayout.NORTH);

            salida.setEditable(false);
            salida.setBorder(BorderFactory.createEmptyBorder(6, 16, 10, 16));
            salida.setBackground(BLANCO);
            Style base = salida.addStyle("base", null);
            StyleConstants.setFontFamily(base, MONO);
            StyleConstants.setFontSize(base, 15);
            StyleConstants.setForeground(base, TEXTO);
            sResultado = base;
            sEntrada = salida.addStyle("entrada", base);
            StyleConstants.setBold(sEntrada, true);
            StyleConstants.setForeground(sEntrada, m.color);
            sError = salida.addStyle("error", base);
            StyleConstants.setForeground(sError, ROJO);
            sSuave = salida.addStyle("suave", base);
            StyleConstants.setForeground(sSuave, TEXTO_SUAVE);
            StyleConstants.setFontFamily(sSuave, SANS);
            JScrollPane sp = desplazable(salida);
            resultados.add(sp, BorderLayout.CENTER);
            add(resultados, BorderLayout.CENTER);
            borrar.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reiniciarSalida();
                }
            });
            reiniciarSalida();
        }

        private void reiniciarSalida() {
            salida.setText("");
            vacio = true;
            agregar("Aquí aparecerán los resultados.\nEscribe algo arriba y presiona Enter, o haz clic en un ejemplo.", sSuave);
        }

        private void agregar(String texto, Style estilo) {
            try {
                doc.insertString(doc.getLength(), texto, estilo);
                salida.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                // no ocurre: se inserta siempre al final
            }
        }

        void ejecutarTexto(String entrada) {
            String texto = entrada == null ? "" : entrada.trim();
            if (texto.isEmpty()) return;
            if (vacio) {
                salida.setText("");
                vacio = false;
            }
            historial.add(texto);
            posicion = historial.size();
            agregar("> " + texto + "\n", sEntrada);
            try {
                agregar(modulo.accion.apply(texto) + "\n\n", sResultado);
            } catch (IllegalArgumentException e) {
                agregar("Error de sintaxis: " + e.getMessage() + "\n\n", sError);
            } catch (ArithmeticException e) {
                agregar("Error matemático: " + e.getMessage() + "\n\n", sError);
            } catch (Exception e) {
                agregar("Error inesperado: " + e.getMessage() + "\n\n", sError);
            }
            campo.setText("");
            campo.requestFocusInWindow();
        }
    }

    // --------------------------------------------------------------- panel de ayuda

    private final class PanelAyuda extends JPanel {
        private final JTextPane texto = new JTextPane();
        private final List<Boton> botones = new ArrayList<>();

        PanelAyuda() {
            super(new BorderLayout(0, 16));
            setBackground(FONDO);
            setBorder(BorderFactory.createEmptyBorder(26, 34, 24, 34));

            final String[][] temas = {
                    {"Aritmética", Ayuda.aritmetica()},
                    {"Lineales y sistemas", Ayuda.lineal()},
                    {"Binomios y trinomios", Ayuda.resolver()},
                    {"Desarrollar", Ayuda.desarrollar()},
                    {"Factorizar", Ayuda.factorizar()}
            };

            JPanel arriba = new JPanel();
            arriba.setOpaque(false);
            arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
            arriba.add(cabecera("Ayuda", "Elige un tema para ver cómo se resuelve paso a paso, con las fórmulas y ejemplos.",
                    GRIS, "?"));
            arriba.add(Box.createVerticalStrut(16));
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            fila.setOpaque(false);
            fila.setAlignmentX(0f);
            for (int i = 0; i < temas.length; i++) {
                final int idx = i;
                Boton b = new Boton(temas[i][0], GRIS, true);
                b.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mostrarTema(temas[idx][1]);
                    }
                });
                botones.add(b);
                fila.add(b);
                fila.add(Box.createHorizontalStrut(8));
            }
            arriba.add(fila);
            add(arriba, BorderLayout.NORTH);

            Tablero tablero = new Tablero(new BorderLayout());
            tablero.setBorder(BorderFactory.createEmptyBorder(8, 2, 12, 2));
            texto.setEditable(false);
            texto.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            texto.setFont(new Font(MONO, Font.PLAIN, 14));
            texto.setForeground(TEXTO);
            texto.setBackground(BLANCO);
            tablero.add(desplazable(texto), BorderLayout.CENTER);
            add(tablero, BorderLayout.CENTER);
            mostrarTema(temas[0][1]);
        }

        private void mostrarTema(String contenidoTexto) {
            texto.setText(contenidoTexto.replaceFirst("^\\n", ""));
            texto.setCaretPosition(0);
        }
    }
}

// ======================================================================
// Clase: EvaluadorAritmetico
// ======================================================================
/**
 * Evalúa expresiones aritméticas escritas como texto, por ejemplo "(5+3)*4-sqrt(9)" o "1/2+3/4".
 *
 *  - Los resultados son FRACCIONES EXACTAS cuando es posible (1/2 + 3/4 = 5/4); si aparece algo
 *    irracional (sqrt(2), sin, ln...) se continúa con decimales.
 *  - Admite varias variables: se guardan con  x = 3,  y = 2/5,  z = x*y ...
 *
 * Algoritmo en tres fases:
 *   1) tokenizar()       : texto -> lista de tokens (y validación de sintaxis)
 *   2) aPostfija()       : Shunting Yard, notación infija -> postfija (RPN)
 *   3) evaluarPostfija() : evalúa la RPN con una pila
 */
class EvaluadorAritmetico {

    private enum Tipo { NUM, OP, FUNC, ABRE, CIERRA }

    /** Valor exacto (fracción) o aproximado (decimal). */
    private static final class Valor {
        final Fraccion exacto; // null si es aproximado
        final double aprox;

        private Valor(Fraccion exacto, double aprox) {
            this.exacto = exacto;
            this.aprox = aprox;
        }

        static Valor exacto(Fraccion f) { return new Valor(f, 0); }
        static Valor aprox(double d)    { return new Valor(null, d); }
        double aDouble()                { return exacto != null ? exacto.toDouble() : aprox; }
    }

    private static final class Token {
        final Tipo tipo;
        final String texto;
        final Valor valor;

        Token(Tipo tipo, String texto, Valor valor) {
            this.tipo = tipo;
            this.texto = texto;
            this.valor = valor;
        }
    }

    private static final String NEG = "neg"; // menos unario: -5, 2*-3
    private static final Set<String> FUNCIONES =
            new HashSet<>(Arrays.asList("sqrt", "cbrt", "abs", "sin", "cos", "tan", "ln", "log", "exp"));
    private static final Pattern ASIGNACION = Pattern.compile("^([A-Za-z]+)\\s*=\\s*(.+)$");

    private final Map<String, Valor> variables = new LinkedHashMap<>();

    // ------------------------------------------------------------------
    // Punto de entrada para la interfaz
    // ------------------------------------------------------------------

    /** Procesa una línea: expresión, asignación (x = 3) o comando (variables, borrar). */
    public String procesar(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }
        String t = linea.trim();
        String minus = t.toLowerCase();

        if (minus.equals("variables")) {
            if (variables.isEmpty()) return "No hay variables guardadas. Crea una así:  x = 3";
            StringBuilder sb = new StringBuilder("Variables guardadas:");
            for (Map.Entry<String, Valor> e : variables.entrySet()) {
                sb.append("\n  ").append(e.getKey()).append(" = ").append(formatear(e.getValue()));
            }
            return sb.toString();
        }
        if (minus.equals("borrar")) {
            variables.clear();
            return "Se borraron todas las variables.";
        }

        Matcher m = ASIGNACION.matcher(t);
        if (m.matches()) {
            String nombre = m.group(1).toLowerCase();
            if (FUNCIONES.contains(nombre) || nombre.equals("pi") || nombre.equals("e")
                    || nombre.equals("variables") || nombre.equals("borrar")) {
                throw new IllegalArgumentException("'" + nombre + "' es una palabra reservada; elige otro nombre.");
            }
            Valor v = evaluar(m.group(2));
            variables.put(nombre, v);
            return nombre + " = " + formatear(v);
        }
        Valor v = evaluar(t);
        return "Resultado = " + formatear(v) + "\nTipo: " + tipoDe(v);
    }

    private Valor evaluar(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }
        List<Token> tokens = tokenizar(expresion);
        List<Token> postfija = aPostfija(tokens);
        Valor r = evaluarPostfija(postfija);
        double d = r.aDouble();
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new ArithmeticException("El resultado no es un número real finito.");
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Presentación del resultado
    // ------------------------------------------------------------------

    /** Clasifica el resultado: número entero, fracción propia o impropia, o decimal aproximado. */
    private String tipoDe(Valor v) {
        if (v.exacto == null) {
            return "NÚMERO DECIMAL (aproximado, por ejemplo una raíz no exacta)";
        }
        Fraccion f = v.exacto;
        if (f.esEntero()) {
            return "NÚMERO ENTERO";
        }
        boolean impropia = f.numerador().abs().compareTo(f.denominador()) > 0;
        return "FRACCIÓN " + (impropia ? "IMPROPIA" : "PROPIA");
    }

    private String formatear(Valor v) {
        if (v.exacto == null) {
            return Formato.numero(v.aprox);
        }
        Fraccion f = v.exacto;
        if (f.esEntero()) {
            String s = f.toString();
            return s.length() > 40 ? String.format("%.10e", f.toDouble()) : s;
        }
        String s = f.toString();
        if (s.length() > 40) {
            return String.format("%.10e", f.toDouble());
        }
        String dec = (f.esDecimalFinito() ? "= " : "aprox. ") + Formato.numero(f.toDouble());
        boolean impropia = f.numerador().abs().compareTo(f.denominador()) > 0;
        return s + "  (" + (impropia ? f.mixto() + ";  " : "") + dec + ")";
    }

    // ------------------------------------------------------------------
    // FASE 1: tokenización
    // ------------------------------------------------------------------
    private List<Token> tokenizar(String s) {
        List<Token> tokens = new ArrayList<>();
        boolean esperaOperando = true; // true al inicio, tras un operador o tras "("
        int i = 0;
        int n = s.length();

        while (i < n) {
            char c = s.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                if (!esperaOperando) {
                    throw new IllegalArgumentException("Falta un operador antes de un número (posición " + (i + 1) + ").");
                }
                int ini = i;
                int puntos = 0;
                while (i < n && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
                    if (s.charAt(i) == '.') puntos++;
                    i++;
                }
                String num = s.substring(ini, i);
                if (puntos > 1 || num.equals(".")) {
                    throw new IllegalArgumentException("Número mal escrito: '" + num + "'.");
                }
                String limpio = num.startsWith(".") ? "0" + num : num;
                if (limpio.endsWith(".")) limpio = limpio + "0";
                tokens.add(new Token(Tipo.NUM, num, Valor.exacto(Fraccion.desdeTexto(limpio))));
                esperaOperando = false;
            } else if (Character.isLetter(c)) {
                int ini = i;
                while (i < n && Character.isLetter(s.charAt(i))) i++;
                String id = s.substring(ini, i).toLowerCase();

                if (FUNCIONES.contains(id)) {
                    if (!esperaOperando) {
                        throw new IllegalArgumentException("Falta un operador antes de '" + id + "'.");
                    }
                    int j = i;
                    while (j < n && Character.isWhitespace(s.charAt(j))) j++;
                    if (j >= n || s.charAt(j) != '(') {
                        throw new IllegalArgumentException("La función '" + id + "' necesita paréntesis: " + id + "(...).");
                    }
                    tokens.add(new Token(Tipo.FUNC, id, null));
                } else {
                    Valor valor;
                    if (id.equals("pi")) {
                        valor = Valor.aprox(Math.PI);
                    } else if (id.equals("e")) {
                        valor = Valor.aprox(Math.E);
                    } else if (variables.containsKey(id)) {
                        valor = variables.get(id);
                    } else {
                        throw new IllegalArgumentException("'" + id + "' no es una función, constante ni variable con valor. "
                                + "Para crearla escribe, por ejemplo:  " + id + " = 3");
                    }
                    if (!esperaOperando) {
                        throw new IllegalArgumentException("Falta un operador antes de '" + id + "'.");
                    }
                    tokens.add(new Token(Tipo.NUM, id, valor));
                    esperaOperando = false;
                }
            } else if (c == '(') {
                if (!esperaOperando) {
                    throw new IllegalArgumentException("Falta un operador antes de '(' (use * para multiplicar).");
                }
                tokens.add(new Token(Tipo.ABRE, "(", null));
                i++;
            } else if (c == ')') {
                if (esperaOperando) {
                    throw new IllegalArgumentException("Falta un operando antes de ')'.");
                }
                tokens.add(new Token(Tipo.CIERRA, ")", null));
                i++;
            } else if ("+-*/%^".indexOf(c) >= 0) {
                if (esperaOperando) {
                    if (c == '-') {
                        tokens.add(new Token(Tipo.OP, NEG, null)); // menos unario
                    } else if (c != '+') {                          // el + unario se ignora
                        throw new IllegalArgumentException("Falta un operando antes de '" + c + "'.");
                    }
                } else {
                    tokens.add(new Token(Tipo.OP, String.valueOf(c), null));
                    esperaOperando = true;
                }
                i++;
            } else {
                throw new IllegalArgumentException("Carácter no válido: '" + c + "'.");
            }
        }

        if (esperaOperando) {
            throw new IllegalArgumentException("La expresión está incompleta.");
        }
        return tokens;
    }

    // ------------------------------------------------------------------
    // FASE 2: Shunting Yard (infija -> postfija)
    // ------------------------------------------------------------------
    private int precedencia(String op) {
        switch (op) {
            case "+": case "-": return 1;
            case "*": case "/": case "%": return 2;
            case NEG: return 3;
            case "^": return 4;
            default: return 0;
        }
    }

    private boolean asociativoDerecha(String op) {
        return op.equals("^") || op.equals(NEG);
    }

    private List<Token> aPostfija(List<Token> tokens) {
        List<Token> salida = new ArrayList<>();
        Deque<Token> pila = new ArrayDeque<>();

        for (Token tk : tokens) {
            switch (tk.tipo) {
                case NUM:
                    salida.add(tk);
                    break;
                case FUNC:
                case ABRE:
                    pila.push(tk); // las funciones y "(" se apilan sin sacar nada
                    break;
                case OP:
                    if (tk.texto.equals(NEG)) {
                        pila.push(tk);
                        break;
                    }
                    while (!pila.isEmpty() && pila.peek().tipo == Tipo.OP) {
                        Token tope = pila.peek();
                        boolean sacar = precedencia(tope.texto) > precedencia(tk.texto)
                                || (precedencia(tope.texto) == precedencia(tk.texto) && !asociativoDerecha(tk.texto));
                        if (!sacar) break;
                        salida.add(pila.pop());
                    }
                    pila.push(tk);
                    break;
                case CIERRA:
                    while (!pila.isEmpty() && pila.peek().tipo != Tipo.ABRE) {
                        salida.add(pila.pop());
                    }
                    if (pila.isEmpty()) {
                        throw new IllegalArgumentException("Paréntesis ')' sin su '(' correspondiente.");
                    }
                    pila.pop(); // descarta "("
                    if (!pila.isEmpty() && pila.peek().tipo == Tipo.FUNC) {
                        salida.add(pila.pop()); // la función va justo tras sus argumentos
                    }
                    break;
                default:
                    break;
            }
        }

        while (!pila.isEmpty()) {
            Token tope = pila.pop();
            if (tope.tipo == Tipo.ABRE) {
                throw new IllegalArgumentException("Paréntesis '(' sin cerrar.");
            }
            salida.add(tope);
        }
        return salida;
    }

    // ------------------------------------------------------------------
    // FASE 3: evaluación de la notación postfija
    // ------------------------------------------------------------------
    private Valor sacar(Deque<Valor> pila) {
        if (pila.isEmpty()) {
            throw new IllegalArgumentException("Expresión mal formada.");
        }
        return pila.pop();
    }

    private Valor evaluarPostfija(List<Token> postfija) {
        Deque<Valor> pila = new ArrayDeque<>();

        for (Token tk : postfija) {
            if (tk.tipo == Tipo.NUM) {
                pila.push(tk.valor);
            } else if (tk.tipo == Tipo.FUNC) {
                pila.push(aplicarFuncion(tk.texto, sacar(pila)));
            } else if (tk.texto.equals(NEG)) {
                Valor a = sacar(pila);
                pila.push(a.exacto != null ? Valor.exacto(a.exacto.negar()) : Valor.aprox(-a.aprox));
            } else {
                Valor b = sacar(pila);
                Valor a = sacar(pila);
                pila.push(aplicarOperador(tk.texto, a, b));
            }
        }

        if (pila.size() != 1) {
            throw new IllegalArgumentException("Expresión mal formada.");
        }
        return pila.pop();
    }

    private Valor aplicarOperador(String op, Valor a, Valor b) {
        if (a.exacto != null && b.exacto != null) {
            Fraccion x = a.exacto;
            Fraccion y = b.exacto;
            switch (op) {
                case "+": return Valor.exacto(x.sumar(y));
                case "-": return Valor.exacto(x.restar(y));
                case "*": return Valor.exacto(x.multiplicar(y));
                case "/":
                    if (y.isZero()) throw new ArithmeticException("División entre cero.");
                    return Valor.exacto(x.dividir(y));
                case "%": {
                    if (y.isZero()) throw new ArithmeticException("Módulo entre cero.");
                    Fraccion c = x.dividir(y);
                    Fraccion truncado = Fraccion.de(c.numerador().divide(c.denominador()), BigInteger.ONE);
                    return Valor.exacto(x.restar(y.multiplicar(truncado)));
                }
                case "^": return potencia(a, b);
                default: throw new IllegalStateException("Operador desconocido: " + op);
            }
        }
        double x = a.aDouble();
        double y = b.aDouble();
        switch (op) {
            case "+": return Valor.aprox(x + y);
            case "-": return Valor.aprox(x - y);
            case "*": return Valor.aprox(x * y);
            case "/":
                if (y == 0) throw new ArithmeticException("División entre cero.");
                return Valor.aprox(x / y);
            case "%":
                if (y == 0) throw new ArithmeticException("Módulo entre cero.");
                return Valor.aprox(x % y);
            case "^": return potencia(a, b);
            default: throw new IllegalStateException("Operador desconocido: " + op);
        }
    }

    /** Potencia exacta si es posible (exponente entero o raíz exacta); si no, decimal. */
    private Valor potencia(Valor a, Valor b) {
        if (a.exacto != null && b.exacto != null) {
            Fraccion base = a.exacto;
            Fraccion e = b.exacto;
            BigInteger limite = BigInteger.valueOf(1000);
            if (e.esEntero() && e.numerador().abs().compareTo(limite) <= 0) {
                int k = e.numerador().intValue();
                if (base.isZero() && k < 0) {
                    throw new ArithmeticException("División entre cero (0 elevado a una potencia negativa).");
                }
                return Valor.exacto(base.pow(k));
            }
            if (!e.esEntero() && e.denominador().compareTo(BigInteger.valueOf(100)) < 0
                    && e.numerador().abs().compareTo(limite) <= 0) {
                Fraccion raiz = base.raizExacta(e.denominador().intValue());
                if (raiz != null) {
                    int k = e.numerador().intValue();
                    if (!(raiz.isZero() && k < 0)) {
                        return Valor.exacto(raiz.pow(k));
                    }
                }
            }
        }
        double r = Math.pow(a.aDouble(), b.aDouble());
        if (Double.isNaN(r)) {
            throw new ArithmeticException("La potencia no tiene resultado real.");
        }
        return Valor.aprox(r);
    }

    private Valor aplicarFuncion(String f, Valor v) {
        if (v.exacto != null) {
            switch (f) {
                case "abs":
                    return Valor.exacto(v.exacto.abs());
                case "sqrt": {
                    if (v.exacto.signum() < 0) {
                        throw new ArithmeticException("No existe la raíz cuadrada real de un negativo.");
                    }
                    Fraccion r = v.exacto.raizExacta(2);
                    if (r != null) return Valor.exacto(r);
                    break;
                }
                case "cbrt": {
                    Fraccion r = v.exacto.raizExacta(3);
                    if (r != null) return Valor.exacto(r);
                    break;
                }
                default:
                    break;
            }
        }
        double x = v.aDouble();
        switch (f) {
            case "sqrt":
                if (x < 0) throw new ArithmeticException("No existe la raíz cuadrada real de un negativo.");
                return Valor.aprox(Math.sqrt(x));
            case "cbrt": return Valor.aprox(Math.cbrt(x));
            case "abs":  return Valor.aprox(Math.abs(x));
            case "sin":  return Valor.aprox(Math.sin(x)); // radianes
            case "cos":  return Valor.aprox(Math.cos(x)); // radianes
            case "tan":  return Valor.aprox(Math.tan(x)); // radianes
            case "exp":  return Valor.aprox(Math.exp(x));
            case "ln":
                if (x <= 0) throw new ArithmeticException("ln solo está definido para valores mayores que 0.");
                return Valor.aprox(Math.log(x));
            case "log":
                if (x <= 0) throw new ArithmeticException("log solo está definido para valores mayores que 0.");
                return Valor.aprox(Math.log10(x));
            default:
                throw new IllegalStateException("Función desconocida: " + f);
        }
    }
}

// ======================================================================
// Clase: SolucionadorAlgebraico
// ======================================================================
/**
 * Resuelve ecuaciones lineales con una o VARIAS incógnitas (x, y, z, ...) y sistemas de ecuaciones.
 *
 *   2x + 5 = 15                      -> x = 5
 *   2x + 3y = 12                     -> despeja x en función de y  (o "... para y")
 *   x + y = 5 ; x - y = 1            -> sistema: x = 3, y = 2
 *   x + y = 5 y x - y = 1            -> lo mismo (se acepta  ;  ,  y  la palabra "y")
 *
 * Método: cada ecuación se pasa a la forma  a1*x + a2*y + ... = b  y el sistema se resuelve
 * con eliminación de Gauss-Jordan usando fracciones exactas.
 */
class SolucionadorAlgebraico {

    public String resolver(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException("La ecuación está vacía.");
        }
        AnalizadorPolinomio.Entrada en = AnalizadorPolinomio.separarPara(entrada);

        List<String> textos = dividirEcuaciones(en.texto);
        if (textos.isEmpty()) {
            throw new IllegalArgumentException("La ecuación está vacía.");
        }

        List<Polinomio> ecs = new ArrayList<>();
        TreeSet<Character> vars = new TreeSet<>();
        for (String txt : textos) {
            // "a = b = c" se entiende como dos ecuaciones: a = b  y  b = c
            String[] partes = txt.split("=", -1);
            if (partes.length < 2) {
                throw new IllegalArgumentException("Falta el signo '=' en: '" + txt + "'.");
            }
            for (String parte : partes) {
                if (parte.trim().isEmpty()) {
                    throw new IllegalArgumentException("Falta una expresión a un lado del '=' en: '" + txt + "'. "
                            + "Si son varias ecuaciones, sepáralas con  ;  ,  o la palabra  y.");
                }
            }
            for (int i = 0; i + 1 < partes.length; i++) {
                AnalizadorPolinomio an = new AnalizadorPolinomio();
                Polinomio p = an.analizar(partes[i]).restar(an.analizar(partes[i + 1]));
                if (p.grado() > 1) {
                    throw new IllegalArgumentException("La ecuación '" + partes[i].trim() + " = " + partes[i + 1].trim()
                            + "' no es lineal (tiene potencias o productos de incógnitas). Para 2.º grado usa la opción 3.");
                }
                ecs.add(p);
                vars.addAll(p.variables());
            }
        }

        // Orden de columnas: primero las letras pedidas con "para", después el resto en orden alfabético.
        List<Character> orden = new ArrayList<>();
        for (char c : en.prioridad) {
            if (!vars.contains(c)) {
                throw new IllegalArgumentException("La letra '" + c + "' no aparece en las ecuaciones.");
            }
            if (!orden.contains(c)) orden.add(c);
        }
        for (char c : vars) {
            if (!orden.contains(c)) orden.add(c);
        }

        int n = orden.size();
        int m = ecs.size();
        Fraccion[][] mat = new Fraccion[m][n + 1];
        StringBuilder sb = new StringBuilder();
        String incognitas = n + (n == 1 ? " incógnita" : " incógnitas");
        sb.append("Tipo: ").append(m > 1 ? "SISTEMA DE " + m + " ECUACIONES con " + incognitas
                : (n == 0 ? "ECUACIÓN sin incógnitas" : "ECUACIÓN LINEAL con " + incognitas)).append("\n");
        sb.append(m == 1 ? "Forma estándar:\n" : "Sistema en forma estándar (" + m + " ecuaciones, " + n + " incógnitas):\n");
        for (int i = 0; i < m; i++) {
            Polinomio p = ecs.get(i);
            Fraccion cte = p.coeficiente(Monomio.UNO);
            for (int j = 0; j < n; j++) {
                mat[i][j] = p.coeficiente(Monomio.de(orden.get(j), 1));
            }
            mat[i][n] = cte.negar();
            Polinomio izq = p.restar(Polinomio.constante(cte));
            sb.append("  ").append(izq.aTexto()).append(" = ").append(cte.negar()).append("\n");
        }

        if (m > 1) {
            sb.append("Paso 1. Matriz ampliada (coeficientes | términos independientes):\n");
            sb.append(matriz(mat, m, n));
        }

        // ---------- Gauss-Jordan ----------
        int rango = 0;
        int[] colPivote = new int[Math.max(1, Math.min(m, n))];
        for (int col = 0; col < n && rango < m; col++) {
            int piv = -1;
            for (int r = rango; r < m; r++) {
                if (!mat[r][col].isZero()) { piv = r; break; }
            }
            if (piv < 0) continue;
            Fraccion[] tmp = mat[piv]; mat[piv] = mat[rango]; mat[rango] = tmp;
            Fraccion div = mat[rango][col];
            for (int k = 0; k <= n; k++) mat[rango][k] = mat[rango][k].dividir(div);
            for (int r = 0; r < m; r++) {
                if (r != rango && !mat[r][col].isZero()) {
                    Fraccion f = mat[r][col];
                    for (int k = 0; k <= n; k++) {
                        mat[r][k] = mat[r][k].restar(f.multiplicar(mat[rango][k]));
                    }
                }
            }
            colPivote[rango] = col;
            rango++;
        }

        if (m > 1) {
            sb.append("Paso 2. Eliminación de Gauss-Jordan (matriz reducida):\n");
            sb.append(matriz(mat, m, n));
        }

        // ---------- interpretar el resultado ----------
        for (int r = rango; r < m; r++) {
            if (!mat[r][n].isZero()) {
                return sb.append("Sin solución (las ecuaciones se contradicen).").toString();
            }
        }
        if (n == 0) {
            return sb.append("Se cumple siempre (identidad): infinitas soluciones.").toString();
        }

        if (rango == n) {
            if (m == 1) {
                char v = orden.get(0);
                Fraccion coef = ecs.get(0).coeficiente(Monomio.de(v, 1));
                sb.append("Paso 1. Despejar ").append(v).append(": dividir entre el coeficiente de ").append(v)
                  .append(" (").append(coef).append(").\n");
            }
            sb.append("Solución: ");
            for (int i = 0; i < n; i++) {
                if (i > 0) sb.append(",  ");
                sb.append(orden.get(colPivote[i])).append(" = ").append(Formato.exacto(mat[i][n]));
            }
            return sb.toString();
        }

        // rango < n: infinitas soluciones, hay variables libres
        List<Character> libres = new ArrayList<>();
        boolean[] esPivote = new boolean[n];
        for (int i = 0; i < rango; i++) esPivote[colPivote[i]] = true;
        for (int j = 0; j < n; j++) {
            if (!esPivote[j]) libres.add(orden.get(j));
        }
        sb.append(m == 1 ? "Despeje" : "Sistema con infinitas soluciones")
          .append(" (").append(libres.size() == 1 ? "variable libre: " : "variables libres: ")
          .append(unir(libres)).append(" puede valer cualquier número):\n");
        if (rango == 0) {
            sb.append("  Todas las incógnitas son libres (todas las ecuaciones son 0 = 0).");
            return sb.toString();
        }
        for (int i = 0; i < rango; i++) {
            Polinomio expr = Polinomio.constante(mat[i][n]);
            for (int j = 0; j < n; j++) {
                if (!esPivote[j] && !mat[i][j].isZero()) {
                    expr = expr.restar(Polinomio.termino(mat[i][j], Monomio.de(orden.get(j), 1)));
                }
            }
            sb.append("  ").append(orden.get(colPivote[i])).append(" = ").append(expr.aTexto());
            if (i < rango - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private static final Pattern CONJUNCION = Pattern.compile("\\s+(y|and)\\s+", Pattern.CASE_INSENSITIVE);

    /**
     * Separa varias ecuaciones escritas en una misma línea. Acepta  ;  ,  &  y las palabras "y" / "and":
     *   2x + y = 7 y x - y = 2        2x + y = 7, x - y = 2        2x + y = 7 ; x - y = 2
     * La palabra "y" solo se toma como separador si está entre dos ecuaciones (a su izquierda ya hay un '='
     * y a su derecha viene otro '='), así que en  x + y = 5  la letra y sigue siendo una incógnita.
     */
    private List<String> dividirEcuaciones(String texto) {
        List<String> r = new ArrayList<>();
        for (String bloque : texto.split("[;,&\\n]")) {
            if (bloque.trim().isEmpty()) continue;
            Matcher m = CONJUNCION.matcher(bloque);
            int ini = 0;
            while (m.find()) {
                if (m.start() == 0 || m.end() >= bloque.length()) continue;
                if (contarIgual(bloque, ini, m.start()) < 1 || contarIgual(bloque, m.end(), bloque.length()) < 1) continue;
                char antes = bloque.charAt(m.start() - 1);
                char despues = bloque.charAt(m.end());
                boolean terminaOperando = Character.isLetterOrDigit(antes) || antes == ')' || antes == '.';
                boolean empiezaOperando = Character.isLetterOrDigit(despues) || despues == '(' || despues == '.';
                if (!terminaOperando || !empiezaOperando) continue;
                r.add(bloque.substring(ini, m.start()).trim());
                ini = m.end();
            }
            r.add(bloque.substring(ini).trim());
        }
        r.removeIf(String::isEmpty);
        return r;
    }

    private int contarIgual(String s, int desde, int hasta) {
        int c = 0;
        for (int i = desde; i < hasta; i++) {
            if (s.charAt(i) == '=') c++;
        }
        return c;
    }

    private String unir(List<Character> letras) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < letras.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(letras.get(i));
        }
        return sb.toString();
    }

    /** Dibuja la matriz ampliada alineada en columnas. */
    private String matriz(Fraccion[][] mat, int m, int n) {
        int ancho = 1;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= n; j++) ancho = Math.max(ancho, mat[i][j].toString().length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m; i++) {
            sb.append("  [ ");
            for (int j = 0; j <= n; j++) {
                if (j == n) sb.append("| ");
                String c = mat[i][j].toString();
                for (int k = c.length(); k < ancho; k++) sb.append(' ');
                sb.append(c).append(' ');
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}

// ======================================================================
// Clase: SolucionadorPolinomico
// ======================================================================
/**
 * Resuelve ecuaciones con pasos, con una o varias letras:
 *   - 1.er grado
 *   - 2.º grado: MONOMIO (ax^2 = 0), BINOMIO (ax^2 + c = 0  |  ax^2 + bx = 0) y TRINOMIO (fórmula general)
 *   - 3.er grado SOLO si es un BINOMIO AL CUBO:  a(x + h)^3 + k = 0   (por ejemplo x^3 - 8 = 0, (x+1)^3 = 27)
 *
 * Si hay varias letras se despeja una (la de mayor grado, o la pedida con "... para y");
 * las demás se tratan como constantes y la respuesta queda en función de ellas.
 * Si no se escribe el "=", se asume "= 0".
 */
class SolucionadorPolinomico {

    public String resolver(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException("La ecuación está vacía.");
        }
        AnalizadorPolinomio.Entrada en = AnalizadorPolinomio.separarPara(entrada);
        String limpia = en.texto.replaceAll("\\s+", "").toLowerCase();
        String[] partes = limpia.split("=", -1);
        if (partes.length == 1) {
            partes = new String[]{limpia, "0"}; // sin '=': se asume "= 0"
        }
        if (partes.length != 2 || partes[0].isEmpty() || partes[1].isEmpty()) {
            throw new IllegalArgumentException("La ecuación debe tener un solo '=' con expresiones a ambos lados.");
        }

        AnalizadorPolinomio an = new AnalizadorPolinomio();
        Polinomio p = an.analizar(partes[0]).restar(an.analizar(partes[1]));

        StringBuilder sb = new StringBuilder();
        sb.append("Forma estándar: ").append(p.aTexto()).append(" = 0\n");

        if (p.esCero()) {
            return sb.append("Es una identidad (0 = 0): infinitas soluciones.").toString();
        }
        Set<Character> vars = p.variables();
        if (vars.isEmpty()) {
            return sb.append("No hay incógnita y la igualdad es falsa: sin solución.").toString();
        }

        char v = elegirVariable(p, vars, en.prioridad);
        if (vars.size() > 1) {
            sb.append("Se despeja ").append(v).append(" (las demás letras se tratan como constantes; "
                    + "escribe '... para y' para despejar otra).\n");
        }

        int g = p.gradoEn(v);
        Polinomio[] c = new Polinomio[g + 1];
        for (int k = 0; k <= g; k++) c[k] = p.coeficienteEn(v, k);

        switch (g) {
            case 1: lineal(sb, c, v); break;
            case 2: cuadratica(sb, c, v); break;
            case 3: cubicaBinomia(sb, c, v); break;
            default:
                throw new IllegalArgumentException("Solo se resuelven ecuaciones de 1.er y 2.º grado, y de 3.er grado "
                        + "cuando son un binomio al cubo (por ejemplo x^3 - 8 = 0).");
        }
        return sb.toString();
    }

    private char elegirVariable(Polinomio p, Set<Character> vars, List<Character> prioridad) {
        for (char c : prioridad) {
            if (!vars.contains(c)) {
                throw new IllegalArgumentException("La letra '" + c + "' no aparece en la ecuación.");
            }
            return c;
        }
        char mejor = vars.iterator().next();
        for (char c : vars) {
            if (p.gradoEn(c) > p.gradoEn(mejor)) mejor = c;
        }
        return mejor;
    }

    // ------------------------------------------------------------ 1.er grado

    private void lineal(StringBuilder sb, Polinomio[] c, char v) {
        sb.append("Es una ecuación de 1.er grado en ").append(v).append(".\n");
        sb.append("Paso 1. Pasar el término independiente al otro lado:  ")
          .append(Polinomio.termino(Fraccion.UNO, Monomio.de(v, 1)).multiplicar(c[1]).aTexto())
          .append(" = ").append(c[0].negar().aTexto()).append("\n");
        if (c[1].esConstante()) {
            Polinomio sol = c[0].multiplicar(c[1].valorConstante().inverso().negar());
            sb.append("Paso 2. Dividir entre ").append(c[1].aTexto()).append("\n");
            sb.append("Solución: ").append(v).append(" = ").append(valor(sol));
        } else {
            sb.append("Solución: ").append(v).append(" = (").append(c[0].negar().aTexto()).append(") / (")
              .append(c[1].aTexto()).append(")");
        }
    }

    // ------------------------------------------------------------ 2.º grado

    private void cuadratica(StringBuilder sb, Polinomio[] c, char v) {
        if (!c[2].esConstante()) {
            throw new IllegalArgumentException("El coeficiente de " + v + "^2 depende de otras letras; "
                    + "no se admite (debe ser un número).");
        }
        Fraccion a = c[2].valorConstante();
        Polinomio b = c[1];
        Polinomio cc = c[0];

        if (b.esCero() && cc.esCero()) {
            monomio(sb, a, v);
        } else if (b.esCero()) {
            binomioConstante(sb, a, cc, v);
        } else if (cc.esCero()) {
            binomioFactorComun(sb, a, b, v);
        } else {
            trinomio(sb, a, b, cc, v);
        }
    }

    private void monomio(StringBuilder sb, Fraccion a, char v) {
        sb.append("Tipo: MONOMIO  (a").append(v).append("^2 = 0)\n");
        sb.append("Paso 1. Dividir entre a = ").append(a).append(":  ").append(v).append("^2 = 0\n");
        sb.append("Paso 2. Sacar raíz cuadrada:  ").append(v).append(" = 0\n");
        sb.append("Solución: ").append(v).append(" = 0  (raíz doble)");
    }

    /** a v^2 + c = 0 */
    private void binomioConstante(StringBuilder sb, Fraccion a, Polinomio cc, char v) {
        Polinomio q = cc.multiplicar(a.inverso().negar()); // v^2 = -c/a
        sb.append("Tipo: BINOMIO  (a").append(v).append("^2 + c = 0)\n");
        sb.append("Paso 1. Pasar el término independiente al otro lado:  ")
          .append(Polinomio.termino(a, Monomio.de(v, 2)).aTexto()).append(" = ").append(cc.negar().aTexto()).append("\n");
        sb.append("Paso 2. Dividir entre a = ").append(a).append(":  ").append(v).append("^2 = ").append(q.aTexto()).append("\n");

        if (q.esConstante() && q.valorConstante().signum() < 0) {
            Fraccion pos = q.valorConstante().negar();
            Polinomio s = Polinomio.constante(pos).raizExacta(2);
            sb.append("Paso 3. El resultado es negativo: no hay solución real (la raíz cuadrada de un negativo no es real).\n");
            double im = Math.sqrt(pos.toDouble());
            Fraccion imEx = (s == null) ? null : s.valorConstante();
            sb.append("Soluciones complejas: ").append(v).append("1 = ").append(complejo(Fraccion.CERO, imEx, im, false))
              .append(",  ").append(v).append("2 = ").append(complejo(Fraccion.CERO, imEx, im, true));
            return;
        }

        Polinomio s = q.raizExacta(2);
        sb.append("Paso 3. Sacar raíz cuadrada en ambos lados:  ").append(v).append(" = ± raíz(").append(q.aTexto()).append(")\n");
        if (s != null) {
            sb.append("Soluciones: ").append(v).append("1 = ").append(valor(s))
              .append(",  ").append(v).append("2 = ").append(valor(s.negar()));
        } else if (q.esConstante()) {
            double r = Math.sqrt(q.valorConstante().toDouble());
            sb.append("Soluciones: ").append(v).append("1 = ").append(Formato.numero(r))
              .append(",  ").append(v).append("2 = ").append(Formato.numero(-r));
        } else {
            sb.append("Soluciones: ").append(v).append(" = ± raíz(").append(q.aTexto()).append(")  (no se puede simplificar más)");
        }
    }

    /** a v^2 + b v = 0 */
    private void binomioFactorComun(StringBuilder sb, Fraccion a, Polinomio b, char v) {
        Polinomio lineal = Polinomio.termino(a, Monomio.de(v, 1)).sumar(b);
        Polinomio x2 = b.multiplicar(a.inverso().negar());
        sb.append("Tipo: BINOMIO  (a").append(v).append("^2 + b").append(v).append(" = 0)\n");
        sb.append("Paso 1. Sacar factor común ").append(v).append(":  ")
          .append(v).append("(").append(lineal.aTexto()).append(") = 0\n");
        sb.append("Paso 2. Un producto vale 0 si algún factor vale 0:  ")
          .append(v).append(" = 0   ó   ").append(lineal.aTexto()).append(" = 0\n");
        sb.append("Paso 3. Despejar en el segundo factor:  ").append(v).append(" = ").append(valor(x2)).append("\n");
        sb.append("Soluciones: ").append(v).append("1 = 0,  ").append(v).append("2 = ").append(valor(x2));
    }

    /** a v^2 + b v + c = 0 con la fórmula general */
    private void trinomio(StringBuilder sb, Fraccion a, Polinomio b, Polinomio cc, char v) {
        Polinomio d = b.multiplicar(b).restar(cc.multiplicar(a.multiplicar(Fraccion.entero(4))));
        Fraccion dosA = a.multiplicar(Fraccion.entero(2));
        Fraccion inv2a = dosA.inverso();

        sb.append("Tipo: TRINOMIO  (a").append(v).append("^2 + b").append(v).append(" + c = 0)\n");
        sb.append("Paso 1. Identificar coeficientes:  a = ").append(a).append(",  b = ").append(b.aTexto())
          .append(",  c = ").append(cc.aTexto()).append("\n");
        sb.append("Paso 2. Discriminante  D = b^2 - 4ac = ");
        if (b.esConstante() && cc.esConstante()) {
            sb.append(par(b.valorConstante())).append("^2 - 4*").append(par(a)).append("*")
              .append(par(cc.valorConstante())).append(" = ");
        }
        sb.append(d.aTexto()).append("\n");
        sb.append("Paso 3. Fórmula general:  ").append(v).append(" = (-b ± raíz(D)) / (2a)\n");

        Polinomio mb = b.negar();

        if (d.esCero()) {
            sb.append("Paso 4. D = 0: hay una solución doble.\n");
            sb.append("Solución: ").append(v).append(" = ").append(valor(mb.multiplicar(inv2a))).append("  (raíz doble)");
            return;
        }

        Polinomio s = d.raizExacta(2);
        if (s != null) {
            sb.append("Paso 4. raíz(D) = ").append(s.aTexto()).append(d.esConstante() ? "  (D > 0: dos soluciones reales distintas)" : "").append("\n");
            sb.append("Soluciones: ").append(v).append("1 = ").append(valor(mb.sumar(s).multiplicar(inv2a)))
              .append(",  ").append(v).append("2 = ").append(valor(mb.restar(s).multiplicar(inv2a)));
            return;
        }

        if (d.esConstante() && b.esConstante()) {
            double dd = d.valorConstante().toDouble();
            double bd = b.valorConstante().toDouble();
            double ad2 = dosA.toDouble();
            if (dd > 0) {
                double raiz = Math.sqrt(dd);
                sb.append("Paso 4. D > 0: dos soluciones reales distintas.  raíz(D) = ").append(Formato.numero(raiz)).append("\n");
                sb.append("Soluciones: ").append(v).append("1 = ").append(Formato.numero((-bd + raiz) / ad2))
                  .append(",  ").append(v).append("2 = ").append(Formato.numero((-bd - raiz) / ad2));
            } else {
                Fraccion pos = d.valorConstante().negar();
                Polinomio sp = Polinomio.constante(pos).raizExacta(2);
                Fraccion re = b.valorConstante().negar().multiplicar(inv2a);
                double im = Math.sqrt(pos.toDouble()) / Math.abs(ad2);
                Fraccion imEx = (sp == null) ? null : sp.valorConstante().multiplicar(inv2a).abs();
                sb.append("Paso 4. D < 0: no hay soluciones reales; las soluciones son complejas (i = raíz de -1).\n");
                sb.append("Soluciones complejas: ").append(v).append("1 = ").append(complejo(re, imEx, im, false))
                  .append(",  ").append(v).append("2 = ").append(complejo(re, imEx, im, true));
            }
            return;
        }

        sb.append("Paso 4. raíz(D) no se puede simplificar; la respuesta queda indicada:\n");
        sb.append("Soluciones: ").append(v).append(" = (").append(mb.aTexto()).append(" ± raíz(").append(d.aTexto())
          .append(")) / ").append(dosA);
    }

    // ------------------------------------------------- 3.er grado: binomio al cubo

    /** a v^3 + b v^2 + c v + d = 0 solo si es  A (v + h)^3 + K = 0. */
    private void cubicaBinomia(StringBuilder sb, Polinomio[] c, char v) {
        if (!c[3].esConstante()) {
            throw new IllegalArgumentException("El coeficiente de " + v + "^3 depende de otras letras; no se admite.");
        }
        Fraccion a = c[3].valorConstante();
        Polinomio h = c[2].multiplicar(a.multiplicar(Fraccion.entero(3)).inverso());      // h = b / 3a
        Polinomio h2 = h.multiplicar(h);
        Polinomio k = c[0].restar(h2.multiplicar(h).multiplicar(a));                        // K = d - a h^3
        boolean esBinomioCubo = c[1].equals(h2.multiplicar(a.multiplicar(Fraccion.entero(3))));
        if (!esBinomioCubo) {
            throw new IllegalArgumentException("Esa ecuación de 3.er grado no es un binomio al cubo. Solo se resuelven las de la forma "
                    + "a(" + v + " + h)^3 + k = 0, por ejemplo  " + v + "^3 - 8 = 0  o  (" + v + "+1)^3 = 27.");
        }

        Polinomio base = Polinomio.variable(v).sumar(h);
        Polinomio q = k.multiplicar(a.inverso().negar()); // (v + h)^3 = -K / a

        sb.append("Tipo: ECUACIÓN BINOMIA DE 3.er GRADO (binomio al cubo)\n");
        sb.append("Paso 1. Reconocer el binomio al cubo:  ").append(a.isOne() ? "" : a + "*")
          .append(base.aTextoPar()).append("^3").append(sumaTexto(k)).append(" = 0\n");
        sb.append("Paso 2. Despejar el cubo:  ").append(base.aTextoPar()).append("^3 = ").append(q.aTexto()).append("\n");

        Polinomio r = q.raizExacta(3);
        sb.append("Paso 3. Sacar raíz cúbica en ambos lados:  ").append(base.aTexto()).append(" = ");
        if (r != null) {
            sb.append(r.aTexto()).append("\n");
        } else if (q.esConstante()) {
            sb.append("raíz cúbica de ").append(q.aTexto()).append(" = ").append(Formato.numero(Math.cbrt(q.valorConstante().toDouble()))).append("\n");
        } else {
            sb.append("raíz cúbica de (").append(q.aTexto()).append(")\n");
        }

        Polinomio mh = h.negar();
        if (r != null) {
            sb.append("Paso 4. Despejar ").append(v).append(":  ").append(v).append(" = ").append(mh.sumar(r).aTexto()).append("\n");
        }

        // raíz real
        if (r != null) {
            sb.append("Solución real: ").append(v).append(" = ").append(valor(mh.sumar(r)));
        } else if (q.esConstante() && h.esConstante()) {
            sb.append("Solución real: ").append(v).append(" = ")
              .append(Formato.numero(Math.cbrt(q.valorConstante().toDouble()) - h.valorConstante().toDouble()));
        } else {
            sb.append("Solución real: ").append(v).append(" = ").append(mh.esCero() ? "" : mh.aTextoPar() + " + ")
              .append("raíz cúbica de (").append(q.aTexto()).append(")");
        }

        // las otras dos raíces (complejas)
        if (q.esCero()) {
            sb.append("  (raíz triple)");
        } else if (q.esConstante() && h.esConstante()) {
            double rd = (r != null) ? r.valorConstante().toDouble() : Math.cbrt(q.valorConstante().toDouble());
            double hd = h.valorConstante().toDouble();
            Fraccion re = (r != null) ? h.valorConstante().negar().sumar(r.valorConstante().multiplicar(Fraccion.de(-1, 2))) : null;
            double reD = -hd - rd / 2.0;
            double im = Math.abs(rd) * Math.sqrt(3) / 2.0;
            sb.append("\nSoluciones complejas: ").append(v).append("2 = ")
              .append(complejoRe(re, reD, im, false)).append(",  ").append(v).append("3 = ")
              .append(complejoRe(re, reD, im, true));
        } else {
            sb.append("\nAdemás hay dos soluciones complejas:  ").append(v).append(" = ").append(mh.esCero() ? "" : mh.aTextoPar() + " + ")
              .append("r*(-1/2 ± (raíz(3)/2) i),  donde r es la raíz cúbica de (").append(q.aTexto()).append(")");
        }
    }

    // ------------------------------------------------------------- ayudas

    /** " + k" o " - k" para escribir una suma de forma natural. */
    private String sumaTexto(Polinomio k) {
        if (k.esCero()) return "";
        if (k.numTerminos() == 1 && k.terminoLider().getValue().signum() < 0) {
            return " - " + k.negar().aTexto();
        }
        return " + " + k.aTextoPar();
    }

    private String par(Fraccion f) {
        return f.signum() < 0 ? "(" + f + ")" : f.toString();
    }

    /** Valor de un polinomio: si es número muestra también su decimal. */
    private String valor(Polinomio p) {
        return p.esConstante() ? Formato.exacto(p.valorConstante()) : p.aTexto();
    }

    /** re ± im*i, con im positivo (magnitud); neg indica la raíz conjugada (resta). */
    private String complejo(Fraccion re, Fraccion imExacto, double imDouble, boolean neg) {
        String im;
        if (imExacto != null) {
            im = imExacto.isOne() ? "i" : (imExacto.esEntero() ? imExacto + "i" : "(" + imExacto + ")i");
        } else {
            im = Formato.numero(imDouble) + "i";
        }
        if (re.isZero()) return (neg ? "-" : "") + im;
        return re + (neg ? " - " : " + ") + im;
    }

    private String complejoRe(Fraccion reExacta, double reD, double im, boolean neg) {
        String re = (reExacta != null) ? reExacta.toString() : Formato.numero(reD);
        String imTxt = Formato.numero(im) + "i";
        if (Math.abs(reD) < 1e-12) return (neg ? "-" : "") + imTxt;
        return re + (neg ? " - " : " + ") + imTxt;
    }
}

// ======================================================================
// Clase: DesarrolladorPolinomios
// ======================================================================
/**
 * Desarrolla binomios y trinomios elevados al cuadrado y al cubo con las fórmulas de productos notables:
 *
 *   (a + b)^2     = a^2 + 2ab + b^2              (a - b)^2 = a^2 - 2ab + b^2
 *   (a + b)^3     = a^3 + 3a^2b + 3ab^2 + b^3    (a - b)^3 = a^3 - 3a^2b + 3ab^2 - b^3
 *   (a + b + c)^2 = a^2 + b^2 + c^2 + 2ab + 2ac + 2bc
 *   (a + b + c)^3 = a^3 + b^3 + c^3 + 3a^2b + 3a^2c + 3ab^2 + 3b^2c + 3ac^2 + 3bc^2 + 6abc
 *
 * Los términos pueden tener varias letras: (2x - 3y)^3, (x + y + z)^2, (a^2 + b)^3...
 * Cualquier otra expresión (por ejemplo (x+y)^4 o (x+1)(x+2)) se desarrolla multiplicando directamente.
 */
class DesarrolladorPolinomios {

    private static final Pattern POTENCIA =
            Pattern.compile("^\\((.+)\\)(?:\\^(\\d{1,2})|(\u00B2)|(\u00B3))$");

    // Cada fila: {coeficiente, exponente de a, exponente de b, exponente de c}
    private static final int[][] BINOMIO_2 = {{1, 2, 0}, {2, 1, 1}, {1, 0, 2}};
    private static final int[][] BINOMIO_3 = {{1, 3, 0}, {3, 2, 1}, {3, 1, 2}, {1, 0, 3}};
    private static final int[][] TRINOMIO_2 = {
            {1, 2, 0, 0}, {1, 0, 2, 0}, {1, 0, 0, 2}, {2, 1, 1, 0}, {2, 1, 0, 1}, {2, 0, 1, 1}};
    private static final int[][] TRINOMIO_3 = {
            {1, 3, 0, 0}, {1, 0, 3, 0}, {1, 0, 0, 3},
            {3, 2, 1, 0}, {3, 2, 0, 1}, {3, 1, 2, 0}, {3, 0, 2, 1}, {3, 1, 0, 2}, {3, 0, 1, 2}, {6, 1, 1, 1}};

    public String desarrollar(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }
        if (entrada.contains("=")) {
            throw new IllegalArgumentException("Escribe solo la expresión, sin '='. Ejemplo: (x+y)^3");
        }
        String s = entrada.replaceAll("\\s+", "").toLowerCase();
        AnalizadorPolinomio an = new AnalizadorPolinomio();
        Polinomio directo = an.analizar(s); // valida la sintaxis y calcula el desarrollo directo

        Matcher m = POTENCIA.matcher(s);
        if (m.matches() && indiceCierre(s) == m.end(1)) {
            int n = (m.group(2) != null) ? Integer.parseInt(m.group(2)) : (m.group(3) != null ? 2 : 3);
            List<String> textos = dividirTerminos(m.group(1));
            if ((n == 2 || n == 3) && (textos.size() == 2 || textos.size() == 3)) {
                return conFormula(an, textos, n, directo);
            }
        }
        return "Desarrollo directo (multiplicando):\nResultado: " + directo.aTexto()
                + "\n(Los pasos con fórmula están disponibles para binomios y trinomios al cuadrado y al cubo.)";
    }

    // ------------------------------------------------------------------
    // Desarrollo con la fórmula del producto notable
    // ------------------------------------------------------------------

    private String conFormula(AnalizadorPolinomio an, List<String> textos, int n, Polinomio directo) {
        int k = textos.size();
        Polinomio[] t = new Polinomio[k];
        for (int i = 0; i < k; i++) t[i] = an.analizar(textos.get(i));

        // En un binomio con resta se usa la fórmula (a - b): b se muestra en positivo.
        boolean resta = (k == 2) && textos.get(1).startsWith("-");
        Polinomio[] mostrar = t.clone();
        if (resta) mostrar[1] = t[1].negar();

        int[][] tabla = (k == 2) ? (n == 2 ? BINOMIO_2 : BINOMIO_3) : (n == 2 ? TRINOMIO_2 : TRINOMIO_3);
        String letras = "abc";

        StringBuilder sb = new StringBuilder();
        sb.append("Tipo: ").append(k == 2 ? "BINOMIO" : "TRINOMIO").append(" AL ").append(n == 2 ? "CUADRADO" : "CUBO").append("\n");

        // Fórmula
        sb.append("Fórmula:  (").append(base(k, resta)).append(")^").append(n).append(" = ")
          .append(unirTerminos(tabla, k, resta, null)).append("\n");

        // Paso 1: identificar
        sb.append("Paso 1. Identificar:  ");
        for (int i = 0; i < k; i++) {
            if (i > 0) sb.append(",  ");
            sb.append(letras.charAt(i)).append(" = ").append(mostrar[i].aTexto());
        }
        if (resta) sb.append("   (la fórmula con resta usa b en positivo)");
        sb.append("\n");

        // Paso 2: sustituir
        sb.append("Paso 2. Sustituir en la fórmula:  ").append(unirTerminos(tabla, k, resta, mostrar)).append("\n");

        // Paso 3: calcular cada término (con a, b, c con su signo)
        List<Polinomio> valores = new ArrayList<>();
        Polinomio suma = Polinomio.CERO;
        for (int[] fila : tabla) {
            Polinomio v = Polinomio.constante(Fraccion.entero(fila[0]));
            for (int i = 0; i < k; i++) v = v.multiplicar(t[i].potencia(fila[i + 1]));
            valores.add(v);
            suma = suma.sumar(v);
        }
        if (!suma.equals(directo)) {
            throw new IllegalStateException("El desarrollo con la fórmula no coincide con la multiplicación directa.");
        }
        String paso3 = sumaTexto(valores);
        sb.append("Paso 3. Calcular cada término:  ").append(paso3).append("\n");
        if (!paso3.equals(suma.aTexto())) {
            sb.append("Paso 4. Reducir términos semejantes:  ").append(suma.aTexto()).append("\n");
        }
        sb.append("Resultado: ").append(suma.aTexto());
        return sb.toString();
    }

    private String base(int k, boolean resta) {
        if (k == 2) return resta ? "a - b" : "a + b";
        return "a + b + c";
    }

    /** Escribe los términos de la fórmula con letras (sust == null) o con las expresiones sustituidas. */
    private String unirTerminos(int[][] tabla, int k, boolean resta, Polinomio[] sust) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < tabla.length; idx++) {
            int[] f = tabla[idx];
            boolean negativo = resta && (f[2] % 2 == 1);
            if (idx > 0) sb.append(negativo ? " - " : " + ");
            else if (negativo) sb.append("-");

            List<String> factores = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                int e = f[i + 1];
                if (e == 0) continue;
                String x = (sust == null) ? String.valueOf("abc".charAt(i)) : "(" + sust[i].aTexto() + ")";
                factores.add(e == 1 ? x : x + "^" + e);
            }
            if (sust == null) {
                if (f[0] != 1) sb.append(f[0]);
                for (String x : factores) sb.append(x);
            } else {
                List<String> todos = new ArrayList<>();
                if (f[0] != 1) todos.add(String.valueOf(f[0]));
                todos.addAll(factores);
                sb.append(String.join("*", todos));
            }
        }
        return sb.toString();
    }

    /** Suma de polinomios escrita de forma natural: 4x^2 - 12xy + 9y^2. */
    private String sumaTexto(List<Polinomio> valores) {
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (Polinomio v : valores) {
            if (v.esCero()) continue;
            boolean unSoloNegativo = v.numTerminos() == 1 && v.terminoLider().getValue().signum() < 0;
            if (primero) {
                sb.append(v.aTextoPar());
            } else if (unSoloNegativo) {
                sb.append(" - ").append(v.negar().aTexto());
            } else {
                sb.append(" + ").append(v.aTextoPar());
            }
            primero = false;
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    // ------------------------------------------------------------------
    // Análisis del texto
    // ------------------------------------------------------------------

    /** Posición del ')' que cierra el '(' inicial. */
    private int indiceCierre(String s) {
        int prof = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') prof++;
            else if (s.charAt(i) == ')') {
                prof--;
                if (prof == 0) return i;
            }
        }
        return -1;
    }

    /** Divide "2x-3y+z" en ["2x", "-3y", "z"] respetando los paréntesis. */
    private List<String> dividirTerminos(String inner) {
        List<String> r = new ArrayList<>();
        int prof = 0;
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '(') prof++;
            if (c == ')') prof--;
            boolean separador = (c == '+' || c == '-') && prof == 0 && i > 0 && inner.charAt(i - 1) != '^';
            if (separador) {
                r.add(actual.toString());
                actual.setLength(0);
                if (c == '-') actual.append('-');
            } else if (!(c == '+' && prof == 0 && i == 0)) {
                actual.append(c);
            }
        }
        r.add(actual.toString());
        r.removeIf(String::isEmpty);
        return r;
    }
}

// ======================================================================
// Clase: FactorizadorPolinomio
// ======================================================================
/**
 * Factoriza binomios, trinomios y polinomios (una o varias letras) mostrando los pasos:
 *   1) Factor común (número y letras)
 *   2) Binomio / trinomio AL CUADRADO o AL CUBO perfecto:  (a + b)^2,  (a + b)^3,  (a + b + c)^3 ...
 *   3) Diferencia de cuadrados                  a^2 - b^2 = (a + b)(a - b)
 *   4) Suma y diferencia de cubos               a^3 ± b^3 = (a ± b)(a^2 ∓ ab + b^2)
 *   5) Trinomio de 2.º grado con el discriminante (también con varias letras: x^2 + 5xy + 6y^2)
 *   6) Raíces racionales para polinomios de una letra y grado 3 o más
 * Al final se multiplica todo para comprobar que se recupera la expresión original.
 */
class FactorizadorPolinomio {

    private StringBuilder sb;
    private int paso;
    private Fraccion extra; // constante sobrante que producen algunos métodos

    public String factorizar(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }
        if (expresion.contains("=")) {
            throw new IllegalArgumentException("Escribe solo la expresión, sin '='. Para resolver una ecuación usa la opción 3.");
        }
        Polinomio p = new AnalizadorPolinomio().analizar(expresion);
        if (p.esCero()) {
            return "La expresión vale 0.";
        }

        sb = new StringBuilder();
        paso = 1;
        extra = Fraccion.UNO;

        int nt = p.numTerminos();
        sb.append("Forma estándar: ").append(p.aTexto()).append("\n");
        sb.append("Tipo: ").append(nt == 1 ? "MONOMIO" : nt == 2 ? "BINOMIO" : nt == 3 ? "TRINOMIO" : "POLINOMIO de " + nt + " términos")
          .append("\n");
        if (nt == 1) {
            return sb.append("Un monomio ya está factorizado.").toString();
        }

        // ---------- factor común ----------
        Fraccion cont = p.contenido();
        Monomio mon = p.mcdMonomios();
        Fraccion c = p.terminoLider().getValue().signum() < 0 ? cont.negar() : cont;
        Polinomio q = p.dividirPorTermino(c, mon);
        boolean hayComun = !(c.isOne() && mon.esConstante());
        if (hayComun) {
            paso("Factor común:  " + describirComun(c, mon));
            paso("Dentro del paréntesis queda:  " + q.aTexto());
        } else {
            paso("Factor común: no hay.");
        }

        List<Polinomio> factores = new ArrayList<>();
        rec(q, factores, 0);

        // ---------- resultado y comprobación ----------
        Fraccion coef = c.multiplicar(extra);
        Map<Polinomio, Integer> mult = new LinkedHashMap<>();
        for (Polinomio f : factores) {
            mult.merge(f, 1, Integer::sum);
        }

        Polinomio producto = Polinomio.termino(coef, mon);
        StringBuilder res = new StringBuilder(prefijo(coef, mon));
        for (Map.Entry<Polinomio, Integer> e : mult.entrySet()) {
            producto = producto.multiplicar(e.getKey().potencia(e.getValue()));
            res.append(e.getKey().aTextoPar());
            if (e.getValue() > 1) res.append("^").append(e.getValue());
        }

        boolean sinCambio = !hayComun && mult.size() == 1 && mult.containsKey(q) && mult.get(q) == 1;
        if (sinCambio) {
            sb.append("Resultado: no se pudo factorizar con los métodos disponibles (puede ser irreducible).");
            return sb.toString();
        }
        sb.append("Resultado: ").append(res).append("\n");
        if (producto.equals(p)) {
            sb.append("Comprobación: al multiplicar los factores se obtiene la expresión original.");
        } else {
            sb.append("Aviso: no se pudo comprobar la factorización; revísala con cuidado.");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Búsqueda recursiva de factores
    // ------------------------------------------------------------------

    private void rec(Polinomio q, List<Polinomio> out, int prof) {
        if (q.numTerminos() == 1 || q.grado() <= 1 || prof > 8) {
            out.add(q);
            return;
        }
        if (potenciaPerfecta(q, out, prof)) return;
        if (q.numTerminos() == 2 && (diferenciaCuadrados(q, out, prof) || cubos(q, out, prof))) return;
        if (cuadratico(q, out, prof)) return;
        if (q.numTerminos() == 4 && agrupacion(q, out, prof)) return;
        if (raicesRacionales(q, out, prof)) return;

        paso("(" + q.aTexto() + ") ya no se puede factorizar con los métodos disponibles.");
        out.add(q);
    }

    /** q = s^2 o q = s^3 (binomio/trinomio al cuadrado o al cubo). */
    private boolean potenciaPerfecta(Polinomio q, List<Polinomio> out, int prof) {
        for (int n = 3; n >= 2; n--) {
            Polinomio s = q.raizExacta(n);
            if (s == null || s.numTerminos() < 2) continue;
            String tipo = s.numTerminos() == 2 ? "binomio" : s.numTerminos() == 3 ? "trinomio" : "polinomio";
            paso("Es un " + tipo + " AL " + (n == 2 ? "CUADRADO" : "CUBO") + " (desarrollo de la fórmula):  "
                    + formula(n, s.numTerminos()));
            paso("Por lo tanto:  " + q.aTexto() + " = (" + s.aTexto() + ")^" + n);
            List<Polinomio> sub = new ArrayList<>();
            rec(s, sub, prof + 1);
            for (int i = 0; i < n; i++) out.addAll(sub);
            return true;
        }
        return false;
    }

    private String formula(int n, int terminos) {
        if (terminos == 2 && n == 2) return "a^2 ± 2ab + b^2 = (a ± b)^2";
        if (terminos == 2 && n == 3) return "a^3 ± 3a^2b + 3ab^2 ± b^3 = (a ± b)^3";
        if (terminos == 3 && n == 2) return "a^2 + b^2 + c^2 + 2ab + 2ac + 2bc = (a + b + c)^2";
        if (terminos == 3 && n == 3) return "(a + b + c)^3 = a^3 + b^3 + c^3 + 3a^2b + 3a^2c + 3ab^2 + 3b^2c + 3ac^2 + 3bc^2 + 6abc";
        return "(a + b + ...)^" + n;
    }

    /** a^2 - b^2 = (a + b)(a - b) */
    private boolean diferenciaCuadrados(Polinomio q, List<Polinomio> out, int prof) {
        Iterator<Map.Entry<Monomio, Fraccion>> it = q.terminos().iterator();
        Map.Entry<Monomio, Fraccion> e1 = it.next();
        Map.Entry<Monomio, Fraccion> e2 = it.next();
        if (e2.getValue().signum() > 0) return false;
        Polinomio a = Polinomio.termino(e1.getValue(), e1.getKey()).raizExacta(2);
        Polinomio b = Polinomio.termino(e2.getValue().negar(), e2.getKey()).raizExacta(2);
        if (a == null || b == null) return false;
        paso("Es una DIFERENCIA DE CUADRADOS:  a^2 - b^2 = (a + b)(a - b),  con a = " + a.aTexto() + "  y  b = " + b.aTexto());
        rec(a.sumar(b), out, prof + 1);
        rec(a.restar(b), out, prof + 1);
        return true;
    }

    /** a^3 + b^3 = (a + b)(a^2 - ab + b^2)   y   a^3 - b^3 = (a - b)(a^2 + ab + b^2) */
    private boolean cubos(Polinomio q, List<Polinomio> out, int prof) {
        Iterator<Map.Entry<Monomio, Fraccion>> it = q.terminos().iterator();
        Map.Entry<Monomio, Fraccion> e1 = it.next();
        Map.Entry<Monomio, Fraccion> e2 = it.next();
        Polinomio a = Polinomio.termino(e1.getValue(), e1.getKey()).raizExacta(3);
        Polinomio b = Polinomio.termino(e2.getValue(), e2.getKey()).raizExacta(3);
        if (a == null || b == null) return false;
        boolean resta = e2.getValue().signum() < 0;
        Polinomio bPos = resta ? b.negar() : b;
        paso(resta
                ? "Es una DIFERENCIA DE CUBOS:  a^3 - b^3 = (a - b)(a^2 + ab + b^2),  con a = " + a.aTexto() + "  y  b = " + bPos.aTexto()
                : "Es una SUMA DE CUBOS:  a^3 + b^3 = (a + b)(a^2 - ab + b^2),  con a = " + a.aTexto() + "  y  b = " + bPos.aTexto());
        Polinomio f1 = a.sumar(b);
        Polinomio f2 = a.multiplicar(a).restar(a.multiplicar(b)).sumar(b.multiplicar(b));
        rec(f1, out, prof + 1);
        rec(f2, out, prof + 1);
        return true;
    }

    /** Trinomio (o binomio) de 2.º grado en alguna letra, resuelto con el discriminante. */
    private boolean cuadratico(Polinomio q, List<Polinomio> out, int prof) {
        for (char v : q.variables()) {
            if (q.gradoEn(v) != 2) continue;
            Polinomio ca = q.coeficienteEn(v, 2);
            if (!ca.esConstante()) continue;

            Fraccion a = ca.valorConstante();
            Polinomio b = q.coeficienteEn(v, 1);
            Polinomio c = q.coeficienteEn(v, 0);
            Polinomio d = b.multiplicar(b).restar(c.multiplicar(a.multiplicar(Fraccion.entero(4))));

            paso("Tratar como ecuación de 2.º grado en " + v + ":  a = " + a + ",  b = " + b.aTexto() + ",  c = " + c.aTexto()
                    + ";  D = b^2 - 4ac = " + d.aTexto());
            Polinomio s = d.raizExacta(2);
            if (s == null) {
                paso("D no es un cuadrado perfecto: no se factoriza con coeficientes racionales (irreducible):  " + q.aTexto());
                out.add(q);
                return true;
            }
            Fraccion inv2a = a.multiplicar(Fraccion.entero(2)).inverso();
            Polinomio r1 = b.negar().sumar(s).multiplicar(inv2a);
            Polinomio r2 = b.negar().restar(s).multiplicar(inv2a);
            paso("raíz(D) = " + s.aTexto() + ";  raíces:  " + v + "1 = " + r1.aTexto() + ",  " + v + "2 = " + r2.aTexto());

            Fraccion d1 = denominadorComun(r1);
            Fraccion d2 = denominadorComun(r2);
            Polinomio f1 = Polinomio.termino(d1, Monomio.de(v, 1)).restar(r1.multiplicar(d1));
            Polinomio f2 = Polinomio.termino(d2, Monomio.de(v, 1)).restar(r2.multiplicar(d2));
            extra = extra.multiplicar(a.dividir(d1.multiplicar(d2)));
            paso("Cada raíz r da el factor (" + v + " - r) (se multiplica por el denominador para quitar fracciones):  ("
                    + f1.aTexto() + ")(" + f2.aTexto() + ")");
            rec(f1, out, prof + 1);
            rec(f2, out, prof + 1);
            return true;
        }
        return false;
    }

    private Fraccion denominadorComun(Polinomio r) {
        BigInteger l = BigInteger.ONE;
        for (Map.Entry<Monomio, Fraccion> e : r.terminos()) {
            BigInteger d = e.getValue().denominador();
            l = l.multiply(d).divide(l.gcd(d));
        }
        return Fraccion.de(l, BigInteger.ONE);
    }

    /** Factorización por agrupación de 4 términos:  xy + x + y + 1 = x(y + 1) + (y + 1) = (x + 1)(y + 1). */
    private boolean agrupacion(Polinomio q, List<Polinomio> out, int prof) {
        List<Polinomio> t = new ArrayList<>();
        for (Map.Entry<Monomio, Fraccion> e : q.terminos()) t.add(Polinomio.termino(e.getValue(), e.getKey()));
        int[][] particiones = {{0, 1, 2, 3}, {0, 2, 1, 3}, {0, 3, 1, 2}};
        for (int[] pt : particiones) {
            Polinomio a = t.get(pt[0]).sumar(t.get(pt[1]));
            Polinomio b = t.get(pt[2]).sumar(t.get(pt[3]));
            if (a.numTerminos() < 2 || b.numTerminos() < 2) continue;
            Fraccion ca = signoLider(a);
            Fraccion cb = signoLider(b);
            Monomio ma = a.mcdMonomios();
            Monomio mb = b.mcdMonomios();
            Polinomio ra = a.dividirPorTermino(ca, ma);
            Polinomio rb = b.dividirPorTermino(cb, mb);
            Polinomio ga = Polinomio.termino(ca, ma);
            Polinomio gb = Polinomio.termino(cb, mb);
            Polinomio f2;
            if (ra.equals(rb)) f2 = ga.sumar(gb);
            else if (ra.equals(rb.negar())) f2 = ga.restar(gb);
            else continue;
            if (ra.grado() < 1 || f2.grado() < 1 || !ra.multiplicar(f2).equals(q)) continue;
            paso("FACTORIZACIÓN POR AGRUPACIÓN: (" + a.aTexto() + ") + (" + b.aTexto() + ") = " + ga.aTextoPar() + ra.aTextoPar()
                    + (ra.equals(rb) ? " + " : " - ") + gb.aTextoPar() + rb.aTextoPar() + " = (" + ra.aTexto() + ")(" + f2.aTexto() + ")");
            rec(ra, out, prof + 1);
            rec(f2, out, prof + 1);
            return true;
        }
        return false;
    }

    /** Contenido de p con el signo de su término líder (para sacar factor común positivo al inicio). */
    private Fraccion signoLider(Polinomio p) {
        Fraccion c = p.contenido();
        return p.terminoLider().getValue().signum() < 0 ? c.negar() : c;
    }

    /** Polinomios de una sola letra y grado >= 3: busca raíces racionales ±p/q. */
    private boolean raicesRacionales(Polinomio q, List<Polinomio> out, int prof) {
        Set<Character> vars = q.variables();
        if (vars.size() != 1) return false;
        char v = vars.iterator().next();
        int d = q.gradoEn(v);
        if (d < 3) return false;

        Fraccion[] co = new Fraccion[d + 1];
        for (int k = 0; k <= d; k++) {
            co[k] = q.coeficienteEn(v, k).valorConstante();
            if (!co[k].esEntero()) return false;
        }
        if (co[0].isZero()) return false;
        BigInteger a0 = co[0].numerador().abs();
        BigInteger ad = co[d].numerador().abs();
        if (a0.bitLength() > 40 || ad.bitLength() > 40) return false;

        for (long p : divisores(a0.longValue())) {
            for (long qq : divisores(ad.longValue())) {
                for (int signo = 1; signo >= -1; signo -= 2) {
                    Fraccion r = Fraccion.de(signo * p, qq);
                    Fraccion h = co[d];
                    for (int k = d - 1; k >= 0; k--) h = h.multiplicar(r).sumar(co[k]);
                    if (!h.isZero()) continue;

                    // división sintética entre (v - r): q = (v - r) * cociente
                    Fraccion[] b = new Fraccion[d];
                    b[d - 1] = co[d];
                    for (int k = d - 1; k >= 1; k--) b[k - 1] = co[k].sumar(r.multiplicar(b[k]));
                    Fraccion den = Fraccion.de(r.denominador(), BigInteger.ONE);
                    Polinomio f1 = Polinomio.termino(den, Monomio.de(v, 1)).sumar(Polinomio.constante(r.numerador().signum() == 0
                            ? Fraccion.CERO : Fraccion.de(r.numerador().negate(), BigInteger.ONE)));
                    Polinomio f2 = Polinomio.CERO;
                    for (int k = 0; k < d; k++) {
                        f2 = f2.sumar(Polinomio.termino(b[k].dividir(den), Monomio.de(v, k)));
                    }
                    if (!f1.multiplicar(f2).equals(q)) continue;

                    paso("Raíces racionales: se prueban los números ±p/q (p divide a " + a0 + ", q divide a " + ad + "):  "
                            + v + " = " + r + " es raíz  →  factor (" + f1.aTexto() + ")");
                    out.add(f1);
                    rec(f2, out, prof + 1);
                    return true;
                }
            }
        }
        return false;
    }

    private List<Long> divisores(long n) {
        List<Long> r = new ArrayList<>();
        for (long i = 1; i * i <= n; i++) {
            if (n % i == 0) {
                r.add(i);
                if (i != n / i) r.add(n / i);
            }
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Texto
    // ------------------------------------------------------------------

    private void paso(String texto) {
        sb.append("Paso ").append(paso++).append(". ").append(texto).append("\n");
    }

    private String describirComun(Fraccion c, Monomio mon) {
        String coef;
        if (c.isOne()) coef = "";
        else if (c.equals(Fraccion.UNO.negar()) && !mon.esConstante()) coef = "-";
        else coef = c.toString();
        return coef + mon.aTexto();
    }

    /** Parte numérica y literal que va delante de los paréntesis. */
    private String prefijo(Fraccion c, Monomio mon) {
        String m = mon.aTexto();
        if (c.isOne()) return m;
        if (c.equals(Fraccion.UNO.negar())) return "-" + m;
        if (c.esEntero()) return c + m;
        return "(" + c + ")" + m;
    }
}

// ======================================================================
// Clase: AnalizadorPolinomio
// ======================================================================
/**
 * Convierte texto en un Polinomio con una o varias incógnitas (x, y, z, ...).
 * Acepta: 2x^2 - 3xy + y^2,  (x+y)^3,  (x+2)(x+3),  x/2,  0.5x,  x²,  x³.
 *
 * Gramática (recursiva descendente):
 *   expresion := termino (('+'|'-') termino)*
 *   termino   := factor (('*'|'/')? factor)*      // permite 2x, xy y 3(x+1)
 *   factor    := ('+'|'-') factor | potencia
 *   potencia  := primario ('^' entero | '²' | '³')?
 *   primario  := numero | letra | '(' expresion ')'
 */
class AnalizadorPolinomio {

    /** Texto ya separado del sufijo opcional "para x" (incógnita preferida). */
    public static final class Entrada {
        public final String texto;
        public final List<Character> prioridad;

        Entrada(String texto, List<Character> prioridad) {
            this.texto = texto;
            this.prioridad = prioridad;
        }
    }

    /** Separa "2x + 3y = 12 para y" en el texto y la lista de letras preferidas. */
    public static Entrada separarPara(String s) {
        String minus = s.toLowerCase();
        int idx = minus.lastIndexOf(" para ");
        List<Character> prio = new ArrayList<>();
        if (idx < 0) {
            return new Entrada(s.trim(), prio);
        }
        String resto = minus.substring(idx + 6).trim();
        if (resto.isEmpty()) {
            throw new IllegalArgumentException("Después de 'para' escribe la letra de la incógnita (por ejemplo: para y).");
        }
        for (String tk : resto.split("[\\s,;]+")) {
            if (tk.length() != 1 || !Character.isLetter(tk.charAt(0))) {
                throw new IllegalArgumentException("Después de 'para' escribe solo letras sueltas, por ejemplo: para x   o   para y, z.");
            }
            prio.add(tk.charAt(0));
        }
        return new Entrada(s.substring(0, idx).trim(), prio);
    }

    private String s;
    private int pos;
    private final TreeSet<Character> variables = new TreeSet<>();

    /** Letras encontradas en todo lo analizado con este objeto. */
    public Set<Character> getVariables() {
        return variables;
    }

    public Polinomio analizar(String texto) {
        s = texto.replaceAll("\\s+", "").toLowerCase();
        pos = 0;
        if (s.isEmpty()) {
            throw new IllegalArgumentException("La expresión está vacía.");
        }
        Polinomio r = expresion();
        if (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == ')') {
                throw new IllegalArgumentException("Paréntesis ')' sin su '(' correspondiente.");
            }
            throw new IllegalArgumentException("Carácter inesperado: '" + c + "'.");
        }
        return r;
    }

    private Polinomio expresion() {
        Polinomio r = termino();
        while (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
            char op = s.charAt(pos++);
            Polinomio t = termino();
            r = (op == '+') ? r.sumar(t) : r.restar(t);
        }
        return r;
    }

    private Polinomio termino() {
        Polinomio r = factor();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == '*') {
                pos++;
                r = r.multiplicar(factor());
            } else if (c == '/') {
                pos++;
                r = r.dividir(factor());
            } else if (Character.isDigit(c) || c == '.' || Character.isLetter(c) || c == '(') {
                r = r.multiplicar(factor()); // multiplicación implícita
            } else {
                break;
            }
        }
        return r;
    }

    private Polinomio factor() {
        if (pos >= s.length()) {
            throw new IllegalArgumentException("La expresión está incompleta.");
        }
        char c = s.charAt(pos);
        if (c == '-') { pos++; return factor().negar(); }
        if (c == '+') { pos++; return factor(); }
        return potencia();
    }

    private Polinomio potencia() {
        Polinomio base = primario();
        if (pos < s.length() && s.charAt(pos) == '^') {
            pos++;
            int ini = pos;
            while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
            if (ini == pos) {
                throw new IllegalArgumentException("El exponente debe ser un entero positivo (por ejemplo x^2).");
            }
            String digitos = s.substring(ini, pos);
            if (digitos.length() > 2 || Integer.parseInt(digitos) > 12) {
                throw new IllegalArgumentException("Exponente demasiado grande (el máximo permitido es 12).");
            }
            return base.potencia(Integer.parseInt(digitos));
        }
        if (pos < s.length() && s.charAt(pos) == '\u00B2') { // ²
            pos++;
            return base.potencia(2);
        }
        if (pos < s.length() && s.charAt(pos) == '\u00B3') { // ³
            pos++;
            return base.potencia(3);
        }
        return base;
    }

    private Polinomio primario() {
        char c = s.charAt(pos);

        if (c == '(') {
            pos++;
            Polinomio r = expresion();
            if (pos >= s.length() || s.charAt(pos) != ')') {
                throw new IllegalArgumentException("Falta cerrar un paréntesis.");
            }
            pos++;
            return r;
        }
        if (Character.isDigit(c) || c == '.') {
            int ini = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
            String num = s.substring(ini, pos);
            try {
                if (num.startsWith(".")) num = "0" + num;
                if (num.endsWith(".")) num = num + "0";
                return Polinomio.constante(Fraccion.desdeTexto(num));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Número mal escrito: '" + s.substring(ini, pos) + "'.");
            }
        }
        if (Character.isLetter(c)) {
            pos++;
            variables.add(c);
            if (pos < s.length() && Character.isDigit(s.charAt(pos))) {
                throw new IllegalArgumentException("Después de '" + c + "' no puede ir un número directo. "
                        + "¿Quiso escribir " + c + "^2?");
            }
            return Polinomio.variable(c);
        }
        throw new IllegalArgumentException("Carácter inesperado: '" + c + "'.");
    }
}

// ======================================================================
// Clase: Polinomio
// ======================================================================
/**
 * Polinomio en varias variables (x, y, z, ...) con coeficientes racionales exactos.
 * Es inmutable: cada operación devuelve un polinomio nuevo.
 */
final class Polinomio {

    private final TreeMap<Monomio, Fraccion> t; // términos ordenados de mayor a menor

    public static final Polinomio CERO = new Polinomio(new TreeMap<Monomio, Fraccion>());
    public static final Polinomio UNO = constante(Fraccion.UNO);

    private Polinomio(TreeMap<Monomio, Fraccion> t) {
        this.t = t;
    }

    // ---------------------------------------------------------- creación

    public static Polinomio constante(Fraccion c) { return termino(c, Monomio.UNO); }

    public static Polinomio variable(char v) { return termino(Fraccion.UNO, Monomio.de(v, 1)); }

    public static Polinomio termino(Fraccion c, Monomio m) {
        TreeMap<Monomio, Fraccion> r = new TreeMap<>();
        if (!c.isZero()) r.put(m, c);
        return new Polinomio(r);
    }

    // ------------------------------------------------------------ consulta

    public boolean esCero()      { return t.isEmpty(); }
    public int numTerminos()     { return t.size(); }

    public Set<Map.Entry<Monomio, Fraccion>> terminos() {
        return Collections.unmodifiableMap(t).entrySet();
    }

    /** Término de mayor orden (primero al escribir el polinomio). */
    public Map.Entry<Monomio, Fraccion> terminoLider() { return t.firstEntry(); }

    public Fraccion coeficiente(Monomio m) {
        Fraccion c = t.get(m);
        return c == null ? Fraccion.CERO : c;
    }

    public boolean esConstante() {
        return t.isEmpty() || (t.size() == 1 && t.firstKey().esConstante());
    }

    public Fraccion valorConstante() {
        return t.isEmpty() ? Fraccion.CERO : t.firstEntry().getValue();
    }

    /** Grado total (suma de exponentes del término mayor). */
    public int grado() { return t.isEmpty() ? 0 : t.firstKey().grado(); }

    public int gradoEn(char v) {
        int g = 0;
        for (Monomio m : t.keySet()) g = Math.max(g, m.gradoEn(v));
        return g;
    }

    public TreeSet<Character> variables() {
        TreeSet<Character> r = new TreeSet<>();
        for (Monomio m : t.keySet()) r.addAll(m.variables());
        return r;
    }

    /** Coeficiente de v^k, como polinomio en las demás variables. */
    public Polinomio coeficienteEn(char v, int k) {
        TreeMap<Monomio, Fraccion> r = new TreeMap<>();
        for (Map.Entry<Monomio, Fraccion> e : t.entrySet()) {
            if (e.getKey().gradoEn(v) == k) {
                r.put(e.getKey().sinVariable(v), e.getValue());
            }
        }
        return new Polinomio(r);
    }

    // ----------------------------------------------------------- operaciones

    public Polinomio sumar(Polinomio o) {
        TreeMap<Monomio, Fraccion> r = new TreeMap<>(t);
        for (Map.Entry<Monomio, Fraccion> e : o.t.entrySet()) {
            Fraccion previo = r.get(e.getKey());
            Fraccion s = previo == null ? e.getValue() : previo.sumar(e.getValue());
            if (s.isZero()) r.remove(e.getKey());
            else r.put(e.getKey(), s);
        }
        return new Polinomio(r);
    }

    public Polinomio negar() {
        TreeMap<Monomio, Fraccion> r = new TreeMap<>();
        for (Map.Entry<Monomio, Fraccion> e : t.entrySet()) r.put(e.getKey(), e.getValue().negar());
        return new Polinomio(r);
    }

    public Polinomio restar(Polinomio o) { return sumar(o.negar()); }

    public Polinomio multiplicar(Fraccion k) {
        if (k.isZero()) return CERO;
        TreeMap<Monomio, Fraccion> r = new TreeMap<>();
        for (Map.Entry<Monomio, Fraccion> e : t.entrySet()) r.put(e.getKey(), e.getValue().multiplicar(k));
        return new Polinomio(r);
    }

    public Polinomio multiplicar(Polinomio o) {
        TreeMap<Monomio, Fraccion> r = new TreeMap<>();
        for (Map.Entry<Monomio, Fraccion> a : t.entrySet()) {
            for (Map.Entry<Monomio, Fraccion> b : o.t.entrySet()) {
                Monomio m = a.getKey().multiplicar(b.getKey());
                Fraccion c = a.getValue().multiplicar(b.getValue());
                Fraccion previo = r.get(m);
                if (previo != null) c = previo.sumar(c);
                if (c.isZero()) r.remove(m);
                else r.put(m, c);
            }
        }
        return new Polinomio(r);
    }

    public Polinomio potencia(int n) {
        Polinomio r = UNO;
        for (int i = 0; i < n; i++) r = r.multiplicar(this);
        return r;
    }

    /** División entre un número (el divisor debe ser constante y distinto de cero). */
    public Polinomio dividir(Polinomio o) {
        if (!o.esConstante()) {
            throw new IllegalArgumentException("Solo se puede dividir entre números (no entre letras).");
        }
        if (o.esCero()) {
            throw new ArithmeticException("División entre cero.");
        }
        return multiplicar(o.valorConstante().inverso());
    }

    // --------------------------------------------------- para factorizar

    /** Contenido racional positivo: mcd de numeradores / mcm de denominadores. */
    public Fraccion contenido() {
        BigInteger g = BigInteger.ZERO;
        BigInteger l = BigInteger.ONE;
        for (Fraccion c : t.values()) {
            g = g.gcd(c.numerador().abs());
            BigInteger d = c.denominador();
            l = l.multiply(d).divide(l.gcd(d));
        }
        if (g.signum() == 0) return Fraccion.UNO;
        return Fraccion.de(g, l);
    }

    /** Monomio que divide a todos los términos (factor común literal). */
    public Monomio mcdMonomios() {
        Monomio r = null;
        for (Monomio m : t.keySet()) r = (r == null) ? m : r.mcd(m);
        return r == null ? Monomio.UNO : r;
    }

    /** Divide cada término entre c*m (se supone que m divide a todos). */
    public Polinomio dividirPorTermino(Fraccion c, Monomio m) {
        TreeMap<Monomio, Fraccion> r = new TreeMap<>();
        for (Map.Entry<Monomio, Fraccion> e : t.entrySet()) {
            r.put(e.getKey().dividir(m), e.getValue().dividir(c));
        }
        return new Polinomio(r);
    }

    /**
     * Raíz n-ésima exacta del polinomio (si existe un polinomio s con s^n = this) o null.
     * Se construye término a término: el primer término es la raíz del término mayor y cada
     * término siguiente sale de dividir el residuo entre n*(primer término)^(n-1).
     */
    public Polinomio raizExacta(int n) {
        if (t.isEmpty()) return CERO;
        Map.Entry<Monomio, Fraccion> lt = t.firstEntry();
        Fraccion rc = lt.getValue().raizExacta(n);
        Monomio rm = lt.getKey().raizExacta(n);
        if (rc == null || rm == null) return null;

        Polinomio s = termino(rc, rm);
        Fraccion divC = rc.pow(n - 1).multiplicar(Fraccion.entero(n));
        Monomio divM = rm.pow(n - 1);

        for (int i = 0; i < 60; i++) {
            Polinomio resto = restar(s.potencia(n));
            if (resto.esCero()) return s;
            Map.Entry<Monomio, Fraccion> lr = resto.t.firstEntry();
            Monomio q = lr.getKey().dividir(divM);
            if (q == null) return null;
            s = s.sumar(termino(lr.getValue().dividir(divC), q));
            if (s.numTerminos() > 40) return null;
        }
        return null;
    }

    // ---------------------------------------------------------------- texto

    @Override
    public boolean equals(Object o) {
        return o instanceof Polinomio && t.equals(((Polinomio) o).t);
    }

    @Override
    public int hashCode() { return t.hashCode(); }

    /** Forma estándar: "2x^2 - 3xy + (1/2)y^2 - 5". */
    public String aTexto() {
        if (t.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (Map.Entry<Monomio, Fraccion> e : t.entrySet()) {
            Fraccion c = e.getValue();
            boolean neg = c.signum() < 0;
            if (primero) {
                if (neg) sb.append("-");
            } else {
                sb.append(neg ? " - " : " + ");
            }
            primero = false;
            sb.append(textoTermino(c.abs(), e.getKey()));
        }
        return sb.toString();
    }

    /** Igual que aTexto pero entre paréntesis si tiene más de un término. */
    public String aTextoPar() {
        return t.size() > 1 ? "(" + aTexto() + ")" : aTexto();
    }

    private static String textoTermino(Fraccion abs, Monomio m) {
        if (m.esConstante()) return abs.toString();
        String mt = m.aTexto();
        if (abs.isOne()) return mt;
        if (abs.esEntero()) return abs + mt;
        return "(" + abs + ")" + mt;
    }
}

// ======================================================================
// Clase: Monomio
// ======================================================================
/**
 * Producto de letras con exponentes (sin coeficiente), por ejemplo x^2*y.
 * Se ordenan de mayor a menor grado y, a igual grado, por orden alfabético (grlex).
 */
final class Monomio implements Comparable<Monomio> {

    public static final Monomio UNO = new Monomio(new TreeMap<Character, Integer>());

    private final TreeMap<Character, Integer> exp;

    private Monomio(TreeMap<Character, Integer> exp) {
        this.exp = exp;
    }

    public static Monomio de(char v, int e) {
        TreeMap<Character, Integer> m = new TreeMap<>();
        if (e != 0) m.put(v, e);
        return new Monomio(m);
    }

    public int grado() {
        int g = 0;
        for (int e : exp.values()) g += e;
        return g;
    }

    public int gradoEn(char v) {
        Integer e = exp.get(v);
        return e == null ? 0 : e;
    }

    public boolean esConstante() { return exp.isEmpty(); }

    public Set<Character> variables() { return Collections.unmodifiableSet(exp.keySet()); }

    public Monomio multiplicar(Monomio o) {
        TreeMap<Character, Integer> r = new TreeMap<>(exp);
        for (Map.Entry<Character, Integer> e : o.exp.entrySet()) {
            r.merge(e.getKey(), e.getValue(), Integer::sum);
        }
        return new Monomio(r);
    }

    /** this / o, o null si no es divisible. */
    public Monomio dividir(Monomio o) {
        TreeMap<Character, Integer> r = new TreeMap<>(exp);
        for (Map.Entry<Character, Integer> e : o.exp.entrySet()) {
            int d = gradoEn(e.getKey()) - e.getValue();
            if (d < 0) return null;
            if (d == 0) r.remove(e.getKey());
            else r.put(e.getKey(), d);
        }
        return new Monomio(r);
    }

    public Monomio pow(int n) {
        TreeMap<Character, Integer> r = new TreeMap<>();
        if (n != 0) {
            for (Map.Entry<Character, Integer> e : exp.entrySet()) r.put(e.getKey(), e.getValue() * n);
        }
        return new Monomio(r);
    }

    /** Raíz n-ésima exacta o null. */
    public Monomio raizExacta(int n) {
        TreeMap<Character, Integer> r = new TreeMap<>();
        for (Map.Entry<Character, Integer> e : exp.entrySet()) {
            if (e.getValue() % n != 0) return null;
            r.put(e.getKey(), e.getValue() / n);
        }
        return new Monomio(r);
    }

    /** Máximo común divisor de dos monomios (mínimo exponente de cada letra). */
    public Monomio mcd(Monomio o) {
        TreeMap<Character, Integer> r = new TreeMap<>();
        for (Map.Entry<Character, Integer> e : exp.entrySet()) {
            int m = Math.min(e.getValue(), o.gradoEn(e.getKey()));
            if (m > 0) r.put(e.getKey(), m);
        }
        return new Monomio(r);
    }

    public Monomio sinVariable(char v) {
        TreeMap<Character, Integer> r = new TreeMap<>(exp);
        r.remove(v);
        return new Monomio(r);
    }

    @Override
    public int compareTo(Monomio o) {
        int d = o.grado() - grado();
        if (d != 0) return d;
        TreeSet<Character> todas = new TreeSet<>(exp.keySet());
        todas.addAll(o.exp.keySet());
        for (char c : todas) {
            int a = gradoEn(c);
            int b = o.gradoEn(c);
            if (a != b) return b - a;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Monomio && exp.equals(((Monomio) o).exp);
    }

    @Override
    public int hashCode() { return exp.hashCode(); }

    /** Texto sin coeficiente: "x^2y". Vacío si es constante. */
    public String aTexto() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, Integer> e : exp.entrySet()) {
            sb.append(e.getKey());
            if (e.getValue() > 1) sb.append("^").append(e.getValue());
        }
        return sb.toString();
    }
}

// ======================================================================
// Clase: Fraccion
// ======================================================================
/**
 * Número racional exacto (numerador/denominador) siempre reducido y con denominador positivo.
 * Permite trabajar con fracciones sin errores de redondeo: 1/3 + 1/6 = 1/2.
 */
final class Fraccion implements Comparable<Fraccion> {

    public static final Fraccion CERO = new Fraccion(BigInteger.ZERO, BigInteger.ONE);
    public static final Fraccion UNO = new Fraccion(BigInteger.ONE, BigInteger.ONE);

    private final BigInteger num;
    private final BigInteger den;

    private Fraccion(BigInteger num, BigInteger den) {
        this.num = num;
        this.den = den;
    }

    public static Fraccion de(BigInteger n, BigInteger d) {
        if (d.signum() == 0) {
            throw new ArithmeticException("División entre cero.");
        }
        if (d.signum() < 0) {
            n = n.negate();
            d = d.negate();
        }
        BigInteger g = n.gcd(d);
        if (g.signum() != 0 && !g.equals(BigInteger.ONE)) {
            n = n.divide(g);
            d = d.divide(g);
        }
        return new Fraccion(n, d);
    }

    public static Fraccion de(long n, long d) {
        return de(BigInteger.valueOf(n), BigInteger.valueOf(d));
    }

    public static Fraccion entero(long n) {
        return new Fraccion(BigInteger.valueOf(n), BigInteger.ONE);
    }

    /** Convierte un decimal escrito en texto ("0.75") en fracción exacta (3/4). */
    public static Fraccion desdeTexto(String s) {
        BigDecimal bd = new BigDecimal(s);
        BigInteger sinEscala = bd.unscaledValue();
        int escala = bd.scale();
        if (escala >= 0) {
            return de(sinEscala, BigInteger.TEN.pow(escala));
        }
        return de(sinEscala.multiply(BigInteger.TEN.pow(-escala)), BigInteger.ONE);
    }

    public BigInteger numerador()   { return num; }
    public BigInteger denominador() { return den; }

    public int signum()        { return num.signum(); }
    public boolean isZero()    { return num.signum() == 0; }
    public boolean isOne()     { return num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE); }
    public boolean esEntero()  { return den.equals(BigInteger.ONE); }

    public Fraccion sumar(Fraccion o) {
        return de(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den));
    }

    public Fraccion restar(Fraccion o) {
        return de(num.multiply(o.den).subtract(o.num.multiply(den)), den.multiply(o.den));
    }

    public Fraccion multiplicar(Fraccion o) {
        return de(num.multiply(o.num), den.multiply(o.den));
    }

    public Fraccion dividir(Fraccion o) {
        if (o.isZero()) {
            throw new ArithmeticException("División entre cero.");
        }
        return de(num.multiply(o.den), den.multiply(o.num));
    }

    public Fraccion negar()   { return new Fraccion(num.negate(), den); }
    public Fraccion abs()     { return new Fraccion(num.abs(), den); }
    public Fraccion inverso() { return de(den, num); }

    public Fraccion pow(int k) {
        if (k >= 0) {
            return new Fraccion(num.pow(k), den.pow(k)); // sigue reducida
        }
        if (isZero()) {
            throw new ArithmeticException("División entre cero.");
        }
        return inverso().pow(-k);
    }

    /** Raíz n-ésima exacta (si existe en los racionales) o null. */
    public Fraccion raizExacta(int n) {
        if (signum() < 0 && n % 2 == 0) {
            return null;
        }
        BigInteger rn = raizEntera(num.abs(), n);
        BigInteger rd = raizEntera(den, n);
        if (rn == null || rd == null) {
            return null;
        }
        return new Fraccion(signum() < 0 ? rn.negate() : rn, rd);
    }

    private static BigInteger raizEntera(BigInteger x, int n) {
        if (x.signum() == 0 || x.equals(BigInteger.ONE)) {
            return x;
        }
        BigInteger lo = BigInteger.ONE;
        BigInteger hi = BigInteger.ONE.shiftLeft(x.bitLength() / n + 1);
        while (lo.compareTo(hi) <= 0) {
            BigInteger mid = lo.add(hi).shiftRight(1);
            int cmp = mid.pow(n).compareTo(x);
            if (cmp == 0) return mid;
            if (cmp < 0) lo = mid.add(BigInteger.ONE);
            else hi = mid.subtract(BigInteger.ONE);
        }
        return null;
    }

    public double toDouble() {
        return new BigDecimal(num).divide(new BigDecimal(den), MathContext.DECIMAL128).doubleValue();
    }

    /** true si el decimal termina (denominador solo con factores 2 y 5), por ejemplo 3/4 = 0.75. */
    public boolean esDecimalFinito() {
        BigInteger d = den;
        BigInteger dos = BigInteger.valueOf(2);
        BigInteger cinco = BigInteger.valueOf(5);
        while (d.mod(dos).signum() == 0) d = d.divide(dos);
        while (d.mod(cinco).signum() == 0) d = d.divide(cinco);
        return d.equals(BigInteger.ONE);
    }

    /** Número mixto: 7/4 -> "1 3/4". */
    public String mixto() {
        BigInteger[] qr = num.abs().divideAndRemainder(den);
        String signo = signum() < 0 ? "-" : "";
        if (qr[1].signum() == 0) return signo + qr[0];
        if (qr[0].signum() == 0) return toString();
        return signo + qr[0] + " " + qr[1] + "/" + den;
    }

    @Override
    public int compareTo(Fraccion o) {
        return num.multiply(o.den).compareTo(o.num.multiply(den));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Fraccion && num.equals(((Fraccion) o).num) && den.equals(((Fraccion) o).den);
    }

    @Override
    public int hashCode() {
        return num.hashCode() * 31 + den.hashCode();
    }

    @Override
    public String toString() {
        return den.equals(BigInteger.ONE) ? num.toString() : num + "/" + den;
    }
}

// ======================================================================
// Clase: Formato
// ======================================================================
/**
 * Utilidades para mostrar números de forma limpia:
 * 5.0 -> "5", 0.30000000000000004 -> "0.3", 3/2 -> "3/2 (= 1.5)".
 */
final class Formato {

    private Formato() { }

    public static String numero(double valor) {
        if (Math.abs(valor - Math.rint(valor)) < 1e-12 && Math.abs(valor) < 1e15) {
            return String.valueOf((long) Math.rint(valor));
        }
        return new BigDecimal(valor)
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    /** Fracción exacta seguida de su valor decimal cuando no es entera: "3/2 (= 1.5)". */
    public static String exacto(Fraccion f) {
        if (f.esEntero()) {
            return f.toString();
        }
        String texto = f.toString();
        if (texto.length() > 40) {
            return String.format("%.10e", f.toDouble());
        }
        return texto + " (" + (f.esDecimalFinito() ? "= " : "aprox. ") + numero(f.toDouble()) + ")";
    }
}

// ======================================================================
// Clase: Ayuda
// ======================================================================
/**
 * Textos de ayuda (pasos y fórmulas de cada método). Los usan la consola y la ventana gráfica.
 */
final class Ayuda {

    private Ayuda() { }

    private static String unir(String... lineas) {
        return String.join("\n", lineas);
    }

    public static String aritmetica() {
        return unir(
                "\n=== AYUDA: EXPRESIONES ARITMÉTICAS ===",
                "Operadores : + - * / % ^ (potencia) y paréntesis",
                "Funciones  : sqrt, cbrt, abs, sin, cos, tan (radianes), ln, log, exp",
                "Constantes : pi, e",
                "Ejemplos (cada uno con su tipo):",
                Ejemplos.lista(Ejemplos.aritmetica(), Ejemplos.aritmeticaExtra()),
                "Nota       : para multiplicar usa siempre '*' (2(3) da error).",
                "",
                "FRACCIONES: el programa calcula con fracciones exactas, sin redondear.",
                "  1/2 + 3/4      ->  5/4  (1 1/4 = 1.25)",
                "  (2/3)^2        ->  4/9",
                "  sqrt(9/16)     ->  3/4        (la raíz exacta se conserva como fracción)",
                "  Los decimales también son exactos: 0.75 se trata como 3/4.",
                "  Solo si aparece algo irracional (sqrt(2), sin, ln...) se usan decimales.",
                "",
                "VARIABLES (x, y, z, ...): se guardan con '=' y se usan en otras expresiones.",
                "  x = 3        y = 2/5        z = x*y + 1        x^2 + y     ->  fracciones exactas",
                "  'variables' muestra las guardadas y 'borrar' las elimina.",
                "",
                "Cómo lo resuelve el programa (algoritmo Shunting Yard):",
                " Paso 1. Tokenizar: separa el texto en números, variables, operadores, paréntesis y funciones,",
                "         y revisa la sintaxis. El '-' al inicio o tras un operador es un menos unario.",
                " Paso 2. Convertir a notación postfija con una lista de salida y una pila:",
                "         a) Un número o variable va directo a la salida.",
                "         b) Una función o un '(' se apilan.",
                "         c) Un operador: mientras el tope de la pila tenga mayor precedencia",
                "            (o igual, si es asociativo por la izquierda), pasa el tope a la salida;",
                "            después apila el operador nuevo.",
                "         d) Un ')': pasa a la salida todo hasta el '(' y descártalo; si debajo hay",
                "            una función, también pasa a la salida.",
                "         e) Al final, vacía la pila hacia la salida.",
                "         Precedencias: + - (1)  |  * / % (2)  |  menos unario (3)  |  ^ (4, de derecha a izquierda)",
                " Paso 3. Evaluar la postfija con una pila: un número se apila; un operador saca dos valores,",
                "         opera con fracciones exactas y devuelve el resultado; una función saca uno.",
                "         Al terminar debe quedar un solo valor: el resultado.",
                "Ejemplo: 1/2+3/4  ->  1 2 / 3 4 / +  ->  1/2, 3/4  =>  5/4"
        );
    }

    public static String lineal() {
        return unir(
                "\n=== AYUDA: ECUACIONES LINEALES Y SISTEMAS ===",
                "Una o varias incógnitas (cualquier letra), sin potencias ni productos entre letras.",
                "Ejemplos (cada uno con su tipo):",
                Ejemplos.lista(Ejemplos.lineales(), Ejemplos.linealesExtra()),
                "",
                "Pasos del método:",
                " Paso 1. Separar cada ecuación en el '=' y pasar todo a un lado:  a1*x + a2*y + ... = b.",
                " Paso 2. Con una sola ecuación y una incógnita: dividir entre el coeficiente (x = b/a).",
                " Paso 3. Con varias ecuaciones o letras: armar la matriz ampliada con los coeficientes y",
                "         aplicar Gauss-Jordan (con fracciones exactas):",
                "         a) buscar un pivote distinto de cero en la columna;",
                "         b) dividir la fila para que el pivote valga 1;",
                "         c) restar múltiplos de esa fila a las demás para dejar ceros en la columna.",
                " Paso 4. Leer el resultado:",
                "         - una fila 0 = número distinto de 0 -> sin solución;",
                "         - una solución para cada incógnita -> solución única;",
                "         - sobran incógnitas -> infinitas soluciones: las 'libres' valen cualquier número y",
                "           las demás se escriben en función de ellas (x = -(3/2)y + 6)."
        );
    }

    public static String resolver() {
        return unir(
                "\n=== AYUDA: RESOLVER BINOMIOS Y TRINOMIOS ===",
                "Escribe la ecuación con ^2 y ^3 (también sirven ² y ³). Si omites el '=', se asume '= 0'.",
                "Ejemplos (cada uno con su tipo):",
                Ejemplos.lista(Ejemplos.resolver(), Ejemplos.resolverExtra()),
                "Con varias letras se despeja una (la de mayor grado, o la que pidas con 'para') y las demás",
                "se tratan como constantes.",
                "",
                "Pasos del método (2.º grado):",
                " Paso 1. Pasar todo a un lado y reducir:  a x^2 + b x + c = 0.",
                " Paso 2. Clasificar:",
                "         - Monomio  : b = 0 y c = 0.",
                "         - Binomio  : solo falta b (a x^2 + c = 0) o solo falta c (a x^2 + b x = 0).",
                "         - Trinomio : a, b y c distintos de cero.",
                " Paso 3. Resolver según el tipo:",
                "         a x^2 + c = 0   -> x^2 = -c/a y raíz cuadrada (x = ± raíz). Si -c/a < 0, soluciones complejas.",
                "         a x^2 + b x = 0 -> factor común: x (a x + b) = 0  ->  x = 0  ó  x = -b/a.",
                "         Trinomio        -> D = b^2 - 4ac y  x = (-b ± raíz(D)) / (2a)",
                "                            D > 0: dos reales  |  D = 0: una doble  |  D < 0: dos complejas",
                "",
                "Binomio AL CUBO (ecuación binomia de 3.er grado):  a (x + h)^3 + k = 0",
                " Paso 1. Reconocer el binomio al cubo (x^3 - 8 = 0 es el caso h = 0).",
                " Paso 2. Despejar el cubo:  (x + h)^3 = -k/a.",
                " Paso 3. Sacar raíz cúbica:  x + h = raíz cúbica de (-k/a).",
                " Paso 4. Despejar x. Además hay dos soluciones complejas.",
                "Ejemplo: (x+1)^3 = 27  ->  x + 1 = 3  ->  x = 2"
        );
    }

    public static String desarrollar() {
        return unir(
                "\n=== AYUDA: DESARROLLAR BINOMIOS Y TRINOMIOS ===",
                "Escribe la expresión como (términos)^2 o (términos)^3. Los términos pueden tener varias letras.",
                "Ejemplos (cada uno con su tipo):",
                Ejemplos.lista(Ejemplos.desarrollar(), Ejemplos.desarrollarExtra()),
                "",
                "Fórmulas (productos notables):",
                "  (a + b)^2     = a^2 + 2ab + b^2",
                "  (a - b)^2     = a^2 - 2ab + b^2",
                "  (a + b)^3     = a^3 + 3a^2b + 3ab^2 + b^3",
                "  (a - b)^3     = a^3 - 3a^2b + 3ab^2 - b^3",
                "  (a + b + c)^2 = a^2 + b^2 + c^2 + 2ab + 2ac + 2bc",
                "  (a + b + c)^3 = a^3 + b^3 + c^3 + 3a^2b + 3a^2c + 3ab^2 + 3b^2c + 3ac^2 + 3bc^2 + 6abc",
                "",
                "Pasos del método:",
                " Paso 1. Identificar a, b (y c): son los términos dentro del paréntesis.",
                " Paso 2. Sustituir en la fórmula cada letra por su término, entre paréntesis.",
                " Paso 3. Calcular cada término (potencias y productos).",
                " Paso 4. Reducir términos semejantes y ordenar.",
                "Ejemplo: (2x - 3y)^2  ->  a = 2x, b = 3y  ->  (2x)^2 - 2*(2x)*(3y) + (3y)^2  ->  4x^2 - 12xy + 9y^2",
                "Cualquier otra expresión, como (x+y)^4 o (x+1)(x+2), se desarrolla multiplicando directamente."
        );
    }

    public static String factorizar() {
        return unir(
                "\n=== AYUDA: FACTORIZAR ===",
                "Escribe solo la expresión (sin '='). Admite fracciones, decimales y varias letras.",
                "Ejemplos (cada uno con su tipo):",
                Ejemplos.lista(Ejemplos.factorizar(), Ejemplos.factorizarExtra()),
                "",
                "Pasos del método (se prueban en este orden):",
                " Paso 1. Factor común: el número y las letras que se repiten en todos los términos.",
                "         3x^2 + 6x = 3x(x + 2).",
                " Paso 2. Binomio o trinomio perfecto al cuadrado o al cubo:",
                "         x^2 + 6x + 9 = (x + 3)^2        x^3 + 3x^2 + 3x + 1 = (x + 1)^3",
                "         (también trinomios: (a + b + c)^2 y (a + b + c)^3).",
                " Paso 3. Diferencia de cuadrados:  a^2 - b^2 = (a + b)(a - b).",
                "         La suma de cuadrados no se factoriza en los reales.",
                " Paso 4. Suma y diferencia de cubos:",
                "         a^3 + b^3 = (a + b)(a^2 - ab + b^2)      a^3 - b^3 = (a - b)(a^2 + ab + b^2)",
                " Paso 5. Trinomio de 2.º grado: D = b^2 - 4ac. Si D es un cuadrado perfecto, las raíces son",
                "         racionales y cada raíz r da el factor (x - r). Sirve también con varias letras.",
                " Paso 6. Cuatro términos: factorización por agrupación.  xy + x + y + 1 = x(y + 1) + (y + 1) = (x + 1)(y + 1).",
                " Paso 7. Polinomios de una letra y grado 3 o más: se prueban las raíces racionales ±p/q",
                "         (p divide al término independiente, q al coeficiente líder).",
                " Paso 8. Cada factor obtenido se vuelve a intentar factorizar, y al final se multiplica todo",
                "         para comprobar que se recupera la expresión original."
        );
    }
}

// ======================================================================
// Clase: Ejemplos
// ======================================================================
/**
 * Ejemplos de cada módulo con su tipo (binomio al cuadrado, al cubo, fracciones, etc.).
 * Cada ejemplo es un par {expresión, tipo}. Los usan la ventana gráfica, la consola y la ayuda.
 */
final class Ejemplos {

    private Ejemplos() { }

    public static String[][] aritmetica() {
        return new String[][]{
                {"(5+3)*4-sqrt(9)", "Operaciones combinadas con raíz"},
                {"1/2+3/4", "Suma de fracciones"},
                {"sqrt(9/16)", "Raíz de una fracción"},
                {"(2/3)^2", "Fracción al cuadrado"},
                {"x = 3", "Guardar una variable"},
                {"variables", "Ver las variables guardadas"}
        };
    }

    public static String[][] aritmeticaExtra() {
        return new String[][]{
                {"2/3*3/4", "Multiplicación de fracciones"},
                {"0.75+1/4", "Decimal más fracción"},
                {"2^3^2", "Potencia encadenada (de derecha a izquierda)"},
                {"-2^2", "Menos con potencia"},
                {"8^(1/3)", "Raíz cúbica con exponente fraccionario"},
                {"x^2+1/3", "Usa la variable x (antes escribe x = 3)"}
        };
    }

    public static String[][] lineales() {
        return new String[][]{
                {"2x + 5 = 15", "Ecuación lineal de 1 incógnita"},
                {"2x + 3y = 12", "Despeje de x (2 incógnitas)"},
                {"2x + 3y = 12 para y", "Despeje de y (2 incógnitas)"},
                {"2x + y = 7 y x - y = 2", "Sistema de 2 ecuaciones (con la palabra y)"},
                {"x + y + z = 6 ; x - y = 0 ; z = 2", "Sistema de 3 ecuaciones (x, y, z)"},
                {"x + y = 2 ; x + y = 3", "Sistema sin solución"}
        };
    }

    public static String[][] linealesExtra() {
        return new String[][]{
                {"x + y = 5 , x - y = 1", "Sistema de 2 ecuaciones (con coma)"},
                {"x = y = 3", "Igualdades encadenadas (x = y  y  y = 3)"}
        };
    }

    public static String[][] resolver() {
        return new String[][]{
                {"x^2 - 5x + 6 = 0", "Trinomio de 2.º grado"},
                {"2x^2 - 8 = 0", "Binomio ax^2 + c"},
                {"3x^2 - 12x = 0", "Binomio ax^2 + bx"},
                {"x^2 + 2x + 5 = 0", "Trinomio con soluciones complejas"},
                {"(x+1)^3 = 27", "Ecuación de binomio al cubo"},
                {"x^2 + 2xy + y^2 - 9 = 0 para x", "Trinomio con dos letras"}
        };
    }

    public static String[][] resolverExtra() {
        return new String[][]{
                {"(x+2)(x+3) = 0", "Trinomio escrito como producto"},
                {"x^3 - 8 = 0", "Binomio cúbico (x^3 = número)"}
        };
    }

    public static String[][] desarrollar() {
        return new String[][]{
                {"(x+y)^3", "Binomio al cubo"},
                {"(2x-3y)^2", "Binomio al cuadrado (a - b)"},
                {"(a+b+c)^3", "Trinomio al cubo"},
                {"(x^2+3y)^3", "Binomio al cubo con potencias"},
                {"(x+y+z)^2", "Trinomio al cuadrado"},
                {"(x-2)^3", "Binomio al cubo (a - b)"}
        };
    }

    public static String[][] desarrollarExtra() {
        return new String[][]{
                {"(x+y)^4", "Binomio a la cuarta (sin fórmula, multiplicando)"},
                {"(x+1)(x+2)", "Producto de dos binomios"}
        };
    }

    public static String[][] factorizar() {
        return new String[][]{
                {"x^2 - 9", "Binomio: diferencia de cuadrados"},
                {"6x^2 + 7x + 2", "Trinomio de 2.º grado"},
                {"x^3 + 3x^2 + 3x + 1", "Cubo perfecto (binomio al cubo)"},
                {"8x^3 - 27y^3", "Binomio: diferencia de cubos"},
                {"x^2 + 5xy + 6y^2", "Trinomio con dos letras"},
                {"xy + x + y + 1", "Polinomio de 4 términos: agrupación"}
        };
    }

    public static String[][] factorizarExtra() {
        return new String[][]{
                {"3x^2 + 6x", "Binomio con factor común"},
                {"x^2 + 6x + 9", "Trinomio cuadrado perfecto (binomio al cuadrado)"},
                {"x^3 + 8", "Binomio: suma de cubos"},
                {"x^3 - 6x^2 + 11x - 6", "Polinomio de 3.er grado (raíces racionales)"}
        };
    }

    /** Una línea de ejemplo:  "  expresión          ->  tipo". */
    public static String linea(String expresion, String tipo) {
        StringBuilder sb = new StringBuilder("  ").append(expresion);
        while (sb.length() < 42) sb.append(' ');
        if (sb.length() == 2 + expresion.length()) sb.append("  ");
        return sb.append("->  ").append(tipo).toString();
    }

    /** Varias líneas de ejemplos (uno o más grupos). */
    public static String lista(String[][]... grupos) {
        StringBuilder sb = new StringBuilder();
        for (String[][] g : grupos) {
            for (String[] e : g) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(linea(e[0], e[1]));
            }
        }
        return sb.toString();
    }
}
