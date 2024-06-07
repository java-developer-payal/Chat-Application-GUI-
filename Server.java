import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private BufferedReader inputReader;
    private PrintWriter outputWriter;

    private JFrame frame;
    private JPanel chatPanel;
    private JTextField messageInput;
    private JButton sendButton;
    private JScrollPane scrollPane;

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is ready to accept connection");
            System.out.println("Waiting for a client...");

            initializeGUI();

            clientSocket = serverSocket.accept();

            inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputWriter = new PrintWriter(clientSocket.getOutputStream());

            startReading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Server Side");
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(240, 242, 245));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        messageInput = new JTextField(30);
        messageInput.setFont(new Font("Arial", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(37, 211, 102), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        messageInput.setBackground(new Color(255, 255, 255));
        messageInput.setForeground(Color.BLACK);
    
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(37, 211, 102));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sendButton.setFocusPainted(false);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageInput.getText();
                sendMessage(message);
                messageInput.setText("");
            }
        });

        messageInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageInput.getText();
                sendMessage(message);
                messageInput.setText("");
            }
        });
    }

    private void sendMessage(String message) {
        outputWriter.println(message);
        outputWriter.flush();
        displayMessage("Server", message);
        if (message.equals("finish")) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayMessage(String sender, String message) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(sender.equals("Server") ? FlowLayout.RIGHT : FlowLayout.LEFT));
        messagePanel.setOpaque(false);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setBackground(sender.equals("Server") ? new Color(37, 211, 102) : new Color(225, 225, 225));
        messageArea.setForeground(sender.equals("Server") ? Color.WHITE : Color.BLACK);
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel roundedPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(sender.equals("Server") ? new Color(37, 211, 102) : new Color(225, 225, 225));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        roundedPanel.setOpaque(false);
        roundedPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundedPanel.add(messageArea);

        messagePanel.add(roundedPanel);
        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void startReading() {
        Runnable readerTask = () -> {
            System.out.println("Reader started.........");

            try {
                while (true) {
                    String msg = inputReader.readLine();
                    if (msg.equals("finish")) {
                        System.out.println("Client terminated the chat");
                        clientSocket.close();
                        break;
                    }
                    displayMessage("Client", msg);
                }
            } catch (Exception e) {
                System.out.println("Connection closed");
            }
        };
        new Thread(readerTask).start();
    }

    public static void main(String[] args) {
        System.out.println("This is the server side");
        new Server(7776);
    }
}
