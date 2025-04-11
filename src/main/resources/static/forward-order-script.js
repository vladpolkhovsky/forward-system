const loadChatInfoUrl = '/api/new-chat/load-chat-info';
const loadNewMessageIds = '/api/new-chat/new-messages-ids-by-http';
const loadMessageByIds = '/api/new-chat/messages-by-ids-http';
const loadActivityUrl = "/activity/all/activity"

const usersTable = {};
const usersActivity = {};

const loadedMessagesIds = new Set();
const loadedMessagesTimestamp = {
    maxTime: 0
}

let ERROR_COUNT = 0

function sendMessageViewed() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: null,
        redirect: "follow"
    };

    fetch(`/api/messenger/message-viewed/${chatId}/${userId}`, requestOptions)
        .then((response) => response.text())
        .catch((err) => {
            //alert("Ошибка при сохранении факта прочтения сообщения.");
            console.error("Ошибка при сохранении факта прочтения сообщения. ", err);
            ERROR_COUNT++;
        });
}

function loadUserActivity() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const requestOptions = {
        method: "GET",
        headers: myHeaders,
        redirect: "follow"
    };

    fetch(loadActivityUrl, requestOptions)
        .then((resp) => resp.json())
        .then((resp) => updateActivity(resp))
        .catch((error) => console.error(error));
}

function loadChatMessages() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "chatId": chatId,
        "userId": userId,
        "size": 500
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadChatInfoUrl, requestOptions)
        .then((response) => response.json())
        .then((result) => initChat({
            chatName: result.chatName,
            members: result.members,
            messages: result.messages
        }))
        .catch((error) => console.error(error))
        .finally(() => {
            sendMessageViewed();
            postChatInitialization();
            clearChatNewMessageIndicator(chatId);
        });
}

const syncMessagesStatus = {
    processing: false,
    fetchingNewMessages: false
}

function syncMessages() {
    if (syncMessagesStatus.processing) {
        return;
    }

    syncMessagesStatus.processing = true;

    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json; charset=UTF-8");

    const raw = JSON.stringify({
        "chatId": chatId,
        "userId": userId
    });

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: raw,
        redirect: "follow"
    };

    fetch(loadNewMessageIds, requestOptions)
        .then((response) => response.json())
        .then((ids) => {
            let notLoadedIds = [];
            for (let id of ids) {
                if (!loadedMessagesIds.has(id)) {
                    notLoadedIds.push(id);
                }
                loadedMessagesIds.add(id);
            }
            return notLoadedIds;
        })
        .then((ids) => {
            if (ids.length === 0 || syncMessagesStatus.fetchingNewMessages) {
                return;
            }

            syncMessagesStatus.fetchingNewMessages = true;

            const loadMessagesRequestOptions = {
                method: "POST",
                headers: myHeaders,
                body: JSON.stringify(ids),
                redirect: "follow"
            };

            fetch(loadMessageByIds + "/" + userId, loadMessagesRequestOptions)
                .then((resp) => resp.json())
                .then((appendMessages) => processNewLoadedMessages(appendMessages))
                .catch((err) => {
                    console.error("Ошибка загрузки новых сообщений. ", err);
                    ERROR_COUNT++;
                })
                .finally(() => {
                    syncMessagesStatus.fetchingNewMessages = false;
                    sendMessageViewed();
                });
        })
        .catch((err) => {
            console.error("Ошибка загрузки новых сообщений. ", err);
            ERROR_COUNT++;
        })
        .finally(() => {
            syncMessagesStatus.processing = false;
        });
}

function createMessage(messageId, fromUserId, fromUserUsername, isSystem, messageType, messageText, messageFiles, createdAt, isNewMessage) {
    let myOwnMessage = fromUserId === userId && (!isSystem && messageType !== 'MESSAGE_FROM_CUSTOMER');

    let bgColor = myOwnMessage ? 'bg-success-subtle' : (isSystem ? 'bg-danger-subtle' : 'bg-white');
    let headerAlign = myOwnMessage ? 'ms-auto' : 'me-auto';

    if (fromUserId !== null) {
        usersTable[fromUserId] = {
            userId: fromUserId,
            username: fromUserUsername
        };
    }

    let name = (isSystem ? 'Система' : (messageType === 'MESSAGE_FROM_CUSTOMER' ? 'Заказчик' : (fromUserId !== null ? usersTable[fromUserId].username : 'Неизвестно от кого...')));

    let attachments = "";
    for (let file of messageFiles) {
        attachments += `<a href="/load-file/${file.id}" target="_blank" class="me-2 d-inline-block line-break">${file.name}</a>`;
    }

    let activityStatus = "";
    let activityStatusId = (!isSystem && messageType !== 'MESSAGE_FROM_CUSTOMER' && fromUserId >= 0) ? fromUserId : null;
    if (usersActivity[activityStatusId] !== undefined) {
        activityStatus = createActivityStatus(activityStatusId);
    }

    let newMessageBadge = "";
    if (isNewMessage) {
        newMessageBadge = `<span class="position-absolute top-0 start-100-minus-35px translate-middle badge rounded-pill bg-primary">Новое</span>`
    }

    let message = `
        <div class="border w-75 rounded-3 p-1 mb-3 position-relative ${headerAlign} ${bgColor}" message-id="${messageId}">
            <p class="h6 m-2" message-from-user-id="${activityStatusId}">${name} <span online-status-id="${activityStatusId}">${activityStatus}</span></p>
            <pre class="m-2 fs-7 line-break montserrat">${messageText !== null ? messageText : ''}</pre>
            <div class="d-flex justify-content-between m-2 fs-7">
                <div class="d-block">
                    ${attachments}
                </div>
                <div>
                    <span>${createdAt}</span>
                </div>
            </div>
            ${newMessageBadge}
        </div>`;

    return message;
}

