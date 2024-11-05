context.loadedChats = new Set();
context.loadedMessages = new Set();
context.chatId = null;
context.loadNewMessagesCount = 30;

context.chatsCache = {};
context.chatsQueue = [];

const loadNewChatsUrl = '/api/new-chat/chats';
const searchChatsUrl = '/api/new-chat/search';
const loadChatByIdUrl = '/api/new-chat/chat-by-id';
const loadChatInfoUrl = '/api/new-chat/load-chat-info';
const loadMoreMessagesUrl = '/api/new-chat/messages';
const loadNewMessagesCount = '/api/new-chat/new-message-info';

const chatTitle = document.getElementById("chat-name");
const techNumber = document.getElementById("additional-tech-number");
const workType = document.getElementById("additional-work-type");
const status = document.getElementById("additional-order-status");
const discipline = document.getElementById("additional-discipline");
const deadline = document.getElementById("additional-deadline");
const intermediateDeadline = document.getElementById("additional-intermediate-deadline");
const additionalDates = document.getElementById("additional-dates");
const additionalViewFull = document.getElementById("additional-view-full");

const chatWindow = document.getElementById("scroll-window");

function getUrlParam(paramName) {
    let searchParams = new URLSearchParams(window.location.search);
    let isTabExists = searchParams.has(paramName);
    if (isTabExists) {
        return searchParams.get(paramName);
    } else {
        return null;
    }
}

function getUrlChatId() {
    return getUrlParam("chatId");
}

function getTab() {
    let paramValue = getUrlParam("tab");
    if (paramValue === null) {
        window.location.replace('/new-chat?tab=new-orders');
    }
    return paramValue;
}

function activateTab() {
    $(`#nav-${context.chatTab}-tab`).addClass('active')
}

function searchForChat(searchText) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const rawJson = JSON.stringify({
        "chatName": searchText,
        "userId": context.userId,
        "chatTab": context.chatTab,
        "size": 50
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: rawJson,
        redirect: "follow"
    };

    fetch(searchChatsUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => processNewSearchedChats(result.chats))
        .catch((error) => alert("Произошла ошибка загрузки чатов. " + JSON.stringify(error)));
}

function loadNewChats() {
    const headers = new Headers();
    headers.append("Content-Type", "application/json; charset=UTF-8");

    const rawJson = JSON.stringify({
        "loaded": [...context.loadedChats],
        "userId": context.userId,
        "chatTab": context.chatTab,
        "size": 50
    });

    const requestOptions = {
        method: "POST",
        headers: headers,
        body: rawJson,
        redirect: "follow"
    };

    fetch(loadNewChatsUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => processNewLoadedChats(result.chats))
        .catch((error) => alert("Произошла ошибка загрузки чатов. " + JSON.stringify(error)));
}

function loadChatById(chatId) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const rawJson = JSON.stringify({
        "chatId": chatId,
        "userId": context.userId,
        "chatTab": context.chatTab
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: rawJson,
        redirect: "follow"
    };

    fetch(loadChatByIdUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => processNewLoadedChats(result.chats))
        .catch((error) => alert("Произошла ошибка загрузки чатов. " + JSON.stringify(error)));
}

function sendMessageViewed() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: null,
        redirect: "follow"
    };

    fetch(`/api/messenger/message-viewed/${context.chatId}/${context.userId}`, requestOptions)
        .then((response) => response.text())
        .catch((error) => alert("Произошла ошибка загрузки чатов. " + JSON.stringify(error)));
}

function loadChatInfoById(chatId) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "chatId": chatId,
        "userId": context.userId,
        "size": context.loadNewMessagesCount
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadChatInfoUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => processLoadChatInfo(result))
        .catch((error) => console.error(error));
}

function loadNewMessageInfo() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "userId": context.userId
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadNewMessagesCount, requestOptions)
        .then((response) => response.json())
        .then((result) => processNewMessagesCount(result))
        .catch((error) => console.error(error));
}

function loadMoreMessages() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "chatId": context.chatId,
        "userId": context.userId,
        "loaded": [...context.loadedMessages],
        "size": context.loadNewMessagesCount
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadMoreMessagesUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => prependChatMessages(result.messages))
        .catch((error) => console.error(error));
}

