package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * 聊天室 - 服务端
 * TCP 一对一通信，Swing 界面，UTF-8 编码
 */
public class ChatServer {

    // ========== 字段 ==========
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;

    public ChatServer() {
        initUI();
        startServer();
    }

    // ========== UI 初始化 ==========
    private void initUI() {
        frame = new JFrame("服务端 - 一对一聊天室");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 点×不直接退出，交给WindowListener
        frame.setSize(500, 450);
        frame.setLocationRelativeTo(null);

        // 聊天记录区
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 底部输入区
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

    // ========== 网络监听（子线程） ==========
    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8888);
                appendMessage("服务端启动成功，等待客户端连接...");

                while (running) {
                    socket = serverSocket.accept();  // 阻塞等待连接
                    appendMessage("客户端已连接！");

                    in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    out = new PrintWriter(
                            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                    new Thread(this::receiveMessages).start();  // 启动接收线程
                }
            } catch (IOException e) {
                if (running) {
                    appendMessage("服务端异常：" + e.getMessage());
                }
            }
        }, "Server-Accept-Thread").start();
    }

    // ========== 接收消息（子线程） ==========
    private void receiveMessages() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                final String received = msg;
                SwingUtilities.invokeLater(() -> appendMessage("客户端说：" + received));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> appendMessage("客户端已断开连接。"));
        } finally {
            closeClientConnection();
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
        }, "Server-Send-Thread").start();

        appendMessage("我（服务端）说：" + msg);
        inputField.setText("");
    }

    // ========== 资源清理 ==========
    private void closeClientConnection() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        in = null;
        out = null;
        socket = null;
    }

    private void shutdown() {
        running = false;
        closeClientConnection();
        try { if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); } catch (IOException ignored) {}
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
        SwingUtilities.invokeLater(ChatServer::new);
    }
}
