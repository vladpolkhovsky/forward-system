function connectToWebSocket(onError) {
    let prefix = "ws:";
    if (window.location.protocol === 'https:') {
        prefix = "wss:";
    }

    const brokerUrl = prefix + '//' + window.location.host + '/ws'

    const stompClient = new StompJs.Client({
        brokerURL: brokerUrl
    });

    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe(`/user/${context.userId}/queue/messages`, (notification) => {
            processWsNotification(JSON.parse(notification.body))
        });
    };

    stompClient.onWebSocketError = (error) => {
        alert('Вы будете переклбючены на SockJS. Ошибка : stompClient.onWebSocketError : ' + JSON.stringify(error));
        console.error('Error with websocket', error);
        onError(error)
    };

    stompClient.onStompError = (frame) => {
        alert('Вы будете переклбючены на SockJS. Ошибка : stompClient.onStompError : ' + JSON.stringify(frame.body));
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        onError(frame);
    };

    stompClient.activate();

    return stompClient;
}

function connectToStompJs(wsStomp) {
    if (wsStomp != null) {
        wsStomp.deactivate();
    }

    const brokerUrl = window.location.protocol + '//' + window.location.host + '/sockjs'

    function mySocketFactory() {
        return new SockJS(brokerUrl);
    }

    const stompClient = new StompJs.Client({

    });

    stompClient.webSocketFactory = mySocketFactory;

    stompClient.onConnect = (frame) => {
        console.log('SockJS Connected: ' + frame);
        stompClient.subscribe(`/user/${context.userId}/queue/messages`, (notification) => {
            processWsNotification(JSON.parse(notification.body))
        });
    };

    stompClient.onWebSocketError = (error) => {
        alert('Критическая ошибка SockJS : SockJS stompClient.onWebSocketError : ' + error);
        console.error('Error with websocket', error);
    };

    stompClient.onStompError = (frame) => {
        alert('Критическая ошибка SockJS : SockJS stompClient.onStompError : ' + frame.body);
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    };

    stompClient.activate();

    return stompClient;
}