function processNewMessagesCount(count) {
    $("#nav-new-orders-tab-count").text(" (" + count.newOrders + ")");
    $("#nav-admin-tab-count").text(" (" + count.admin + ")");
    $("#nav-orders-tab-count").text(" (" + count.orders + ")");
    $("#nav-special-tab-count").text(" (" + count.special + ")");
}

function clearChatsWindow() {
    $("#chat-window").empty();
}

function clearMessagesWindow() {
    $("#messages-window").empty();
}

function appendChatToChatsWindows(chatJson) {
    let chatWindowElement = createChatWindowElement(chatJson);
    $("#chat-window").append(chatWindowElement);
}

function appendChatsToChatsWindows(chatsArray) {
    for (let chat of chatsArray) {
        appendChatToChatsWindows(chat)
    }
}

function createNotificationElement(id, title, content) {
    return `<div id="${id}" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header">
            <strong class="me-auto">${title}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">${content}</div>
    </div>`
}

function showNotification(id, title, content) {
    $("#notification-container").append(createNotificationElement(id, title, content));
    let toastBootstrap = bootstrap.Toast.getOrCreateInstance(document.getElementById(id), {
        delay: 10000
    });
    toastBootstrap.show();
}

function createChatWindowElement(chatJson) {
    let text_bg = chatJson.newMessageCount === 0 ? "bg-white" : "text-bg-primary";
    return `
    <li id="chat-${chatJson.id}" class="list-group-item d-flex justify-content-between align-items-start" onclick="onChatLoadBtnClick(${chatJson.id})">
        <div class="ms-2 me-auto main-font">
            <div class="fw-bold">${chatJson.chatName}</div>
            <p><span id="chat-${chatJson.id}-date">${chatJson.lastMessageDate}</span> <span id="chat-1-chat-id">(chat-id = ${chatJson.id})</span>
            </p>
        </div>
        <span id="chat-${chatJson.id}-new-msg-count" class="badge ${text_bg} rounded-pill">${chatJson.newMessageCount}</span>
    </li>`;
}

function createStatus(orderStatusName) {
    let color = 'btn-secondary';
    let orderStatusRus = "Не назначен";

    if (orderStatusName === 'IN_PROGRESS') {
        color = 'btn-primary';
        orderStatusRus = "В работе";
    }
    if (orderStatusName === 'REVIEW') {
        color = 'btn-info';
        orderStatusRus = "Проверка";
    }
    if (orderStatusName === 'FINALIZATION') {
        color = 'btn-warning';
        orderStatusRus = "Доработка";
    }
    if (orderStatusName === 'GUARANTEE' || orderStatusName === 'CLOSED') {
        color = 'btn-success';
        orderStatusRus = "На гаранити/Завершён";
    }
    return `<a class="btn ${color}">${orderStatusRus}</a>`;
}

function createDates(datesArray) {
    let str = "";
    for (let date of datesArray) {
        str = str + `<li class="list-group-item">${date.text} : ${date.time.replace('T', ' ')}</li>`
    }
    if (str.length === 0) {
        str = "Не назначено."
    }
    return `<ul class="list-group">${str}</ul>`
}

function formatChatHeader(chatName, orderInfo) {
    chatTitle.innerText = chatName;
    if (orderInfo !== null) {
        techNumber.innerText = orderInfo.techNumber;
        workType.innerText = orderInfo.workType;
        deadline.innerText = orderInfo.deadline;
        discipline.innerText = orderInfo.discipline;
        status.innerHTML = createStatus(orderInfo.orderStatus);
        intermediateDeadline.innerText = orderInfo.intermediateDeadline;
        additionalDates.innerHTML = createDates(JSON.parse(orderInfo.dates));

        additionalViewFull.setAttribute("href", `/order-info/${orderInfo.id}`)

        showAdditionalOrderData()
    }
}

function buildChatTable(chatInfoJson) {
    context.userIdToRole = {};
    context.userIdToUsername = {};
    for (let member of chatInfoJson.members) {
        context.userIdToUsername[member.userId] = member.username;
    }
    if (chatInfoJson.order === null) {
        return;
    }
    for (let participant of chatInfoJson.order.participants) {
        if (context.userIdToRole[participant.userId] === undefined) {
            context.userIdToRole[participant.userId] = [];
        }
        context.userIdToRole[participant.userId].push(participant.typeRus);
    }
}