function clearChatWindow() {
    $("#chat-message-box").empty();
}

function showChatWindow() {
    $("#chat-window-loader").addClass("d-none")
    $("#chat-window").removeClass("d-none")
}

function initChat(chatJson) {
    $("#chat-name").text(chatJson.chatName);

    for (let member of chatJson.members) {
        usersTable[member.userId] = member;
    }

    processNewLoadedMessages(chatJson.messages, "bottom");

    showChatWindow();

    scrollChatWindowToEnd();
}

function processNewLoadedMessages(messages, appendTo = "top") {
    for (let message of messages) {
        loadedMessagesIds.add(message.id);
        loadedMessagesTimestamp.maxTime = Math.max(loadedMessagesTimestamp.maxTime, message.createdAtTimestamp);
    }

    let hasNewMessageInOpenChat = false;

    for (let message of messages) {
        if (message.chatId !== chatId) {
            processMessageThatNotInOpenChat(message)
            continue;
        }

        let messageRendered = createMessage(
            message.id,
            message.fromUserId,
            message.fromUserUsername,
            message.systemMessage,
            message.messageType,
            message.text,
            message.attachments,
            message.createdAt,
            !message.viewed
        );

        if (appendTo === "top") $("#chat-message-box").append(messageRendered);
        if (appendTo === "bottom") $("#chat-message-box").prepend(messageRendered);

        hasNewMessageInOpenChat = true;
    }

    if (hasNewMessageInOpenChat) {
        scrollChatWindowToEnd();
    }
}

function processMessageThatNotInOpenChat(message) {
    showNewMessageNotification(message.id, message.chatName, message.fromUserUsername, message.systemMessage, message.messageType, message.text);
    incChatNewMessageIndicator(message.chatId);
}

function updateActivity(activityList) {
    for (let activity of activityList) {
        usersActivity[activity.id] = activity;

        let activityStatus = createActivityStatus(activity.id);

        $(`[online-status-id="${activity.id}"]`).html(activityStatus);
    }
}

function createActivityStatus(userId) {
    return usersActivity[userId].online ? `<span class="badge text-bg-success">Онлайн</span>` :
        `<span class="badge text-bg-secondary">Был в сети: ${usersActivity[userId].lastOnlineAt}</span>`;
}

function scrollChatWindowToEnd() {
    let chatMessageBox = document.getElementById("chat-message-box");
    chatMessageBox.scroll(0, chatMessageBox.scrollHeight)
}

function showNewMessageNotification(id, chatName, fromName, isSystem, messageType, content) {
    if (isSystem && fromName == null) {
        fromName = "Система";
    }

    if (messageType === "MESSAGE_FROM_CUSTOMER") {
        fromName = "Заказчик";
    }

    if (content == null || content.trim().length === 0) {
        content = "~~Сообщение без текста~~";
    }

    $("#forward-order-notification-container").append(createNewMessageNotificationBody(id, chatName, fromName, content));

    let toastBootstrap = bootstrap.Toast.getOrCreateInstance(document.getElementById(id), {
        delay: 2000
    });

    toastBootstrap.show();
}

function createNewMessageNotificationBody(id, chatName, fromName, content) {
    return `<div id="${id}" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header">
            <strong class="me-auto">${chatName}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">${fromName} : ${content}</div>
    </div>`
}


function incChatNewMessageIndicator(chatId) {
    let indicator = $(`[chat-id-new-message-indicator=${chatId}]`);

    indicator.removeClass("d-none");

    let newMessageCount = indicator.attr("new-message-count");

    newMessageCount = parseInt(newMessageCount) + 1;

    indicator.attr("new-message-count", newMessageCount);
    indicator.text(newMessageCount);
}

function clearChatNewMessageIndicator(chatId) {
    let indicator = $(`[chat-id-new-message-indicator=${chatId}]`);
    indicator.addClass("d-none");
    indicator.attr("new-message-count", 0);
    indicator.text("");
}