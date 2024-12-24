context.loadedChats = new Set();
context.loadedMessages = new Set();
context.chatId = null;
context.loadNewMessagesCount = 30;

context.chatsCache = {};
context.chatsQueue = [];

const loadNotesUrl = "/api/new-chat/notes/" + context.userId;
const createNoteUrl = "/api/new-chat/notes/" + context.userId + "/new";
const updateNoteUrl = "/api/new-chat/notes/" + context.userId + "/";
const deleteNoteUrl = "/api/new-chat/notes/" + context.userId + "/";

const loadNewChatsUrl = '/api/new-chat/chats';
const loadStatusUrl = '/api/new-chat/load-order-status';
const loadIsMemberUrl = '/api/new-chat/load-order-member';
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
const mainWindow = document.getElementById("id-body");

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

function loadNoteByUser() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        redirect: "follow"
    };

    fetch(loadNotesUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => processLoadedNotes(result))
        .catch((error) => alert("Произошла ошибка загрузки заметок. " + JSON.stringify(error)));
}

function loadSaveNote(noteChatId, noteText) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const rawJson = JSON.stringify({
        "chatId": noteChatId,
        "text": noteText
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: rawJson,
        redirect: "follow"
    };

    fetch(createNoteUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => processLoadedNotes(result))
        .catch((error) => alert("Произошла ошибка загрузки заметок. " + JSON.stringify(error)));
}

function loadUpdateNote(noteId, noteText) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const rawJson = JSON.stringify({
        "id": noteId,
        "text": noteText
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: rawJson,
        redirect: "follow"
    };

    fetch(updateNoteUrl + noteId, requestOptions)
        .then((response) => response.json())
        .then((result) => processLoadedNotes(result))
        .catch((error) => alert("Произошла ошибка загрузки заметок. " + JSON.stringify(error)));
}

function loadDeleteNote(noteId) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const requestOptions = {
        method: "DELETE",
        headers: myHeaders,
        redirect: "follow"
    };

    fetch(deleteNoteUrl + noteId, requestOptions)
        .then((response) => response.json())
        .then((result) => processLoadedNotes(result))
        .catch((error) => alert("Произошла ошибка загрузки заметок. " + JSON.stringify(error)));
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
        .then((result) => {
            processNewSearchedChats(result.chats)
            return result;
        })
        .then((result) => postProcessChat(extractChatIds(result.chats)))
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
        .then((result) => {
            processNewLoadedChats(result.chats);
            return result;
        })
        .then((result) => postProcessChat(extractChatIds(result.chats)))
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
        .then((result) => {
            processNewLoadedChats(result.chats)
            return result;
        })
        .then((result) => postProcessChat(extractChatIds(result.chats)))
        .catch((error) => alert("Произошла ошибка загрузки чатов. " + JSON.stringify(error)));
}

function loadChatByIdOnNewMessage(chatId) {
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
        .then((result) => {
            processNewLoadedChats(result.chats);
            return result.chats;
        })
        .then((chatsArray) => {
            for (let chat of chatsArray) {
                moveChatInChatWindowToFront(chat.id);
            }
            return chatsArray
        })
        .then((chatsArray) => postProcessChat(extractChatIds(chatsArray)))
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
        .then(() => loadNewMessageInfo())
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

function loadChatStatus(chatIds) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "chatIds": chatIds
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadStatusUrl, requestOptions)
        .then((response) => response.json())
        .then((response) => fillChatStatus(response.statuses))
}

function loadIsChatOrderMember(chatIds) {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "chatIds": chatIds,
        "userId": context.userId
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadIsMemberUrl, requestOptions)
        .then((response) => response.json())
        .then((response) => markMyChats(response.statuses))
}

function postProcessChat(chatIds) {
    if (chatIds === undefined || chatIds === null || chatIds.length === 0) {
        return;
    }
    loadChatStatus(chatIds);
    loadIsChatOrderMember(chatIds);
}

function processNewMessagesCount(count) {
    $("#nav-new-orders-tab-count").text(" (" + count.newOrders + ")");
    $("#nav-admin-tab-count").text(" (" + count.admin + ")");
    $("#nav-orders-tab-count").text(" (" + count.orders + ")");
    $("#nav-special-tab-count").text(" (" + count.special + ")");
}

function markMyChats(chatStatusArray) {
    for (let chat of chatStatusArray) {
        $(`#chat-${chat.id}`).addClass('bg-success-subtle')
    }
}

function fillChatStatus(chatStatusArray) {
    for (let chat of chatStatusArray) {
        $(`#chat-${chat.id}-status`).prepend(createChatStatus(chat.status));
    }
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
        appendChatToChatsWindows(chat);
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
        delay: 20000
    });
    toastBootstrap.show();
}