function getUserToIdRole(userId, isSystem) {
    if (isSystem) {
        return "";
    }
    if (context.userIdToRole[userId] === undefined) {
        return "";
    } else {
        return `(${context.userIdToRole[userId].join(', ')})`;
    }
}

function getUsername(userId, isSystemMessage) {
    if (isSystemMessage) {
        return "Система";
    } else {
        return context.userIdToUsername[userId];
    }
}

function createMessageElement(fromUserId, isSystemMessage, attachments, options, text, date, viewed) {
    let additionalStyles = "";
    let textAlign = "text-start"
    let newBadge = "";
    let optionHtml = "";
    let attachmentHtml = "";

    if (!viewed) {
        newBadge = `<span class="badge text-bg-info">Новое</span>`;
    }

    if (context.userId === fromUserId && !isSystemMessage) {
        additionalStyles = "bg-success-subtle ";
        textAlign = "text-end";
    } else {
        additionalStyles = "bg-secondary-subtle ";
    }

    if (isSystemMessage) {
        additionalStyles = "bg-danger-subtle ";
    }

    if (attachments.length > 0) {
        for (let attachment of attachments) {
            attachmentHtml += `<a class="me-2 mt-1" target="_blank" href="/load-file/${attachment.id}">${attachment.name}</a>`
        }
    }

    if (options.length > 0) {
        for (let option of options) {
            optionHtml += `<a class="me-2 mt-1 btn btn-primary" target="_blank" href="${option.url}">${option.name}</a>`
        }
    }

    return `
        <div class="border p-2 mt-1 main-font ${additionalStyles}">
            <p class="m-0 fw-bold ${textAlign}">${getUsername(fromUserId, isSystemMessage)} ${getUserToIdRole(fromUserId, isSystemMessage)}</p>
            <hr class="mt-1 mb-1"/>
            <pre class="m-0 main-font text-break fs-6" style="white-space: pre-wrap">${text}</pre>
            ${attachmentHtml}
            ${optionHtml}
            <hr class="mt-1 mb-1">
            <p class="m-0 text-end">${newBadge} ${date}</p>
        </div>`
}

function prependChatMessages(messages) {
    for (let message of messages) {
        context.loadedMessages.add(message.id);

        if (message.hidden) {
            continue;
        }

        $("#messages-window").prepend(createMessageElement(message.fromUserId, message.systemMessage, message.attachments, message.options, message.text, message.createdAt, message.viewed));
    }

    hideChatNewMessagesSpinner();

    if (messages.length < context.loadNewMessagesCount) {
        hideMessageLoadMoreBtn();
    } else {
        showMessageLoadMoreBtn();
    }
}

function appendWSMessage(message) {
    if (message.fromUserId !== context.userId) {
        notificationAudio.play();
    }

    if (message.chatId !== context.chatId) {
        let content = message.content;
        if (message.content === null) {
            content = "Сообщение без текста.";
        }
        showNotification(message.id, "Новое сообщение в чате: " + message.chatName, content.substring(0, 50))
        incNewMessageCount(message.chatId);
        return;
    } else {
        sendMessageViewed();
    }

    context.loadedMessages.add(message.id);
    let attachments = []
    if (message.attachments.length > 0) {
        attachments.push({
            id: message.attachments[0].attachmentId,
            name: message.attachments[0].attachmentName,
        });
    }

    let options = []
    if (message.options.length > 0) {
        options.push({
            url: message.options[0].content,
            name: message.options[0].optionName,
        });
    }

    $("#messages-window").append(createMessageElement(message.fromUserId, message.isSystemMessage, attachments, options, message.content, "Недавно", false));

    chatWindow.scroll(0, chatWindow.scrollHeight)
}

function processLoadChatInfo(chatInfoJson) {
    hideChatNewMessagesSpinner();
    showMessageLoadMoreBtn();

    formatChatHeader(chatInfoJson.chatName, chatInfoJson.order);

    buildChatTable(chatInfoJson);
    prependChatMessages(chatInfoJson.messages);

    if (chatInfoJson.metadata.onlyOwnerCanType && !context.isOwner) {
        $("#message-div").addClass("d-none");
    } else {
        $("#message-div").removeClass("d-none");
    }

    chatWindow.scroll(0, chatWindow.scrollHeight)

    sendMessageViewed();
    clearNewMessageCount(chatInfoJson.chatId);
}

