import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerFrame extends JFrame {
    private final int SERVER_PORT = 8880;

    private JTextField msgInputField;
    private JTextArea chatArea;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isRunning = true;

    public ServerFrame() {
        prepareGUI();
        new Thread(this::createConnection).start();
    }

    private void createConnection() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            chatArea.append("Сервер запущен\n");
            while(isRunning) {
                socket = serverSocket.accept();
                chatArea.append("Сервер подключен\n");
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                while (true) {
                    String str = dis.readUTF();
                    if (str.equalsIgnoreCase("/stop")) {
                        dos.writeUTF("/stop");
                        closeConnection();
                        chatArea.append("Клиент отключился\n");
                        break;
                    }
                    chatArea.append("Клиент отправил: " + str + "\n");
                }
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    private void sendMessage() {
        String msg = msgInputField.getText();
        if (msg != null && !msg.trim().isEmpty()) {
            if(msg.equalsIgnoreCase("/stop")) {
                isRunning = false;
            }
            try {
                if(isRunning) {
                    dos.writeUTF(msg);
                    chatArea.append("Ваше сообщение: " + msg + "\n");
                } else {
                    dos.writeUTF("/stop");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            msgInputField.setText("");
            msgInputField.grabFocus();
            if (msg.equalsIgnoreCase("/stop")) {
                closeConnection();
                System.exit(0);
            }
        }
    }

    public void prepareGUI() { // Параметры фреймов окна

        setBounds(600, 300, 500, 500);
        setTitle("Сервер");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setText("Для закрытия сервера напишите комманду /stop\n");
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
                if(socket != null) {
                    try {
                        dos.writeUTF("/stop");
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            }
        });
        setVisible(true);
    }
}