self.addEventListener("push", e => {
    console.log("PUSH", e.data.json());
    self.registration.showNotification(e.data.json().title, {body: e.data.json().body})
})