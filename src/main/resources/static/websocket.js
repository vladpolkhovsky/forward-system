function connectToWebSocket() {
    const brokerUrl = 'ws://' + window.location.host + '/ws'

    const stompClient = new StompJs.Client({
        brokerURL: brokerUrl
    });

    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe(`/user/${context.userId}/queue/messages`, (notification) => {
            processWsNotification(JSON.parse(notification.body))
        });
        // stompClient.publish({
        //     destination: "/app/chat",
        //     headers: {priority: 9},
        //     body: JSON.stringify({
        //         userId: 1
        //     })
        // });
    };

    stompClient.onWebSocketError = (error) => {
        alert('Ошибка : stompClient.onWebSocketError : ' + error);
        console.error('Error with websocket', error);
    };

    stompClient.onStompError = (frame) => {
        alert('Ошибка : stompClient.onStompError : ' + frame.body);
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    };

    stompClient.activate();

    return stompClient;
}