function processNewSearchedChats(chatsArray) {
    hideLoadChatSpinner();
    updateChatsCache(chatsArray);
    appendChatsToChatsWindows(chatsArray);
}

function processNewLoadedChats(chatsArray) {
    hideLoadChatSpinner();
    updateChatsCache(chatsArray);
    appendChatsToChatsWindows(chatsArray);
}

function drawChatsByChatQueue() {
    let chatsArray = [];
    for (let i = 0; i < context.chatsQueue.length; i++) {
        chatsArray.push(context.chatsCache[context.chatsQueue[i]]);
    }
    appendChatsToChatsWindows(chatsArray);
}

function updateChatsCache(chatsArray) {
    for (let chat of chatsArray) {
        context.loadedChats.add(chat.id);
        context.chatsCache[chat.id] = chat;
        pushToChatToQueueBack(chat.id);
    }
}

function pushToChatToQueueFront(chatId) {
    const index = context.chatsQueue.indexOf(chatId);
    if (index > -1) {
        context.chatsQueue.splice(index, 1);
    }
    context.chatsQueue.unshift(chatId);
}

function pushToChatToQueueBack(chatId) {
    const index = context.chatsQueue.indexOf(chatId);
    if (index > -1) {
        context.chatsQueue.splice(index, 1);
    }
    context.chatsQueue.push(chatId);
}

function hideAdditionalOrderData() {
    $("#chat-additional-info").hide();
}

function showAdditionalOrderData() {
    $("#chat-additional-info").show();
}

function showChatNewMessagesSpinner() {
    $("#message-loader-spin").removeClass("d-none")
}

function hideChatNewMessagesSpinner() {
    $("#message-loader-spin").addClass("d-none")
}

function hideMessageLoadMoreBtn() {
    $("#message-load-more").addClass("d-none")
}

function showMessageLoadMoreBtn() {
    $("#message-load-more").removeClass("d-none")
}

function showLoadChatSpinner() {
    $("#chat-loader-spin").removeClass("d-none");
}

function hideLoadChatSpinner() {
    $("#chat-loader-spin").addClass("d-none");
}

function onSearchBarBtnClick() {
    let searchText = $("#search-input").val();
    if (searchText.length === 0) {
        onSearchBarClearBtnClick();
    } else {
        clearChatsWindow();
        showLoadChatSpinner();
        searchForChat(searchText);
    }
}

function onLoadNewChatsBtnClick() {
    showLoadChatSpinner();
    loadNewChats();
}

function onLoadMoreMessagesBtnClick() {
    showChatNewMessagesSpinner();
    loadMoreMessages();
}

function onLoadChatById(chatId) {
    showLoadChatSpinner();
    loadChatById(chatId);
}

function onSearchBarClearBtnClick() {
    $("#search-input").val('');
    clearChatsWindow();
    drawChatsByChatQueue();
}

function onChatLoadBtnClick(chatId) {
    if (context.chatId === chatId) {
        return;
    }

    clearMessagesWindow();

    $(".bg-primary-subtle").removeClass("bg-primary-subtle")
    $(`#chat-${chatId}`).addClass('bg-primary-subtle');
    hideAdditionalOrderData();
    showChatNewMessagesSpinner()

    context.chatId = chatId;
    context.loadedMessages.clear();

    loadChatInfoById(chatId)
}

function onNewMessage(chatId, message) {
    pushToChatToQueueFront(chatId);
    moveChatInChatWindowToFront(chatId);
}

function moveChatInChatWindowToFront(chatId) {
    if ($(`#chat-${chatId}`).length) {
        $(`#chat-${chatId}`).prependTo("#chat-window");
    }
}

function incNewMessageCount(chatId) {
    $(`#chat-${chatId}-new-msg-count`).removeClass("bg-white");
    $(`#chat-${chatId}-new-msg-count`).addClass("text-bg-primary");
    let text = $(`#chat-${chatId}-new-msg-count`).text();

    if (text === null || text === undefined || text === '') {
        text = 0;
    } else {
        text = parseInt(text);
    }

    $(`#chat-${chatId}-new-msg-count`).text(text + 1);
}

function clearNewMessageCount(chatId) {
    $(`#chat-${chatId}-new-msg-count`).addClass("bg-white");
    $(`#chat-${chatId}-new-msg-count`).removeClass("text-bg-primary");


    $(`#chat-${chatId}-new-msg-count`).text('');
}