function createChatWindowElement(chatJson) {
    let text_bg = chatJson.newMessageCount === 0 ? "text-bg-light" : "text-bg-primary";
    let count = chatJson.newMessageCount === 0 ? "" : chatJson.newMessageCount;
    return `
    <li id="chat-${chatJson.id}" class="list-group-item d-flex justify-content-between align-items-start" onclick="onChatLoadBtnClick(${chatJson.id})">
        <div class="ms-2 me-auto main-font">
            <div id="chat-${chatJson.id}-status">
                <span class="fw-bold m-0">${chatJson.chatName}</span>
            </div>
            <p class="m-0 mt-1">
                <span id="chat-${chatJson.id}-date" class="me-2">${chatJson.lastMessageDate}</span>
                <span id="chat-1-chat-id" class="me-2">(chat-id = ${chatJson.id})</span>
                <button type="button" class="btn btn-primary me-2 p-1" data-bs-toggle="modal" data-bs-target="#show-create-note-modal" onclick="updateNote(null, ${chatJson.id}, '${chatJson.chatName}', null)">&#9997;</button>
            </p>
        </div>
        <span id="chat-${chatJson.id}-new-msg-count" class="badge ${text_bg} rounded-pill">${count}</span>
    </li>`;
}

function createStatus(orderStatusName, orderId) {
    let color = 'text-bg-secondary';
    let orderStatusRus = "Создан/На распределении";

    if (orderStatusName === 'IN_PROGRESS') {
        color = 'text-bg-primary';
        orderStatusRus = "В работе";
    }
    if (orderStatusName === 'REVIEW') {
        color = 'text-bg-info';
        orderStatusRus = "Проверка";
    }
    if (orderStatusName === 'FINALIZATION') {
        color = 'text-bg-warning';
        orderStatusRus = "Доработка";
    }
    if (orderStatusName === 'GUARANTEE' || orderStatusName === 'CLOSED') {
        color = 'text-bg-success';
        orderStatusRus = "На гарантии/Завершён";
    }

    return `<a class="badge ${color} p-2 p-sm-3" href="/change-order-status/${orderId}" target="_blank">${orderStatusRus}</a>`;
}

function createChatStatus(orderStatusName) {
    let color = 'text-bg-secondary';
    let orderStatusRus = "Создан/На распределении";

    if (orderStatusName === 'IN_PROGRESS') {
        color = 'text-bg-primary';
        orderStatusRus = "В работе";
    }
    if (orderStatusName === 'REVIEW') {
        color = 'text-bg-info';
        orderStatusRus = "Проверка";
    }
    if (orderStatusName === 'FINALIZATION') {
        color = 'text-bg-warning';
        orderStatusRus = "Доработка";
    }
    if (orderStatusName === 'GUARANTEE' || orderStatusName === 'CLOSED') {
        color = 'text-bg-success';
        orderStatusRus = "На гарантии/Завершён";
    }

    return `<span class="badge ${color} p-2">${orderStatusRus}</span>`;
}

function createDates(datesArray) {
    let str = "";
    for (let date of datesArray) {
        let dateText = date.text;
        if (date.time !== null) {
            dateText = dateText + " : " + date.time;
        }
        str = str + `<li class="list-group-item p-1 ps-3 pe-3">${dateText}</li>`
    }
    if (str.length === 0) {
        str = "Не назначено."
    }
    return `<ul class="list-group">${str}</ul>`
}

function formatChatHeader(chatId, chatName, orderInfo) {
    chatTitle.innerText = chatName;

    $("#note-chat-header").empty();
    $("#note-chat-header").removeClass("d-none");
    $("#note-chat-header").append(`<button class="p-1 btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#show-notes-modal" onclick="loadNotes(${chatId})">&#128203;</button>`);

    if (orderInfo !== null) {
        techNumber.innerText = orderInfo.techNumber;
        workType.innerText = orderInfo.workType;
        deadline.innerText = orderInfo.deadline;
        discipline.innerText = orderInfo.discipline;
        status.innerHTML = createStatus(orderInfo.orderStatus, orderInfo.id);
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
            attachmentHtml += `<a class="mt-1" target="_blank" href="/load-file/${attachment.id}"><span>Файл: </span>${attachment.name}</a><br/>`
        }
    }

    if (options.length > 0) {
        for (let option of options) {
            optionHtml += `<a class="btn btn-primary mt-1 w-100" target="_blank" href="${option.url}">${option.name}</a>`
        }
    }

    return `
        <div class="border p-2 mt-1 main-font fs-6 ${additionalStyles}">
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
        if (message.content === null || message.content === '') {
            content = "Сообщение без текста.";
        }

        showNotification(message.id, "Новое сообщение в чате: " + message.chatName, content.substring(0, 150))
        incNewMessageCount(message.chatId);

        if (context.loadedChats.has(message.chatId)) {
            moveChatInChatWindowToFront(message.chatId);
        } else {
            loadChatByIdOnNewMessage(message.chatId);
        }

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

    formatChatHeader(chatInfoJson.chatId, chatInfoJson.chatName, chatInfoJson.order);

    buildChatTable(chatInfoJson);
    prependChatMessages(chatInfoJson.messages);

    if (chatInfoJson.metadata.onlyOwnerCanType && !context.isOwner) {
        $("#message-div").addClass("d-none");
    } else {
        $("#message-div").removeClass("d-none");
    }

    chatWindow.scroll(0, chatWindow.scrollHeight);
    mainWindow.scroll(0, mainWindow.scrollHeight);

    sendMessageViewed();
    clearNewMessageCount(chatInfoJson.chatId);
}

function processNewSearchedChats(chatsArray) {
    hideLoadChatSpinner();
    updateChatsCache(chatsArray);
    appendChatsToChatsWindows(chatsArray);
}

function extractChatIds(chatArray) {
    let ids = [];
    for (let chat of chatArray) {
        ids.push(chat.id);
    }
    return ids;
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

    $(".border-select").removeClass("border-select")
    $(".rounded-0").removeClass("rounded-0")
    $(".border-end-0").removeClass("border-end-0")
    $(".border-top-0").removeClass("border-top-0")
    $(".border-bottom-0").removeClass("border-bottom-0")
    $(".border-primary").removeClass("border-primary")

    $(`#chat-${chatId}`).addClass('border-select');
    $(`#chat-${chatId}`).addClass('rounded-0');
    $(`#chat-${chatId}`).addClass('border-end-0');
    $(`#chat-${chatId}`).addClass('border-top-0');
    $(`#chat-${chatId}`).addClass('border-bottom-0');
    $(`#chat-${chatId}`).addClass('border-primary');

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
    $(`#chat-${chatId}-new-msg-count`).removeClass("text-bg-light");
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

