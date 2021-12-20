import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ClientFrame extends JFrame {
    private final String SERVER_ADDR = "127.0.0.1";
    private final int SERVER_PORT = 8880;


    private JTextField msgInputField;
    private JTextArea chatArea;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ClientFrame() {
        prepareGUI();
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                while (true) {
                    String inputText = dis.readUTF();
                    if (inputText.equalsIgnoreCase("/stop")) {
                        chatArea.append("Сообщение от сервера: " + inputText + "\n");
                        try {
                            dos.writeUTF(inputText);
                        } catch (Exception e) {

                        }
                        finally {
                            closeConnection();
                            msgInputField.setText("");
                            msgInputField.setEditable(false);
                            break;
                        }
                    }
                    chatArea.append("Сообщение от сервера: " + inputText + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка подключения к серверу.");
            }
            return;
        }).start();
    }

    private void closeConnection() {
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String msg = msgInputField.getText();
        if (msg != null && !msg.trim().isEmpty()) {
            try {
                dos.writeUTF(msg);
                chatArea.append("Ваше сообщение: " + msg + "\n");
                msgInputField.setText("");
                msgInputField.grabFocus();
                if (msg.equalsIgnoreCase("/stop")) {
                    msgInputField.setText("");
                    msgInputField.setEditable(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения.");
            }
        }
    }


    public void prepareGUI() { // настройки фремов окна из методички

        setBounds(600, 300, 500, 500);
        setTitle("Тестовый Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);
        btnSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        msgInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    dos.writeUTF("/stop");
                } catch (IOException exc) {
                    exc.printStackTrace();
                }

            }
        });
        setVisible(true);
    }
}