package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * 聊天室 - 客户端
 * TCP 一对一通信，Swing 界面，UTF-8 编码
 */
public class ChatClient {

    // ========== 字段 ==========
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;

    public ChatClient() {
        initUI();
        connectToServer();
    }

    // ========== UI 初始化 ==========
    private void initUI() {
        frame = new JFrame("客户端 - 一对一聊天室");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(500, 450);
        frame.setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        JButton sendBtn = new JButton("发送");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        frame.setVisible(true);
    }

    // ========== 连接服务端（子线程） ==========
    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 8888);
                appendMessage("已连接到服务端！");

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                receiveMessages();  // 阻塞接收循环
            } catch (IOException e) {
                appendMessage("连接服务端失败：" + e.getMessage());
            }
        }, "Client-Connect-Thread").start();
    }

    // ========== 接收消息（子线程） ==========
    private void receiveMessages() {
        try {
            String msg;
            while (running && (msg = in.readLine()) != null) {
                final String received = msg;
                SwingUtilities.invokeLater(() -> appendMessage("服务端说：" + received));
            }
        } catch (IOException e) {
            if (running) {
                SwingUtilities.invokeLater(() -> appendMessage("服务端已断开连接。"));
            }
        } finally {
            closeConnection();
        }
    }

    // ========== 发送消息 ==========
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty() || out == null) return;

        final PrintWriter currentOut = this.out;
        final String message = msg;

        new Thread(() -> {
            currentOut.println(message);
        }, "Client-Send-Thread").start();

        appendMessage("我（客户端）说：" + msg);
        inputField.setText("");
    }

    // ========== 资源清理 ==========
    private void closeConnection() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        in = null;
        out = null;
        socket = null;
    }

    private void shutdown() {
        running = false;
        closeConnection();
        frame.dispose();
        System.exit(0);
    }

    // ========== 更新聊天区 ==========
    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}