function clearNote() {
    $("#note-id").val(null);
    $("#note-chat-id").val(null);
    $("#note-chat-name").val(null);
    $("#note-text").val(null);
}

function updateNoteById(noteId) {
    updateNote(context.notes[noteId].id, context.notes[noteId].chatId, context.notes[noteId].chatName, context.notes[noteId].text)
}

function updateNote(noteId, chatId, chatName, noteText) {
    clearNote();

    $("#note-id").val(noteId);
    $("#note-chat-id").val(chatId);
    $("#note-chat-name").val(chatName);
    $("#note-text").val(noteText);
}

function deleteNote(noteId) {
    let isOk = confirm("Удалить заметку?");

    if (!isOk) {
        return;
    }

    $("#notes-content").empty();
    $("#notes-loader-spin").removeClass("d-none");

    loadDeleteNote(noteId)
}

function saveNote() {
    let noteId = $("#note-id").val();
    let noteChatId = $("#note-chat-id").val();
    let noteText = $("#note-text").val();

    if (noteId && noteId.length > 0) {
        noteId = parseInt(noteId);
    } else {
        noteId = null;
    }

    if (noteChatId && noteChatId.length > 0) {
        noteChatId = parseInt(noteChatId);
    } else {
        noteChatId = null;
    }

    if (!(noteText && noteText.length > 0)) {
        noteText = null;
    }

    $("#notes-content").empty();
    $("#notes-loader-spin").removeClass("d-none");

    if (noteId != null) {
        loadUpdateNote(noteId, noteText);
    } else {
        loadSaveNote(noteChatId, noteText);
    }
}

function loadNotes(filterChatId) {
    context.notesFilterChatId = filterChatId;

    $("#notes-content").empty();
    $("#notes-loader-spin").removeClass("d-none");

    loadNoteByUser();
}

function processLoadedNotes(notesArray) {
    let filteredNotes = [];
    for (let note of notesArray) {
        if (context.notesFilterChatId === null || context.notesFilterChatId === undefined) {
            filteredNotes.push(note);
            continue;
        }
        if (note.chatId === context.notesFilterChatId) {
            filteredNotes.push(note);
        }
    }

    if (filteredNotes.length === 0) {
        $("#notes-content").append(`<p class="fs-4 text-center">Нет заметок.</p>`)
    }

    context.notes = {};
    for (let note of filteredNotes) {
        context.notes[note.id] = note;
        $("#notes-content").append(createNoteItem(note))
    }

    $("#notes-loader-spin").addClass("d-none");
}

function createNoteItem(note) {
    let chatLink = "<p>Нет привязки к чату.</p>"
    if ((note.chatId === 0) || note.chatTab) {
        chatLink = `<p>Чат: <a href="/new-messenger?tab=${note.chatTab}&chatId=${note.chatId}">${note.chatName}</a></p>`
    }

    let noteText = "<i>Нет текста заметки</i>"
    if (note.text && note.text.length > 0) {
        noteText = `<pre class="m-0 main-font text-break fs-6" style="white-space: pre-wrap">${note.text}</pre>`;
    }

    return `
        <div class="row">
            <div class="col-12 mb-2 d-flex justify-content-between">
                <div>
                    <i>Заметка от ${note.createdAt}</i>
                </div>
                <div>
                    <button type="button" class="btn btn-primary me-2 p-1" data-bs-toggle="modal" data-bs-target="#show-create-note-modal" onclick="updateNoteById(${note.id})">&#9997;</button>
                    <button type="button" class="btn btn-danger p-1" onclick="deleteNote(${note.id})">&#128465;</button>
                </div>
            </div>
            <div class="col-12 col-md-4">${chatLink}</div>
            <div class="col-12 col-md-8">${noteText}</div>
            <hr class="m-0 mt-2 mb-2">
        </div>
    `
}
