export type SaveCallback = (id: number) => void;
export type ErrorCallback = () => void;

export class AttachmentsService {
    private constructor() {

    }

    public static upload(fileFormData: FormData, saveCallback: SaveCallback, errorCallback: ErrorCallback = () => {}): void {
        fetch("/api/messenger/file-save-form", {
            method: "POST",
            body: fileFormData,
            redirect: "follow"
        }).then((response) => response.text())
            .then((result) => saveCallback(parseInt(result)))
            .catch((error) => {
                errorCallback();
                console.error(error);
            });
    }
}