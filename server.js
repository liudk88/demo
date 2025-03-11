const WebSocket = require('ws');

// 创建WebSocket服务器，监听8080端口
const wss = new WebSocket.Server({ port: 8080 });

// 监听连接事件
wss.on('connection', function connection(ws) {
    console.log('新的客户端已连接');

    // 发送欢迎消息给客户端
    ws.send('欢迎连接到WebSocket服务器！（node.js版本）');

    // 监听客户端发送的消息
    ws.on('message', function incoming(message) {
        console.log('收到客户端消息:', message.toString());
        
        // 广播消息给所有连接的客户端
        wss.clients.forEach(function each(client) {
            if (client !== ws && client.readyState === WebSocket.OPEN) {
                client.send(`其他用户说: ${message}`);
            }
        });
        
        // 发送确认消息给发送者
        ws.send(`服务器已收到你的消息: ${message}`);
    });

    // 监听连接关闭事件
    ws.on('close', function() {
        console.log('客户端已断开连接');
    });
});

console.log('WebSocket服务器正在运行，端口: 8080'); 